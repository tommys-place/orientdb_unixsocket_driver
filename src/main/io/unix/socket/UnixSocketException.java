package io.unix.socket;

import java.net.SocketException;

/*
 * Original author
 * Copyright (c) 2009,2014 Dr. Christian Kohlschütter
 * See http://code.google.com/p/junixsocket/ for further information.
 */

/**
 * Something went wrong with the communication to a Unix socket.
 */
public class UnixSocketException extends SocketException {

	private static final long serialVersionUID = 1L;

	private final String socketFile;

	public UnixSocketException(String reason) {
		this(reason, (String) null);
	}

	public UnixSocketException(String reason, final Throwable cause) {
		this(reason, (String) null);
		initCause(cause);
	}

	public UnixSocketException(String reason, final String socketFile) {
		super(reason);
		this.socketFile = socketFile;
	}

	@Override
	public String toString() {

		if (socketFile == null) {
			return super.toString();
		} else {
			return super.toString() + " (socket: " + socketFile + ")";
		}
	}
}
