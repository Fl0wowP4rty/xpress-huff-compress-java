package tech.skidonion.compress.xpresshuff;

public class Utils {
    public static int getInt32(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) | (bytes[offset + 1] & 0xFF) << 8 | (bytes[offset + 2] & 0xFF) << 16 | (bytes[offset + 3] & 0xFF) << 24;
    }

    public static short getInt16(byte[] bytes, int offset) {
        return (short) ((bytes[offset] & 0xFF) | (bytes[offset + 1] & 0xFF) << 8);
    }

    public static long getUInt32(byte[] bytes, int offset) {
        return Integer.toUnsignedLong(getInt32(bytes, offset));
    }

    public static int getUInt16(byte[] bytes, int offset) {
        return Short.toUnsignedInt(getInt16(bytes, offset));
    }

    public static int toUInt16(short value) {
        return Short.toUnsignedInt(value);
    }
}
