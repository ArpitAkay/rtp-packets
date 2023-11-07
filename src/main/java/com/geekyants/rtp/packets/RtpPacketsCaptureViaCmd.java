package com.geekyants.rtp.packets;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.concurrent.TimeUnit;

@Component
public class RtpPacketsCaptureViaCmd {

    @PostConstruct
    public void captureRtpPackets() throws IOException {
        System.out.println("Capturing RTP packets");
        try {
            String ssrc = getssrc();
            System.out.println("SSRC: " + ssrc);
            ProcessBuilder processBuilder =
                    new ProcessBuilder("tshark", "-i", "enp0s1", "-f", "udp", "-R", "rtp.ssrc == " + ssrc, "-T", "fields", "-e", "rtp.payload");
            Process process = processBuilder.start();

            // Create a file to store the captured data
            File outputFile = new File("call.rtp");
            FileOutputStream output = new FileOutputStream(outputFile);

            long timeout = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(20);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while (System.currentTimeMillis() < timeout && (line = reader.readLine()) != null) {
                // Remove newlines and colons and write the data to the output file
                String sanitizedLine = line.replaceAll("[\n:]", "");
                byte[] data = hexStringToByteArray(sanitizedLine);
                output.write(data);
            }

            process.destroy();
            output.close();

            if (System.currentTimeMillis() >= timeout) {
                System.out.println("Timeout reached. Stopping the capture.");
                // Execute the sox command to convert call.rtp to call.wav
                ProcessBuilder soxProcessBuilder = new ProcessBuilder("sox", "-t", "ul", "-r", "8000", "-c", "1", "call.rtp", "call.wav");
                Process soxProcess = soxProcessBuilder.start();
                int soxExitCode = soxProcess.waitFor();

                if (soxExitCode == 0) {
                    System.out.println("Conversion from call.rtp to call.wav successful.");
                } else {
                    System.err.println("Error while converting call.rtp to call.wav. Exit code: " + soxExitCode);
                }
            } else {
                System.out.println("Capture completed.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public String getssrc() throws IOException {
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
            System.out.println("SSRC: " + firstNonEmptyValue);
            return firstNonEmptyValue;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Error while capturing RTP packets");
        }
    }
}
