package com.ocommerce.api.jpa.repositories;

import com.ocommerce.api.jpa.entities.UserReg;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRegRepository extends CrudRepository<UserReg, Long> {

    Optional<UserReg> findByUsernameIgnoreCase(String username);

    Optional<UserReg> findByEmailIgnoreCase(String email);

}
