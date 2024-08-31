package me.ratsiel.json.model;

import me.ratsiel.json.abstracts.JsonValue;

public class JsonString extends JsonValue {
    private String value;

    public JsonString() {
    }

    public JsonString(final String key, final String value) {
        super(key);
        this.value = value;
    }

    public JsonString(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        final String space = this.createSpace();
        stringBuilder.append(space);
        if (this.getKey() != null && !this.getKey().isEmpty()) {
            stringBuilder.append("\"").append(this.getKey()).append("\"").append(" : ").append("\"").append(this.getValue()).append("\"");
        } else {
            stringBuilder.append("\"").append(this.value).append("\"");
        }
        return stringBuilder.toString();
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
