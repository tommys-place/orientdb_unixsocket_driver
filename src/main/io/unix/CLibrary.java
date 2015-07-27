package io.unix;

import java.nio.ByteBuffer;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * NOTE: DirectMapping is much faster but var args NOT SUPORTED
 */
class CLibrary  {

    static {
        Native.register(Platform.C_LIBRARY_NAME);
    }

	// not supported
	// public static native void printf(String format, Object... args);
	// public static native int ioctl(int filedes, int request, Object... args);
	// public static native int ioctl(int filedes, int request, Object... args);


	// basic commands
    public static native String strerror(int errnum);

	public static native int kill(int pid, int signal);

	public static native int chmod(String filename, int mode);
	public static native int chown(String filename, int user, int group);
	public static native int rename(String oldpath, String newpath);

	public static native int mkdir(String path, int mode);
	public static native int rmdir(String path);

	public static native int link(String oldpath, String newpath);
	public static native int unlink(String path);

	// basic I/O

	public static native int read (int filedes, byte[] buffer,  int size);
	public static native int write (int filedes, byte[] buffer, int size);
	public static native int read (int filedes, ByteBuffer buffer,  int size);
	public static native int write (int filedes, ByteBuffer buffer, int size);
	public static native int close(int s);

	//ioctl methods

	public static native int ioctl(int sockfd, int mode, IntByReference count);
	public static native int ioctl(int fd, int cmd, byte[] arg);
    public static native int ioctl(int fd, int cmd, Pointer p);

    //socket methods

    /*
	public static native int setsockopt(int s, int level, int optname, byte[] optval, int optlen);
	public static native int getsockopt(int s, int level, int optname, byte[] optval, IntByReference optlen);
	*/

	public static native int socket(int domain, int type, int protocol);
	public static native int connect(int s, SockAddrUn name, int namelen);
	public static native int bind(int s, SockAddrUn name, int namelen);
	public static native int accept(int s, SockAddrUn addr, IntByReference addrlen);
	public static native int listen(int s, int backlog);
	public static native int shutdown (int socket, int how);

}

