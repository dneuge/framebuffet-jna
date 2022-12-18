package de.energiequant.framebuffet.jna;

import java.io.Closeable;
import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import de.energiequant.framebuffet.jna.FrameBuffetNative.LogReceiverNative;

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
     * The log receiver currently registered with the framework instance. This
     * reference must be kept for the duration of the framework instance's existence
     * to avoid garbage collection of the callback provided to the framework.
     */
    @SuppressWarnings("unused")
    private FrameBuffetNative.LogReceiverNative providedLibLogReceiver;

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
     * Sets the log receiver callback to be used by this framework instance.
     * 
     * <p>
     * The receiver should be provided as early as possible as the framework does
     * not perform any logging without a callback. Also remember to set the desired
     * log level {@link #setMinimumLogLevel(FrameBuffetLogLevel)}.
     * </p>
     * 
     * <p>
     * If you are not receiving expected messages, check if framework and modules
     * were actually compiled with the wanted log level enabled. For performance
     * reasons, generally unwanted log levels (such as
     * {@link FrameBuffetLogLevel#DEBUG} or {@link FrameBuffetLogLevel#TRACE}) may
     * already be filtered out at compile-time unless configured otherwise. The
     * runtime log level is only an additional filter for the messages that were not
     * already optimized away.
     * </p>
     * 
     * @param logReceiver will be called for every message not already filtered out
     *                    by the framework
     * @see #setMinimumLogLevel(FrameBuffetLogLevel)
     */
    public void provideLogReceiver(BiConsumer<FrameBuffetLogLevel, String> logReceiver) {
        LogReceiverNative libLogReceiver = new FrameBuffetNative.LogReceiverNative() {
            @Override
            public void invoke(byte logLevel, Pointer message) {
                String javaMessage = new NativeCharArrayString(message).asJavaString();

                FrameBuffetLogLevel javaLogLevel = null;
                try {
                    javaLogLevel = FrameBuffetLogLevel.resolve(logLevel);
                } catch (IllegalArgumentException ex) {
                    // TODO: log internally?
                }

                try {
                    logReceiver.accept(javaLogLevel, javaMessage);
                } catch (Exception ex) {
                    // TODO: log internally?
                }
            }
        };

        lib.framebuffet_provide_log_receiver(instance, libLogReceiver);
        providedLibLogReceiver = libLogReceiver;
    }

    /**
     * Sets the minimum log message level required to call the log receiver (see
     * {@link #provideLogReceiver(BiConsumer)}).
     * 
     * <p>
     * If you are not receiving expected messages, check if framework and modules
     * were actually compiled with the wanted log level enabled. For performance
     * reasons, generally unwanted log levels (such as
     * {@link FrameBuffetLogLevel#DEBUG} or {@link FrameBuffetLogLevel#TRACE}) may
     * already be filtered out at compile-time unless configured otherwise. The
     * runtime log level is only an additional filter for the messages that were not
     * already optimized away.
     * </p>
     * 
     * @param logLevel least severe log level to request log output for
     * @see #provideLogReceiver(BiConsumer)
     */
    public void setMinimumLogLevel(FrameBuffetLogLevel logLevel) {
        FrameBuffetError error = FrameBuffetError.resolve( //
                lib.framebuffet_set_minimum_log_level(instance, logLevel.getNativeValue()) //
        );

        if (error != FrameBuffetError.NONE) {
            throw new ConfigurationFailed("Failed to set minimum log level to " + logLevel, error);
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
     * Configures this instance by applying all actions as defined in the given
     * JSON-encoded string.
     * 
     * <p>
     * Before configuring, all required modules need to have been registered with
     * the framework instance and logging should have been configured, if wanted.
     * Previous configurations are extended. Create a new {@link FrameBuffet}
     * instance to restart from scratch, if needed.
     * </p>
     * 
     * @param json contents of the JSON file to apply
     * @throws ConfigurationFailed if any of the requested actions could not be
     *                             applied
     */
    public void configureFromActionsJson(String json) {
        FrameBuffetError error = FrameBuffetError.resolve( //
                lib.framebuffet_configure_from_actions_json(instance, json) //
        );

        if (error != FrameBuffetError.NONE) {
            throw new ConfigurationFailed("Failed to apply actions JSON", error);
        }
    }

    /**
     * Configures this instance by applying all actions as defined in the given JSON
     * file.
     * 
     * <p>
     * Before configuring, all required modules need to have been registered with
     * the framework instance and logging should have been configured, if wanted.
     * Previous configurations are extended. Create a new {@link FrameBuffet}
     * instance to restart from scratch, if needed.
     * </p>
     * 
     * @param jsonFile the JSON file to be applied
     * @throws ConfigurationFailed if any of the requested actions could not be
     *                             applied
     */
    public void configureFromActionsJson(File jsonFile) {
        String path = jsonFile.getAbsolutePath();
        FrameBuffetError error = FrameBuffetError.resolve( //
                lib.framebuffet_configure_from_actions_json_file(instance, path) //
        );

        if (error != FrameBuffetError.NONE) {
            throw new ConfigurationFailed("Failed to apply actions from " + path, error);
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

    /**
     * Retrieves the source module previously registered under the given name.
     * 
     * @param instanceName name of the instance to be retrieved
     * @return the framebuffer source instance, empty if not found
     */
    public Optional<Source> getSource(String instanceName) {
        Pointer sourceInstanceRef = lib.framebuffet_get_source_instance(instance, instanceName);

        if (sourceInstanceRef == null) {
            return Optional.empty();
        }

        return Optional.of(new Source(this, sourceInstanceRef));
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

    /**
     * Stores the given configuration under the specified name. If a configuration
     * had already been filed under the same name, it will be replaced by the new
     * configuration.
     * 
     * <p>
     * Note that this is used for general configurations (usually templates).
     * Sources use their own configurations, set up via
     * {@link Source#configure(Map)}.
     * </p>
     * 
     * @param name   name to store the configuration under
     * @param config the configuration to be stored
     */
    public void setConfiguration(String name, Map<String, String> config) {
        Pointer libConfig = mapToLib(config).orElseThrow( //
                () -> new IllegalArgumentException(
                        "Map for \"" + name + "\" could not be converted to FrameBuffet collection") //
        );

        FrameBuffetError error = FrameBuffetError.resolve( //
                lib.framebuffet_set_configuration(instance, name, libConfig) //
        );

        lib.fbcollections_map_free_map_and_payloads(libConfig);

        if (error != FrameBuffetError.NONE) {
            throw new ModuleFailed("Failed to store configuration \"" + name + "\"", error);
        }
    }

    /**
     * Retrieves the configuration stored under the given name.
     * 
     * <p>
     * Note that this is used for general configurations (usually templates).
     * Sources use their own configurations, available via
     * {@link Source#getSourceConfiguration()}. Buffer configurations are retrieved
     * via {@link Source#getBufferConfiguration(int)}.
     * </p>
     * 
     * @param name name of the stored configuration
     * @return copy of the configuration registered to this framework instance;
     *         empty if not registered
     */
    public Optional<Map<String, String>> getConfiguration(String name) {
        PointerByReference configRef = new PointerByReference();
        FrameBuffetError error = FrameBuffetError.resolve( //
                lib.framebuffet_get_configuration(instance, name, configRef) //
        );

        if (error != FrameBuffetError.NONE) {
            throw new ModuleFailed("Failed to get configuration \"" + name + "\" from framework", error);
        }

        if (configRef.getValue() == null) {
            return Optional.empty();
        }

        Map<String, String> map = mapFromLib(configRef.getValue());

        lib.fbcollections_map_free_map_and_payloads(configRef.getValue());

        return Optional.of(map);
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

    boolean waitForUpdate(Source source, int bufferId, int lastToken) {
        int result = lib.framebuffet_wait_for_update(source.instance, bufferId, lastToken);

        return convertWaitForUpdateResult(result);
    }

    boolean waitForUpdate(Source source, int bufferId, int lastToken, Duration timeout) {
        int result = lib.framebuffet_wait_for_update_micros(source.instance, bufferId, lastToken,
                timeout.toMillis() * 1000);

        return convertWaitForUpdateResult(result);
    }

    private boolean convertWaitForUpdateResult(int result) {
        FrameBuffetError error = FrameBuffetError.resolve(result);

        // @formatter:off
        switch (error) {
            case NONE:
                return true;
    
            case NO_FRAME:
                return false;
    
            default:
                throw new ModuleFailed("Failed waiting for an update from source", error);
        }
        // @formatter:on
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
     * Thrown if framework configuration fails.
     */
    public static class ConfigurationFailed extends FrameBuffetException {
        private ConfigurationFailed(String msg, FrameBuffetError error) {
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
