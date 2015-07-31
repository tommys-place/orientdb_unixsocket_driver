package io.unix.struct;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * A Unix domain socket address is represented in the following structure:
 *
 * <code>
 * #define UNIX_PATH_MAX 108
 *
 * struct sockaddr_un {
 *   sa_family_t sun_family;
 *   char sun_path[UNIX_PATH_MAX];
 * };
 * </code>
 *
 * sun_family always contains AF_UNIX.
 *
 * Look man unix(7) for more infos
 */
public class SockAddrUn extends Structure {

    /** The maximum length of sun_path. */
    private static final int UNIX_PATH_MAX = 108;
    private static final int AF_UNIX = 1;

    static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    /** The sun_family of sockaddr_un. */
    public short sunFamily_ = AF_UNIX;

    /**
     * The sun_path of sockaddr_un.
     * The length of this array is 108. The null character must be appended in this array.
     */
    public byte[] sunPath_ = new byte[UNIX_PATH_MAX];

    /**
     * Constructs a new instance.
     */
    public SockAddrUn() {}

    public void setSunPath(String sunPath) {
        if (sunPath == null) {
            throw new NullPointerException("bytes");
        }
    	setSunPath(sunPath.getBytes());
    }

    public void setSunPath(byte[] sunPath) {
        if (sunPath == null) {
            throw new NullPointerException("bytes");
        }
        if (sunPath.length >= UNIX_PATH_MAX) {
            String msg = "The length of sunPath must be less than " + UNIX_PATH_MAX + " as byte.";
            throw new IllegalArgumentException(msg);
        }
        System.arraycopy(sunPath, 0, sunPath_, 0, sunPath.length);
        sunPath_[sunPath.length] = 0;
    }

    public String getSunPath() {
        byte[] sp = sunPath_;
        int length = sp.length;
        for (int i = 0; i < sp.length; i++) {
            if (sp[i] == 0) {
                return new String(sp, 0, i, DEFAULT_CHARSET);
            }
        }
        return new String(sp, 0, length, DEFAULT_CHARSET);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected List getFieldOrder() {
        return Arrays.asList("sunFamily_", "sunPath_");
    }
}
