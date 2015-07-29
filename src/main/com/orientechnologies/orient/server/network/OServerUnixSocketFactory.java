package com.orientechnologies.orient.server.network;

//(c) 2015 tommys-place
//Released under the Apache licence - see LICENSE for details

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.server.config.OServerParameterConfiguration;

import io.unix.socket.UnixServerSocket;
import io.unix.socket.UnixSocketAddress;

/**
 * Main connection factory for OrientDB server
 *
 */
public class OServerUnixSocketFactory extends OServerSocketFactory {

	public static final String PARAM_NETWORK_SOCKET_UNIX = "network.socket.unix";

	private String unixSocket = "/tmp/orientdb.sock";

	public OServerUnixSocketFactory() {

	}

	@Override
	public void config(String name, final OServerParameterConfiguration[] iParameters) {

		super.config(name, iParameters);
		for (OServerParameterConfiguration param : iParameters) {
			if (param.name.equalsIgnoreCase(PARAM_NETWORK_SOCKET_UNIX)) {
				unixSocket = param.value;
			}
		}
	}

	private ServerSocket createSocket() throws IOException {

	    UnixServerSocket socket = null;

	    try {
	      socket = UnixServerSocket.newInstance();
	      socket.bind(new UnixSocketAddress(new File(unixSocket)));
	    } catch (Exception e) {
	    	OLogManager.instance().error(this, "Socket creation error", e);
	    }

	    return socket;
	}

	@Override
	public ServerSocket createServerSocket(int port) throws IOException {
		return createSocket();
	}

	@Override
	public ServerSocket createServerSocket(int port, int backlog) throws IOException {
		return createSocket();
	}

	@Override
	public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress) throws IOException {
		return createSocket();
	}

}
