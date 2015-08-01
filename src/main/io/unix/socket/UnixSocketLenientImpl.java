package io.unix.socket;

import java.net.Socket;
import java.net.SocketException;
import java.net.SocketOptions;

/*
 * Original author
 * Copyright (c) 2009,2014 Dr. Christian Kohlschütter
 * See http://code.google.com/p/junixsocket/ for further information.
 */


/**
 * Changes the behavior to be somewhat lenient with respect to the
 * specification.
 *
 * In particular, we ignore calls to {@link Socket#getTcpNoDelay()} and
 * {@link Socket#setTcpNoDelay(boolean)}.
 */
public class UnixSocketLenientImpl extends UnixSocketImpl {

	UnixSocketLenientImpl() {
		super();
	}

	@Override
	public void setOption(int optID, Object value) throws SocketException {
		try {
			super.setOption(optID, value);
		} catch (SocketException e) {
			switch (optID) {
			case SocketOptions.TCP_NODELAY:
				return;
			default:
				throw e;
			}
		}
	}

	@Override
	public Object getOption(int optID) throws SocketException {
		try {
			return super.getOption(optID);
		} catch (SocketException e) {
			switch (optID) {
			case SocketOptions.TCP_NODELAY:
			case SocketOptions.SO_KEEPALIVE:
				return false;
			default:
				throw e;
			}
		}
	}

}
