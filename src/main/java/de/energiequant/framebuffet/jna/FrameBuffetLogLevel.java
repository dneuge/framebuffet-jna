package de.energiequant.framebuffet.jna;

import java.util.HashMap;
import java.util.Map;

/**
 * Message log levels used by the FrameBuffet logger.
 */
public enum FrameBuffetLogLevel {
    /**
     * Messages logged with this level describe critical errors which prevent
     * further operation (recovery is unlikely).
     */
    ERROR(127),

    /**
     * Messages logged with this level describe errors which impact operation but
     * might be possible to be recovered from.
     */
    WARN(63),

    /**
     * Used for any messages that are deemed useful to inform users of the module or
     * framework about.
     */
    INFO(31),

    /**
     * Used for regular debugging messages.
     */
    DEBUG(15),

    /**
     * Used for detailed debugging.
     */
    TRACE(7);

    private final byte nativeValue;

    private final static Map<Byte, FrameBuffetLogLevel> BY_NATIVE_VALUE = new HashMap<>();

    static {
        for (FrameBuffetLogLevel level : values()) {
            BY_NATIVE_VALUE.put(level.nativeValue, level);
        }
    }

    private FrameBuffetLogLevel(int nativeValue) {
        this.nativeValue = (byte) nativeValue;
    }

    static FrameBuffetLogLevel resolve(byte nativeValue) {
        FrameBuffetLogLevel error = BY_NATIVE_VALUE.get(nativeValue);
        if (error != null) {
            return error;
        }

        throw new IllegalArgumentException("Unknown log level: " + nativeValue);
    }

    public byte getNativeValue() {
        return nativeValue;
    }

    @Override
    public String toString() {
        return name() + " (" + nativeValue + ")";
    }
}
