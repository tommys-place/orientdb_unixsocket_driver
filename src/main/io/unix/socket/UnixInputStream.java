package io.unix.socket;

import io.unix.NativeUnixSocket;

import java.io.IOException;
import java.io.InputStream;

/*
 * Original author
 * Copyright (c) 2009,2014 Dr. Christian Kohlschütter
 * See http://code.google.com/p/junixsocket/ for further information.
 */
class UnixInputStream extends InputStream {

	private boolean streamClosed = false;
	private UnixSocketImpl impl;

	public UnixInputStream(UnixSocketImpl impl) {
		super();
		this.impl = impl;

	}

	@Override
	public int read(byte[] buf, int off, int len) throws IOException {
		if (streamClosed) {
			throw new IOException("This InputStream has already been closed.");
		}
		if (len == 0) {
			return 0;
		}
		int maxRead = buf.length - off;
		if (len > maxRead) {
			len = maxRead;
		}
		try {
			return NativeUnixSocket.read(impl.getFD(), buf, off, len);
		} catch (final IOException e) {
			throw (IOException) new IOException(e.getMessage() + " at "	+ impl.toString()).initCause(e);
		}
	}

	@Override
	public int read() throws IOException {
		final byte[] buf1 = new byte[1];
		final int numRead = read(buf1, 0, 1);
		if (numRead <= 0) {
			return -1;
		} else {
			return buf1[0] & 0xFF;
		}
	}

	@Override
	public void close() throws IOException {

		if (streamClosed) {
			return;
		}
		streamClosed = true;
		if (impl.getFD().valid()) {
			NativeUnixSocket.shutdown(impl.getFD(), UnixSocketImpl.SHUT_RD);
		}
	}

	@Override
	public int available() throws IOException {
		final int av = NativeUnixSocket.available(impl.getFD());
		return av;
	}
}