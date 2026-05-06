package com.booknest.order.controller;

import com.booknest.order.entity.Address;
import com.booknest.order.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
public class AddressResource {

    @Autowired
    private AddressRepository addressRepository;

    @GetMapping("/user/{userId}")
    public List<Address> getAddressesByUser(@PathVariable String userId) {
        return addressRepository.findByCustomerId(userId);
    }

    @PostMapping
    public Address saveAddress(@RequestBody Address address) {
        return addressRepository.save(address);
    }

    @DeleteMapping("/{id}")
    public void deleteAddress(@PathVariable String id) {
        try {
            String cleanId = id.contains(":") ? id.split(":")[0] : id;
            Long addressId = Long.parseLong(cleanId);
            
            // Soft delete: instead of deleting from DB (which might break existing orders),
            // we detach it from the customer so it no longer appears in their saved list.
            addressRepository.findById(addressId).ifPresent(address -> {
                address.setCustomerId(null);
                addressRepository.save(address);
            });
        } catch (Exception e) {
            throw new RuntimeException("Error processing address deletion: " + id);
        }
    }
}
