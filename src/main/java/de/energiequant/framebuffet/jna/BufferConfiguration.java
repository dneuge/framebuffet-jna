package de.energiequant.framebuffet.jna;

/**
 * Describes the parameters of a buffer.
 */
public class BufferConfiguration {
    private int width;
    private int height;
    private int size;

    BufferConfiguration(FBSourceBufferConfigNative libConf) {
        // FIXME: native values are unsigned
        // FIXME: abort on excessive size
        this.width = libConf.width;
        this.height = libConf.height;
        this.size = (int) libConf.size;
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
