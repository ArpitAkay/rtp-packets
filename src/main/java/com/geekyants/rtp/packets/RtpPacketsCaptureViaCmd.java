package com.geekyants.rtp.packets;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class RtpPacketsCaptureViaCmd {

    @PostConstruct
    public void capturePacketsIntoWav() throws IOException {
        try {
            ProcessBuilder processBuilder =
                    new ProcessBuilder("tshark", "-i", "enp0s1", "-f", "udp", "-T", "fields", "-e", "rtp.ssrc");
            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            String firstNonEmptyValue = null;

            while ((line = reader.readLine()) != null) {
                // Check if the line is not empty or just whitespace
                if (!line.trim().isEmpty()) {
                    firstNonEmptyValue = line;
                    break;
                }
            }

            process.destroy();

            if (firstNonEmptyValue != null) {
                System.out.println("First non-empty value: " + firstNonEmptyValue);
            } else {
                System.out.println("No non-empty value found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
