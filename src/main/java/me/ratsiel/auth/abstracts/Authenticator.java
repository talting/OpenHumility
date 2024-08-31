package me.ratsiel.auth.abstracts;

import me.ratsiel.json.Json;

public abstract class Authenticator<T> {
    protected final Json json;

    public Authenticator() {
        this.json = new Json();
    }

    public abstract T login(final String p0, final String p1);
}
