package com.geekyants.rtp.packets;

import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.util.NifSelector;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;

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
        PcapDumper dumper = handle.dumpOpen("out.pcap");

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

                // Dump packets to file
                try {
                    dumper.dump(packet, handle.getTimestamp());
                } catch (NotOpenException e) {
                    e.printStackTrace();
                }

                if (packet.contains(UdpPacket.class)) {
                    System.out.println("UDP packet");
                    UdpPacket udpPacket = packet.get(UdpPacket.class);
                    byte[] udpPayload = udpPacket.getPayload().getRawData();

                    // Check if the payload represents an RTP packet (you may need to validate this)
                    if (udpPayload.length >= 12) {
                        System.out.println("RTP packet");
                        // Parse the RTP header
                        int payloadType = udpPayload[1] & 0x7F;  // Extract payload type
                        int sequenceNumber = ((udpPayload[2] & 0xFF) << 8) | (udpPayload[3] & 0xFF);
                        long timestamp =
                                ((udpPayload[4] & 0xFF) << 24) |
                                        ((udpPayload[5] & 0xFF) << 16) |
                                        ((udpPayload[6] & 0xFF) << 8) |
                                        (udpPayload[7] & 0xFF);

                        // Extract audio data (skip RTP header)
                        byte[] audioData = Arrays.copyOfRange(udpPayload, 12, udpPayload.length);
                        System.out.println(audioData);
                        // Depending on the payloadType, choose the appropriate decoder
//                        if (payloadType == YOUR_AUDIO_CODEC_TYPE) {
                            // Decode the audio data using the corresponding decoder
                            // Example: decode and play audio using a decoder library
                            // AudioDecoder.decodeAndPlay(audioData);
//                        }
                    }
                }
            }
        };

        // Tell the handle to loop using the listener we created
        try {
            int maxPackets = 50;
            handle.loop(maxPackets, listener);
        } catch (InterruptedException | PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }

        // Print out handle statistics
        PcapStat stats = handle.getStats();
        System.out.println("Packets received: " + stats.getNumPacketsReceived());
        System.out.println("Packets dropped: " + stats.getNumPacketsDropped());
        System.out.println("Packets dropped by interface: " + stats.getNumPacketsDroppedByIf());

        // Cleanup when complete
        dumper.close();
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
