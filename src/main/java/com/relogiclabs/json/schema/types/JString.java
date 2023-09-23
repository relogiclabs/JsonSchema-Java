package com.relogiclabs.json.schema.types;

import com.relogiclabs.json.schema.exception.JsonSchemaException;
import com.relogiclabs.json.schema.internal.message.ActualHelper;
import com.relogiclabs.json.schema.internal.message.ExpectedHelper;
import com.relogiclabs.json.schema.message.ErrorDetail;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.relogiclabs.json.schema.internal.message.MessageHelper.ValueMismatch;
import static com.relogiclabs.json.schema.internal.util.StringHelper.quote;
import static com.relogiclabs.json.schema.message.ErrorCode.STRN01;
import static java.util.Objects.requireNonNull;

@Getter
@EqualsAndHashCode
public class JString extends JPrimitive {
    private final String value;

    private JString(Builder builder) {
        super(builder.relations, builder.context);
        this.value = requireNonNull(builder.value);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public JsonType getType() {
        return JsonType.STRING;
    }

    @Override
    public boolean match(JNode node) {
        var other = castType(node, JString.class);
        if(other == null) return false;
        if(value.equals(other.value)) return true;
        return failWith(new JsonSchemaException(
                new ErrorDetail(STRN01, ValueMismatch),
                ExpectedHelper.asValueMismatch(this),
                ActualHelper.asValueMismatch(other)));
    }

    @Override
    public String toString() {
        return quote(value);
    }

    @Setter
    @Accessors(fluent = true)
    public static class Builder extends JNode.Builder<Builder> {
        protected String value;

        @Override
        public JString build() {
            return new JString(this).initialize();
        }
    }
}