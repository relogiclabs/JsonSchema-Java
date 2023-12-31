package com.relogiclabs.json.schema;

import com.relogiclabs.json.schema.internal.util.DebugUtilities;
import com.relogiclabs.json.schema.tree.ExceptionRegistry;
import com.relogiclabs.json.schema.tree.JsonTree;
import com.relogiclabs.json.schema.tree.RuntimeContext;
import com.relogiclabs.json.schema.tree.SchemaTree;
import lombok.Getter;

import static com.relogiclabs.json.schema.message.MessageFormatter.SCHEMA_VALIDATION;

/**
 * {@code JsonSchema} provides Schema validation functionalities for JSON document.
 */
@Getter
public class JsonSchema {
    private final RuntimeContext runtime;
    private final SchemaTree schemaTree;
    private final ExceptionRegistry exceptions;

    /**
     * Initializes a new instance of the {@link JsonSchema} class for the
     * specified Schema string.
     * @param schema A Schema string for validation
     */
    public JsonSchema(String schema) {
        runtime = new RuntimeContext(SCHEMA_VALIDATION, false);
        exceptions = runtime.getExceptions();
        schemaTree = new SchemaTree(runtime, schema);
    }

    /**
     * Indicates whether the input JSON string conforms to the Schema specified
     * in the {@link JsonSchema} constructor.
     * @param json The JSON to validate with Schema
     * @return Returns {@code true} if the JSON string conforms to the Schema and {@code false} otherwise.
     */
    public boolean isValid(String json) {
        runtime.clear();
        var jsonTree = new JsonTree(runtime, json);
        DebugUtilities.print(schemaTree, jsonTree);
        return schemaTree.match(jsonTree);
    }

    /**
     *  Writes error messages that occur during Schema validation process, to the
     *  standard error output stream.
     */
    public void writeError() {
        for(var exception : exceptions)
            System.err.println(exception.getMessage());
    }

    /**
     * Indicates whether the input JSON string conforms to the given Schema string.
     * @param schema The Schema string to conform or validate
     * @param json The JSON string to conform or validate
     * @return Returns {@code true} if the JSON string conforms to the Schema and {@code false} otherwise.
     */
    public static boolean isValid(String schema, String json) {
        var jsonSchema = new JsonSchema(schema);
        var result = jsonSchema.isValid(json);
        if(!result) jsonSchema.writeError();
        return result;
    }
}