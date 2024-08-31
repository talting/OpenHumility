package me.ratsiel.auth.model.mojang.profile;

import me.ratsiel.auth.abstracts.TextureVariable;

public class MinecraftSkin extends TextureVariable {
    private String variant;

    public MinecraftSkin() {
    }

    public MinecraftSkin(final String variant) {
        this.variant = variant;
    }

    public MinecraftSkin(final String id, final String state, final String url, final String alias, final String variant) {
        super(id, state, url, alias);
        this.variant = variant;
    }

    public String getVariant() {
        return this.variant;
    }

    @Override
    public String toString() {
        return "MinecraftSkin{id='" + this.getId() + '\'' + ", state='" + this.getState() + '\'' + ", url='" + this.getUrl() + '\'' + ", alias='" + this.getAlias() + '\'' + "variant='" + this.variant + '\'' + '}';
    }
}
