package com.geekyants.rtp.packets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
public class AudioUtil {

    @Autowired
    private RestTemplate restTemplate;

    public void convertPcapToRtpFile() {
        String ssrc = getSsrcViaApi();
        System.out.println("ssrc : " + ssrc);
        String cmd1 = "tshark -n -r call.pcap -2 -R rtp -R \"rtp.ssrc == " + ssrc + "\" -T fields -e rtp.payload | tr -d '\\n',':' | xxd -r -ps >call.rtp";
        try {
            // Execute the command
            Process process = Runtime.getRuntime().exec(new String[] { "bash", "-c", cmd1 });
            int exitCode = process.waitFor();

            // Check the exit code to see if the command was successful
            if (exitCode == 0) {
                System.out.println("First command executed successfully.");
                String cmd2 = "sox -t ul -r 8000 -c 1 call.rtp call.wav";
                Process process2 = Runtime.getRuntime().exec(new String[] { "bash", "-c", cmd2 });
                int exitCode2 = process2.waitFor();

                // Check the exit code of the second command
                if (exitCode2 == 0) {
                    System.out.println("Second command executed successfully.");
                } else {
                    System.err.println("Second command failed with exit code: " + exitCode2);
                }
            } else {
                System.err.println("First command failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getSsrcViaApi() {
        // call the http://localhost:8080/ssrc API using rest template
        String url = "http://192.168.141.187:8081/ssrc";
        return restTemplate.getForObject(url, String.class);
    }
}
