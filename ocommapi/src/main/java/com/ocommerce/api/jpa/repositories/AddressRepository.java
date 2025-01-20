package com.ocommerce.api.jpa.repositories;

import com.ocommerce.api.jpa.entities.Address;
import org.springframework.data.repository.ListCrudRepository;

public interface AddressRepository extends ListCrudRepository<Address, Long> {
}