package com.relogiclabs.json.schema.positive;

import com.relogiclabs.json.schema.JsonAssert;
import org.junit.jupiter.api.Test;

public class PragmaTests {
    @Test
    public void When_UndefinedPropertyInObject_ValidTrue() {
        var schema =
            """
            %pragma IgnoreUndefinedProperties: true
            %schema:
            {
                "key1": #integer
            }
            """;
        var json =
            """
            {
                "key1": 10,
                "key2": "value1"
            }
            """;
        JsonAssert.isValid(schema, json);
    }

    @Test
    public void When_IgnorePropertyOrderFalseOfObject_ValidTrue() {
        var schema =
            """
            %pragma IgnoreObjectPropertyOrder: false
            %schema:
            {
                "key1": #integer,
                "key2": #string,
                "key3": #float
            }
            """;
        var json =
            """
            {
                "key1": 10,
                "key2": "value1",
                "key3": 2.1
            }
            """;
        JsonAssert.isValid(schema, json);
    }

    @Test
    public void When_IgnorePropertyOrderTrueOfObject_ValidTrue() {
        var schema =
            """
            %pragma IgnoreObjectPropertyOrder: true
            %schema:
            {
                "key1": #integer,
                "key2": #string,
                "key3": #float
            }
            """;
        var json =
            """
            {
                "key1": 10,
                "key3": 2.1,
                "key2": "value1"
            }
            """;
        JsonAssert.isValid(schema, json);
    }

    @Test
    public void When_FloatingPointToleranceOfNumber_ValidTrue() {
        var schema =
            """
            %pragma FloatingPointTolerance: 0.00001
            %schema:
            {
                "key1": 5.00 #float,
                "key2": 10.00E+0 #double
            }
            """;
        var json =
            """
            {
                "key1": 5.000001,
                "key2": 10.000001E+0
            }
            """;
        JsonAssert.isValid(schema, json);
    }
}