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
    MEMORY_ALLOCATION(5),

    /**
     * error code indicating a resource cannot be freed because it is still in use
     */
    IN_USE(6),

    /**
     * Error code indicating that a request cannot be fulfilled because destruction
     * is pending. Check that you didn't continue to use a source instance after
     * releasing it. This error is expected when publishing updates from a source
     * module whose destruction is about to be triggered. If you receive this error
     * when trying to destroy an instance this is essentially a "double-free" soft
     * error or race condition; the source has already been marked to be destroyed.
     */
    DESTRUCTION_PENDING(7),

    /**
     * Error code returned framework-side from a failed call to a module's
     * destructor. Further behaviour is unspecified (see documentation of
     * fbsource_destroy_f) with at least a memory leak or even external side-effects
     * that cannot be cleaned up (such as shared memory not being released). You
     * probably want to terminate your application as soon as you see encounter this
     * error and inform users about the issue.
     */
    MODULE_DESTRUCTOR_FAILED(8);

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
