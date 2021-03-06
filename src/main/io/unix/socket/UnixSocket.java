package io.unix.socket;

/*
 * Original author
 * Copyright (c) 2009,2014 Dr. Christian Kohlschütter
 * See http://code.google.com/p/junixsocket/ for further information.
 */

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import io.unix.ClassUtil;

/**
 * Implementation of an AF_UNIX domain socket.
 */
public class UnixSocket extends Socket {

	protected UnixSocketImpl impl;
	UnixSocketAddress addr;

	private UnixSocket(final UnixSocketImpl impl) throws IOException {
		super(impl);

		try {
			ClassUtil.setCreated(this);
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a new, unbound {@link UnixSocket}.
	 *
	 * This "default" implementation is a bit "lenient" with respect to the
	 * specification.
	 *
	 * In particular, we ignore calls to {@link Socket#getTcpNoDelay()} and
	 * {@link Socket#setTcpNoDelay(boolean)}.
	 *
	 * @return A new, unbound socket.
	 */
	public static UnixSocket newInstance() throws IOException {
		final UnixSocketImpl impl = new UnixSocketLenientImpl();
		UnixSocket instance = new UnixSocket(impl);
		instance.impl = impl;
		return instance;
	}

	/**
	 * Creates a new, unbound, "strict" {@link UnixSocket}.
	 *
	 * This call uses an implementation that tries to be closer to the
	 * specification than {@link #newInstance()}, at least for some cases.
	 *
	 * @return A new, unbound socket.
	 */
	public static UnixSocket newStrictInstance() throws IOException {
		final UnixSocketImpl impl = new UnixSocketImpl();
		UnixSocket instance = new UnixSocket(impl);
		instance.impl = impl;
		return instance;
	}

	/**
	 * Creates a new {@link UnixSocket} and connects it to the given
	 * {@link UnixSocketAddress}.
	 *
	 * @param addr
	 *            The address to connect to.
	 * @return A new, connected socket.
	 */
	public static UnixSocket connectTo(UnixSocketAddress addr) throws IOException {
		UnixSocket socket = newInstance();
		socket.connect(addr);
		return socket;
	}

	/**
	 * Binds this {@link UnixSocket} to the given bindpoint. Only bindpoints of
	 * the type {@link UnixSocketAddress} are supported.
	 */
	@Override
	public void bind(SocketAddress bindpoint) throws IOException {
		super.bind(bindpoint);
		this.addr = (UnixSocketAddress) bindpoint;
	}

	@Override
	public void connect(SocketAddress endpoint) throws IOException {
		connect(endpoint, 0);
	}

	@Override
	public void connect(SocketAddress endpoint, int timeout) throws IOException {
		if (!(endpoint instanceof UnixSocketAddress)) {
			throw new IOException("Can only connect to endpoints of type "	+ UnixSocketAddress.class.getName());
		}
		impl.connect(endpoint, timeout);
		this.addr = (UnixSocketAddress) endpoint;
		ClassUtil.setConnected(this);
	}

	@Override
	public String toString() {
		if (isConnected()) {
			return "UNIXSocket[fd=" + impl.getFD() + ";path="	+ addr.getSocketFile() + "]";
		}
		return "UnixXSocket[unconnected]";
	}

}
