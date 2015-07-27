package io.unix;

import io.unix.sock.JNASockOpt;
import io.unix.sock.JNASockOption;
import io.unix.sock.JNASockOptionDetails;
import io.unix.sock.JNASockOptionLevel;
import io.unix.sock.struct.JNASockOptionLingerStruct;
import io.unix.sock.struct.JNASockOptionTimevalStruct;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketImpl;
import java.nio.ByteBuffer;

import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

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

  // TODO - check env supported
  public static boolean isLoaded() {
    return true;
  }

  static void checkSupported() {
  }


  static int getOption(JNASockOption option) throws IOException {
	  return JNASockOptionDetails.getInstance().getOption(option);
  }

  static int getLevel(JNASockOptionLevel level) throws IOException {
	  return JNASockOptionDetails.getInstance().getLevel(level);
  }

  public static void bind(final String socketFile, final FileDescriptor fd, final int backlog) throws IOException {

	  if (socketFile == null) {
		  return;
	  }

	  if (socketFile.length() >= 104) {
		  throw new IOException("Pathname too long for socket");
	  }

	  int serverHandle = CLibrary.socket(AF_UNIX, SOCK_STREAM, 0);
	  if(serverHandle == -1) {
		  throw new IOException(CLibrary.strerror(Native.getLastError()));
	  }

      // This block is only prophylactic, as SO_REUSEADDR seems not to work with AF_UNIX
	  JNASockOpt.setSockOpt(serverHandle, JNASockOptionLevel.SOL_SOCKET, JNASockOption.SO_REUSEADDR, 1);

	  SockAddrUn opt = new SockAddrUn();
	  opt.setSunPath(socketFile.getBytes());

	  int bindRes = CLibrary.bind(serverHandle, opt, opt.size());
	  if(bindRes == -1) {
			int myErr = Native.getLastError();
			if (myErr == EADDRINUSE) {
				// Let's check whether the address *really* is in use.
				// Maybe it's just a dead reference

				// if the given file exists, but is not a socket, ENOTSOCK is returned
				// if access is denied, EACCESS is returned
				int ret = CLibrary.connect(serverHandle, opt, opt.size());
				if(ret == -1 && Native.getLastError() == ECONNREFUSED) {

					// assume non-connected socket
					CLibrary.close(serverHandle);
					if (CLibrary.unlink(socketFile) == -1) {
						throw new IOException(CLibrary.strerror(Native.getLastError()));
					}

					serverHandle = CLibrary.socket(AF_UNIX, SOCK_STREAM, 0);
					if(serverHandle == -1) {
						throw new IOException(CLibrary.strerror(Native.getLastError()));
					}

					bindRes = CLibrary.bind(serverHandle, opt, opt.size());
					if(bindRes == -1) {
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
		if(chmodRes == -1) {
			throw new IOException(CLibrary.strerror(Native.getLastError()));
		}

		int listenRes = CLibrary.listen(serverHandle, backlog);
		if(listenRes == -1) {
			throw new IOException(CLibrary.strerror(Native.getLastError()));
		}

		ClassUtil.setfd(fd, serverHandle);
  }

  public static void connect(final String socketFile, final FileDescriptor fd) throws IOException {

	  if (socketFile == null) {
		  return;
	  }

	  if (socketFile.length() >= 104) {
		  throw new IOException("Pathname too long for socket");
	  }

	  int socketHandle = CLibrary.socket(AF_UNIX, SOCK_STREAM, 0);
	  if(socketHandle == -1) {
		  throw new IOException(CLibrary.strerror(Native.getLastError()));
	  }

	  SockAddrUn opt = new SockAddrUn();
	  opt.setSunPath(socketFile.getBytes());
	  int ret = CLibrary.connect(socketHandle, opt, opt.size());
      if(ret < 0) {
    	  CLibrary.close(socketHandle);
    	  throw new IOException(CLibrary.strerror(Native.getLastError()));
	  }
      ClassUtil.setfd(fd, socketHandle);
  }

  public static void accept(final String socketFile, final FileDescriptor fdServer, final FileDescriptor fd) throws IOException {

	  if (socketFile == null) {
		  return;
	  }

	  if (socketFile.length() >= 104) {
		  throw new IOException("Pathname too long for socket");
	  }

	  int serverHandle = ClassUtil.getfd(fdServer);

	  SockAddrUn opt = new SockAddrUn();
	  opt.setSunPath(socketFile.getBytes());

	  IntByReference ref = new IntByReference(opt.size());
	  int socketHandle = CLibrary.accept(serverHandle, opt, ref);
      if(socketHandle < 0) {
    	  throw new IOException(CLibrary.strerror(Native.getLastError()));
	  }
      ClassUtil.setfd(fd, socketHandle);
  }


  public static int read(final FileDescriptor fd, byte[] buf, int offset, int length) throws IOException {

	  if (buf == null) {
		  return -1;
	  }

	  if(offset < 0 || length < 0) {
			throw new IOException("Illegal offset or length");
	  }

	  int bufLen = buf.length;
	  int maxRead = bufLen - offset;
	  if(length > maxRead) {
		 length = maxRead;
	  }

	  int handle = ClassUtil.getfd(fd);
	  int count = 0;

	  ByteBuffer bb = ByteBuffer.wrap(buf);
	  bb.position(offset);
	  count = CLibrary.read(handle, bb, length);


	  if(count == 0) {
			// read(2) returns 0 on EOF. Java returns -1.
			return -1;
	  } else if(count == -1) {
			// read(2) returns -1 on error. Java throws an Exception.

			// Removed since non-blocking is not yet supported
			// if(errno == EAGAIN || errno == EWOULDBLOCK) {
			//		return 0;
			// }
		    throw new IOException(CLibrary.strerror(Native.getLastError()));
		}

	  return count;
  }

  public static int write(final FileDescriptor fd, byte[] buf, int offset, int length) throws IOException {

	  if (buf == null) {
		  return -1;
	  }

	  if(offset < 0 || length < 0) {
			throw new IOException("Illegal offset or length");
	  }

	  int bufLen = buf.length;
	  if(length > bufLen - offset) {
		   throw new IndexOutOfBoundsException();
	  }

	  int handle = ClassUtil.getfd(fd);
	  int count = 0;

	  ByteBuffer bb = ByteBuffer.wrap(buf);
	  bb.position(offset);
	  count = CLibrary.write(handle, bb, length);

	  if(count == -1) {
		  int errno = Native.getLastError();
		  if(errno == EAGAIN || errno == EWOULDBLOCK) {
			  return 0;
		  }
		  throw new IOException(CLibrary.strerror(Native.getLastError()));
	  }

	  return count;
  }


	static JNASockOption convertSocketOptionToNative(int optID) throws IOException
	{
		switch(optID) {
		case 0x0008:
			return JNASockOption.SO_KEEPALIVE;
		case 0x0080:
			return JNASockOption.SO_LINGER;
		case 0x1005:
			return JNASockOption.SO_SNDTIMEO;
		case 0x1006:
			return JNASockOption.SO_RCVTIMEO;
		case 0x1002:
			return JNASockOption.SO_RCVBUF;
		case 0x1001:
			return JNASockOption.SO_SNDBUF;
		default:
			throw new IOException("Unsupported socket option");
		}
	}

	public static int getSocketOptionInt(final FileDescriptor fd, int optionId) throws IOException {

	  int handle = ClassUtil.getfd(fd);
	  JNASockOption optID = convertSocketOptionToNative(optionId);

	  if(optID == JNASockOption.SO_SNDTIMEO || optID == JNASockOption.SO_RCVTIMEO) {
		  JNASockOptionTimevalStruct ref = new JNASockOptionTimevalStruct.ByReference();
		  JNASockOpt.getSockOpt(handle, JNASockOptionLevel.SOL_SOCKET, optID, ref);
		  return (int) (ref.tv_sec * 1000 + ref.tv_usec / 1000);
	  } else if(optID == JNASockOption.SO_LINGER) {
		  JNASockOptionLingerStruct ref = new JNASockOptionLingerStruct.ByReference();
		  JNASockOpt.getSockOpt(handle, JNASockOptionLevel.SOL_SOCKET, optID, ref);
		  if(ref.l_onoff == 0) {
				return -1;
			} else {
				return ref.l_linger;
			}
	  } else {
		  IntByReference ref = new IntByReference(0);
		  JNASockOpt.getSockOpt(handle, JNASockOptionLevel.SOL_SOCKET, optID, ref);
		  return ref.getValue();
	  }

  }

  public static void setSocketOptionInt(final FileDescriptor fd, int optionId, int value) throws IOException {

	  int handle = ClassUtil.getfd(fd);
	  JNASockOption optID = convertSocketOptionToNative(optionId);

	  if(optID == JNASockOption.SO_SNDTIMEO || optID == JNASockOption.SO_RCVTIMEO) {
		JNASockOptionTimevalStruct ref = new JNASockOptionTimevalStruct.ByReference();
		ref.tv_sec = value / 1000;
		ref.tv_usec =  (value % 1000) * 1000;
		JNASockOpt.setSockOpt(handle, JNASockOptionLevel.SOL_SOCKET, optID, ref);
	  } else if(optID == JNASockOption.SO_LINGER) {
		JNASockOptionLingerStruct ref = new JNASockOptionLingerStruct.ByReference();
		ref.l_linger = value >= 0 ? value : 0;
		ref.l_onoff = value >= 0 ? 1 :0;
		JNASockOpt.setSockOpt(handle, JNASockOptionLevel.SOL_SOCKET, optID, ref);
	  } else {
		  JNASockOpt.setSockOpt(handle, JNASockOptionLevel.SOL_SOCKET, optID, value);
	  }
  }

  public static void listen(final FileDescriptor fd, final int backlog) throws IOException {
	  int serverHandle = ClassUtil.getfd(fd);
	  int ret = CLibrary.listen(serverHandle, backlog);
	  if (ret == -1) {
		  throw new IOException(CLibrary.strerror(Native.getLastError()));
	  }
  }

  public static void close(final FileDescriptor fd) throws IOException {
	  int ifd = ClassUtil.getfd(fd);
	  int ret = CLibrary.close(ifd);
	  if (ret == -1) {
		  //ignore error
	  }
  }

  public static void shutdown(final FileDescriptor fd, int mode) throws IOException {
	  int ifd = ClassUtil.getfd(fd);
	  int ret = CLibrary.shutdown(ifd, mode);
	  if (ret == -1) {
		  int errno = Native.getLastError();
		  if(errno != ENOTCONN) {
			  throw new IOException(CLibrary.strerror(errno));
		  }
	  }
  }


  public static void unlink(final String socketFile) throws IOException {
	  int i = CLibrary.unlink(socketFile);
	  if (i == -1) {
		  //ignore error
	  }
  }

  public static int available(final FileDescriptor fd) throws IOException {

	    int handle = ClassUtil.getfd(fd);
	    IntByReference count = new IntByReference(-1);
	    CLibrary.ioctl(handle, FIONREAD, count);
		if(count.getValue() == -1) {
			throw new IOException(CLibrary.strerror(Native.getLastError()));
		}

		return count.getValue();
  };


  private final static Field _impl = ClassUtil.getProtectedField(ServerSocket.class, "impl");
  private final static Method _setPort = ClassUtil.getProtectedmethod(InetSocketAddress.class, "setPort");
  private final static Method _setCreated = ClassUtil.getProtectedmethod(Socket.class, "setCreated");
  private final static Method _setConnected = ClassUtil.getProtectedmethod(Socket.class, "setConnected");
  private final static Method _setServerCreated = ClassUtil.getProtectedmethod(ServerSocket.class, "setCreated");


  private static void setFieldValue(Field field, Object obj, Object value) {
	  if (field != null) {
		  try {
				field.set(obj, value);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
	  }
  }

  private static void setMethodValue(Method method, Object obj) {
	  if (method != null) {
		  try {
				method.invoke(obj);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
	  }
  }

  private static void setMethodValue(Method method, Object obj, Object value) {
	  if (method != null) {
		  try {
				method.invoke(obj, value);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
	  }
  }

  public static void initServerImpl(final ServerSocket serverSocket,  final SocketImpl impl) {
	  setFieldValue(_impl, serverSocket, impl);
  };

  public static void setConnected(final Socket socket) {
	  setMethodValue(_setConnected, socket);
  }

  public static void setCreated(final Socket socket) {
	  setMethodValue(_setCreated, socket);
  }

  public static void setCreatedServer(final ServerSocket socket) {
	  setMethodValue(_setServerCreated, socket);
  }

  static void setPort(final InetSocketAddress addr, int port) {
	  setMethodValue(_setPort, addr, port);
  };

  public static void setPort1(InetSocketAddress addr, int port) throws IOException {
    if (port < 0) {
      throw new IllegalArgumentException("port out of range:" + port);
    }

    boolean setOk = false;
    try {
      final Field holderField = ClassUtil.getProtectedField(InetSocketAddress.class, "holder");
      if (holderField != null) {

        final Object holder = holderField.get(addr);
        if (holder != null) {
          final Field portField = ClassUtil.getProtectedField(holder.getClass(), "port");
          if (portField != null) {
            portField.set(holder, port);
            setOk = true;
          }
        }
      } else {
        setPort(addr, port);
      }
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Exception e) {
      if (e instanceof IOException) {
        throw (IOException) e;
      }
      throw new IOException("Could not set port", e);
    }
    if (!setOk) {
      throw new IOException("Could not set port");
    }
  }
}
