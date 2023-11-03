package com.geekyants.rtp.packets;

import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.util.NifSelector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class RtpPacketsApplication {

	public static void main(String[] args) {
		System.out.println("Select a device to capture packets from");
		PcapNetworkInterface device = null;
		try {
			device = new NifSelector().selectNetworkInterface();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("You chose: " + device);
		SpringApplication.run(RtpPacketsApplication.class, args);
	}

}
