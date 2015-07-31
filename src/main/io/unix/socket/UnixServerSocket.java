package io.unix.socket;

/*
 * Original author Kohl Schutter
 * https://github.com/kohlschutter/junixsocket/tree/master/junixsocket-common
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import io.unix.ClassUtil;
import io.unix.NativeUnixSocket;

/**
 * The server part of an AF_UNIX domain socket.
 */
public class UnixServerSocket extends ServerSocket {

	private final UnixSocketImpl implementation;
	private UnixSocketAddress boundEndpoint = null;

	private final Thread shutdownThread = new Thread() {

		@Override
		public void run() {
			try {
				if (boundEndpoint != null) {
					NativeUnixSocket.unlink(boundEndpoint.getSocketFile());
				}
			} catch (IOException e) {
				// ignore
			}
		}
	};

	protected UnixServerSocket() throws IOException {
		super();
		this.implementation = new UnixSocketImpl();
		ClassUtil.initServerImpl(this, implementation);

		Runtime.getRuntime().addShutdownHook(shutdownThread);
		ClassUtil.setCreatedServer(this);
	}

	/**
	 * Returns a new, unbound AF_UNIX {@link ServerSocket}.
	 *
	 * @return The new, unbound {@link UnixServerSocket}.
	 */
	public static UnixServerSocket newInstance() throws IOException {
		UnixServerSocket instance = new UnixServerSocket();
		return instance;
	}

	/**
	 * Returns a new AF_UNIX {@link ServerSocket} that is bound to the given
	 * {@link UnixSocketAddress}.
	 *
	 * @return The new, unbound {@link UnixServerSocket}.
	 */
	public static UnixServerSocket bindOn(final UnixSocketAddress addr)	throws IOException {
		UnixServerSocket socket = newInstance();
		socket.bind(addr);
		return socket;
	}

	@Override
	public void bind(SocketAddress endpoint, int backlog) throws IOException {

		if (isClosed()) {
			throw new SocketException("Socket is closed");
		}

		if (isBound()) {
			throw new SocketException("Already bound");
		}

		if (!(endpoint instanceof UnixSocketAddress)) {
			throw new IOException("Can only bind to endpoints of type " + UnixSocketAddress.class.getName());
		}

		implementation.bind(backlog, endpoint);
		boundEndpoint = (UnixSocketAddress) endpoint;
	}

	@Override
	public boolean isBound() {
		return boundEndpoint != null;
	}

	@Override
	public Socket accept() throws IOException {

		if (isClosed()) {
			throw new SocketException("Socket is closed");
		}

		UnixSocket as = UnixSocket.newInstance();
		implementation.accept(as.impl);
		as.addr = boundEndpoint;
		ClassUtil.setConnected(as);
		return as;
	}

	@Override
	public String toString() {

		if (!isBound()) {
			return "UNIXServerSocket[unbound]";
		}
		return "UNIXServerSocket[" + boundEndpoint.getSocketFile() + "]";
	}

	@Override
	public void close() throws IOException {

		if (isClosed()) {
			return;
		}

		super.close();
		implementation.close();
		if (boundEndpoint != null) {
			NativeUnixSocket.unlink(boundEndpoint.getSocketFile());
		}
		try {
			Runtime.getRuntime().removeShutdownHook(shutdownThread);
		} catch (IllegalStateException e) {
			// ignore
		}
	}

}
