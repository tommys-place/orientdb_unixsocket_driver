package io.unix.test;

import io.unix.socket.UnixServerSocket;
import io.unix.socket.UnixSocketAddress;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SimpleTestServer {

	final static File socketFile = new File(new File(System.getProperty("java.io.tmpdir")), "unixsocket-test.sock");

	public static void main(String[] args) throws IOException {

		try (UnixServerSocket server = UnixServerSocket.newInstance()) {
			server.bind(new UnixSocketAddress(socketFile));
			System.out.println("server: " + server);

			while (!Thread.interrupted()) {
				System.out.println("Waiting for connection...");
				try (Socket sock = server.accept()) {
					System.out.println("Connected: " + sock);

					try (InputStream is = sock.getInputStream(); OutputStream os = sock.getOutputStream()) {
						System.out.println("Saying hello to client " + os);
						os.write("Hello, dear Client".getBytes());
						os.flush();

						byte[] buf = new byte[128];
						int read = is.read(buf);
						System.out.println("Client's response: " + new String(buf, 0, read));
					}
				}
			}
		}
	}
}
