package luna;

/**
 * Serializes the independent custom packet namespace inside the native command packet.
 *
 * @author lare96
 */
public final class CustomPacketCodec {

    public static final int ITEM_EXAMINE_PRICE = 0;
    public static final int MAX_ENVELOPE_BYTES = 254;
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private CustomPacketCodec() {
    }

    /**
     * Builds a complete ASCII envelope before the caller touches its outgoing buffer or cipher.
     */
    public static String encode(int id, Object... payload) {
        if (id < 0) throw new IllegalArgumentException("Custom packet IDs must be nonnegative.");
        if (payload == null || payload.length > 8)
            throw new IllegalArgumentException("Custom packets support at most eight non-null values.");
        StringBuilder message = new StringBuilder(64).append("/packet #").append(id).append(" [");
        for (int index = 0; index < payload.length; index++) {
            if (index > 0) message.append(',');
            Object value = payload[index];
            if (value instanceof String text) {
                appendString(message, text);
            } else if (value instanceof Boolean || value instanceof Byte || value instanceof Short
                    || value instanceof Integer || value instanceof Long) {
                message.append(value);
            } else {
                throw new IllegalArgumentException("Custom packet values must be strings, booleans or signed integers.");
            }
            checkLength(message);
        }
        message.append(']');
        checkLength(message);
        return message.toString();
    }

    private static void appendString(StringBuilder message, String text) {
        message.append('"');
        for (int index = 0; index < text.length(); index++) {
            char value = text.charAt(index);
            switch (value) {
                case '"' -> message.append("\\\"");
                case '\\' -> message.append("\\\\");
                case '\b' -> message.append("\\b");
                case '\f' -> message.append("\\f");
                case '\n' -> message.append("\\n");
                case '\r' -> message.append("\\r");
                case '\t' -> message.append("\\t");
                default -> {
                    if (value < 0x20 || value >= 0x7f) {
                        message.append("\\u").append(HEX[value >>> 12]).append(HEX[value >>> 8 & 15])
                                .append(HEX[value >>> 4 & 15]).append(HEX[value & 15]);
                    } else {
                        message.append(value);
                    }
                }
            }
            checkLength(message);
        }
        message.append('"');
    }

    private static void checkLength(StringBuilder message) {
        if (message.length() > MAX_ENVELOPE_BYTES)
            throw new IllegalArgumentException("Custom packet envelopes must fit in 254 ASCII bytes.");
    }
}
