Contents
--------
* Raspberry Pi 2 Model B
* IEEE 802.15.4 Radio
* bluetooth-next kernel
* wpan-tools
* Serial over USB debugging on Rpi
* Building and deploying Yocto OS image
* Booting into OS image on Rpi
* Raspberry Pi 2 B Kernel Patch
* AT86RF233 Device Tree Spec
* wpan%d configuration using wpan tools
* 6LoWPAN over 802.15.4: Creating a lowpan interface
* Discover nodes in the same PAN
* Packet capture at 802.15.4 interface
* Sniffing 802.15.4 traffic
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
The prescribed experimental setup includes the Open Labs 802.15.4 Radio. It expose a SPI
hardware interface, and is built to be pin compatible with the Raspberry Pi.

http://openlabs.co/OSHW/Raspberry-Pi-802.15.4-radio

Schematic: http://openlabs.co/OSHW/Raspberry-Pi-802.15.4-radio-files/rpi802154-r1.pdf

This radio uses the Atmel AT86RF233 transceiver IC. A driver for this radio
exists on the mainline kernel and is regularly maintained. The radio plugs directly
into pins 15 - 26 on the Raspberry Pi.

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

You can then access the serial console using 'screen'. Eg. screen /dev/ttyUSB0 115200

Building and deploying Yocto OS image
=====================================
1) mkdir ~/pi && cd pi

2) git clone https://github.com/kmaloor/raspberrypi2b

3) git clone git://git.yoctoproject.org/poky -b dizzy

4) source poky/oe-init-build-env

5) vi build/conf/bblayers.conf
```
...
BBLAYERS ?= " \

            /home/.../pi/poky/meta \
  
            /home/.../pi/poky/meta-yocto \
  
            /home/.../pi/poky/meta-yocto-bsp \
  
            /home/.../pi/raspberrypi2b/meta-raspberrypi \
  
            /home/.../pi/raspberrypi2b/meta-rpi26lo \
...
```

6) vi build/conf/local.conf
```
...

MACHINE ??= "raspberrypi2"

BB_NUMBER_THREADS = "12" (optional)

...
```
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

Raspberry Pi 2 B Kernel Patch
=============================
The bluetooth-next kernel is a "vanilla" kernel. It doesn't not contain Rpi 2B's machine configuration, BSP or drivers as they haven't been upstreamed. In order to make bluetooth-next bootable on the Rpi, a patch has been prepared by cherry picking various pieces from Rpi's custom kernel. 

You can find this patch at: https://github.com/kmaloor/raspberrypi2b/blob/master/meta-rpi26lo/recipes-kernel/linux/bluetooth-next/rpi2.patch

It includes mach-bcm2709 machine configuration, BSP, device tree, I2C, USB drivers and other patches. 
The process of preparing this patch was best-effort, but it made it possible to successfully boot and run the bluetooth-next kernel with sufficient capacity to exercise 802.15.4/6LoWPAN features. 

It includes a default kernel configuration that enables all 6LoWPAN, NHC, 802.15.4 features. This gets automatically applied during build in Yocto.

The patch currently doesn't enable Raspberry Pi's audio/video drivers, and the onboard ethernet is not functional. 

AT86RF233 Device Tree Spec
==========================
For reference, this is the device tree description for the module and its interface to the Raspberry Pi.
```
&spi0 {

        pinctrl-names = "default";
        
        pinctrl-0 = <&spi0_pins>;
        
        status = "okay";
        
        at86rf233@0 {
        
            compatible = "atmel,at86rf233";
            
            reg = <0>;
            
            interrupts = <23 1>;
            
            interrupt-parent = <&gpio>;
            
            reset-gpio = <&gpio 24 1>;
            
            sleep-tpio = <&gpio 25 1>;
            
            spi-max-frequency = <500000>;
            
            xtal-trim = /bits/ 8 <0xf>;
            
        };
        
};
```

wpan%d configuration using wpan tools
=====================================
View wpan0 settings:

