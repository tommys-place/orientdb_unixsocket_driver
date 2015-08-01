package io.unix.socket;

import io.unix.NativeUnixSocket;

import java.io.IOException;
import java.io.OutputStream;

/*
 * Original author
 * Copyright (c) 2009,2014 Dr. Christian Kohlschütter
 * See http://code.google.com/p/junixsocket/ for further information.
 */
class UnixOutputStream extends OutputStream {

	private boolean streamClosed = false;
	private UnixSocketImpl impl;

	public UnixOutputStream(UnixSocketImpl impl) {
		super();
		this.impl = impl;
	}

	@Override
	public void write(int oneByte) throws IOException {
		final byte[] buf1 = new byte[] { (byte) oneByte };
		write(buf1, 0, 1);
	}

	@Override
	public void write(byte[] buf, int off, int len) throws IOException {
		if (streamClosed) {
			throw new UnixSocketException("This OutputStream has already been closed.");
		}
		if (len > buf.length - off) {
			throw new IndexOutOfBoundsException();
		}
		try {
			while (len > 0 && !Thread.interrupted()) {
				final int written = NativeUnixSocket.write(impl.getFD(), buf, off, len);
				if (written == -1) {
					throw new IOException("Unspecific error while writing");
				}
				len -= written;
				off += written;
			}
		} catch (final IOException e) {
			throw (IOException) new IOException(e.getMessage() + " at "	+ impl.toString()).initCause(e);
		}
	}

	@Override
	public void close() throws IOException {

		if (streamClosed) {
			return;
		}
		streamClosed = true;
		if (impl.getFD().valid()) {
			NativeUnixSocket.shutdown(impl.getFD(), UnixSocketImpl.SHUT_WR);
		}
	}
}