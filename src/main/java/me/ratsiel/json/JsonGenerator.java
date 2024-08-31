package me.ratsiel.json;

import me.ratsiel.json.abstracts.JsonHandler;
import me.ratsiel.json.abstracts.JsonValue;
import me.ratsiel.json.model.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class JsonGenerator {
    protected final HashMap<Class<?>, JsonHandler<?>> handlers = new HashMap();

    public <T> JsonValue toJson(T value) {
        if (value == null) {
            JsonValue jsonValue = new JsonNull();
            return jsonValue;
        } else {
            JsonHandler jsonHandler = this.getHandler(value.getClass());
            if (jsonHandler != null) {
                return jsonHandler.deserialize(value);
            } else {
                Class<?> clazz = value.getClass();
                if (String.class.isAssignableFrom(clazz)) {
                    JsonValue jsonValue = new JsonString();
                    ((JsonString) jsonValue).setValue((String) value);
                    return jsonValue;
                } else if (Enum.class.isAssignableFrom(clazz)) {
                    JsonValue jsonValue = new JsonString();
                    ((JsonString) jsonValue).setValue(((Enum) value).name());
                    return jsonValue;
                } else if (Number.class.isAssignableFrom(clazz)) {
                    JsonValue jsonValue = new JsonNumber();
                    ((JsonNumber) jsonValue).setValue(value.toString());
                    return jsonValue;
                } else if (Boolean.class.isAssignableFrom(clazz)) {
                    JsonValue jsonValue = new JsonBoolean();
                    ((JsonBoolean) jsonValue).setValue((Boolean) value);
                    return jsonValue;
                } else if (List.class.isAssignableFrom(clazz)) {
                    JsonArray jsonArray = new JsonArray();

                    for (Object listValue : (List) value) {
                        jsonArray.add(this.toJson(listValue));
                    }

                    return jsonArray;
                } else if (Map.class.isAssignableFrom(clazz)) {
                    JsonObject jsonObject = new JsonObject();
                    Map<?, ?> map = (Map) value;

                    for (Entry<?, ?> entry : map.entrySet()) {
                        Object key = entry.getKey();
                        Object mapValue = entry.getValue();
                        if (key == null) {
                            jsonObject.add("null", this.toJson(mapValue));
                        } else {
                            Class<?> keyClazz = key.getClass();
                            if (!String.class.isAssignableFrom(keyClazz) && !Number.class.isAssignableFrom(keyClazz) && !Boolean.class.isAssignableFrom(keyClazz)) {
                                if (Enum.class.isAssignableFrom(keyClazz)) {
                                    jsonObject.add(((Enum) key).name(), this.toJson(mapValue));
                                }
                            } else {
                                jsonObject.add(String.valueOf(key), this.toJson(mapValue));
                            }
                        }
                    }

                    return jsonObject;
                } else {
                    JsonObject jsonObject = new JsonObject();
                    if (this.hasSuperclass(clazz)) {
                        for (Field declaredField : clazz.getSuperclass().getDeclaredFields()) {
                            this.populateFields(value, jsonObject, declaredField);
                        }
                    }

                    for (Field declaredField : clazz.getDeclaredFields()) {
                        this.populateFields(value, jsonObject, declaredField);
                    }

                    return jsonObject;
                }
            }
        }
    }

    private <T> void populateFields(T value, JsonObject jsonObject, Field declaredField) {
        if (declaredField.getModifiers() != 8) {
            declaredField.setAccessible(true);

            try {
                JsonValue transformedValue = this.toJson(declaredField.get(value));
                transformedValue.setKey(declaredField.getName());
                jsonObject.add(transformedValue);
            } catch (IllegalAccessException var5) {
                var5.printStackTrace();
            }

        }
    }

    public <T extends List<K>, K> T fromJson(JsonValue jsonValue, Class<T> listClazz, Class<K> clazz) {
        return listClazz.cast(this.fromJson(null, jsonValue, clazz));
    }

    public <T> T fromJsonCast(JsonValue jsonValue, Class<T> clazz) {
        return clazz.cast(this.fromJson(null, jsonValue, clazz));
    }

    public <T> Object fromJson(JsonValue jsonValue, Class<T> clazz) {
        return this.fromJson(null, jsonValue, clazz);
    }

    public <T> Object fromJson(String key, JsonValue jsonValue, Class<T> clazz) {
        JsonHandler jsonHandler = this.getHandler(clazz);
        if (jsonValue instanceof JsonString) {
            return jsonHandler != null ? jsonHandler.serialize(jsonValue) : clazz.cast(((JsonString) jsonValue).getValue());
        } else if (jsonValue instanceof JsonNumber) {
            return jsonHandler != null ? jsonHandler.serialize(jsonValue) : ((JsonNumber) jsonValue).getNumber(clazz);
        } else if (jsonValue instanceof JsonBoolean) {
            return jsonHandler != null ? jsonHandler.serialize(jsonValue) : clazz.cast(((JsonBoolean) jsonValue).isValue());
        } else if (jsonValue instanceof JsonNull) {
            return jsonHandler != null ? jsonHandler.serialize(jsonValue) : null;
        } else {
            if (jsonValue instanceof JsonObject) {
                if (key != null) {
                    return this.fromJson(((JsonObject) jsonValue).get(key), clazz);
                }

                try {
                    T object = clazz.newInstance();
                    if (jsonHandler != null) {
                        return jsonHandler.serialize(jsonValue);
                    }

                    Class<?> objectClazz = object.getClass();
                    if (this.hasSuperclass(objectClazz)) {
                        for (Field declaredField : objectClazz.getSuperclass().getDeclaredFields()) {
                            declaredField.setAccessible(true);
                            declaredField.set(object, this.fromJson(declaredField.getName(), jsonValue, declaredField.getType()));
                        }
                    }

                    for (Field declaredField : objectClazz.getDeclaredFields()) {
                        declaredField.setAccessible(true);
                        declaredField.set(object, this.fromJson(declaredField.getName(), jsonValue, declaredField.getType()));
                    }

                    return object;
                } catch (IllegalAccessException | InstantiationException var11) {
                    var11.printStackTrace();
                }
            } else if (jsonValue instanceof JsonArray) {
                List list = new ArrayList();
                ((JsonArray) jsonValue).loop((integer, listValue) -> list.add(this.fromJson(listValue, clazz)));
                return list;
            }

            return null;
        }
    }

    public <T> boolean hasSuperclass(Class<T> clazz) {
        return clazz.getSuperclass() != null;
    }

    public void registerHandler(Class<?> clazz, JsonHandler<?> jsonHandler) {
        if (!this.isRegistered(clazz)) {
            this.handlers.put(clazz, jsonHandler);
        }

    }

    public void unregisterHandler(Class<?> clazz) {
        if (this.isRegistered(clazz)) {
            this.handlers.remove(clazz);
        }

    }

    public JsonHandler<?> getHandler(Class<?> clazz) {
        return !this.isRegistered(clazz) ? null : this.handlers.get(clazz);
    }

    public boolean isRegistered(Class<?> clazz) {
        return this.handlers.containsKey(clazz);
    }
}
