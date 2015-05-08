Contents
--------
* Raspberry Pi 2 Model B
* IEEE 802.15.4 Radio
* bluetooth-next kernel
* wpan-tools
* Serial over USB debugging on Rpi
* Building and deploying Yocto OS image
* Booting into OS image on Rpi
* Misc. Rpi2 Resources

This repository contains 2 Yocto layers with recipes to construct a Linux image for the Raspberry Pi 2 B for developing, testing and exercising the newest features of the 6LoWPAN / IEEE 802.15.4 implmenetation on Linux.

meta-raspberrypi: Raspberry Pi BSP obtained from yoctoproject.org.
meta-rpi26lo: Recipes for building the bluetooth-next kernel, tcpdump (obtained from OpenEmbedded), wpan-toools.

Raspberry Pi 2 Model B
======================
Rpi 2B Spec: https://www.raspberrypi.org/products/raspberry-pi-2-model-b/

Pinout: http://pi.gadgetoid.com/pinout

IEEE 802.15.4 Radio
===================
The prescribed experimental setup includes the 
Open Labs 802.15.4 Radio http://openlabs.co/OSHW/Raspberry-Pi-802.15.4-radio
This radio uses the Atmel AT86RF233 transceiver IC. A driver for this radio
exists on the mainline kernel and is regularly maintained. 

bluetooth-next kernel
=====================
http://git.kernel.org/cgit/linux/kernel/git/bluetooth/bluetooth-next.git
Mirror: https://github.com/kmaloor/bluetooth-next-mirror

Kernel tree updated by bluetooth and 6Lo sub-system maintainers with the newest features  that don't yet exist on the mainline.

wpan-tools
==========
http://wpan.cakelab.org/
Userspace wpan interface configuration tool (based on iw). It uses the new nl802154 netlink interface. 

Serial over USB debugging on Rpi
================================
You can access the serial console using a USB to TTL serial cable.
https://learn.adafruit.com/adafruits-raspberry-pi-lesson-5-using-a-console-cable/overview
You can then access the serial console using
'screen'. Eg. screen /dev/ttyUSB0 115200

Building and deploying Yocto OS image
=====================================
1) mkdir ~/pi && cd pi
2) git clone https://github.com/kmaloor/raspberrypi2b
3) git clone git://git.yoctoproject.org/poky -b dizzy
4) source poky/oe-init-build-env
5) vi build/conf/bblayers.conf

...
BBLAYERS ?= " \
  /home/.../pi/poky/meta \
  /home/.../pi/poky/meta-yocto \
  /home/.../pi/poky/meta-yocto-bsp \
  /home/.../pi/raspberrypi2b/meta-raspberrypi \
  /home/.../pi/raspberrypi2b/meta-rpi26lo \

6) vi build/conf/local.conf
...
MACHINE ??= "raspberrypi2"
BB_NUMBER_THREADS = "12" (optional)
...

7) bitbake rpi-hwup-image

8) cd build/tmp/deploy/images/raspberrypi2

9) sudo dd if=rpi-hwup-image-raspberrypi2.rpi-sdimg of=/dev/sdb bs=1048576
(SDCard as /dev/sdb)

Booting into OS image on Rpi
============================
Insert SD card into slot and plug in the serial console cable. 

sudo screen /dev/ttyUSB0 115200
...
...
done.
Starting syslogd/klogd: done

Poky (Yocto Project Reference Distro) 1.7.2 raspberrypi2 /dev/ttyAMA0

raspberrypi2 login: root
root@raspberrypi2:~# 

Misc. Rpi2 Resources
====================
Raspbian (Debian Wheezy) OS image: http://downloads.raspberrypi.org/raspbian_latest
You can simply download and flash to SD card
and run on the Pi. Login: pi, password: raspberry. This has complete support for all
Raspberry Pi features.

Raspberry Pi Custom kernel repository:
https://github.com/raspberrypi/linux

Raspberry Pi Toolchain: https://github.com/raspberrypi/tools
