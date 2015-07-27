package io.unix.sock;

/*
 * Fork of:
 * https://github.com/abligh/jnasockopt/tree/master/src/org/jnasockopt
 */
class JNASockOptionDetailsMac extends JNASockOptionDetails {

	public JNASockOptionDetailsMac() {
		super ();
		putOption (JNASockOption.IP_ADD_MEMBERSHIP, 0xc);
		putOption (JNASockOption.IP_ADD_SOURCE_MEMBERSHIP, 0x46);
		putOption (JNASockOption.IP_BLOCK_SOURCE, 0x48);
		putOption (JNASockOption.IP_BOUND_IF, 0x19);
		putOption (JNASockOption.IP_DROP_MEMBERSHIP, 0xd);
		putOption (JNASockOption.IP_DROP_SOURCE_MEMBERSHIP, 0x47);
		putOption (JNASockOption.IP_DUMMYNET_CONFIGURE, 0x3c);
		putOption (JNASockOption.IP_DUMMYNET_DEL, 0x3d);
		putOption (JNASockOption.IP_DUMMYNET_FLUSH, 0x3e);
		putOption (JNASockOption.IP_DUMMYNET_GET, 0x40);
		putOption (JNASockOption.IP_FAITH, 0x16);
		putOption (JNASockOption.IP_FW_ADD, 0x28);
		putOption (JNASockOption.IP_FW_DEL, 0x29);
		putOption (JNASockOption.IP_FW_FLUSH, 0x2a);
		putOption (JNASockOption.IP_FW_GET, 0x2c);
		putOption (JNASockOption.IP_FW_RESETLOG, 0x2d);
		putOption (JNASockOption.IP_FW_ZERO, 0x2b);
		putOption (JNASockOption.IP_HDRINCL, 0x2);
		putOption (JNASockOption.IP_IPSEC_POLICY, 0x15);
		putOption (JNASockOption.IP_MSFILTER, 0x4a);
		putOption (JNASockOption.IP_MULTICAST_IF, 0x9);
		putOption (JNASockOption.IP_MULTICAST_IFINDEX, 0x42);
		putOption (JNASockOption.IP_MULTICAST_LOOP, 0xb);
		putOption (JNASockOption.IP_MULTICAST_TTL, 0xa);
		putOption (JNASockOption.IP_MULTICAST_VIF, 0xe);
		putOption (JNASockOption.IP_NAT__XXX, 0x37);
		putOption (JNASockOption.IP_OLD_FW_ADD, 0x32);
		putOption (JNASockOption.IP_OLD_FW_DEL, 0x33);
		putOption (JNASockOption.IP_OLD_FW_FLUSH, 0x34);
		putOption (JNASockOption.IP_OLD_FW_GET, 0x36);
		putOption (JNASockOption.IP_OLD_FW_RESETLOG, 0x38);
		putOption (JNASockOption.IP_OLD_FW_ZERO, 0x35);
		putOption (JNASockOption.IP_OPTIONS, 0x1);
		putOption (JNASockOption.IP_PKTINFO, 0x1a);
		putOption (JNASockOption.IP_PORTRANGE, 0x13);
		putOption (JNASockOption.IP_RECVDSTADDR, 0x7);
		putOption (JNASockOption.IP_RECVIF, 0x14);
		putOption (JNASockOption.IP_RECVOPTS, 0x5);
		putOption (JNASockOption.IP_RECVPKTINFO, 0x1a);
		putOption (JNASockOption.IP_RECVRETOPTS, 0x6);
		putOption (JNASockOption.IP_RECVTTL, 0x18);
		putOption (JNASockOption.IP_RETOPTS, 0x8);
		putOption (JNASockOption.IP_RSVP_OFF, 0x10);
		putOption (JNASockOption.IP_RSVP_ON, 0xf);
		putOption (JNASockOption.IP_RSVP_VIF_OFF, 0x12);
		putOption (JNASockOption.IP_RSVP_VIF_ON, 0x11);
		putOption (JNASockOption.IP_STRIPHDR, 0x17);
		putOption (JNASockOption.IP_TOS, 0x3);
		putOption (JNASockOption.IP_TRAFFIC_MGT_BACKGROUND, 0x41);
		putOption (JNASockOption.IP_TTL, 0x4);
		putOption (JNASockOption.IP_UNBLOCK_SOURCE, 0x49);
		putOption (JNASockOption.LOCAL_PEERCRED, 0x1);
		putOption (JNASockOption.LOCAL_PEEREPID, 0x3);
		putOption (JNASockOption.LOCAL_PEEREUUID, 0x5);
		putOption (JNASockOption.LOCAL_PEERPID, 0x2);
		putOption (JNASockOption.LOCAL_PEERUUID, 0x4);
		putOption (JNASockOption.MCAST_BLOCK_SOURCE, 0x54);
		putOption (JNASockOption.MCAST_JOIN_GROUP, 0x50);
		putOption (JNASockOption.MCAST_JOIN_SOURCE_GROUP, 0x52);
		putOption (JNASockOption.MCAST_LEAVE_GROUP, 0x51);
		putOption (JNASockOption.MCAST_LEAVE_SOURCE_GROUP, 0x53);
		putOption (JNASockOption.MCAST_UNBLOCK_SOURCE, 0x55);
		putOption (JNASockOption.SCM_TIMESTAMP, 0x2);
		putOption (JNASockOption.SOL_SOCKET, 0xffff);
		putOption (JNASockOption.SO_ACCEPTCONN, 0x2);
		putOption (JNASockOption.SO_BROADCAST, 0x20);
		putOption (JNASockOption.SO_DEBUG, 0x1);
		putOption (JNASockOption.SO_DONTROUTE, 0x10);
		putOption (JNASockOption.SO_ERROR, 0x1007);
		putOption (JNASockOption.SO_KEEPALIVE, 0x8);
		putOption (JNASockOption.SO_LINGER, 0x80);
		putOption (JNASockOption.SO_OOBINLINE, 0x100);
		putOption (JNASockOption.SO_RCVBUF, 0x1002);
		putOption (JNASockOption.SO_RCVLOWAT, 0x1004);
		putOption (JNASockOption.SO_RCVTIMEO, 0x1006);
		putOption (JNASockOption.SO_REUSEADDR, 0x4);
		putOption (JNASockOption.SO_REUSEPORT, 0x200);
		putOption (JNASockOption.SO_SNDBUF, 0x1001);
		putOption (JNASockOption.SO_SNDLOWAT, 0x1003);
		putOption (JNASockOption.SO_SNDTIMEO, 0x1005);
		putOption (JNASockOption.SO_TIMESTAMP, 0x400);
		putOption (JNASockOption.SO_TYPE, 0x1008);
		putOption (JNASockOption.TCP_KEEPCNT, 0x102);
		putOption (JNASockOption.TCP_KEEPINTVL, 0x101);
		putOption (JNASockOption.TCP_MAXSEG, 0x2);
		putOption (JNASockOption.TCP_NODELAY, 0x1);
		putOption (JNASockOption.UTUN_OPT_EXT_IFDATA_STATS, 0x3);
		putOption (JNASockOption.UTUN_OPT_FLAGS, 0x1);
		putOption (JNASockOption.UTUN_OPT_IFNAME, 0x2);
		putOption (JNASockOption.UTUN_OPT_INC_IFDATA_STATS_IN, 0x4);
		putOption (JNASockOption.UTUN_OPT_INC_IFDATA_STATS_OUT, 0x5);
		putOption (JNASockOption.UTUN_OPT_SET_DELEGATE_INTERFACE, 0xf);
		putLevel (JNASockOptionLevel.SOL_SOCKET, 0xffff);
		putLevel (JNASockOptionLevel.SOL_LOCAL, 0x0);
	}
}
