package io.unix.sock.struct;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class JNASockOptionLingerStruct  extends Structure {

	public static class ByReference extends JNASockOptionLingerStruct implements Structure.ByReference {}

	public int l_onoff;
	public int l_linger;

	@SuppressWarnings("rawtypes")
	protected List getFieldOrder() {
		return Arrays.asList("l_onoff", "l_linger");
	}
}

