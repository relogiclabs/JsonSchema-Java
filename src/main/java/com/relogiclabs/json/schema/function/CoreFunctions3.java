package com.relogiclabs.json.schema.function;

import com.relogiclabs.json.schema.exception.DateTimeLexerException;
import com.relogiclabs.json.schema.exception.InvalidDateTimeException;
import com.relogiclabs.json.schema.exception.JsonSchemaException;
import com.relogiclabs.json.schema.message.ActualDetail;
import com.relogiclabs.json.schema.message.ErrorDetail;
import com.relogiclabs.json.schema.message.ExpectedDetail;
import com.relogiclabs.json.schema.time.DateTimeType;
import com.relogiclabs.json.schema.time.DateTimeValidator;
import com.relogiclabs.json.schema.tree.RuntimeContext;
import com.relogiclabs.json.schema.types.JArray;
import com.relogiclabs.json.schema.types.JNode;
import com.relogiclabs.json.schema.types.JObject;
import com.relogiclabs.json.schema.types.JString;

import java.net.URI;
import java.util.Arrays;
import java.util.regex.Pattern;

import static com.relogiclabs.json.schema.internal.util.CollectionHelper.containsKeys;
import static com.relogiclabs.json.schema.internal.util.CollectionHelper.containsValues;
import static com.relogiclabs.json.schema.internal.util.StreamHelper.count;
import static com.relogiclabs.json.schema.internal.util.StringHelper.concat;
import static com.relogiclabs.json.schema.internal.util.StringHelper.quote;
import static com.relogiclabs.json.schema.message.ErrorCode.ELEM01;
import static com.relogiclabs.json.schema.message.ErrorCode.EMAL01;
import static com.relogiclabs.json.schema.message.ErrorCode.KEYS01;
import static com.relogiclabs.json.schema.message.ErrorCode.PHON01;
import static com.relogiclabs.json.schema.message.ErrorCode.REGX01;
import static com.relogiclabs.json.schema.message.ErrorCode.URLA01;
import static com.relogiclabs.json.schema.message.ErrorCode.URLA02;
import static com.relogiclabs.json.schema.message.ErrorCode.URLA03;
import static com.relogiclabs.json.schema.message.ErrorCode.URLA04;
import static com.relogiclabs.json.schema.message.ErrorCode.VALU01;

public class CoreFunctions3 extends CoreFunctions2 {
    private static final String URI_SCHEME_HTTPS = "https";
    private static final String URI_SCHEME_HTTP = "http";

    private static final Pattern EMAIL_REGEX
            = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_REGEX
            = Pattern.compile("\\+?[0-9 ()-]+");

    public CoreFunctions3(RuntimeContext runtime) {
        super(runtime);
    }

    public boolean elements(JArray target, JNode... items) {
        return count(Arrays.stream(items)
                .filter(n -> !target.getElements().contains(n))
                .map(n -> failOnElement(target, n))) == 0;
    }

    private boolean failOnElement(JArray target, JNode node) {
        return failWith(new JsonSchemaException(
                new ErrorDetail(ELEM01, "Value is not an element of array"),
                new ExpectedDetail(function, "array with value ", node),
                new ActualDetail(target, "not found in ", target.getOutline())));
    }

    public boolean keys(JObject target, JString... items) {
        return count(containsKeys(target.getProperties(), items)
                .stream().map(s -> failOnKey(target, s))) == 0;
    }

    private boolean failOnKey(JObject target, String string) {
        return failWith(new JsonSchemaException(
                new ErrorDetail(KEYS01, "Object does not contain the key"),
                new ExpectedDetail(function, "object with key ", quote(string)),
                new ActualDetail(target, "does not contain in ", target.getOutline())));
    }

    public boolean values(JObject target, JNode... items) {
        return count(containsValues(target.getProperties(), items)
                .stream().map(n -> failOnValue(target, n))) == 0;
    }

    private boolean failOnValue(JObject target, JNode node) {
        return failWith(new JsonSchemaException(
                new ErrorDetail(VALU01, "Object does not contain the value"),
                new ExpectedDetail(function, "object with value ", node),
                new ActualDetail(target, "does not contain in ", target.getOutline())));
    }

