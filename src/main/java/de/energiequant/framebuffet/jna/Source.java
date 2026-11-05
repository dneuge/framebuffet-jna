package de.energiequant.framebuffet.jna;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import de.energiequant.framebuffet.jna.FrameBuffet.FrameBuffetException;
import de.energiequant.framebuffet.jna.FrameBuffet.FrameBuffetIOException;
import de.energiequant.framebuffet.jna.FrameBuffet.ModuleFailed;

/**
 * An instance of a Frame Buffet source module.
 */
public class Source implements Closeable, AutoCloseable {
    private final FrameBuffet fb;
    Pointer instance;

    Source(FrameBuffet fb, Pointer instance) {
        this.fb = fb;
        this.instance = instance;
    }

    Source(FrameBuffet fb, PointerByReference instanceRef) {
        this(fb, instanceRef.getValue());
    }

    /**
     * @return number of buffers currently available from the source instance; may
     *     be <code>0</code>
     * @throws ModuleFailed if unable to provide any indication
     */
    public int getNumBuffers() {
        failIfReleased();
        return fb.getNumBuffers(this);
    }

    /**
     * Configures the source instance with given parameters (specific to the
     * module).
     * <p>
     * Exact behaviour depends on the module's implementation but it is recommended
     * to always provide a complete list of parameters because modules are not
     * required to support progressive configuration and may fully reinitialize. In
     * particular, some modules may interpret an empty configuration map as a
     * pause/stop command.
     * </p>
     * <p>
     * Configuration may be applied asynchronously by the module and thus not take
     * immediate affect upon return.
     * </p>
     *
     * @param config parameters to be configured; may be empty
     * @throws ModuleFailed if configuration failed
     */
    public void configure(Map<String, String> config) {
        failIfReleased();
        fb.configureSource(this, config);
    }

    /**
     * Returns the current configuration of the source instance.
     * <p>
     * This is the equivalent to {@link #configure(Map)}. For actual buffer
     * parameters call {@link #getBufferConfiguration(int)}.
     * </p>
     *
     * @return current configuration of source instance; may be empty
     * @throws ModuleFailed if the instance failed to provide any configuration
     * @see #getBufferConfiguration(int)
     */
    public Map<String, String> getSourceConfiguration() {
        failIfReleased();
        return fb.getSourceConfiguration(this);
    }

    /**
     * Returns the current parameters of the specified buffer. Call
     * {@link #getNumBuffers()} to check for the total number of available buffers.
     * Buffers are addressed from <code>0</code> to <code>numBuffers-1</code>.
     * <p>
     * Note that buffers are not required to always exist, e.g. during source
     * initialization. It is possible that a source does not return any buffer
     * configuration although the buffer is within the range indicated by
     * {@link #getNumBuffers()}.
     * </p>
     *
     * @param id zero-based index of the buffer to access
     * @return configuration of the buffer; empty if unavailable
     */
    public Optional<BufferConfiguration> getBufferConfiguration(int id) {
        failIfReleased();
        return fb.getBufferConfiguration(this, id);
    }

    /**
     * Copies the latest complete frame from the specified buffer to the given byte
     * array.
     *
     * <p>
     * Note that buffers are not required to always exist, e.g. during source
     * initialization. It is possible that copying fails although the buffer is
     * within the range indicated by {@link #getNumBuffers()}.
     * </p>
     *
     * <p>
     * For performance reasons, you will probably want to check via
     * {@link #hasUpdate(int, int)} if an update to a previously copied buffer would
     * yield any difference. This method does not perform any such optimization and
     * will always copy, even if buffer contents are still the same since last call.
     * Tokens have no specified meaning and should not be interpreted or manipulated
     * further.
     * </p>
     *
     * @param destination byte array to copy into; must fit the whole image (check
     *                    {@link BufferConfiguration#getSize()}}
     * @param bufferId    zero-based index of the buffer to copy from
     * @return token of this frame, can be used for {@link #hasUpdate(int, int)}
     * @throws ModuleFailed if the requested copy could not be performed
     */
    public int copyLatestFrame(byte[] destination, int bufferId) {
        failIfReleased();
        return fb.copyLatestFrame(this, destination, bufferId);
    }

    /**
     * Checks if an updated frame is available for the given buffer.
     *
     * <p>
     * Tokens are returned from {@link #copyLatestFrame(byte[], int)} and have no
     * specified meaning.
     * </p>
     *
     * <p>
     * Note that modules are not required to support token-based update checks. Some
     * modules may always return <code>true</code>, so an additional rate limit may
     * be useful to avoid excessive calls to {@link #copyLatestFrame(byte[], int)}.
     * </p>
     *
     * @param bufferId  zero-based index of the buffer to check
     * @param lastToken token received from last
     *                  {@link #copyLatestFrame(byte[], int)} call
     * @return <code>false</code> if the buffer does not have an updated image ready
     *     for copy, otherwise <code>true</code>
     */
    public boolean hasUpdate(int bufferId, int lastToken) {
        failIfReleased();
        return fb.hasUpdate(this, bufferId, lastToken);
    }

    /**
     * Waits indefinitely until an updated frame is available for the given buffer.
     *
     * <p>
     * Tokens are returned from {@link #copyLatestFrame(byte[], int)} and have no
     * specified meaning.
     * </p>
     *
     * <p>
     * Note that modules are not required to support token-based update checks. This
     * call may block "forever" in such cases.
     * </p>
     *
     * <p>
     * Waiting can only be interrupted by the framework. You probably want to
     * specify a timeout to unblock the calling thread at some point, see
     * {@link #waitForUpdate(int, int, Duration)}.
     * </p>
     *
     * @param bufferId  zero-based index of the buffer to check
     * @param lastToken token received from last
     *                  {@link #copyLatestFrame(byte[], int)} call
     * @return <code>false</code> if the buffer does not have an updated image ready
     *     for copy, otherwise <code>true</code>
     */
    public boolean waitForUpdate(int bufferId, int lastToken) {
        failIfReleased();
        return fb.waitForUpdate(this, bufferId, lastToken);
    }

    /**
     * Waits until an updated frame is available for the given buffer or the timeout
     * has expired.
     *
     * <p>
     * Tokens are returned from {@link #copyLatestFrame(byte[], int)} and have no
     * specified meaning.
     * </p>
     *
     * <p>
     * Note that modules are not required to support token-based update checks. This
     * call will just timeout in that case.
     * </p>
     *
     * @param bufferId  zero-based index of the buffer to check
     * @param lastToken token received from last
     *                  {@link #copyLatestFrame(byte[], int)} call
     * @param timeout   maximum time to block the calling thread
     * @return <code>false</code> if the buffer does not have an updated image ready
     *     for copy, otherwise <code>true</code>
     */
    public boolean waitForUpdate(int bufferId, int lastToken, Duration timeout) {
        failIfReleased();
        return fb.waitForUpdate(this, bufferId, lastToken, timeout);
    }

    @Override
    public void close() throws IOException {
        if (instance == null) {
            return;
        }

        try {
            fb.releaseSourceInstance(this);
        } catch (FrameBuffetException ex) {
            throw FrameBuffetIOException.fromFrameBuffetException(ex);
        }

        instance = null;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void finalize() throws Throwable {
        // TODO: find a better way that works in Java 8 as well as later versions
        super.finalize();
        close();
    }

    private void failIfReleased() {
        if (instance == null) {
            throw new SourceHasBeenReleased();
        }
    }

    private static class SourceHasBeenReleased extends FrameBuffetException {
        private SourceHasBeenReleased() {
            super("This source instance has already been released on framework-side and thus cannot be accessed any longer.");
        }
    }
}
