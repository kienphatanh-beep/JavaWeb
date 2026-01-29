package com.example.didong2jv.service; //

import java.util.List; //
import com.example.didong2jv.entity.Address; //
import com.example.didong2jv.payloads.AddressDTO; //

public interface AddressService { //
    AddressDTO createAddress(AddressDTO addressDTO); //
    List<AddressDTO> getAddresses(); //
    AddressDTO getAddress(Long addressId); //
    AddressDTO updateAddress(Long addressId, Address address); //
    String deleteAddress(Long addressId); //
}