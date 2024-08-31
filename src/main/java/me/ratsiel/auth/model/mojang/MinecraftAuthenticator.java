package me.ratsiel.auth.model.mojang;

import me.ratsiel.auth.abstracts.Authenticator;
import me.ratsiel.auth.abstracts.exception.AuthenticationException;
import me.ratsiel.auth.model.microsoft.MicrosoftAuthenticator;
import me.ratsiel.auth.model.microsoft.XboxToken;
import me.ratsiel.auth.model.mojang.profile.MinecraftCape;
import me.ratsiel.auth.model.mojang.profile.MinecraftProfile;
import me.ratsiel.auth.model.mojang.profile.MinecraftSkin;
import me.ratsiel.json.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MinecraftAuthenticator
        extends Authenticator<MinecraftToken> {
    protected final MicrosoftAuthenticator microsoftAuthenticator = new MicrosoftAuthenticator();

    @Override
    public MinecraftToken login(String email, String password) {
        try {
            URL url = new URL("https://authserver.mojang.com/authenticate");
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            JsonObject request = new JsonObject();
            JsonObject agent = new JsonObject();
            agent.add(new JsonString("name", "Minecraft"));
            agent.add(new JsonNumber("version", "1"));
            request.add("agent", agent);
            request.add(new JsonString("username", email));
            request.add(new JsonString("password", password));
            request.add(new JsonBoolean("requestUser", false));
            String requestBody = request.toString();
            httpURLConnection.setFixedLengthStreamingMode(requestBody.length());
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Host", "authserver.mojang.com");
            httpURLConnection.connect();
            try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
                outputStream.write(requestBody.getBytes(StandardCharsets.US_ASCII));
            }
            JsonObject jsonObject = this.parseResponseData(httpURLConnection);
            return new MinecraftToken(jsonObject.get("accessToken", JsonString.class).getValue(), ((JsonObject) jsonObject.get("selectedProfile")).get("name", JsonString.class).getValue());
        } catch (IOException exception) {
            throw new AuthenticationException(String.format("Authentication error. Request could not be made! Cause: '%s'", exception.getMessage()));
        }
    }

    public MinecraftToken loginWithXbox(String email, String password) {
        XboxToken xboxToken = this.microsoftAuthenticator.login(email, password);
        try {
            URL url = new URL("https://api.minecraftservices.com/authentication/login_with_xbox");
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            JsonObject request = new JsonObject();
            request.add("identityToken", new JsonString("XBL3.0 x=" + xboxToken.getUhs() + ";" + xboxToken.getToken()));
            String requestBody = request.toString();
            httpURLConnection.setFixedLengthStreamingMode(requestBody.length());
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Host", "api.minecraftservices.com");
            httpURLConnection.connect();
            try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
                outputStream.write(requestBody.getBytes(StandardCharsets.US_ASCII));
            }
            JsonObject jsonObject = this.microsoftAuthenticator.parseResponseData(httpURLConnection);
            return new MinecraftToken(jsonObject.get("access_token", JsonString.class).getValue(), jsonObject.get("username", JsonString.class).getValue());
        } catch (IOException exception) {
            throw new AuthenticationException(String.format("Authentication error. Request could not be made! Cause: '%s'", exception.getMessage()));
        }
    }

    public MinecraftProfile checkOwnership(MinecraftToken minecraftToken) {
        try {
            URL url = new URL("https://api.minecraftservices.com/minecraft/profile");
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + minecraftToken.getAccessToken());
            httpURLConnection.setRequestProperty("Host", "api.minecraftservices.com");
            httpURLConnection.connect();
            JsonObject jsonObject = this.parseResponseData(httpURLConnection);
            UUID uuid = this.generateUUID(jsonObject.get("id", JsonString.class).getValue());
            String name = jsonObject.get("name", JsonString.class).getValue();
            List minecraftSkins = this.json.fromJson(jsonObject.get("skins", JsonArray.class), List.class, MinecraftSkin.class);
            List minecraftCapes = this.json.fromJson(jsonObject.get("capes", JsonArray.class), List.class, MinecraftCape.class);
            return new MinecraftProfile(uuid, name, minecraftSkins, minecraftCapes);
        } catch (IOException exception) {
            throw new AuthenticationException(String.format("Authentication error. Request could not be made! Cause: '%s'", exception.getMessage()));
        }
    }

    public JsonObject parseResponseData(HttpURLConnection httpURLConnection) throws IOException {
        BufferedReader bufferedReader = httpURLConnection.getResponseCode() != 200 ? new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream())) : new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String lines = bufferedReader.lines().collect(Collectors.joining());
        JsonObject jsonObject = this.json.fromJsonString(lines, JsonObject.class);
        if (jsonObject.has("error")) {
            throw new AuthenticationException(String.format("Could not find profile!. Error: '%s'", jsonObject.get("errorMessage", JsonString.class).getValue()));
        }
        return jsonObject;
    }

    public UUID generateUUID(String trimmedUUID) throws IllegalArgumentException {
        if (trimmedUUID == null) {
            throw new IllegalArgumentException();
        }
        StringBuilder builder = new StringBuilder(trimmedUUID.trim());
        try {
            builder.insert(20, "-");
            builder.insert(16, "-");
            builder.insert(12, "-");
            builder.insert(8, "-");
            return UUID.fromString(builder.toString());
        } catch (StringIndexOutOfBoundsException e) {
            return null;
        }
    }
}