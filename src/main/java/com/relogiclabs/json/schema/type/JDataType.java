package com.relogiclabs.json.schema.type;

import com.relogiclabs.json.schema.exception.DefinitionNotFoundException;
import com.relogiclabs.json.schema.exception.JsonSchemaException;
import com.relogiclabs.json.schema.internal.builder.JDataTypeBuilder;
import com.relogiclabs.json.schema.internal.message.ActualHelper;
import com.relogiclabs.json.schema.internal.message.ExpectedHelper;
import com.relogiclabs.json.schema.internal.message.MatchReport;
import com.relogiclabs.json.schema.message.ErrorDetail;
import com.relogiclabs.json.schema.util.Reference;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.relogiclabs.json.schema.internal.message.MatchReport.AliasError;
import static com.relogiclabs.json.schema.internal.message.MatchReport.ArgumentError;
import static com.relogiclabs.json.schema.internal.message.MatchReport.Success;
import static com.relogiclabs.json.schema.internal.message.MatchReport.TypeError;
import static com.relogiclabs.json.schema.internal.message.MessageHelper.DataTypeArgumentFailed;
import static com.relogiclabs.json.schema.internal.message.MessageHelper.DataTypeMismatch;
import static com.relogiclabs.json.schema.internal.message.MessageHelper.InvalidNestedDataType;
import static com.relogiclabs.json.schema.internal.util.CollectionHelper.asList;
import static com.relogiclabs.json.schema.internal.util.StreamHelper.allTrue;
import static com.relogiclabs.json.schema.internal.util.StringHelper.concat;
import static com.relogiclabs.json.schema.internal.util.StringHelper.quote;
import static com.relogiclabs.json.schema.message.ErrorCode.DTYP03;
import static com.relogiclabs.json.schema.message.MessageFormatter.formatForSchema;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

@Getter
@EqualsAndHashCode
public final class JDataType extends JBranch implements NestedMode {
    static final String NESTED_MARKER = "*";
    private final JsonType jsonType;
    private final JAlias alias;
    private final boolean nested;

    private JDataType(JDataTypeBuilder builder) {
        super(builder);
        jsonType = requireNonNull(builder.jsonType());
        nested = requireNonNull(builder.nested());
        alias = builder.alias();
        children = asList(alias);
    }

    public static JDataType from(JDataTypeBuilder builder) {
        return new JDataType(builder).initialize();
    }

    @Override
    public boolean match(JNode node) {
        if(!nested) return isMatchCurrent(node);
        if(!(node instanceof JComposite composite)) return false;
        return composite.components().stream().map(this::isMatchCurrent).allMatch(allTrue());
    }

    private boolean isMatchCurrent(JNode node) {
        return matchCurrent(node, new Reference<>()) == Success;
    }

    private MatchReport matchCurrent(JNode node, Reference<String> error) {
        var result = jsonType.match(node, error) ? Success : TypeError;
        if(alias == null || result != Success) return result;
        var validator = getRuntime().getDefinitions().get(alias);
        if(validator == null) return AliasError;
        result = validator.match(node) ? Success : ArgumentError;
        return result;
    }

    boolean matchForReport(JNode node) {
        if(!nested) return matchForReport(node, false);
        if(!(node instanceof JComposite composite))
            return failWith(new JsonSchemaException(
                    new ErrorDetail(DTYP03, InvalidNestedDataType),
                    ExpectedHelper.asInvalidNestedDataType(this),
                    ActualHelper.asInvalidNestedDataType(node)));
        boolean result = true;
        for(var c : composite.components()) result &= matchForReport(c, true);
        return result;
    }

    private boolean matchForReport(JNode node, boolean nested) {
        Reference<String> error = new Reference<>();
        var result = matchCurrent(node, error);
        if(result == TypeError) return failWith(new JsonSchemaException(
                new ErrorDetail(TypeError.getCode(nested),
                        formatMessage(DataTypeMismatch, error.getValue())),
                ExpectedHelper.asDataTypeMismatch(this),
                ActualHelper.asDataTypeMismatch(node)));
        if(result == AliasError) return failWith(new DefinitionNotFoundException(formatForSchema(
                AliasError.getCode(nested), "No definition found for " + quote(alias), this)));
        if(result == ArgumentError) return failWith(new JsonSchemaException(
                new ErrorDetail(ArgumentError.getCode(nested), DataTypeArgumentFailed),
                ExpectedHelper.asDataTypeArgumentFailed(this),
                ActualHelper.asDataTypeArgumentFailed(node)));
        return true;
    }

    private static String formatMessage(String main, String optional) {
        return isEmpty(optional) ? main : concat(main, " (", uncapitalize(optional), ")");
    }

    boolean isApplicable(JNode node) {
        return !nested || node instanceof JComposite;
    }

    boolean isMatchNull() {
        return !nested && jsonType == JsonType.NULL;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean baseForm) {
        var builder = new StringBuilder(jsonType.toString());
        if(nested && !baseForm) builder.append(NESTED_MARKER);
        if(alias != null && !baseForm) builder.append("(").append(alias).append(")");
        return builder.toString();
    }
}