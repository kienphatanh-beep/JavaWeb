package com.example.didong2jv.service.impl; //

import java.util.List; //
import java.util.stream.Collectors; //
import org.modelmapper.ModelMapper; //
import org.springframework.beans.factory.annotation.Autowired; //
import org.springframework.stereotype.Service; //

import com.example.didong2jv.entity.Address; //
import com.example.didong2jv.entity.User; //
import com.example.didong2jv.exceptions.APIException; //
import com.example.didong2jv.exceptions.ResourceNotFoundException; //
import com.example.didong2jv.payloads.AddressDTO; //
import com.example.didong2jv.repository.AddressRepo; //
import com.example.didong2jv.repository.UserRepo; //
import com.example.didong2jv.service.AddressService; //
import jakarta.transaction.Transactional; //

@Transactional //
@Service //
public class AddressServiceImpl implements AddressService { //

    @Autowired private AddressRepo addressRepo; //
    @Autowired private UserRepo userRepo; //
    @Autowired private ModelMapper modelMapper; //

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO) { //
        Address addressFromDB = addressRepo.findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(
                addressDTO.getCountry(), addressDTO.getState(), addressDTO.getCity(), 
                addressDTO.getPincode(), addressDTO.getStreet(), addressDTO.getBuildingName()); //

        if (addressFromDB != null) { //
            throw new APIException("Address already exists with addressId: " + addressFromDB.getAddressId()); //
        }

        Address address = modelMapper.map(addressDTO, Address.class); //
        return modelMapper.map(addressRepo.save(address), AddressDTO.class); //
    }

    @Override
    public List<AddressDTO> getAddresses() { //
        return addressRepo.findAll().stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .collect(Collectors.toList()); //
    }

    @Override
    public AddressDTO getAddress(Long addressId) { //
        Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId)); //
        return modelMapper.map(address, AddressDTO.class); //
    }

    @Override
    public AddressDTO updateAddress(Long addressId, Address address) { //
        Address addressFromDB = addressRepo.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId)); //

        addressFromDB.setCountry(address.getCountry()); //
        addressFromDB.setState(address.getState()); //
        addressFromDB.setCity(address.getCity()); //
        addressFromDB.setPincode(address.getPincode()); //
        addressFromDB.setStreet(address.getStreet()); //
        addressFromDB.setBuildingName(address.getBuildingName()); //

        return modelMapper.map(addressRepo.save(addressFromDB), AddressDTO.class); //
    }

    @Override
    public String deleteAddress(Long addressId) { //
        Address addressFromDB = addressRepo.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId)); //

        addressRepo.delete(addressFromDB); //
        return "Address deleted successfully with addressId: " + addressId; //
    }
}