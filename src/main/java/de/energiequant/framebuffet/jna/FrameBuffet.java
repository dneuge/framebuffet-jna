package de.energiequant.framebuffet.jna;

import java.io.Closeable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Higher-level bindings for Frame Buffet library access.
 * <p>
 * To stop and free the instance call {@link #close()} at an appropriate time.
 * </p>
 */
public class FrameBuffet implements Closeable, AutoCloseable {
    private final FrameBuffetNative lib = FrameBuffetNative.INSTANCE;
    private Pointer instance;

    /**
     * Creates a new instance of the framework.
     * 
     * @throws InstantiationFailed if the framework failed to initialize
     */
    public FrameBuffet() {
        instance = lib.framebuffet_init();
        if (instance == null) {
            throw new InstantiationFailed("Framework could not be initialized");
        }
    }

    /**
     * Loads a module compiled as a shared library from the given path.
     * 
     * @param modulePath path to the shared library to load
     * @throws ModuleLoadFailed if the module could not be loaded
     */
    public void loadModule(File modulePath) {
        String path = modulePath.getAbsolutePath();
        FrameBuffetError error = FrameBuffetError.resolve( //
                lib.framebuffet_load_module(instance, path) //
        );

        if (error != FrameBuffetError.NONE) {
            throw new ModuleLoadFailed("Failed to load module from " + path, error);
        }
    }

    /**
     * Creates a new instance of the specified module, to be registered under the
     * given name.
     * 
     * @param moduleName   module to be instantiated, as registered to the framework
     * @param instanceName name of the instance to be created, as to be registered
     *                     with the framework
     * @return the framebuffer source instance
     * @throws InstantiationFailed if the source could not be instantiated
     */
    public Source createSource(String moduleName, String instanceName) {
        PointerByReference sourceInstanceRef = new PointerByReference();
        FrameBuffetError error = FrameBuffetError.resolve( //
                lib.framebuffet_create_source_instance(instance, sourceInstanceRef, moduleName, instanceName) //
        );

        if (error != FrameBuffetError.NONE) {
            throw new InstantiationFailed(
                    "Failed to create source instance \"" + instanceName + "\" of module " + moduleName, //
                    error //
            );
        }

        return new Source(this, sourceInstanceRef);
    }

    @Override
    public void close() {
        if (instance == null) {
            return;
        }

        lib.framebuffet_destroy(instance);

        instance = null;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void finalize() throws Throwable {
        // TODO: find a better way that works in Java 8 as well as later versions
        super.finalize();
        close();
    }

    int getNumBuffers(Source source) {
        return lib.framebuffet_get_num_buffers(source.instance);
    }

    Map<String, String> getSourceConfiguration(Source source) {
        PointerByReference configRef = new PointerByReference();
        FrameBuffetError error = FrameBuffetError.resolve( //
                lib.framebuffet_get_source_configuration(source.instance, configRef) //
        );

        if (error != FrameBuffetError.NONE) {
            throw new ModuleFailed("Failed to get configuration from source", error);
        }

        Map<String, String> map = mapFromLib(configRef.getValue());

        lib.fbcollections_map_free_map_and_payloads(configRef.getValue());

        return map;
    }

    void configureSource(Source source, Map<String, String> config) {
        Pointer libConfig = mapToLib(config).orElseThrow( //
                () -> new IllegalArgumentException("Map could not be converted to FrameBuffet collection") //
        );

        FrameBuffetError error = FrameBuffetError.resolve( //
                lib.framebuffet_configure_source_instance(source.instance, libConfig) //
        );

        lib.fbcollections_map_free_map_and_payloads(libConfig);

        if (error != FrameBuffetError.NONE) {
            throw new ModuleFailed("Failed to configure source", error);
        }
    }

    Optional<BufferConfiguration> getBufferConfiguration(Source source, int id) {
        Pointer libConfig = lib.framebuffet_get_buffer_configuration(source.instance, id);
        if (libConfig == null) {
            return Optional.empty();
        }

        return Optional.of(BufferConfiguration.consumeNative(libConfig));
    }

    int copyLatestFrame(Source source, byte[] destination, int bufferId) {
        IntByReference libToken = new IntByReference();

        FrameBuffetError error = FrameBuffetError.resolve( //
                lib.framebuffet_copy_latest_frame( //
                        source.instance, //
                        destination, //
                        destination.length, //
                        bufferId, //
                        libToken.getPointer() //
                ) //
        );

        if (error != FrameBuffetError.NONE) {
            throw new ModuleFailed("Failed to copy buffer from source", error);
        }

        return libToken.getValue();
    }

    boolean hasUpdate(Source source, int bufferId, int lastToken) {
        return lib.framebuffet_has_update(source.instance, bufferId, lastToken) != 0;
    }

    /**
     * Creates a new framebuffet collections library map containing all entries as
     * null-terminated strings. All memory is supposed to be managed by the
     * framework upon success, thus requiring "detached" copies of all entry keys
     * and values; i.e. JNA is not allowed to manage them.
     * 
     * @param javaMap map to be converted
     * @return a pointer to the converted and detached map; empty if creation failed
     */
    private Optional<Pointer> mapToLib(Map<String, String> javaMap) {
        Pointer libMap = lib.fbcollections_map_create();
        if (libMap == null) {
            return Optional.empty();
        }

        for (Map.Entry<String, String> javaEntry : javaMap.entrySet()) {
            NativeCharArrayString libKey = NativeCharArrayString.createDetached(javaEntry.getKey());
            NativeCharArrayString libValue = NativeCharArrayString.createDetached(javaEntry.getValue());

            // FIXME: sizes need to be recoded to unsigned long
            int error = lib.fbcollections_map_put_and_free_old_entry_with_payloads( //
                    libMap, //
                    libKey.pointer, //
                    libKey.size, //
                    libValue.pointer, //
                    libValue.size //
            );

            if (error < 0) {
                // entry has not been registered and copied strings need to be freed
                Native.free(Pointer.nativeValue(libKey.pointer));
                Native.free(Pointer.nativeValue(libValue.pointer));

                // we have no more use for the native map, release
                lib.fbcollections_map_free_map_and_payloads(libMap);

                return Optional.empty();
            }
        }

        return Optional.of(libMap);
    }

    /**
     * Reads the whole content of a framebuffet collections library map to Java,
     * assuming keys and values are both null-terminated strings.
     * 
     * @param libMap any entry of the map to be converted
     * @return converted map
     */
    private Map<String, String> mapFromLib(Pointer libMap) {
        Map<String, String> out = new HashMap<>();

        // pointer can be any entry, we need to find the head to process all entries
        FBCollectionsMapEntryNative entry = new FBCollectionsMapEntryNative(libMap).head();

        while (entry.hasNext()) {
            // head entry never has content and needs to be skipped
            entry = entry.next();

            String key = new NativeCharArrayString(entry.key, entry.key_size).asJavaString();
            String value = new NativeCharArrayString(entry.value, entry.value_size).asJavaString();

            if (out.containsKey(key)) {
                // it is technically possible that a library map contains duplicates if the same
                // key in which case the library's find function will always return only the
                // first value; we mimic that behaviour by ignoring all duplicates except the
                // first one
                continue;
            }

            out.put(key, value);
        }

        return out;
    }

    /**
     * Exceptions related to errors specific to the Frame Buffet framework, usually
     * holding a {@link FrameBuffetError} which may provide further indication of
     * what went wrong.
     */
    public static class FrameBuffetException extends RuntimeException {
        private final FrameBuffetError error;

        private FrameBuffetException(String msg) {
            super(msg);
            this.error = null;
        }

        private FrameBuffetException(String msg, FrameBuffetError error) {
            super(msg + " [FrameBuffet error " + error + "]");
            this.error = error;
        }

        public Optional<FrameBuffetError> getError() {
            return Optional.ofNullable(error);
        }
    }

    /**
     * Thrown if instantiation of library or modules fails.
     */
    public static class InstantiationFailed extends FrameBuffetException {
        private InstantiationFailed(String msg) {
            super(msg);
        }

        private InstantiationFailed(String msg, FrameBuffetError error) {
            super(msg, error);
        }
    }

    /**
     * Thrown if a module cannot be loaded by the framework.
     */
    public static class ModuleLoadFailed extends FrameBuffetException {
        private ModuleLoadFailed(String msg, FrameBuffetError error) {
            super(msg, error);
        }
    }

    /**
     * Thrown if errors occur while trying to interact with modules.
     */
    public static class ModuleFailed extends FrameBuffetException {
        private ModuleFailed(String msg, FrameBuffetError error) {
            super(msg, error);
        }
    }
}
