package me.ratsiel.auth.model.microsoft;

public class XboxToken extends XboxLiveToken {
    public XboxToken() {
    }

    public XboxToken(final String token, final String uhs) {
        super(token, uhs);
    }
}
