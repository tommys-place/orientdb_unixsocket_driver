package io.unix;

import io.unix.struct.SockAddrUn;
import io.unix.struct.SockOption;
import io.unix.struct.SockOptionLevel;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

/*
 * Original author Kohl Schutter created JNI C code;
 * This is complete rewrite to Java and JNA
 * https://github.com/kohlschutter/junixsocket/tree/master/junixsocket-common
 */

/**
 * JNI connector to native JNI C code.
 */
final public class NativeUnixSocket {

	static final short AF_UNIX = 1;
	static final int SOCK_STREAM = 1;
	static final int FIONREAD = 21531;
	static final int ENOTCONN = 107;
	static final int EADDRINUSE = 98;
	static final int ECONNREFUSED = 111;
	static final int EAGAIN = 11;
	static final int EWOULDBLOCK = 11;

	/**
	 * Get unix socket option.
	 * Option can receive different data types based on flags.
	 * Here we are using Int values
	 *
	 * @param fd
	 * @param optionId
	 * @return
	 * @throws IOException
	 */
	public static int getSocketOptionInt(final FileDescriptor fd, int optionId) throws IOException {
		return NativeUnixSocketOption.getSocketOptionInt(fd, optionId);
	}

	/**
	 * Set unix socket option.
	 * Option can set different data types based on flags.
	 * Here we are using Int values
	 *
	 * @param fd
	 * @param optionId
	 * @param value
	 * @throws IOException
	 */
	public static void setSocketOptionInt(final FileDescriptor fd, int optionId, int value) throws IOException {
		NativeUnixSocketOption.setSocketOptionInt(fd, optionId, value);
	}

	/**
	 * Bind to native unix socket
	 * @param socketFile
	 * @param fd
	 * @param backlog
	 * @throws IOException
	 */
	public static void bind(final String socketFile, final FileDescriptor fd, final int backlog) throws IOException {

		if (socketFile == null) {
			return;
		}

		if (socketFile.length() >= 104) {
			throw new IOException("Pathname too long for socket");
		}

		int serverHandle = CLibrary.socket(AF_UNIX, SOCK_STREAM, 0);
		if (serverHandle == -1) {
			throw new IOException(CLibrary.strerror(Native.getLastError()));
		}

		// This block is only prophylactic, as SO_REUSEADDR seems not to work with AF_UNIX
		NativeUnixSocketOption.setSockOpt(serverHandle, SockOptionLevel.SOL_SOCKET, SockOption.SO_REUSEADDR, 1);

		SockAddrUn opt = new SockAddrUn();
		opt.setSunPath(socketFile);

		int bindRes = CLibrary.bind(serverHandle, opt, opt.size());
		if (bindRes == -1) {
			int myErr = Native.getLastError();
			if (myErr == EADDRINUSE) {
				// Let's check whether the address *really* is in use.
				// Maybe it's just a dead reference

				// if the given file exists, but is not a socket, ENOTSOCK is
				// returned
				// if access is denied, EACCESS is returned
				int ret = CLibrary.connect(serverHandle, opt, opt.size());
				if (ret == -1 && Native.getLastError() == ECONNREFUSED) {

					// assume non-connected socket
					CLibrary.close(serverHandle);
					if (CLibrary.unlink(socketFile) == -1) {
						throw new IOException(CLibrary.strerror(Native.getLastError()));
					}

					serverHandle = CLibrary.socket(AF_UNIX, SOCK_STREAM, 0);
					if (serverHandle == -1) {
						throw new IOException(CLibrary.strerror(Native.getLastError()));
					}

					bindRes = CLibrary.bind(serverHandle, opt, opt.size());
					if (bindRes == -1) {
						CLibrary.close(serverHandle);
						throw new IOException(CLibrary.strerror(Native.getLastError()));
					}

				} else {
					CLibrary.close(serverHandle);
					throw new IOException(CLibrary.strerror(Native.getLastError()));
				}

			} else {
				CLibrary.close(serverHandle);
				throw new IOException(CLibrary.strerror(Native.getLastError()));
			}
		}

		int chmodRes = CLibrary.chmod(socketFile, 0666);
		if (chmodRes == -1) {
			throw new IOException(CLibrary.strerror(Native.getLastError()));
		}

		int listenRes = CLibrary.listen(serverHandle, backlog);
		if (listenRes == -1) {
			throw new IOException(CLibrary.strerror(Native.getLastError()));
		}

		ClassUtil.setfd(fd, serverHandle);
	}

	/**
	 * Connect to native unix connection
	 *
	 * @param socketFile
	 * @param fd
	 * @throws IOException
	 */
	public static void connect(final String socketFile, final FileDescriptor fd) throws IOException {

		if (socketFile == null) {
			return;
		}

		SockAddrUn opt = new SockAddrUn();
		opt.setSunPath(socketFile);

		int socketHandle = CLibrary.socket(AF_UNIX, SOCK_STREAM, 0);
		if (socketHandle == -1) {
			throw new IOException(CLibrary.strerror(Native.getLastError()));
		}

		int ret = CLibrary.connect(socketHandle, opt, opt.size());
		if (ret < 0) {
			CLibrary.close(socketHandle);
			throw new IOException(CLibrary.strerror(Native.getLastError()));
		}
		ClassUtil.setfd(fd, socketHandle);
	}

