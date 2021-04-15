package com.example.ale.repository;

import com.example.ale.model.entity.GeofenceNtf;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeofenceNtfRepository extends MongoRepository<GeofenceNtf, String> {

}
