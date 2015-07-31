package io.unix.struct;

/**
 * Unix socket option levels flag
 */
public enum SockOptionLevel {

	SOL_SOCKET(0);

	private final static int[] LINUX = {0x1};
	private final static int[] MAC = {0xffff};

	private final static int[] instance;

	static {
	    String osName = System.getProperty("os.name");
	    if (osName.startsWith("Linux")) {
	        instance = LINUX;
	    }
	    else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
	        instance = MAC;
	    } else {
	    	instance = new int[1];
	    }
	}

    private int value;

    private SockOptionLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return instance[value];
    }

}
