package org.hsbp.spalarm.android;

import android.content.Context;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.net.*;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.Set;

public class Device
{
    public final String nickname;
    public final InetAddress address;
    private final static byte[] DISCOVER;
    private final static int PORT = 42620;
    private final static int DISCOVERY_PORT = 42621;
    
    static {
        byte[] b;
        try {
            b = "org.hsbp.spalarm.DISCOVER".getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            b = new byte[1];
            /* UTF-8 should be always supported */
        }
        DISCOVER = b;
    }

    private Device(String nickname, InetAddress address) {
        this.nickname = nickname;
        this.address = address;
    }

    public static Set<Device> discover(final Context ctx) throws IOException {
        final InetAddress broadcast = getBroadcastAddress(ctx);
        if (broadcast == null) return null;

        final Set<Device> devices = new TreeSet<Device>(new DeviceComparator());
        final DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT);
        socket.setBroadcast(true);
        final DatagramPacket packet = new DatagramPacket(DISCOVER,
                DISCOVER.length, broadcast, DISCOVERY_PORT);
        socket.send(packet);
        socket.setSoTimeout(100);

        byte[] buf = new byte[1024];
        final DatagramPacket response = new DatagramPacket(buf, buf.length);
        for (int i = 0; i < 30; i++) {
            try {
                socket.receive(response);
            } catch (SocketTimeoutException ste) {
                continue;
            }
            devices.add(new Device(
                        new String(buf, response.getOffset(), response.getLength()),
                        response.getAddress()));
        }
        socket.close();

        return devices;
    }

    public boolean setAlarm(final int color, final int hourOfDay,
            final int minuteOfHour) throws IOException {
        final byte[] payload = new byte[5];
        payload[0] = (byte)Color.red(color);
        payload[1] = (byte)Color.green(color);
        payload[2] = (byte)Color.blue(color);
        payload[3] = (byte)hourOfDay;
        payload[4] = (byte)minuteOfHour;

        final DatagramSocket socket = new DatagramSocket(PORT);
        try {
            byte[] buf = new byte[3];
            final DatagramPacket response = new DatagramPacket(buf, buf.length);
            final DatagramPacket packet = new DatagramPacket(payload, 5, address, PORT);
            socket.setSoTimeout(500);

            for (int i = 0; i < 4; i++) {
                socket.send(packet);

                try {
                    socket.receive(response);
                } catch (SocketTimeoutException ste) {
                    continue;
                }
                if (response.getAddress().equals(address) &&
                        response.getLength() == 3 &&
                        new String(buf, 0, 3).equals("ACK")) return true;
            }
        } finally {
            socket.close();
        }
        return false;
    }

    private static InetAddress getBroadcastAddress(final Context ctx)
            throws UnknownHostException {
        final WifiManager wifi = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) return null;
        final DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp == null) return null;

        final int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        final byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (!(other instanceof Device)) return false;
        final Device dev = (Device)other;
        return dev.nickname.equals(nickname) && dev.address.equals(address);
    }

    @Override
    public int hashCode() {
        return nickname.hashCode() * 23 + address.hashCode();
    }

    public static class DeviceComparator implements Comparator<Device> {
        public int compare(final Device lhs, final Device rhs) {
            return lhs.nickname.compareToIgnoreCase(rhs.nickname);
        }
    }

    @Override
    public String toString() {
        return nickname + " @ " + address.toString();
    }
}
