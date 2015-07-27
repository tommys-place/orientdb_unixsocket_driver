package io.travelin.unix.test;

import io.unix.socket.UnixSocket;
import io.unix.socket.UnixSocketAddress;
import io.unix.socket.UnixSocketException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class SimpleTestClient {

	final static File socketFile = new File(new File(System.getProperty("java.io.tmpdir")), "unixsocket-test.sock");

	public static void main(String[] args) throws IOException {

		try (UnixSocket sock = UnixSocket.newInstance()) {
			try {
				sock.connect(new UnixSocketAddress(socketFile));
			} catch (UnixSocketException e) {
				System.out.println("Cannot connect to server. Have you started it?");
				System.out.flush();
				throw e;
			}
			System.out.println("Connected");

			try (InputStream is = sock.getInputStream(); OutputStream os = sock.getOutputStream();) {

				byte[] buf = new byte[128];

				int read = is.read(buf);
				System.out.println("Server says: " + new String(buf, 0, read));

				System.out.println("Replying to server...");
				os.write("Hello Server".getBytes());
				os.flush();
			}
		}

		System.out.println("End of communication.");
	}
}
