package de.energiequant.framebuffet.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * JNA binding to access native <code>fbcollections_map_entry_t</code>
 * structures.
 * <p>
 * <strong>Only to be used within framework bindings.</strong>
 * </p>
 */
public class FBCollectionsMapEntryNative extends Structure {
    public Pointer _prev;
    public Pointer _next;

    public Pointer key;
    public long key_size;

    public Pointer value;
    public long value_size;

    FBCollectionsMapEntryNative(Pointer pointer) {
        super(pointer);
        read();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected List getFieldOrder() {
        return Arrays.asList(new String[] { "_prev", "_next", "key", "key_size", "value", "value_size" });
    }

    public boolean hasPrev() {
        return (_prev != null);
    }

    public boolean hasNext() {
        return (_next != null);
    }

    public FBCollectionsMapEntryNative head() {
        FBCollectionsMapEntryNative entry = this;

        while (entry.hasPrev()) {
            entry = prev();
        }

        return entry;
    }

    public FBCollectionsMapEntryNative prev() {
        if (_prev == null) {
            return null;
        }

        return new FBCollectionsMapEntryNative(_prev);
    }

    public FBCollectionsMapEntryNative next() {
        if (_next == null) {
            return null;
        }

        return new FBCollectionsMapEntryNative(_next);
    }
}