    public boolean regex(JString target, JString pattern) {
        if(!Pattern.matches(pattern.getValue(), target.getValue()))
            return failWith(new JsonSchemaException(
                    new ErrorDetail(REGX01, "Regex pattern does not match"),
                    new ExpectedDetail(function, "string of pattern ", pattern.getOutline()),
                    new ActualDetail(target, "found ", target.getOutline(),
                            " that mismatches with pattern")));
        return true;
    }

    public boolean email(JString target) {
        // Based on SMTP protocol RFC 5322
        if(!EMAIL_REGEX.matcher(target.getValue()).matches())
            return failWith(new JsonSchemaException(
                    new ErrorDetail(EMAL01, "Invalid email address"),
                    new ExpectedDetail(function, "a valid email address"),
                    new ActualDetail(target, "found ", target, " that is invalid")));
        return true;
    }

    public boolean url(JString target) {
        boolean result;
        URI uri;
        try {
            uri = new URI(target.getValue());
            result = switch(uri.getScheme()) {
                case URI_SCHEME_HTTP, URI_SCHEME_HTTPS -> true;
                default -> false;
            };
        } catch (Exception e) {
            return failWith(new JsonSchemaException(
                    new ErrorDetail(URLA01, "Invalid url address"),
                    new ExpectedDetail(function, "a valid url address"),
                    new ActualDetail(target, "found ", target, " that is invalid")));
        }
        if(!result) return failWith(new JsonSchemaException(
                new ErrorDetail(URLA02, "Invalid url address scheme"),
                new ExpectedDetail(function, "HTTP or HTTPS scheme"),
                new ActualDetail(target, "found ", quote(uri.getScheme()), " from ",
                        target, " that has invalid scheme")));
        return true;
    }

    public boolean url(JString target, JString scheme) {
        boolean result;
        URI uri;
        try {
            uri = new URI(target.getValue());
        } catch (Exception e) {
            return failWith(new JsonSchemaException(
                    new ErrorDetail(URLA03, "Invalid url address"),
                    new ExpectedDetail(function, "a valid url address"),
                    new ActualDetail(target, "found ", target, " that is invalid")));
        }
        result = scheme.getValue().equals(uri.getScheme());
        if(!result) return failWith(new JsonSchemaException(
                new ErrorDetail(URLA04, "Mismatch url address scheme"),
                new ExpectedDetail(function, "scheme ", scheme, " for url address"),
                new ActualDetail(target, "found ", quote(uri.getScheme()), " from ",
                        target, " that does not matched")));
        return true;
    }

    public boolean phone(JString target) {
        // Based on ITU-T E.163 and E.164 (extended)
        if(!PHONE_REGEX.matcher(target.getValue()).matches())
            return failWith(new JsonSchemaException(
                    new ErrorDetail(PHON01, "Invalid phone number format"),
                    new ExpectedDetail(function, "a valid phone number"),
                    new ActualDetail(target, "found ", target, " that is invalid")));
        return true;
    }

    public boolean date(JString target, JString pattern) {
        return dateTime(target, pattern, DateTimeType.DATE_TYPE);
    }

    public boolean time(JString target, JString pattern) {
        return dateTime(target, pattern, DateTimeType.TIME_TYPE);
    }

    private boolean dateTime(JString target, JString pattern, DateTimeType type) {
        try {
            var validator = new DateTimeValidator(pattern.getValue());
            if(type == DateTimeType.DATE_TYPE) validator.ValidateDate(target.getValue());
            else if(type == DateTimeType.TIME_TYPE) validator.ValidateTime(target.getValue());
            else throw new IllegalArgumentException(concat("Invalid ",
                        DateTimeType.class.getSimpleName(), " value"));
            return true;
        } catch(DateTimeLexerException e) {
            return failWith(new JsonSchemaException(
                    new ErrorDetail(e.getCode(), e.getMessage()),
                    new ExpectedDetail(function, "a valid ", type, " pattern"),
                    new ActualDetail(target, "found ", pattern, " that is invalid"), e));
        } catch(InvalidDateTimeException e) {
            return failWith(new JsonSchemaException(
                new ErrorDetail(e.getCode(), e.getMessage()),
                new ExpectedDetail(function, "a valid ", type, " formatted as ", pattern),
                new ActualDetail(target, "found ", target,
                        " that is invalid or malformatted"), e));
        }
    }
}