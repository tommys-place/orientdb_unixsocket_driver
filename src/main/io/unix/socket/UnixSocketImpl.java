package io.unix.socket;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketOptions;

import io.unix.NativeUnixSocket;

/*
 * Original author Kohl Schutter
 * https://github.com/kohlschutter/junixsocket/tree/master/junixsocket-common
 */

/**
 * The Java-part of the {@link UnixSocket} implementation.
 */
class UnixSocketImpl extends SocketImpl {

	static final int SHUT_RD = 0;
	static final int SHUT_WR = 1;
	static final int SHUT_RD_WR = 2;

	private String socketFile;
	private boolean closed = false;
	private boolean bound = false;
	private boolean connected = false;

	private final UnixInputStream in;
	private final UnixOutputStream out;

	UnixSocketImpl() {
		super();
		this.fd = new FileDescriptor();
		in = new UnixInputStream(this);
		out = new UnixOutputStream(this);
	}

	FileDescriptor getFD() {
		return fd;
	}

	@Override
	protected void accept(SocketImpl socket) throws IOException {
		final UnixSocketImpl si = (UnixSocketImpl) socket;
		NativeUnixSocket.accept(socketFile, fd, si.fd);
		si.socketFile = socketFile;
		si.connected = true;
	}

	@Override
	protected int available() throws IOException {
		return NativeUnixSocket.available(fd);
	}

	protected void bind(SocketAddress addr) throws IOException {
		bind(0, addr);
	}

	protected void bind(int backlog, SocketAddress addr) throws IOException {
		if (!(addr instanceof UnixSocketAddress)) {
			throw new SocketException("Cannot bind to this type of address: " + addr.getClass());
		}
		final UnixSocketAddress socketAddress = (UnixSocketAddress) addr;
		socketFile = socketAddress.getSocketFile();
		NativeUnixSocket.bind(socketFile, fd, backlog);
		bound = true;
		this.localport = socketAddress.getPort();
	}

	@Override
	protected void bind(InetAddress host, int port) throws IOException {
		throw new SocketException("Cannot bind to this type of address: " + InetAddress.class);
	}

	@Override
	protected synchronized void close() throws IOException {

		if (closed) {
			return;
		}
		closed = true;
		if (fd.valid()) {
			NativeUnixSocket.shutdown(fd, SHUT_RD_WR);
			NativeUnixSocket.close(fd);
		}
		if (bound) {
			NativeUnixSocket.unlink(socketFile);
		}
		connected = false;
	}

	@Override
	protected void connect(String host, int port) throws IOException {
		throw new SocketException("Cannot bind to this type of address: " + InetAddress.class);
	}

	@Override
	protected void connect(InetAddress address, int port) throws IOException {
		throw new SocketException("Cannot bind to this type of address: " + InetAddress.class);
	}

	@Override
	protected void connect(SocketAddress addr, int timeout) throws IOException {

		if (!(addr instanceof UnixSocketAddress)) {
			throw new SocketException("Cannot bind to this type of address: " + addr.getClass());
		}

		final UnixSocketAddress socketAddress = (UnixSocketAddress) addr;
		socketFile = socketAddress.getSocketFile();
		NativeUnixSocket.connect(socketFile, fd);
		this.address = socketAddress.getAddress();
		this.port = socketAddress.getPort();
		this.localport = 0;
		this.connected = true;
	}

	@Override
	protected void create(boolean stream) throws IOException {

	}

	@Override
	protected InputStream getInputStream() throws IOException {
		if (!connected && !bound) {
			throw new IOException("Not connected/not bound");
		}
		return in;
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		if (!connected && !bound) {
			throw new IOException("Not connected/not bound");
		}
		return out;
	}

	@Override
	protected void listen(int backlog) throws IOException {
		NativeUnixSocket.listen(fd, backlog);
	}

	@Override
	protected void sendUrgentData(int data) throws IOException {
		NativeUnixSocket.write(fd, new byte[] { (byte) (data & 0xFF) }, 0, 1);
	}


	@Override
	public String toString() {
		return super.toString() + "[fd=" + fd + "; file=" + this.socketFile	+ "; connected=" + connected + "; bound=" + bound + "]";
	}

	private static int expectInteger(Object value) throws SocketException {
		try {
			return (Integer) value;
		} catch (final ClassCastException e) {
			throw new UnixSocketException("Unsupported value: " + value, e);
		} catch (final NullPointerException e) {
			throw new UnixSocketException("Value must not be null", e);
		}
	}

	private static int expectBoolean(Object value) throws SocketException {
		try {
			return ((Boolean) value).booleanValue() ? 1 : 0;
		} catch (final ClassCastException e) {
			throw new UnixSocketException("Unsupported value: " + value, e);
		} catch (final NullPointerException e) {
			throw new UnixSocketException("Value must not be null", e);
		}
	}

	@Override
	public Object getOption(int optID) throws SocketException {
		try {
			switch (optID) {
			case SocketOptions.SO_KEEPALIVE:
			case SocketOptions.TCP_NODELAY:
				return NativeUnixSocket.getSocketOptionInt(fd, optID) != 0 ? true : false;
			case SocketOptions.SO_LINGER:
			case SocketOptions.SO_TIMEOUT:
			case SocketOptions.SO_RCVBUF:
			case SocketOptions.SO_SNDBUF:
				return NativeUnixSocket.getSocketOptionInt(fd, optID);
			default:
				throw new UnixSocketException("Unsupported option: " + optID);
			}
		} catch (final UnixSocketException e) {
			throw e;
		} catch (final Exception e) {
			throw new UnixSocketException("Error while getting option", e);
		}
	}

	@Override
	public void setOption(int optID, Object value) throws SocketException {
		try {
			switch (optID) {
			case SocketOptions.SO_LINGER:

				if (value instanceof Boolean) {
					final boolean b = (Boolean) value;
					if (b) {
						throw new SocketException("Only accepting Boolean.FALSE here");
					}
					NativeUnixSocket.setSocketOptionInt(fd, optID, -1);
					return;
				}
				NativeUnixSocket.setSocketOptionInt(fd, optID,	expectInteger(value));
				return;
			case SocketOptions.SO_RCVBUF:
			case SocketOptions.SO_SNDBUF:
			case SocketOptions.SO_TIMEOUT:
				NativeUnixSocket.setSocketOptionInt(fd, optID,	expectInteger(value));
				return;
			case SocketOptions.SO_KEEPALIVE:
			case SocketOptions.TCP_NODELAY:
				NativeUnixSocket.setSocketOptionInt(fd, optID, expectBoolean(value));
				return;
			default:
				throw new UnixSocketException("Unsupported option: " + optID);
			}
		} catch (final UnixSocketException e) {
			throw e;
		} catch (final Exception e) {
			throw new UnixSocketException("Error while setting option", e);
		}
	}

	@Override
	protected void shutdownInput() throws IOException {
		if (!closed && fd.valid()) {
			NativeUnixSocket.shutdown(fd, SHUT_RD);
			// TODO in.close();
		}
	}

	@Override
	protected void shutdownOutput() throws IOException {
		if (!closed && fd.valid()) {
			NativeUnixSocket.shutdown(fd, SHUT_WR);
			// TODO out.close();
		}
	}

}
