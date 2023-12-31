package com.relogiclabs.json.schema.tree;

import com.relogiclabs.json.schema.type.JRoot;

public interface DataTree {
    boolean match(DataTree dataTree);
    RuntimeContext getRuntime();
    JRoot getRoot();
    TreeType getType();
}