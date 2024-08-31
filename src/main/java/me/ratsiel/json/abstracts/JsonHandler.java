package me.ratsiel.json.abstracts;

public abstract class JsonHandler<T> {
    public abstract T serialize(final JsonValue p0);

    public abstract JsonValue deserialize(final T p0);
}
