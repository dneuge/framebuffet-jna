package de.energiequant.framebuffet.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * Describes the parameters of a buffer.
 */
public class BufferConfiguration {
    private int width;
    private int height;
    private int size;

    private BufferConfiguration(FBSourceBufferConfigNative libConf) {
        // FIXME: native values are unsigned
        // FIXME: abort on excessive size
        this.width = libConf.width;
        this.height = libConf.height;
        this.size = (int) libConf.size;
    }

    /**
     * Reads from the given pointer and frees the memory afterwards.
     * 
     * @param pointer pointer to structure to be read
     * @return converted object
     */
    static BufferConfiguration consumeNative(Pointer pointer) {
        if (pointer == null) {
            throw new NullPointerException("given pointer is null");
        }

        FBSourceBufferConfigNative libConfig = new FBSourceBufferConfigNative(pointer);
        BufferConfiguration out = new BufferConfiguration(libConfig);
        Native.free(Pointer.nativeValue(pointer));

        return out;
    }

    /**
     * @return the width of the buffer's image in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height of the buffer's image in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the minimum size in bytes required for the whole image
     */
    public int getSize() {
        return size;
    }
}
