package io.unix;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketImpl;

/**
 * An utility class for handling private fields & methods
 */
public class ClassUtil {

	// cache static methods, we don't need to search every time
	private final static Field _impl = ClassUtil.getProtectedField(ServerSocket.class, "impl");
	private final static Method _setPort = ClassUtil.getProtectedmethod(InetSocketAddress.class, "setPort");
	private final static Method _setCreated = ClassUtil.getProtectedmethod(Socket.class, "setCreated");
	private final static Method _setConnected = ClassUtil.getProtectedmethod(Socket.class, "setConnected");
	private final static Method _setServerCreated = ClassUtil.getProtectedmethod(ServerSocket.class, "setCreated");
	private static final Field fdField = ClassUtil.getProtectedField(FileDescriptor.class, "fd");

	public static void initServerImpl(final ServerSocket serverSocket, final SocketImpl impl) {
		ClassUtil.setFieldValue(_impl, serverSocket, impl);
	};

	public static void setConnected(final Socket socket) {
		ClassUtil.setMethodValue(_setConnected, socket);
	}

	public static void setCreated(final Socket socket) {
		ClassUtil.setMethodValue(_setCreated, socket);
	}

	public static void setCreatedServer(final ServerSocket socket) {
		ClassUtil.setMethodValue(_setServerCreated, socket);
	}


    public static void setfd(FileDescriptor descriptor, int fd) {
        try {
            fdField.setInt(descriptor, fd);
        } catch (SecurityException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }
    }


    public static int getfd(FileDescriptor descriptor) {
        if (descriptor == null || fdField == null) return -1;
        try {
            return fdField.getInt(descriptor);
        } catch (SecurityException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }

        return -1;
    }

	/**
	 *
	 * @param addr
	 * @param port
	 * @throws IOException
	 */
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
				ClassUtil.setMethodValue(_setPort, addr, port);
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

	/*
	 * Internal private methods
	 */

	static private Method getProtectedmethod(Class<?> klass, String methodName) {
		Method method = null;
        try {
            method = klass.getDeclaredMethod(methodName);
            if (method != null) {
            	method.setAccessible(true);
            }
        } catch (Exception e) {
        }
		return method;
	}

    static private Field getProtectedField(Class<?> klass, String fieldName) {
        Field field = null;
        try {
            field = klass.getDeclaredField(fieldName);
            if (field != null) {
            	field.setAccessible(true);
            }
        } catch (Exception e) {
        }
        return field;
    }

    static private void setFieldValue(Field field, Object obj, Object value) {
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

    static private void setMethodValue(Method method, Object obj) {
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

    static private  void setMethodValue(Method method, Object obj, Object value) {
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

}
