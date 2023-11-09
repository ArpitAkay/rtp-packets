package com.geekyants.rtp.packets;

import com.geekyants.rtp.packets.repository.DtmfEventRequestRepository;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class AudioUtil {

    private final DtmfEventRequestRepository packetSsrcRepository;

    public AudioUtil(
            DtmfEventRequestRepository packetSsrcRepository
    ) {
        this.packetSsrcRepository = packetSsrcRepository;
    }

    public void convertPcapToRtpFile() {
        try {
            String cmd1 = "tshark -r out.pcap -Y \"rtp.payload == 00\" -T fields -e rtp.ssrc";
            Process process1 = Runtime.getRuntime().exec(cmd1);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process1.getInputStream()));
            StringBuilder output = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode1 = process1.waitFor();

            if(exitCode1 == 0) {
                System.out.println("First command executed successfully with exit code : " + exitCode1);

                String capturedOutput = output.toString();
                System.out.println("Captured Output from first command : " + capturedOutput);

                String cmd2 = "tshark -n -r out.pcap -2 -R rtp -R \"rtp.ssrc == " + capturedOutput + "\" -T fields -e rtp.payload | tr -d '\\n',':' | xxd -r -ps >call.rtp";

                Process process2 = Runtime.getRuntime().exec(new String[]{"bash", "-c", cmd2});
                int exitCode2 = process2.waitFor();

                if (exitCode2 == 0) {
                    System.out.println("Second command executed successfully : " + exitCode2);

                    String cmd3 = "sox -t ul -r 8000 -c 1 call.rtp call.wav";
                    Process process3 = Runtime.getRuntime().exec(new String[]{"bash", "-c", cmd3});
                    int exitCode3 = process3.waitFor();

                    // Check the exit code of the second command
                    if (exitCode3 == 0) {
                        System.out.println("Third command executed successfully with exit code : " + exitCode3);
                    } else {
                        System.err.println("Third command failed with exit code : " + exitCode3);
                    }
                } else {
                    System.err.println("Second command failed with exit code: " + exitCode2);
                }
            } else {
                System.err.println("First command failed with exit code: " + exitCode1);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
