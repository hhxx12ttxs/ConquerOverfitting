package io.undertow.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

import io.undertow.UndertowLogger;
import org.xnio.ChannelListeners;
import org.xnio.IoUtils;
import org.xnio.Option;
import org.xnio.Options;
import org.xnio.XnioExecutor;
import org.xnio.channels.StreamSinkChannel;
import org.xnio.channels.StreamSourceChannel;

/**
 * Wrapper for write timeout. This should always be the first wrapper applied to the underlying channel.
 * <p/>
 *
 * @author Stuart Douglas
 * @see org.xnio.Options#WRITE_TIMEOUT
 */
public final class WriteTimeoutStreamSinkChannel extends DelegatingStreamSinkChannel<WriteTimeoutStreamSinkChannel> {

    private int writeTimeout;
    private XnioExecutor.Key handle;

    private final Runnable timeoutCommand = new Runnable() {
        @Override
        public void run() {
            UndertowLogger.REQUEST_LOGGER.tracef("Timing out channel %s due to inactivity");
            try {
                if (delegate.isWriteResumed()) {
                    ChannelListeners.invokeChannelListener(WriteTimeoutStreamSinkChannel.this, writeSetter.get());
                }
            } finally {
                IoUtils.safeClose(delegate);
            }
        }
    };

    /**
     * @param delegate    The underlying channel
     */
    public WriteTimeoutStreamSinkChannel(final StreamSinkChannel delegate) {
        super(delegate);
        try {
            Integer timeout = delegate.getOption(Options.WRITE_TIMEOUT);
            if (timeout != null) {
                this.writeTimeout = timeout;
            } else {
                this.writeTimeout = 0;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleWriteTimeout(final long ret) {
        if (writeTimeout > 0) {
            if (ret == 0 && handle == null) {
                handle = delegate.getWriteThread().executeAfter(timeoutCommand, writeTimeout, TimeUnit.MILLISECONDS);
            } else if (ret > 0 && handle != null) {
                handle.remove();
            }
        }
    }

    @Override
    public int write(final ByteBuffer src) throws IOException {
        int ret = delegate.write(src);
        handleWriteTimeout(ret);
        return ret;
    }

    @Override
    public long write(final ByteBuffer[] srcs, final int offset, final int length) throws IOException {
        long ret = delegate.write(srcs, offset, length);
        handleWriteTimeout(ret);
        return ret;
    }

    @Override
    public long transferFrom(final FileChannel src, final long position, final long count) throws IOException {
        long ret = delegate.transferFrom(src, position, count);
        handleWriteTimeout(ret);
        return ret;
    }

    @Override
    public long transferFrom(final StreamSourceChannel source, final long count, final ByteBuffer throughBuffer) throws IOException {
        long ret = delegate.transferFrom(source, count, throughBuffer);
        handleWriteTimeout(ret);
        return ret;
    }

    @Override
    public <T> T setOption(final Option<T> option, final T value) throws IllegalArgumentException, IOException {
        T ret = super.setOption(option, value);
        if (option == Options.WRITE_TIMEOUT) {
            writeTimeout = (Integer) value;
            if (handle != null) {
                handle.remove();
                if(writeTimeout > 0) {
                    getWriteThread().executeAfter(timeoutCommand, writeTimeout, TimeUnit.MILLISECONDS);
                }
            }
        }
        return ret;
    }
}

