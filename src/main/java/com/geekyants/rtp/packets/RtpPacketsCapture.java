package com.geekyants.rtp.packets;

import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.util.NifSelector;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

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

        String filter = "udp and (port 10000 or port 20000)";
        handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);

        AtomicInteger packetCount = new AtomicInteger(0);

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

                //extract audio from rtp and store that audio in a file
                byte[] data = packet.getRawData();
                byte[] audio = Arrays.copyOfRange(data, 12, data.length);
                System.out.println("audio : " + audio);

                //store audio in a file
                try {
                    AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
                    ByteArrayInputStream bais = new ByteArrayInputStream(audio);
                    AudioInputStream audioInputStream = new AudioInputStream(bais, format, audio.length);
                    File file = new File(packetCount + " " + "out.wav");
                    AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("********************************************");

                // Dump packets to file
                try {
                    dumper.dump(packet, handle.getTimestamp());
                } catch (NotOpenException e) {
                    e.printStackTrace();
                }
                packetCount.incrementAndGet();
            }
        };

        // Tell the handle to loop using the listener we created
        try {
            int maxPackets = 500;
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
