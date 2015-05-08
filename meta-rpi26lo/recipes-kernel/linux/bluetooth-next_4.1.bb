SUMMARY = "Linux bluetooth-next kernel." 
DESCRIPTION = "Builds the bluetooth-next tree for the Raspberry Pi 2B from its mirror on github. This recipe applies a large patch that includes Rpi 2B's CPU config (mach_bcm2709), BSP, device tree, and I2C, USB, MMC drivers plus misc. patches to other drivers."
COMPATIBLE_MACHINE = "raspberrypi2"
LINUX_VERSION ?= "4.1.0-rc1-v7+"
SRCREV = "1add15646672ff4e7fe59bec2afcb5a0c80c5e49" 
SRC_URI = "git://github.com/kmaloor/bluetooth-next-mirror;protocol=https \
           file://rpi2.patch \
          "
require linux-raspberrypi.inc
KERNEL_DEVICETREE = " \
    bcm2709-rpi-2-b.dtb \
    "
