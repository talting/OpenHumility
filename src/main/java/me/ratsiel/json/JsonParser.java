package me.ratsiel.json;

import me.ratsiel.json.abstracts.JsonValue;
import me.ratsiel.json.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class JsonParser {
    private char[] jsonBuffer;
    private int jsonBufferPosition;

    public JsonParser() {
        this.jsonBufferPosition = 0;
    }

    public <T extends JsonValue> T parse(final InputStream inputStream, final Class<T> clazz) {
        return clazz.cast(this.parse(inputStream));
    }

    public JsonValue parse(final InputStream inputStream) {
        try {
            this.prepareParser(inputStream);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        this.skipWhiteSpace();
        switch (this.peekChar()) {
            case '+':
            case '-':
            case '.':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case 'E':
            case 'e': {
                return this.parseJsonNumber();
            }
            case 'f':
            case 't': {
                return this.parseJsonBoolean();
            }
            case 'n': {
                return this.parseJsonNull();
            }
            case '\"': {
                return this.parseJsonString();
            }
            case '{': {
                final JsonObject jsonObject = this.parseJsonObject();
                this.jsonBuffer = null;
                this.jsonBufferPosition = 0;
                return jsonObject;
            }
            case '[': {
                final JsonArray jsonArray = this.parseJsonArray();
                this.jsonBuffer = null;
                this.jsonBufferPosition = 0;
                return jsonArray;
            }
            default: {
                throw new RuntimeException("Could not parse file to Json!");
            }
        }
    }

    public <T extends JsonValue> T parse(final String json, final Class<T> clazz) {
        return clazz.cast(this.parse(json));
    }

    public JsonValue parse(final String json) {
        return this.parse(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
    }

    public <T extends JsonValue> T parse(final File file, final Class<T> clazz) throws IOException {
        return clazz.cast(this.parse(file));
    }

    public JsonValue parse(final File file) throws IOException {
        return this.parse(Files.newInputStream(file.toPath()));
    }

    public void prepareParser(final InputStream inputStream) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        this.jsonBuffer = stringBuilder.toString().toCharArray();
        bufferedReader.close();
    }

    public char parseChar() {
        try {
            return this.jsonBuffer[this.jsonBufferPosition++];
        } catch (IndexOutOfBoundsException exception) {
            return '\uffff';
        }
    }

    public char peekChar() {
        final char currentChar = this.parseChar();
        --this.jsonBufferPosition;
        return currentChar;
    }

    public void skipWhiteSpace() {
        while (this.isWhiteSpace()) {
            this.parseChar();
        }
    }

    public JsonObject parseJsonObject() {
        final JsonObject jsonObject = new JsonObject();
        this.skipWhiteSpace();
        this.parseChar();
        char currentChar = this.peekChar();
        while (currentChar != '}') {
            this.skipWhiteSpace();
            switch (this.peekChar()) {
                case '\"': {
                    final String key = this.parseJsonString().getValue();
                    this.skipWhiteSpace();
                    currentChar = this.parseChar();
                    this.skipWhiteSpace();
                    switch (this.peekChar()) {
                        case '+':
                        case '-':
                        case '.':
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                        case 'E':
                        case 'e': {
                            final JsonNumber jsonNumber = this.parseJsonNumber();
                            jsonNumber.setKey(key);
                            jsonObject.add(jsonNumber);
                            continue;
                        }
                        case 'f':
                        case 't': {
                            final JsonBoolean jsonBoolean = this.parseJsonBoolean();
                            jsonBoolean.setKey(key);
                            jsonObject.add(jsonBoolean);
                            continue;
                        }
                        case 'n': {
                            final JsonNull jsonNull = this.parseJsonNull();
                            jsonNull.setKey(key);
                            jsonObject.add(jsonNull);
                            continue;
                        }
                        case '\"': {
                            final JsonString jsonString = this.parseJsonString();
                            jsonString.setKey(key);
                            jsonObject.add(jsonString);
                            continue;
                        }
                        case '{': {
                            final JsonObject object = this.parseJsonObject();
                            object.setKey(key);
                            jsonObject.add(object);
                            continue;
                        }
                        case '[': {
                            final JsonArray jsonArray = this.parseJsonArray();
                            jsonArray.setKey(key);
                            jsonObject.add(jsonArray);
                            continue;
                        }
                    }
                    continue;
                }
                case ',':
                case '}': {
                    currentChar = this.parseChar();
                    continue;
                }
                default: {
                    throw new RuntimeException(String.format("Could not parse JsonObject stuck at Index: %s as Char: %s", this.jsonBufferPosition, this.jsonBuffer[this.jsonBufferPosition]));
                }
            }
        }
        return jsonObject;
    }

    public JsonArray parseJsonArray() {
        final JsonArray jsonArray = new JsonArray();
        this.skipWhiteSpace();
        this.parseChar();
        char currentChar = this.peekChar();
        while (currentChar != ']') {
            this.skipWhiteSpace();
            switch (this.peekChar()) {
                case '+':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case 'E':
                case 'e': {
                    jsonArray.add(this.parseJsonNumber());
                    continue;
                }
                case '\"': {
                    jsonArray.add(this.parseJsonString());
                    continue;
                }
                case 'f':
                case 't': {
                    jsonArray.add(this.parseJsonBoolean());
                    continue;
                }
                case 'n': {
                    jsonArray.add(this.parseJsonNull());
                    continue;
                }
                case '{': {
                    jsonArray.add(this.parseJsonObject());
                    continue;
                }
                case '[': {
                    jsonArray.add(this.parseJsonArray());
                    continue;
                }
                case ',':
                case ']': {
                    currentChar = this.parseChar();
                    continue;
                }
                default: {
                    throw new RuntimeException(String.format("Could not parse JsonArray stuck at Index: %s as Char: %s", this.jsonBufferPosition, this.jsonBuffer[this.jsonBufferPosition]));
                }
            }
        }
        return jsonArray;
    }

    public JsonString parseJsonString() {
        final JsonString jsonString = new JsonString();
        final StringBuilder stringBuilder = new StringBuilder();
        this.skipWhiteSpace();
        this.parseChar();
        for (char currentChar = this.parseChar(); currentChar != '\"'; currentChar = this.parseChar()) {
            if (currentChar == '\\') {
                stringBuilder.append(currentChar);
                currentChar = this.parseChar();
            }
            stringBuilder.append(currentChar);
        }
        this.skipWhiteSpace();
        jsonString.setValue(stringBuilder.toString());
        return jsonString;
    }

    public JsonNull parseJsonNull() {
        final char currentChar = this.parseChar();
        if (currentChar == 'n' && this.isNextChar('u', 0) && this.isNextChar('l', 1) && this.isNextChar('l', 2)) {
            for (int i = 0; i < 3; ++i) {
                ++this.jsonBufferPosition;
            }
            return new JsonNull();
        }
        return null;
    }

    public JsonBoolean parseJsonBoolean() {
        final char currentChar = this.parseChar();
        JsonBoolean jsonBoolean = null;
        if (currentChar == 't' && this.isNextChar('r', 0) && this.isNextChar('u', 1) && this.isNextChar('e', 2)) {
            for (int i = 0; i < 3; ++i) {
                ++this.jsonBufferPosition;
            }
            jsonBoolean = new JsonBoolean();
            jsonBoolean.setValue(true);
        } else if (currentChar == 'f' && this.isNextChar('a', 0) && this.isNextChar('l', 1) && this.isNextChar('s', 2) && this.isNextChar('e', 3)) {
            for (int i = 0; i < 4; ++i) {
                ++this.jsonBufferPosition;
            }
            jsonBoolean = new JsonBoolean();
            jsonBoolean.setValue(false);
        }
        return jsonBoolean;
    }

    public JsonNumber parseJsonNumber() {
        final StringBuilder stringBuilder = new StringBuilder();
        for (char currentChar = this.parseChar(); this.isNumber(currentChar); currentChar = this.parseChar()) {
            stringBuilder.append(currentChar);
        }
        --this.jsonBufferPosition;
        final JsonNumber jsonNumber = new JsonNumber();
        jsonNumber.setValue(stringBuilder.toString());
        return jsonNumber;
    }

    public boolean isWhiteSpace() {
        final char currentChar = this.jsonBuffer[this.jsonBufferPosition];
        return currentChar == ' ' || currentChar == '\t' || currentChar == '\r' || currentChar == '\n';
    }

    public boolean isNextChar(final char currentChar, final int position) {
        for (int i = 0; i < position; ++i) {
            this.parseChar();
        }
        final char nextChar = this.peekChar();
        for (int j = 0; j < position; ++j) {
            --this.jsonBufferPosition;
        }
        return nextChar == currentChar;
    }

    public boolean isNumber(final char currentChar) {
        final char[] array;
        final char[] numberElements = array = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', 'E', 'e', '+', '-'};
        for (final char listChar : array) {
            if (listChar == currentChar) {
                return true;
            }
        }
        return false;
    }
}
