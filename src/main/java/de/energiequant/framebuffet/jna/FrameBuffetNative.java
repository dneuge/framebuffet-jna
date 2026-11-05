package de.energiequant.framebuffet.jna;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Low-level JNA binding to access native Frame Buffet library functions.
 * <p>
 * <strong>Should only be used by framework. See {@link FrameBuffet} for
 * higher-level bindings.</strong>
 * </p>
 */
public interface FrameBuffetNative extends Library {
    public static interface LogReceiverNative extends Callback {
        void invoke(byte logLevel, Pointer message);
    }

    FrameBuffetNative INSTANCE = (FrameBuffetNative) Native.load("framebuffet", FrameBuffetNative.class);

    Pointer framebuffet_init();

    // NOTE: not used by wrapper in favor of "retrying" option
    byte framebuffet_destroy(Pointer framebuffet, IntByReference num_sources_destroyed);

    byte framebuffet_destroy_retrying(Pointer framebuffet, IntByReference num_sources_destroyed, int maximum_attempts,
            NativeLong millis_between_attempts, ByteByReference module_destructor_failed);

    void framebuffet_provide_log_receiver(Pointer framebuffet, LogReceiverNative log_receiver);

    byte framebuffet_set_minimum_log_level(Pointer framebuffet, byte level);

    byte framebuffet_load_module(Pointer framebuffet, String file_path);

    byte framebuffet_configure_from_actions_json(Pointer framebuffet, String json_string);

    byte framebuffet_configure_from_actions_json_file(Pointer framebuffet, String file_path);

    byte framebuffet_set_configuration(Pointer framebuffet, String config_name, Pointer config);

    byte framebuffet_get_configuration(Pointer framebuffet, String config_name, PointerByReference config);

    byte framebuffet_create_source_instance(Pointer framebuffet, PointerByReference source_instance, String module_name,
            String instance_name);

    byte framebuffet_configure_source_instance(Pointer source_instance, Pointer config);

    Pointer framebuffet_get_source_instance(Pointer framebuffet, String instance_name);

    byte framebuffet_release_source_instance(Pointer source_instance);

    // TODO: add to wrapper
    byte framebuffet_destroy_source_instance(Pointer framebuffet, String instance_name);

    // TODO: add to wrapper
    byte framebuffet_destroy_unused_source_instances(Pointer framebuffet, IntByReference num_destroyed);

    byte framebuffet_get_source_configuration(Pointer source_instance, PointerByReference config);

    int framebuffet_get_num_buffers(Pointer source_instance);

    byte framebuffet_get_buffer_configuration(Pointer source_instance, int buffer_id,
            FBSourceBufferConfigNative.ByReference buffer_config);

    byte framebuffet_copy_latest_frame(Pointer source_instance, byte[] destination, NativeLong buffer_size, int buffer_id,
            Pointer token);

    boolean framebuffet_has_update(Pointer source_instance, int buffer_id, int last_token);

    byte framebuffet_wait_for_update(Pointer source_instance, int buffer_id, int last_token);

    byte framebuffet_wait_for_update_micros(Pointer source_instance, int buffer_id, int last_token, int microseconds);

    Pointer fbcollections_map_create();

    int fbcollections_map_put(Pointer entry, PointerByReference old_entry, Pointer key, NativeLong key_size, Pointer value,
            NativeLong value_size);

    int fbcollections_map_put_and_free_old_entry_with_payloads(Pointer entry, Pointer key, NativeLong key_size, Pointer value,
            NativeLong value_size);

    void fbcollections_map_free_map_and_payloads(Pointer entry);

    // @formatter:off
    // not bound yet:
    // fberror_t framebuffet_register_module(framebuffet_t *framebuffet, fbsource_module_descriptor_t module_descriptor);
    //
    // fbcollections_map_entry_t* fbcollections_map_copy_with_payloads(fbcollections_map_entry_t *entry);
    // fbcollections_map_entry_t* fbcollections_map_find_key(fbcollections_map_entry_t *entry, void *key, unsigned long key_size);
    // int fbcollections_map_put(fbcollections_map_entry_t *entry, fbcollections_map_entry_t **old_entry, void *key, unsigned long key_size, void *value, unsigned long value_size);
    // fbcollections_map_entry_t* fbcollections_map_remove(fbcollections_map_entry_t *entry, void *key, unsigned long key_size);
    // void fbcollections_map_free_entry_and_payloads(fbcollections_map_entry_t *entry);
    // fbcollections_map_entry_t* fbcollections_map_tail(fbcollections_map_entry_t *entry);
    //
    // @formatter:on
}
