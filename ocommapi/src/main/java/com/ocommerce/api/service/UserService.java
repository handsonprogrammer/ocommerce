package com.ocommerce.api.service;

import com.ocommerce.api.exception.UserAlreadyExistsException;
import com.ocommerce.api.jpa.entities.UserReg;
import com.ocommerce.api.jpa.repositories.UserRegRepository;
import com.ocommerce.api.model.LoginRequest;
import com.ocommerce.api.model.RegistrationBody;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    /** The UserRegRepository. */
    private UserRegRepository userRegRepo;
    private EncryptionService encryptionService;

    private JWTService jwtService;

    /**
     * Constructor injected by spring.
     * @param userRegRepo
     * @param encryptionService
     * @param jwtService
     */
    public UserService(UserRegRepository userRegRepo, EncryptionService encryptionService, JWTService jwtService) {
        this.userRegRepo = userRegRepo;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
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
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
       userRegRepo.save(user);
    }

    /**
     * Logins in a user and provides an authentication token back.
     * @param loginRequest The login request.
     * @return The authentication token. Null if the request was invalid.
     */
    public String loginUser(LoginRequest loginRequest) {
        Optional<UserReg> opUser = userRegRepo.findByUsernameIgnoreCase(loginRequest.getUsername());
        if (opUser.isPresent()) {
            UserReg user = opUser.get();
            if (encryptionService.verifyPassword(loginRequest.getPassword(), user.getPassword())) {
                return jwtService.generateJWT(user);
            }
        }
        return null;
    }

}
