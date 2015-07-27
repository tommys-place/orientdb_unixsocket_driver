package io.unix.sock;

// (c) 2015 Alex Bligh
// Released under the Apache licence - see LICENSE for details

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

/*
 * Fork of:
 * https://github.com/abligh/jnasockopt/tree/master/src/org/jnasockopt
 */
public class JNASockOpt {

    private static Field fdField;

    static {
    	Native.register("c");
        try {
            fdField = FileDescriptor.class.getDeclaredField("fd");
            fdField.setAccessible(true);
        } catch (Exception ex) {
            fdField = null;
        }
    }

    public static int getInputFd(Socket s) {
        try {
            FileInputStream in = (FileInputStream)s.getInputStream();
            FileDescriptor fd = in.getFD();
            return fdField.getInt(fd);
        } catch (Exception e) { }
        return -1;
    }

    public static int getOutputFd(Socket s) {
        try {
            FileOutputStream in = (FileOutputStream)s.getOutputStream();
            FileDescriptor fd = in.getFD();
            return fdField.getInt(fd);
        } catch (Exception e) { }
        return -1;
    }

    public static int getFd(Socket s) {
    	int fd = getInputFd(s);
    	if (fd != -1)
    		return fd;
    	return getOutputFd(s);
    }


    private static native int setsockopt(int fd, int level, int option_name, Pointer option_value, int option_len) throws LastErrorException;

    public static void setSockOpt (Socket socket, JNASockOptionLevel level, JNASockOption option, int option_value) throws IOException {
    	if (socket == null)
    		throw new IOException("Null socket");
    	int fd = getFd(socket);
    	if (fd == -1)
    		throw new IOException("Bad socket FD");
    	setSockOpt(fd, level, option, option_value);
    }

    public static void setSockOpt (int fd, JNASockOptionLevel level, JNASockOption option, int option_value) throws IOException {
    	IntByReference val = new IntByReference(option_value);
    	int lev = JNASockOptionDetails.getInstance().getLevel(level);
    	int opt = JNASockOptionDetails.getInstance().getOption(option);
    	try {
    	    setsockopt(fd, lev, opt, val.getPointer(), 4);
    	} catch (LastErrorException ex) {
    	    throw new IOException("setsockopt: " + strerror(ex.getErrorCode()));
    	}
    }

    public static void setSockOpt (int fd, JNASockOptionLevel level, JNASockOption option, Structure ref) throws IOException {
    	int lev = JNASockOptionDetails.getInstance().getLevel(level);
    	int opt = JNASockOptionDetails.getInstance().getOption(option);
    	try {
    	    setsockopt(fd, lev, opt, ref.getPointer(), ref.size());
    	} catch (LastErrorException ex) {
    	    throw new IOException("setsockopt: " + strerror(ex.getErrorCode()));
    	}
    }


    private static native int getsockopt(int fd, int level, int option_name, IntByReference option_value, int option_len) throws LastErrorException;
    private static native int getsockopt(int fd, int level, int option_name, Pointer option_value, int option_len) throws LastErrorException;

    public static void getSockOpt (Socket socket, JNASockOptionLevel level, JNASockOption option, IntByReference option_value) throws IOException {
    	if (socket == null)
    		throw new IOException("Null socket");
    	int fd = getFd(socket);
    	if (fd == -1)
    		throw new IOException("Bad socket FD");
    	getSockOpt(fd, level, option, option_value);
    }

    public static void getSockOpt (int fd, JNASockOptionLevel level, JNASockOption option, IntByReference option_value) throws IOException {
    	int lev = JNASockOptionDetails.getInstance().getLevel(level);
    	int opt = JNASockOptionDetails.getInstance().getOption(option);
    	try {
    	    getsockopt(fd, lev, opt, option_value, 4);
    	} catch (LastErrorException ex) {
    	    throw new IOException("setsockopt: " + strerror(ex.getErrorCode()));
    	}
    }

    public static void getSockOpt (int fd, JNASockOptionLevel level, JNASockOption option, Structure ref) throws IOException {
    	int lev = JNASockOptionDetails.getInstance().getLevel(level);
    	int opt = JNASockOptionDetails.getInstance().getOption(option);
    	try {
    	    getsockopt(fd, lev, opt, ref.getPointer(), ref.size());
    	} catch (LastErrorException ex) {
    	    throw new IOException("setsockopt: " + strerror(ex.getErrorCode()));
    	}
    }

    public static native String strerror(int errnum);

	private JNASockOpt() {
	}
}