	/**
	 * Accept native unix connection
	 *
	 * @param socketFile
	 * @param fdServer
	 * @param fd
	 * @throws IOException
	 */
	public static void accept(final String socketFile, final FileDescriptor fdServer, final FileDescriptor fd) throws IOException {

		if (socketFile == null) {
			return;
		}

		SockAddrUn opt = new SockAddrUn();
		opt.setSunPath(socketFile);

		int serverHandle = ClassUtil.getfd(fdServer);

		IntByReference ref = new IntByReference(opt.size());
		int socketHandle = CLibrary.accept(serverHandle, opt, ref);
		if (socketHandle < 0) {
			throw new IOException(CLibrary.strerror(Native.getLastError()));
		}
		ClassUtil.setfd(fd, socketHandle);
	}

	/**
	 * Read data from native unix socket
	 *
	 * @param fd
	 * @param buf
	 * @param offset
	 * @param length
	 * @return
	 * @throws IOException
	 */
	public static int read(final FileDescriptor fd, byte[] buf, int offset, int length) throws IOException {

		if (buf == null) {
			return -1;
		}

		if (offset < 0 || length < 0) {
			throw new IOException("Illegal offset or length");
		}

		int bufLen = buf.length;
		int maxRead = bufLen - offset;
		if (length > maxRead) {
			length = maxRead;
		}

		int handle = ClassUtil.getfd(fd);
		int count = 0;

		ByteBuffer bb = ByteBuffer.wrap(buf);
		bb.position(offset);
		count = CLibrary.read(handle, bb, length);

		if (count == 0) {
			// read(2) returns 0 on EOF. Java returns -1.
			return -1;
		} else if (count == -1) {
			// read(2) returns -1 on error. Java throws an Exception.

			// Removed since non-blocking is not yet supported
			// if(errno == EAGAIN || errno == EWOULDBLOCK) {
			// return 0;
			// }
			throw new IOException(CLibrary.strerror(Native.getLastError()));
		}

		return count;
	}

	/**
	 * Write data to native unix socket
	 *
	 * @param fd
	 * @param buf
	 * @param offset
	 * @param length
	 * @return
	 * @throws IOException
	 */
	public static int write(final FileDescriptor fd, byte[] buf, int offset, int length) throws IOException {

		if (buf == null) {
			return -1;
		}

		if (offset < 0 || length < 0) {
			throw new IOException("Illegal offset or length");
		}

		int bufLen = buf.length;
		if (length > bufLen - offset) {
			throw new IndexOutOfBoundsException();
		}

		int handle = ClassUtil.getfd(fd);
		int count = 0;

		ByteBuffer bb = ByteBuffer.wrap(buf);
		bb.position(offset);
		count = CLibrary.write(handle, bb, length);

		if (count == -1) {
			int errno = Native.getLastError();
			if (errno == EAGAIN || errno == EWOULDBLOCK) {
				return 0;
			}
			throw new IOException(CLibrary.strerror(Native.getLastError()));
		}

		return count;
	}

	/**
	 * Listen for unix socket connection
	 *
	 * @param fd
	 * @param backlog
	 * @throws IOException
	 */
	public static void listen(final FileDescriptor fd, final int backlog) throws IOException {
		int serverHandle = ClassUtil.getfd(fd);
		int ret = CLibrary.listen(serverHandle, backlog);
		if (ret == -1) {
			throw new IOException(CLibrary.strerror(Native.getLastError()));
		}
	}

	/**
	 * Close connection
	 *
	 * @param fd
	 * @throws IOException
	 */
	public static void close(final FileDescriptor fd) throws IOException {
		int ifd = ClassUtil.getfd(fd);
		int ret = CLibrary.close(ifd);
		if (ret == -1) {
			// ignore error
		}
	}

	/**
	 * Shutdown socket connection
	 *
	 * @param fd
	 * @param mode
	 * @throws IOException
	 */
	public static void shutdown(final FileDescriptor fd, int mode) throws IOException {
		int ifd = ClassUtil.getfd(fd);
		int ret = CLibrary.shutdown(ifd, mode);
		if (ret == -1) {
			int errno = Native.getLastError();
			if (errno != ENOTCONN) {
				throw new IOException(CLibrary.strerror(errno));
			}
		}
	}

	/**
	 * Remove old unix socket file
	 * @param socketFile
	 * @throws IOException
	 */
	public static void unlink(final String socketFile) throws IOException {
		int i = CLibrary.unlink(socketFile);
		if (i == -1) {
			// ignore error
		}
	}

	/**
	 * Check for available bytes to read
	 * @param fd
	 * @return
	 * @throws IOException
	 */
	public static int available(final FileDescriptor fd) throws IOException {
		int handle = ClassUtil.getfd(fd);
		IntByReference count = new IntByReference(-1);
		CLibrary.ioctl(handle, FIONREAD, count);
		if (count.getValue() == -1) {
			throw new IOException(CLibrary.strerror(Native.getLastError()));
		}

		return count.getValue();
	};

}
