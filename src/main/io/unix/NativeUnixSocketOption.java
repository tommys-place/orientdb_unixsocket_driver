package io.unix;

import io.unix.struct.SockOption;
import io.unix.struct.SockOptionLevel;
import io.unix.struct.SockOptionLingerStruct;
import io.unix.struct.SockOptionTimevalStruct;

import java.io.FileDescriptor;
import java.io.IOException;

import com.sun.jna.LastErrorException;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

/*
 * Original author
 * Copyright (c) 2009,2014 Dr. Christian Kohlschütter
 * See http://code.google.com/p/junixsocket/ for further information.
 */


/**
 * JNI connector to native JNI C code.
 */
final public class NativeUnixSocketOption {

	public static int getSocketOptionInt(final FileDescriptor fd, int optionId) throws IOException {

		  int handle = ClassUtil.getfd(fd);
		  SockOption optID = SockOption.convertSocketOptionToEnum(optionId);

		  if(optID == SockOption.SO_SNDTIMEO || optID == SockOption.SO_RCVTIMEO) {
			  SockOptionTimevalStruct ref = new SockOptionTimevalStruct.ByReference();
			  getSockOpt(handle, SockOptionLevel.SOL_SOCKET, optID, ref);
			  return ref.getValue();
		  } else if(optID == SockOption.SO_LINGER) {
			  SockOptionLingerStruct ref = new SockOptionLingerStruct.ByReference();
			  getSockOpt(handle, SockOptionLevel.SOL_SOCKET, optID, ref);
			  return ref.getValue();
		  } else {
			  IntByReference ref = new IntByReference(0);
			  getSockOpt(handle, SockOptionLevel.SOL_SOCKET, optID, ref);
			  return ref.getValue();
		  }
	  }

	  public static void setSocketOptionInt(final FileDescriptor fd, int optionId, int value) throws IOException {

		  int handle = ClassUtil.getfd(fd);
		  SockOption optID = SockOption.convertSocketOptionToEnum(optionId);

		  if(optID == SockOption.SO_SNDTIMEO || optID == SockOption.SO_RCVTIMEO) {
			SockOptionTimevalStruct ref = new SockOptionTimevalStruct.ByReference();
			ref.setValue(value);
			setSockOpt(handle, SockOptionLevel.SOL_SOCKET, optID, ref);
		  } else if(optID == SockOption.SO_LINGER) {
			SockOptionLingerStruct ref = new SockOptionLingerStruct.ByReference();
			ref.setValue(value);
			setSockOpt(handle, SockOptionLevel.SOL_SOCKET, optID, ref);
		  } else {
			  setSockOpt(handle, SockOptionLevel.SOL_SOCKET, optID, value);
		  }
	  }

	  static void getSockOpt (int fd, SockOptionLevel lev, SockOption opt, IntByReference optVal) throws IOException {
	    	try {
	    		CLibrary.getsockopt(fd, lev.getValue(), opt.getValue(), optVal, 4);
	    	} catch (LastErrorException ex) {
	    	    throw new IOException("setsockopt: " + CLibrary.strerror(ex.getErrorCode()));
	    	}
	  }

	  static void getSockOpt (int fd, SockOptionLevel lev, SockOption opt, Structure ref) throws IOException {
	    	try {
	    		CLibrary.getsockopt(fd, lev.getValue(), opt.getValue(), ref.getPointer(), ref.size());
	    	} catch (LastErrorException ex) {
	    	    throw new IOException("setsockopt: " + CLibrary.strerror(ex.getErrorCode()));
	    	}
	  }

	  static void setSockOpt (int fd, SockOptionLevel lev, SockOption opt, int optVal) throws IOException {
	    	IntByReference val = new IntByReference(optVal);
	    	try {
	    	    CLibrary.setsockopt(fd, lev.getValue(), opt.getValue(), val.getPointer(), 4);
	    	} catch (LastErrorException ex) {
	    	    throw new IOException("setsockopt: " + CLibrary.strerror(ex.getErrorCode()));
	    	}
	  }

	  static void setSockOpt (int fd, SockOptionLevel lev, SockOption opt, Structure ref) throws IOException {
	    	try {
	    		CLibrary.setsockopt(fd, lev.getValue(), opt.getValue(), ref.getPointer(), ref.size());
	    	} catch (LastErrorException ex) {
	    	    throw new IOException("setsockopt: " + CLibrary.strerror(ex.getErrorCode()));
	    	}
	  }
}
