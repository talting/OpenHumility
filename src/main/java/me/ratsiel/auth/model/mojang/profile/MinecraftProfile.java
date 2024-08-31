package me.ratsiel.auth.model.mojang.profile;

import java.util.List;
import java.util.UUID;

public class MinecraftProfile {
    private UUID uuid;
    private String username;
    private List<MinecraftSkin> skins;
    private List<MinecraftCape> capes;

    public MinecraftProfile() {
    }

    public MinecraftProfile(final UUID uuid, final String username, final List<MinecraftSkin> skins, final List<MinecraftCape> capes) {
        this.uuid = uuid;
        this.username = username;
        this.skins = skins;
        this.capes = capes;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getUsername() {
        return this.username;
    }

    public List<MinecraftSkin> getSkins() {
        return this.skins;
    }

    public List<MinecraftCape> getCapes() {
        return this.capes;
    }

    @Override
    public String toString() {
        return "MinecraftProfile{uuid=" + this.uuid + ", username='" + this.username + '\'' + ", skins=" + this.skins + ", capes=" + this.capes + '}';
    }
}
