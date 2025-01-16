package com.ocommerce.api.service;

import com.ocommerce.api.exception.UserAlreadyExistsException;
import com.ocommerce.api.jpa.entities.UserReg;
import com.ocommerce.api.jpa.repositories.UserRegRepository;
import com.ocommerce.api.model.RegistrationBody;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    /** The LocalUserDAO. */
    private UserRegRepository userRegRepo;

    /**
     * Constructor injected by spring.
     * @param userRegRepo
     */
    public UserService(UserRegRepository userRegRepo) {
        this.userRegRepo = userRegRepo;
    }

    /**
     * Attempts to register a user given the information provided.
     * @param registrationBody The registration information.
     * @return The local user that has been written to the database.
     * @throws UserAlreadyExistsException Thrown if there is already a user with the given information.
     */
    public void registerUser(RegistrationBody registrationBody) throws UserAlreadyExistsException {
        if (userRegRepo.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()
                || userRegRepo.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        UserReg user = new UserReg();
        user.setEmail(registrationBody.getEmail());
        user.setUsername(registrationBody.getUsername());
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        //TODO: Encrypt passwords!!
        user.setPassword(registrationBody.getPassword());
       userRegRepo.save(user);
    }
}
