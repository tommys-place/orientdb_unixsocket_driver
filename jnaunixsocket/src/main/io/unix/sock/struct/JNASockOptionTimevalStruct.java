package io.unix.sock.struct;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class JNASockOptionTimevalStruct  extends Structure {

	public static class ByReference extends JNASockOptionTimevalStruct implements Structure.ByReference {}

	public long tv_sec;
	public long tv_usec;

	@SuppressWarnings("rawtypes")
	protected List getFieldOrder() {
		return Arrays.asList("tv_sec", "tv_usec");
	}
}