root@raspberrypi2:~# iwpan dev wpan0 info
```
Interface wpan0
        ifindex 3
        wpan_dev 0x1
        extended_addr 0xef992aa141c54b92
        short_addr 0xffff
        pan_id 0xffff
        type node
        max_frame_retries -1
        min_be 3
        max_be 5
        max_csma_backoffs 4
        lbt 0
```
Set 16-bit PAN id:

root@raspberrypi2:~# iwpan dev wpan0 set pan_id 0xacdc

View current settings:

root@raspberrypi2:~# iwpan dev wpan0 info
```
Interface wpan0
        ifindex 3
        wpan_dev 0x1
        extended_addr 0xef992aa141c54b92
        short_addr 0xffff
        pan_id 0xacdc
        type node
        max_frame_retries -1
        min_be 3
        max_be 5
        max_csma_backoffs 4
        lbt 0
```
Two nodes can see each other if they're in the same PAN (PAN ids match).

6LoWPAN over 802.15.4: Creating a lowpan interface
==================================================
View all netwrk interfaces:

root@raspberrypi2:~# ip a
```
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue 

    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host 
       valid_lft forever preferred_lft forever
       
2: eth0: <NO-CARRIER,BROADCAST,MULTICAST,UP> mtu 1500 qdisc pfifo_fast qlen 1000

    link/ether b8:27:eb:9e:3c:b3 brd ff:ff:ff:ff:ff:ff
    
3: wpan0: <BROADCAST,NOARP> mtu 127 qdisc noop qlen 300

    link/[804] 71:7b:10:8c:84:e2:2e:22 brd ff:ff:ff:ff:ff:ff:ff:ff
```
Create a lowpan%d (6LoWPAN) interface:

root@raspberrypi2:~# ip link add link wpan0 name lowpan0 type lowpan

Bring up wpan0 and lowpan0:

root@raspberrypi2:~# ifconfig wpan0 up

root@raspberrypi2:~# ifconfig lowpan0 up

View all active interfaces:

root@raspberrypi2:~# ifconfig
```
...

lowpan0   Link encap:UNSPEC  HWaddr 71-7B-10-8C-84-E2-2E-22-00-00-00-00-00-00-00-00  

          inet6 addr: fe80::737b:108c:84e2:2e22/64 Scope:Link
          UP BROADCAST RUNNING MULTICAST  MTU:1280  Metric:1
          RX packets:0 errors:0 dropped:0 overruns:0 frame:0
          TX packets:0 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:0 
          RX bytes:0 (0.0 B)  TX bytes:0 (0.0 B)

wpan0     Link encap:UNSPEC  HWaddr 71-7B-10-8C-84-E2-2E-22-00-00-00-00-00-00-00-00  

          UP BROADCAST RUNNING NOARP  MTU:127  Metric:1
          RX packets:0 errors:0 dropped:0 overruns:0 frame:0
          TX packets:8 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:300 
          RX bytes:0 (0.0 B)  TX bytes:397 (397.0 B)
```          
Notice that wpan0 has an MTU size of 127 whereas lowpan0 has an MTU size of 1280. It is the interface 
for transporting IPv6 traffic. Also note that the lowpan0 interface is configured with a link-local
IPv6 address.

Discover nodes in the same PAN
==============================

If we have two nodes set up as follows:

node 1:

root@raspberrypi2:~# iwpan dev wpan0 info
```
Interface wpan0
        ifindex 3
        wpan_dev 0x1
        extended_addr 0x4a1c6e57f6fdff5c
        short_addr 0xffff
        pan_id 0xacdc
        type node
        max_frame_retries -1
        min_be 3
        max_be 5
        max_csma_backoffs 4
        lbt 0
```
node 2:

root@raspberrypi2:~# iwpan dev wpan0 info
```
Interface wpan0
        ifindex 3
        wpan_dev 0x1
        extended_addr 0xef992aa141c54b92
        short_addr 0xffff
        pan_id 0xacdc
        type node
        max_frame_retries -1
        min_be 3
        max_be 5
        max_csma_backoffs 4
        lbt 0
```
We can see that the 802.15.4 interfaces on both nodes are configured with the same PAN ID.

We can now ping the multicast address of all link-local nodes using the 6LoWPAN interface
on node 1, and see a response from the 6LoWPAN interface on node 2 (and vice-versa)

