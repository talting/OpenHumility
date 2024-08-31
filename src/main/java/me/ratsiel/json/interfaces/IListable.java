package me.ratsiel.json.interfaces;

import me.ratsiel.json.abstracts.JsonValue;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IListable<T> {
    int size();

    void add(final T p0);

    void add(final int p0, final T p1);

    T get(final int p0, final Class<T> p1);

    T get(final int p0);

    void loop(final Consumer<JsonValue> p0);

    void loop(final BiConsumer<Integer, JsonValue> p0);
}
