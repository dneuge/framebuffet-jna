package de.energiequant.framebuffet.jna;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
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

    int framebuffet_destroy(Pointer framebuffet);

    void framebuffet_provide_log_receiver(Pointer framebuffet, LogReceiverNative log_receiver);

    int framebuffet_set_minimum_log_level(Pointer framebuffet, byte level);

    int framebuffet_load_module(Pointer framebuffet, String file_path);

    int framebuffet_create_source_instance(Pointer framebuffet, PointerByReference source_instance, String module_name,
            String instance_name);

    int framebuffet_configure_source_instance(Pointer source_instance, Pointer config);

    int framebuffet_get_source_configuration(Pointer source_instance, PointerByReference config);

    int framebuffet_get_num_buffers(Pointer source_instance);

    Pointer framebuffet_get_buffer_configuration(Pointer source_instance, int buffer_id);

    int framebuffet_copy_latest_frame(Pointer source_instance, byte[] destination, long buffer_size, int buffer_id,
            Pointer token);

    int framebuffet_has_update(Pointer source_instance, int buffer_id, int last_token);

    int framebuffet_wait_for_update(Pointer source_instance, int buffer_id, int last_token);

    int framebuffet_wait_for_update_micros(Pointer source_instance, int buffer_id, int last_token, long microseconds);

    Pointer fbcollections_map_create();

    int fbcollections_map_put(Pointer entry, PointerByReference old_entry, Pointer key, long key_size, Pointer value,
            long value_size);

    int fbcollections_map_put_and_free_old_entry_with_payloads(Pointer entry, Pointer key, long key_size, Pointer value,
            long value_size);

    void fbcollections_map_free_map_and_payloads(Pointer entry);

    // @formatter:off
    // not bound yet:
    // int framebuffet_register_module(framebuffet_t *framebuffet, fbsource_module_descriptor_t module_descriptor);
    // fbsource_module_instance_entry_t* framebuffet_get_source_instance(framebuffet_t* framebuffet, char *instance_name);
    //
    // fbcollections_map_entry_t* fbcollections_map_copy_with_payloads(fbcollections_map_entry_t *entry);
    // fbcollections_map_entry_t* fbcollections_map_find_key(fbcollections_map_entry_t *entry, void *key, unsigned long key_size);
    // int fbcollections_map_put(fbcollections_map_entry_t *entry, fbcollections_map_entry_t **old_entry, void *key, unsigned long key_size, void *value, unsigned long value_size);
    // void fbcollections_map_free_entry_and_payloads(fbcollections_map_entry_t *entry);
    // @formatter:on
}
