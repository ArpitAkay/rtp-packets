package com.geekyants.rtp.packets.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "DtmfEventRequest")
public class DtmfEventRequest {
    @Id
    private String id;
    private boolean asterisk;
    private boolean hash;

    public DtmfEventRequest() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isAsterisk() {
        return asterisk;
    }

    public void setAsterisk(boolean asterisk) {
        this.asterisk = asterisk;
    }

    public boolean isHash() {
        return hash;
    }

    public void setHash(boolean hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "DtmfEventRequest{" +
                "id='" + id + '\'' +
                ", asterisk=" + asterisk +
                ", hash=" + hash +
                '}';
    }
}
