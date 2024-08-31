package me.ratsiel.json.model;

import me.ratsiel.json.abstracts.JsonValue;
import me.ratsiel.json.interfaces.IListable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class JsonArray extends JsonValue implements IListable<JsonValue> {
    protected final List<JsonValue> values = new ArrayList();

    public JsonArray() {
    }

    public JsonArray(String key) {
        super(key);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        String space = this.createSpace();
        stringBuilder.append(space);
        if (this.getKey() != null && !this.getKey().isEmpty()) {
            stringBuilder.append("\"").append(this.getKey()).append("\"").append(" : ");
        }

        stringBuilder.append("[");
        this.loop((integer, jsonValue) -> {
            jsonValue.setIntend(this.getIntend() + 2);
            stringBuilder.append("\n");
            if (integer != this.size() - 1) {
                stringBuilder.append(jsonValue).append(",");
            } else {
                stringBuilder.append(jsonValue);
            }

        });
        stringBuilder.append("\n").append(space).append("]");
        return stringBuilder.toString();
    }

    public int size() {
        return this.values.size();
    }

    public void add(JsonValue value) {
        this.values.add(value);
    }

    public void add(int index, JsonValue value) {
        this.values.set(index, value);
    }

    public void addString(String value) {
        JsonString jsonString = new JsonString(value);
        this.add(jsonString);
    }

    public void addBoolean(boolean value) {
        JsonBoolean jsonBoolean = new JsonBoolean(value);
        this.add(jsonBoolean);
    }

    public void addByte(byte value) {
        JsonNumber jsonNumber = new JsonNumber(String.valueOf(value));
        this.add(jsonNumber);
    }

    public void addInteger(int value) {
        JsonNumber jsonNumber = new JsonNumber(String.valueOf(value));
        this.add(jsonNumber);
    }

    public void addShort(short value) {
        JsonNumber jsonNumber = new JsonNumber(String.valueOf(value));
        this.add(jsonNumber);
    }

    public void addDouble(double value) {
        JsonNumber jsonNumber = new JsonNumber(String.valueOf(value));
        this.add(jsonNumber);
    }

    public void addFloat(float value) {
        JsonNumber jsonNumber = new JsonNumber(String.valueOf(value));
        this.add(jsonNumber);
    }

    public void addLong(long value) {
        JsonNumber jsonNumber = new JsonNumber(String.valueOf(value));
        this.add(jsonNumber);
    }

    public JsonValue get(int index, Class<JsonValue> clazz) {
        return clazz.cast(this.get(index));
    }

    public JsonValue get(int index) {
        return this.values.get(index);
    }

    public void loop(Consumer<JsonValue> consumer) {
        for (JsonValue value : this.values) {
            consumer.accept(value);
        }

    }

    public void loop(BiConsumer<Integer, JsonValue> consumer) {
        for (int index = 0; index < this.values.size(); ++index) {
            JsonValue jsonValue = this.values.get(index);
            consumer.accept(index, jsonValue);
        }

    }
}
