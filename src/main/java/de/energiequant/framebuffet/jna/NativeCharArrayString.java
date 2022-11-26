package de.energiequant.framebuffet.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * Conversion to and from native <code>char*</code> "C strings" with
 * null-termination and size indication.
 */
class NativeCharArrayString {
    Pointer pointer;
    long size;

    private NativeCharArrayString() {
        // nothing to do
    }

    NativeCharArrayString(Pointer pointer, long size) {
        this.pointer = pointer;
        this.size = size;
    }

    /**
     * Reads the content as a string in system encoding, handling size constraint
     * and optional null termination.
     * 
     * @return content read in system-encoding; <code>null</code> if pointer is
     *         <code>null</code>
     */
    String asJavaString() {
        if (pointer == null) {
            return null;
        }

        if (size == 0) {
            return "";
        }

        byte[] arr = pointer.getByteArray(0L, (int) size);

        // locate optional null termination
        int endExcl = arr.length;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == 0) {
                endExcl = i;
                break;
            }
        }

        return new String(arr, 0, endExcl);
    }

    /**
     * Converts the given string to a null-terminated <code>char*</code>. Memory
     * allocation is detached from JNA to prevent the strings from being
     * garbage-collected and eventually double-freed by Java. Make sure the created
     * pointer is actually managed by someone afterwards, otherwise it will leak
     * memory.
     * 
     * @param s string to be converted
     * @return converted pointer to a native null-terminated <code>char</code>
     *         representation compatible with C
     */
    static NativeCharArrayString createDetached(String s) {
        // NOTE: we cannot use Memory as it gets freed on GC causing all sorts of race
        // conditions and memory access violations

        byte[] bytes = s.getBytes();

        NativeCharArrayString out = new NativeCharArrayString();
        out.size = bytes.length + 1;

        long addr = Native.malloc(out.size);
        if (addr == 0) {
            throw new IllegalStateException("malloc for our string failed?! tried to get " + bytes.length + " bytes");
        }

        out.pointer = new Pointer(addr);
        out.pointer.write(0, bytes, 0, bytes.length);
        out.pointer.setByte(bytes.length, (byte) 0);

        return out;
    }
}
