package io.travelin.unix.test;

import io.unix.socket.UnixServerSocket;
import io.unix.socket.UnixSocketAddress;

import java.io.File;
import java.io.IOException;

public class Test {

	private static UnixServerSocket createSocket() throws IOException {

	    final File socketFile = new File("/tmp/orient.sock");
	    UnixServerSocket socket = null;

	    try (UnixServerSocket server = UnixServerSocket.newInstance()) {
	        server.bind(new UnixSocketAddress(socketFile));
	        socket = server;
	    }

	    return socket;
	}

	private static void getme() {

	}

	public static void main(String[] args) throws IOException {
		UnixServerSocket socket = Test.createSocket();
	}

}
