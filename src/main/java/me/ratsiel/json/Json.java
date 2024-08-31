package me.ratsiel.json;

import me.ratsiel.json.abstracts.JsonHandler;
import me.ratsiel.json.abstracts.JsonValue;
import me.ratsiel.json.model.JsonString;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

public class Json {
    private final JsonGenerator jsonGenerator;
    private final JsonParser jsonParser;

    public Json() {
        this.jsonGenerator = new JsonGenerator();
        this.jsonParser = new JsonParser();
        this.registerHandler(UUID.class, new JsonHandler<UUID>() {
            @Override
            public UUID serialize(final JsonValue jsonValue) {
                final JsonString jsonString = (JsonString) jsonValue;
                return UUID.fromString(jsonString.getValue());
            }

            @Override
            public JsonValue deserialize(final UUID value) {
                return new JsonString(value.toString());
            }
        });
    }

    public JsonValue fromJsonString(final String json) {
        return this.jsonParser.parse(json);
    }

    public <T extends JsonValue> T fromJsonString(final String json, final Class<T> clazz) {
        return this.jsonParser.parse(json, clazz);
    }

    public <T extends JsonValue> T fromFile(final File file, final Class<T> clazz) throws IOException {
        return clazz.cast(this.jsonParser.parse(file));
    }

    public JsonValue fromFile(final File file) throws IOException {
        return this.jsonParser.parse(Files.newInputStream(file.toPath()));
    }

    public <T extends JsonValue> T parse(final InputStream inputStream, final Class<T> clazz) {
        return this.jsonParser.parse(inputStream, clazz);
    }

    public JsonValue parse(final InputStream inputStream) {
        return this.jsonParser.parse(inputStream);
    }

    public <T> JsonValue toJson(final T value) {
        return this.jsonGenerator.toJson(value);
    }

    public <T extends List<K>, K> T fromJson(final JsonValue jsonValue, final Class<T> listClazz, final Class<K> clazz) {
        return this.jsonGenerator.fromJson(jsonValue, listClazz, clazz);
    }

    public <T> T fromJsonCast(final JsonValue jsonValue, final Class<T> clazz) {
        return this.jsonGenerator.fromJsonCast(jsonValue, clazz);
    }

    public <T> Object fromJson(final JsonValue jsonValue, final Class<T> clazz) {
        return this.jsonGenerator.fromJson(jsonValue, clazz);
    }

    public void registerHandler(final Class<?> clazz, final JsonHandler<?> jsonHandler) {
        this.jsonGenerator.registerHandler(clazz, jsonHandler);
    }

    public void unregisterHandler(final Class<?> clazz) {
        this.jsonGenerator.unregisterHandler(clazz);
    }
}
