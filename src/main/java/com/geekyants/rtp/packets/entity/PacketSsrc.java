package com.geekyants.rtp.packets.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "PacketSsrc")
public class PacketSsrc {
    @Id
    private String id;
    private String ssrcValue;

    public PacketSsrc() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSsrcValue() {
        return ssrcValue;
    }

    public void setSsrcValue(String ssrcValue) {
        this.ssrcValue = ssrcValue;
    }

    @Override
    public String toString() {
        return "PacketSsrc{" +
                "id='" + id + '\'' +
                ", ssrcValue='" + ssrcValue + '\'' +
                '}';
    }
}
