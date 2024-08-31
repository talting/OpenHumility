package me.ratsiel.json.model;

import me.ratsiel.json.abstracts.JsonValue;

public class JsonNull extends JsonValue {
    protected final String value = "null";

    public JsonNull() {
    }

    public JsonNull(final String key) {
        super(key);
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        final String space = this.createSpace();
        stringBuilder.append(space);
        if (this.getKey() != null && !this.getKey().isEmpty()) {
            stringBuilder.append("\"").append(this.getKey()).append("\"").append(" : ");
        }
        stringBuilder.append("null");
        return stringBuilder.toString();
    }

    public String getValue() {
        return "null";
    }
}
