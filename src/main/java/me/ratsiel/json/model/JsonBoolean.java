package me.ratsiel.json.model;

import me.ratsiel.json.abstracts.JsonValue;

public class JsonBoolean extends JsonValue {
    public boolean value;

    public JsonBoolean() {
    }

    public JsonBoolean(final boolean value) {
        this.value = value;
    }

    public JsonBoolean(final String key, final boolean value) {
        super(key);
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        final String space = this.createSpace();
        stringBuilder.append(space);
        if (this.getKey() != null && !this.getKey().isEmpty()) {
            stringBuilder.append("\"").append(this.getKey()).append("\"").append(" : ");
        }
        stringBuilder.append(this.isValue());
        return stringBuilder.toString();
    }

    public boolean isValue() {
        return this.value;
    }

    public void setValue(final boolean value) {
        this.value = value;
    }
}
