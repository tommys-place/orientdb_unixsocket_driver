package io.unix.struct;

import java.io.IOException;

/**
 * Unix socket options flags
 */
public enum SockOption {

	SO_KEEPALIVE(0),
	SO_LINGER(1),
	SO_SNDTIMEO(2),
	SO_RCVTIMEO(3),
	SO_RCVBUF(4),
	SO_SNDBUF(5),
	SO_REUSEADDR(6);

	private final static int[] LINUX = {0x9, 0xD, 0x15, 0x14, 0x8, 0x7, 0x2};
	private final static int[] MAC = {0x8, 0x80, 0x1005, 0x1006, 0x1002, 0x1001, 0x4};

	private final static int[] instance;

	static {
	    String osName = System.getProperty("os.name");
	    if (osName.startsWith("Linux")) {
	        instance = LINUX;
	    }
	    else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
	        instance = MAC;
	    } else {
	    	instance = new int[7];
	    }
	}

    private int value;

    private SockOption(int value) {
        this.value = value;
    }

    public int getValue() {
        return instance[value];
    }

	public static SockOption convertSocketOptionToEnum(int optID) throws IOException {
		switch(optID) {
		case 0x0008:
			return SockOption.SO_KEEPALIVE;
		case 0x0080:
			return SockOption.SO_LINGER;
		case 0x1005:
			return SockOption.SO_SNDTIMEO;
		case 0x1006:
			return SockOption.SO_RCVTIMEO;
		case 0x1002:
			return SockOption.SO_RCVBUF;
		case 0x1001:
			return SockOption.SO_SNDBUF;
		default:
			throw new IOException("Unsupported socket option");
		}
	}

    public static void main(String args[]) {
    	System.out.println(SockOption.SO_KEEPALIVE.getValue());
    }
}
