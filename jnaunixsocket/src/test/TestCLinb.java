

import java.nio.ByteBuffer;

import com.sun.jna.Native;

/**
 * NOTE: DirectMapping is much faster but var args NOT SUPORTED
 * For use with test.c file
 */
public class TestCLinb  {

    static {
        Native.register("test");
    }

	public static native int funct (byte[] buffer,  int size);
	public static native int read (ByteBuffer buffer,  int size);

    public static void main(String[] args) {
    	byte[] buffer = {0,1,2,3,4,5};
    	funct(buffer, 6);
    	System.out.println(buffer);
    }
}

