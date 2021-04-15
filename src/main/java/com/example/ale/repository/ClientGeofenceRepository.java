package com.example.ale.repository;

import com.example.ale.model.entity.ClientGeofence;
import com.example.ale.model.entity.ClientLocation;
import com.example.ale.model.entity.GeofenceNtf;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientGeofenceRepository extends MongoRepository<ClientGeofence, String> {

    ClientGeofence findByMacAddress(String macAddress);
}
