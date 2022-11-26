package de.energiequant.framebuffet.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * JNA binding to access native <code>fbsource_buffer_config_t</code>
 * structures.
 * <p>
 * <strong>Only to be used within framework bindings.</strong>
 * </p>
 */
public class FBSourceBufferConfigNative extends Structure {
    public int width;
    public int height;
    public long size;

    FBSourceBufferConfigNative(Pointer pointer) {
        super(pointer);
        read();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected List getFieldOrder() {
        return Arrays.asList(new String[] { "width", "height", "size" });
    }

}
