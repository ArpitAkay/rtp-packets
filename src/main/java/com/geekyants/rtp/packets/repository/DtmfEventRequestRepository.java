package com.geekyants.rtp.packets.repository;

import com.geekyants.rtp.packets.entity.DtmfEventRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DtmfEventRequestRepository extends MongoRepository<DtmfEventRequest, String> {
}
