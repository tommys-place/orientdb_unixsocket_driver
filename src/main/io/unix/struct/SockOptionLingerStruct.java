package io.unix.struct;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * Struct for get/set socket linger options of underlying system
 */
public class SockOptionLingerStruct extends Structure {

	public static class ByReference extends SockOptionLingerStruct implements
			Structure.ByReference {
	}

	public int l_onoff;
	public int l_linger;

	@SuppressWarnings("rawtypes")
	protected List getFieldOrder() {
		return Arrays.asList("l_onoff", "l_linger");
	}

	public int getValue() {
		if (l_onoff == 0) {
			return -1;
		} else {
			return l_linger;
		}
	}

	public void setValue(int value) {
		l_linger = value >= 0 ? value : 0;
		l_onoff = value >= 0 ? 1 :0;
	}
}
