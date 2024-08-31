package me.ratsiel.auth.model.microsoft;

import me.ratsiel.auth.abstracts.Authenticator;
import me.ratsiel.auth.abstracts.exception.AuthenticationException;
import me.ratsiel.json.model.JsonArray;
import me.ratsiel.json.model.JsonObject;
import me.ratsiel.json.model.JsonString;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MicrosoftAuthenticator extends Authenticator<XboxToken> {
    protected final String clientId = "00000000402b5328";
    protected final String scopeUrl = "service::user.auth.xboxlive.com::MBI_SSL";
    protected String loginUrl;
    protected String loginCookie;
    protected String loginPPFT;

    @Override
    public XboxToken login(final String email, final String password) {
        final MicrosoftToken microsoftToken = this.generateTokenPair(this.generateLoginCode(email, password));
        final XboxLiveToken xboxLiveToken = this.generateXboxTokenPair(microsoftToken);
        return this.generateXboxTokenPair(xboxLiveToken);
    }

    private String generateLoginCode(final String email, final String password) {
        try {
            final URL url = new URL("https://login.live.com/oauth20_authorize.srf?redirect_uri=https://login.live.com/oauth20_desktop.srf&scope=service::user.auth.xboxlive.com::MBI_SSL&display=touch&response_type=code&locale=en&client_id=00000000402b5328");
            final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            final InputStream inputStream = (httpURLConnection.getResponseCode() == 200) ? httpURLConnection.getInputStream() : httpURLConnection.getErrorStream();
            this.loginCookie = httpURLConnection.getHeaderField("set-cookie");
            final String responseData = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining());
            Matcher bodyMatcher = Pattern.compile("sFTTag:[ ]?'.*value=\"(.*)\"/>'").matcher(responseData);
            if (!bodyMatcher.find()) {
                throw new AuthenticationException("Authentication error. Could not find 'LOGIN-PFTT' tag from response!");
            }
            this.loginPPFT = bodyMatcher.group(1);
            bodyMatcher = Pattern.compile("urlPost:[ ]?'(.+?(?='))").matcher(responseData);
            if (!bodyMatcher.find()) {
                throw new AuthenticationException("Authentication error. Could not find 'LOGIN-URL' tag from response!");
            }
            this.loginUrl = bodyMatcher.group(1);
            if (this.loginCookie == null || this.loginPPFT == null || this.loginUrl == null) {
                throw new AuthenticationException("Authentication error. Error in authentication process!");
            }
        } catch (IOException exception) {
            throw new AuthenticationException(String.format("Authentication error. Request could not be made! Cause: '%s'", exception.getMessage()));
        }
        return this.sendCodeData(email, password);
    }

    private String sendCodeData(final String email, final String password) {
        final Map<String, String> requestData = new HashMap<String, String>();
        requestData.put("login", email);
        requestData.put("loginfmt", email);
        requestData.put("passwd", password);
        requestData.put("PPFT", this.loginPPFT);
        final String postData = this.encodeURL(requestData);
        String authToken;
        try {
            final byte[] data = postData.getBytes(StandardCharsets.UTF_8);
            final HttpURLConnection connection = (HttpURLConnection) new URL(this.loginUrl).openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            connection.setRequestProperty("Content-Length", String.valueOf(data.length));
            connection.setRequestProperty("Cookie", this.loginCookie);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            try (final OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(data);
            }
            if (connection.getResponseCode() != 200 || connection.getURL().toString().equals(this.loginUrl)) {
                throw new AuthenticationException("Authentication error. Username or password is not valid.");
            }
            final Pattern pattern = Pattern.compile("[?|&]code=([\\w.-]+)");
            final Matcher tokenMatcher = pattern.matcher(URLDecoder.decode(connection.getURL().toString(), StandardCharsets.UTF_8.name()));
            if (!tokenMatcher.find()) {
                throw new AuthenticationException("Authentication error. Could not handle data from response.");
            }
            authToken = tokenMatcher.group(1);
        } catch (IOException exception) {
            throw new AuthenticationException(String.format("Authentication error. Request could not be made! Cause: '%s'", exception.getMessage()));
        }
        this.loginUrl = null;
        this.loginCookie = null;
        this.loginPPFT = null;
        return authToken;
    }

    private void sendXboxRequest(final HttpURLConnection httpURLConnection, final JsonObject request, final JsonObject properties) throws IOException {
        request.add("Properties", properties);
        final String requestBody = request.toString();
        httpURLConnection.setFixedLengthStreamingMode(requestBody.length());
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        httpURLConnection.connect();
        try (final OutputStream outputStream = httpURLConnection.getOutputStream()) {
            outputStream.write(requestBody.getBytes(StandardCharsets.US_ASCII));
        }
    }

    private MicrosoftToken generateTokenPair(final String authToken) {
        try {
            final Map<String, String> arguments = new HashMap<String, String>();
            arguments.put("client_id", "00000000402b5328");
            arguments.put("code", authToken);
            arguments.put("grant_type", "authorization_code");
            arguments.put("redirect_uri", "https://login.live.com/oauth20_desktop.srf");
            arguments.put("scope", "service::user.auth.xboxlive.com::MBI_SSL");
            final StringJoiner argumentBuilder = new StringJoiner("&");
            for (final Map.Entry<String, String> entry : arguments.entrySet()) {
                argumentBuilder.add(this.encodeURL(entry.getKey()) + "=" + this.encodeURL(entry.getValue()));
            }
            final byte[] data = argumentBuilder.toString().getBytes(StandardCharsets.UTF_8);
            final URL url = new URL("https://login.live.com/oauth20_token.srf");
            final URLConnection urlConnection = url.openConnection();
            final HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setFixedLengthStreamingMode(data.length);
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.connect();
            try (final OutputStream outputStream = httpURLConnection.getOutputStream()) {
                outputStream.write(data);
            }
            final JsonObject jsonObject = this.parseResponseData(httpURLConnection);
            return new MicrosoftToken(jsonObject.get("access_token", JsonString.class).getValue(), jsonObject.get("refresh_token", JsonString.class).getValue());
        } catch (IOException exception) {
            throw new AuthenticationException(String.format("Authentication error. Request could not be made! Cause: '%s'", exception.getMessage()));
        }
    }

    public XboxLiveToken generateXboxTokenPair(final MicrosoftToken microsoftToken) {
        try {
            final URL url = new URL("https://user.auth.xboxlive.com/user/authenticate");
            final URLConnection urlConnection = url.openConnection();
            final HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.setDoOutput(true);
            final JsonObject request = new JsonObject();
            request.add("RelyingParty", new JsonString("http://auth.xboxlive.com"));
            request.add("TokenType", new JsonString("JWT"));
            final JsonObject properties = new JsonObject();
            properties.add("AuthMethod", new JsonString("RPS"));
            properties.add("SiteName", new JsonString("user.auth.xboxlive.com"));
            properties.add("RpsTicket", new JsonString(microsoftToken.getToken()));
            this.sendXboxRequest(httpURLConnection, request, properties);
            final JsonObject jsonObject = this.parseResponseData(httpURLConnection);
            final String uhs = ((JsonObject) jsonObject.get("DisplayClaims", JsonObject.class).get("xui", JsonArray.class).get(0)).get("uhs", JsonString.class).getValue();
            return new XboxLiveToken(jsonObject.get("Token", JsonString.class).getValue(), uhs);
        } catch (IOException exception) {
            throw new AuthenticationException(String.format("Authentication error. Request could not be made! Cause: '%s'", exception.getMessage()));
        }
    }

    public XboxToken generateXboxTokenPair(final XboxLiveToken xboxLiveToken) {
        try {
            final URL url = new URL("https://xsts.auth.xboxlive.com/xsts/authorize");
            final URLConnection urlConnection = url.openConnection();
            final HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            final JsonObject request = new JsonObject();
            request.add("RelyingParty", new JsonString("rp://api.minecraftservices.com/"));
            request.add("TokenType", new JsonString("JWT"));
            final JsonObject properties = new JsonObject();
            properties.add("SandboxId", new JsonString("RETAIL"));
            final JsonArray userTokens = new JsonArray();
            userTokens.add(new JsonString(xboxLiveToken.getToken()));
            properties.add("UserTokens", userTokens);
            this.sendXboxRequest(httpURLConnection, request, properties);
            if (httpURLConnection.getResponseCode() == 401) {
                throw new AuthenticationException("No xbox account was found!");
            }
            final JsonObject jsonObject = this.parseResponseData(httpURLConnection);
            final String uhs = ((JsonObject) jsonObject.get("DisplayClaims", JsonObject.class).get("xui", JsonArray.class).get(0)).get("uhs", JsonString.class).getValue();
            return new XboxToken(jsonObject.get("Token", JsonString.class).getValue(), uhs);
        } catch (IOException exception) {
            throw new AuthenticationException(String.format("Authentication error. Request could not be made! Cause: '%s'", exception.getMessage()));
        }
    }

    public JsonObject parseResponseData(final HttpURLConnection httpURLConnection) throws IOException {
        BufferedReader bufferedReader;
        if (httpURLConnection.getResponseCode() != 200) {
            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
        } else {
            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        }
        final String lines = bufferedReader.lines().collect(Collectors.joining());
        final JsonObject jsonObject = this.json.fromJsonString(lines, JsonObject.class);
        if (jsonObject.has("error")) {
            throw new AuthenticationException(jsonObject.get("error") + ": " + jsonObject.get("error_description"));
        }
        return jsonObject;
    }

    private String encodeURL(final String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException exception) {
            throw new UnsupportedOperationException(exception);
        }
    }

    private String encodeURL(final Map<String, String> map) {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s", this.encodeURL(entry.getKey()), this.encodeURL(entry.getValue())));
        }
        return sb.toString();
    }
}
