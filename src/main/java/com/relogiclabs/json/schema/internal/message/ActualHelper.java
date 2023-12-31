package com.relogiclabs.json.schema.internal.message;

import com.relogiclabs.json.schema.message.ActualDetail;
import com.relogiclabs.json.schema.type.JArray;
import com.relogiclabs.json.schema.type.JNode;
import com.relogiclabs.json.schema.type.JProperty;

import static com.relogiclabs.json.schema.internal.message.MessageHelper.getTypeName;
import static com.relogiclabs.json.schema.internal.util.StringHelper.quote;

public final class ActualHelper {

    private ActualHelper() {
        throw new UnsupportedOperationException();
    }

    public static ActualDetail asArrayElementNotFound(JArray array, int index) {
        return new ActualDetail(array, "not found");
    }

    public static ActualDetail asValueMismatch(JNode node) {
        return new ActualDetail(node, "found ", node.getOutline());
    }

    public static ActualDetail asGeneralValueMismatch(JNode node) {
        return new ActualDetail(node, "found ", node.getOutline());
    }

    public static ActualDetail asInvalidNonCompositeType(JNode node) {
        return new ActualDetail(node, "found non-composite ", getTypeName(node),
                " value ", node.getOutline());
    }

    public static ActualDetail asDataTypeArgumentFailed(JNode node) {
        return new ActualDetail(node, "found invalid value ", node.getOutline());
    }

    public static ActualDetail asDataTypeMismatch(JNode node) {
        return new ActualDetail(node, "found ", getTypeName(node),
                " inferred by ", node.getOutline());
    }

    public static ActualDetail asPropertyNotFound(JNode node, JProperty property) {
        return new ActualDetail(node, "not found property key ", quote(property.getKey()));
    }

    public static ActualDetail asUndefinedProperty(JProperty property) {
        return new ActualDetail(property, "property found {", property.getOutline(), "}");
    }

    public static ActualDetail asPropertyOrderMismatch(JNode node) {
        return node instanceof JProperty property
            ? new ActualDetail(property, "key ", quote(property.getKey()),
                " is found at current position")
            : new ActualDetail(node, "key not found at current position");
    }

    public static ActualDetail asInvalidFunction(JNode node) {
        return new ActualDetail(node, "applied on non-composite type ", getTypeName(node));
    }
}