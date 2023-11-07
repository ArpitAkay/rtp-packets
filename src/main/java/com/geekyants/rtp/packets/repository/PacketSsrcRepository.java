package com.geekyants.rtp.packets.repository;

import com.geekyants.rtp.packets.entity.PacketSsrc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacketSsrcRepository extends MongoRepository<PacketSsrc, String> {
}
