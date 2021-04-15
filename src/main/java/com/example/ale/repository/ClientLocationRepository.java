package com.example.ale.repository;

import com.example.ale.model.entity.ClientLocation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientLocationRepository extends MongoRepository<ClientLocation, String> {

    ClientLocation findByMacAddress(String macAddress);
}
