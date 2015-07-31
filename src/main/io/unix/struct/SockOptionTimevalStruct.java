package io.unix.struct;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * Struct for get/set socket options of underlying system
 */
public class SockOptionTimevalStruct  extends Structure {

	public static class ByReference extends SockOptionTimevalStruct implements Structure.ByReference {}

	public long tv_sec;
	public long tv_usec;

	@SuppressWarnings("rawtypes")
	protected List getFieldOrder() {
		return Arrays.asList("tv_sec", "tv_usec");
	}

	public int getValue() {
		return (int) (tv_sec * 1000 + tv_usec / 1000);
	}

	public void setValue(int value) {
		tv_sec = value / 1000;
		tv_usec =  (value % 1000) * 1000;
	}
}

