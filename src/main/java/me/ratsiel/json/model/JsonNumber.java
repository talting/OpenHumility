package me.ratsiel.json.model;

import me.ratsiel.json.abstracts.JsonValue;

import java.math.BigDecimal;

public class JsonNumber extends JsonValue {
    protected String value;

    public JsonNumber() {
    }

    public JsonNumber(final String key, final String value) {
        super(key);
        this.value = value;
    }

    public JsonNumber(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        final String space = this.createSpace();
        stringBuilder.append(space);
        if (this.getKey() != null && !this.getKey().isEmpty()) {
            stringBuilder.append("\"").append(this.getKey()).append("\"").append(" : ");
        }
        stringBuilder.append(this.getValue());
        return stringBuilder.toString();
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public byte byteValue() {
        return this.getBigDecimal().byteValue();
    }

    public int intValue() {
        return this.getBigDecimal().intValue();
    }

    public short shortValue() {
        return this.getBigDecimal().shortValue();
    }

    public double doubleValue() {
        return this.getBigDecimal().doubleValue();
    }

    public float floatValue() {
        return this.getBigDecimal().floatValue();
    }

    public long longValue() {
        return this.getBigDecimal().longValue();
    }

    public <T> Object getNumber(final Class<T> clazz) {
        if (clazz.equals(Byte.class) || clazz.equals(Byte.TYPE)) {
            return this.byteValue();
        }
        if (clazz.equals(Integer.class) || clazz.equals(Integer.TYPE)) {
            return this.intValue();
        }
        if (clazz.equals(Short.class) || clazz.equals(Short.TYPE)) {
            return this.shortValue();
        }
        if (clazz.equals(Double.class) || clazz.equals(Double.TYPE)) {
            return this.doubleValue();
        }
        if (clazz.equals(Float.class) || clazz.equals(Float.TYPE)) {
            return this.floatValue();
        }
        if (clazz.equals(Long.class) || clazz.equals(Long.TYPE)) {
            return this.longValue();
        }
        return null;
    }

    public BigDecimal getBigDecimal() {
        return new BigDecimal(this.getValue());
    }

    public boolean isNumeric(final String value) {
        if (value == null) {
            return false;
        }
        final boolean isNumber = value.matches("[+-]?(\\d+([.]\\d*)?([eE][+-]?\\d+)?|[.]\\d+([eE][+-]?\\d+)?)");
        if (isNumber) {
            String testValue;
            if (this.isInteger(value)) {
                final int integerValue = this.intValue();
                testValue = String.valueOf(integerValue);
            } else if (this.isDouble(value)) {
                final double doubleValue = this.doubleValue();
                if (doubleValue > Double.MAX_VALUE || doubleValue < -1022.0) {
                    return false;
                }
                testValue = String.valueOf(Math.round(doubleValue));
            } else if (this.isFloat(value)) {
                final float floatValue = this.floatValue();
                if (floatValue > Float.MAX_VALUE || floatValue < -126.0f) {
                    return false;
                }
                testValue = String.valueOf(Math.round(floatValue));
            } else {
                testValue = value;
            }
            try {
                Long.parseLong(testValue);
            } catch (Exception exception) {
                return false;
            }
        }
        return isNumber;
    }

    public boolean isFloat(final String value) {
        try {
            Float.parseFloat(value);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public boolean isInteger(final String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public boolean isDouble(final String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}
