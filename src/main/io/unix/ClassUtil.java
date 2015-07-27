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

	public static Method getProtectedmethod(Class klass, String methodName, Class param) {
		Method method = null;
        try {
            method = klass.getMethod(methodName, param);
            if (method != null) {
            	method.setAccessible(true);
            }
        } catch (Exception e) {
        }
		return method;
	}

	public static Method getProtectedmethod(Class klass, String methodName) {
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

    public static Field getProtectedField(Class klass, String fieldName) {
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

    public static Object getProtectedFieldValue(Class klass, String fieldName, Object instance) {
        try {
            Field f = getProtectedField(klass, fieldName);
            return f.get(instance);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    static final Field handleField = ClassUtil.getProtectedField(FileDescriptor.class,  "handle");
    static final Field fdField = ClassUtil.getProtectedField(FileDescriptor.class, "fd");

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

    public static long gethandle(FileDescriptor descriptor) {
        if (descriptor == null || handleField == null) return -1;
        try {
            return handleField.getLong(descriptor);
        } catch (SecurityException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }

        return -1;
    }

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
