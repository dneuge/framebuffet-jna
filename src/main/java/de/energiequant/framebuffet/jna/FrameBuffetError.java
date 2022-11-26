package de.energiequant.framebuffet.jna;

import java.util.HashMap;
import java.util.Map;

/**
 * Error codes used by Frame Buffet.
 */
public enum FrameBuffetError {
    /**
     * error code indicating no error has occurred
     */
    NONE(0),

    /**
     * error code indicating an unspecific error has occurred
     */
    UNSPECIFIC(1),

    /**
     * error code indicating the provided buffer was too small to fit an image copy;
     * the buffer may or may not have been updated
     */
    BUFFER_TOO_SMALL(2),

    /**
     * error code indicating no frame was available; buffer may be blank or not have
     * been written to at all
     */
    NO_FRAME(3),

    /**
     * error code indicating the requested buffer does not exist
     */
    NO_SUCH_BUFFER(4),

    /**
     * error code indicating memory allocation failed
     */
    MEMORY_ALLOCATION(5);

    private final int code;

    private final static Map<Integer, FrameBuffetError> BY_CODE = new HashMap<>();

    static {
        for (FrameBuffetError error : values()) {
            BY_CODE.put(error.code, error);
        }
    }

    private FrameBuffetError(int code) {
        this.code = code;
    }

    static FrameBuffetError resolve(int code) {
        FrameBuffetError error = BY_CODE.get(code);
        if (error != null) {
            return error;
        }

        throw new IllegalArgumentException("Unknown error code: " + code);
    }

    @Override
    public String toString() {
        return name() + " (" + code + ")";
    }
}
