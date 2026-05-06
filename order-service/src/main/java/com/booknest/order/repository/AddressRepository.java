package com.booknest.order.repository;

import com.booknest.order.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCity(String city);
    List<Address> findByCustomerId(String customerId);
    void deleteByCustomerId(String customerId);
}

