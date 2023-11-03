package com.geekyants.rtp.packets;

import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.NifSelector;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Component
public class RtpPacketsCapture {

    @PostConstruct
    public void captureRtpPackets() throws PcapNativeException, NotOpenException {
        System.out.println("Capturing RTP packets");
        PcapNetworkInterface device = getNetworkDevice();
        System.out.println("You chose: " + device);

        // New code below here
        if (device == null) {
            System.out.println("No device chosen.");
            System.exit(1);
        }

        // Open the device and get a handle
        int snapshotLength = 65536; // in bytes
        int readTimeout = 50; // in milliseconds
        final PcapHandle handle;
        handle = device.openLive(snapshotLength, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, readTimeout);

        String filter = "udp port 5060";
        handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);

        // Create a listener that defines what to do with the received packets
        PacketListener listener = new PacketListener() {
            @Override
            public void gotPacket(Packet packet) {
                // Override the default gotPacket() function and process packet
                System.out.println("********************************************");
                System.out.println("timestamp : " + handle.getTimestamp());
                System.out.println("packet : " + packet);
                System.out.println("packet header : " + packet.getHeader());
                System.out.println("packet payload : " + packet.getPayload());
                System.out.println("packet length : " + packet.length());
                System.out.println("packet raw data : " + packet.getRawData());
            }
        };

        // Tell the handle to loop using the listener we created
        try {
            int maxPackets = 50;
            handle.loop(maxPackets, listener);
        } catch (InterruptedException | PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }

        // Cleanup when complete
        handle.close();
    }

    public PcapNetworkInterface getNetworkDevice() {
        // The class that will store the network device
        // we want to use for capturing.
        PcapNetworkInterface device = null;

        // Pcap4j comes with a convenient method for listing
        // and choosing a network interface from the terminal
        try {
            // List the network devices available with a prompt
            device = new NifSelector().selectNetworkInterface();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return device;
    }
}