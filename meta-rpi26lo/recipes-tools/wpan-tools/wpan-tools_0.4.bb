SUMMARY = "linux-wpan tools to configure the IEEE 802.15.4 WPAN interface."
DESCRIPTION = "Provides the iwpan tool (based on iw) to set up the wpan%d interface. It does this by accessinfg the nl802154 netlink interface."
DEPENDS = "libnl"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://COPYING;md5=4c90a0ebb6b0b86b0ab38254fc853b57"
SRC_URI = "http://wpan.cakelab.org/releases/wpan-tools-0.4.tar.bz2 \
          "
SRC_URI[md5sum] = "37575523d1543e06b11295f03a891fa7"
SRC_URI[sha256sum] = "772ad396139da8bc24103c7db44751f19ff8495fb48a75ccafb85877b133ccd0"

inherit autotools pkgconfig

RDEPENDS_${PN} = "libnl libnl-genl"