node 1:

root@raspberrypi2:~# ping6 -I lowpan0 ff02::1
```
PING ff02::1 (ff02::1): 56 data bytes
64 bytes from fe80::481c:6e57:f6fd:ff5c: seq=0 ttl=64 time=0.535 ms
64 bytes from fe80::ed99:2aa1:41c5:4b92: seq=0 ttl=64 time=15.985 ms (DUP!)
64 bytes from fe80::481c:6e57:f6fd:ff5c: seq=1 ttl=64 time=0.232 ms
64 bytes from fe80::ed99:2aa1:41c5:4b92: seq=1 ttl=64 time=15.484 ms (DUP!)
64 bytes from fe80::481c:6e57:f6fd:ff5c: seq=2 ttl=64 time=0.229 ms
64 bytes from fe80::ed99:2aa1:41c5:4b92: seq=2 ttl=64 time=15.400 ms (DUP!)
...
```

Packet capture at 802.15.4 interface
====================================
We can use tcpdump for packet capture. It is already included into the OS image.

The following command captures 100 packets on node 1.

Ping the multicast address at the lowpan0 interface on node 2 to generate some traffic.

root@raspberrypi2:~# tcpdump -c 100 -w 154capture.pcap -i wpan0
```
[ 1146.144932] device wpan0 entered promiscuous mode
tcpdump: listening on wpan0, link-type IEEE802_15_4_NOFCS (IEEE 802.15.4 without FCS), capture size 262144 bytes
100 packets captured
100 [ 1207.146391] device wpan0 left promiscuous mode
packets received by filter
0 packets dropped by kernel
```

You can mount a USB drive to copy the .pcap file. The captured packets may then be viewed using wireshark.

Sniffing 802.15.4 traffic
=========================
Follow the instructions from http://wpan.cakelab.org/ to set up a monitor interface on
node 1. 

The instructions are posted here for your convenience.

Do the following on node 1.

1) To sniff first remove the wpan interface which sits on top of the wpan phy. 

You will get a list of all current running phy interface with:
```
iwpan dev
```
then delete the wpan0 interface with:
```
iwpan dev wpan0 del
```
Finally create a monitor interface:
```
iwpan phy phy0 interface add monitor0 type monitor
```
This should now be visible as follows:

root@raspberrypi2:~# ip a
```
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue 
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host 
       valid_lft forever preferred_lft forever
2: eth0: <NO-CARRIER,BROADCAST,MULTICAST,UP> mtu 1500 qdisc pfifo_fast qlen 1000
    link/ether b8:27:eb:9e:3c:b3 brd ff:ff:ff:ff:ff:ff
5: monitor0: <BROADCAST,NOARP> mtu 127 qdisc noop qlen 300
    link/[805] 00:00:00:00:00:00:00:00 brd ff:ff:ff:ff:ff:ff:ff:ff
```
Bring up the monitor0 interface using:

root@raspberrypi2:~# ifconfig monitor0 up

We can now use tcpdump to capture packats at the monitor0 interface on node 1.

We can ping the multicast address at the lowpan0 interface on node 2 to generate some traffic.

root@raspberrypi2:~# tcpdump -c 100 -w sniff.pcap -i monitor0
```
[ 2358.724906] device monitor0 entered promiscuous mode
tcpdump: listening on monitor0, link-type LINUX_SLL (Linux cooked), capture size 262144 bytes
100 packets captured
100 [ 2471.726329] device monitor0 left promiscuous mode
packets received by filter
0 packets dropped by kernel
```
You can mount a USB drive to copy the .pcap file. The captured packets may then be viewed using wireshark.

Misc. Rpi2 Resources
====================
Raspbian (Debian Wheezy) OS image: http://downloads.raspberrypi.org/raspbian_latest
You can simply download and flash to SD card
and run on the Pi. Login: pi, password: raspberry. This has complete support for all
Raspberry Pi features.

Raspberry Pi Custom kernel repository:
https://github.com/raspberrypi/linux

Raspberry Pi Toolchain: https://github.com/raspberrypi/tools

Raspberry Pi Kernel Compilation : http://elinux.org/RPi_Upstream_Kernel_Compilation
