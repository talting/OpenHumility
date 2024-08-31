package me.ratsiel.json.abstracts;

public abstract class JsonValue {
    private int intend;
    private String key;

    public JsonValue() {
        this.intend = 0;
    }

    public JsonValue(final String key) {
        this.intend = 0;
        this.key = key;
    }

    protected String createSpace() {
        final StringBuilder spaceBuilder = new StringBuilder();
        for (int i = 0; i < this.intend; ++i) {
            spaceBuilder.append(" ");
        }
        return spaceBuilder.toString();
    }

    @Override
    public abstract String toString();

    public String getKey() {
        return this.key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public int getIntend() {
        return this.intend;
    }

    public void setIntend(final int intend) {
        this.intend = intend;
    }
}
