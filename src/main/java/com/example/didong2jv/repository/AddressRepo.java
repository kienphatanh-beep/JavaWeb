package com.example.didong2jv.repository;

import com.example.didong2jv.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepo extends JpaRepository<Address, Long> {
    Address findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(
            String country, String state, String city, String pincode, String street, String buildingName);
}