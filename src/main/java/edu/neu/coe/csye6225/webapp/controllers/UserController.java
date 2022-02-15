package edu.neu.coe.csye6225.webapp.controllers;

import edu.neu.coe.csye6225.webapp.repositories.UserRepository;
import edu.neu.coe.csye6225.webapp.models.UserModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
public class UserController {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/v1/user")
    public ResponseEntity createUser(@RequestBody UserModel user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        try {
            return new ResponseEntity<UserModel>(repository.save(user), HttpStatus.CREATED);
        } catch (DataIntegrityViolationException e) {
            System.out.println(e);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/v1/user/self")
    public UserModel getSelf(@RequestHeader("Authorization") String token) {
        String base64Credentials = token.substring("Basic".length()).trim();
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        String username = credentials.split(":")[0];
        return repository.findByUsername(username);
    }

    @PutMapping("/v1/user/self")
    public ResponseEntity updateSelf(@RequestHeader("Authorization") String token, @RequestBody UserModel user) {
        try {
            String base64Credentials = token.substring("Basic".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            String username = credentials.split(":")[0];
            UserModel updatedUser = repository.findByUsername(username);

            updatedUser.updateUser(user.getFirstName(), user.getLastName(), passwordEncoder.encode(user.getPassword()));
            repository.save(updatedUser);
            return new ResponseEntity(HttpStatus.NO_CONTENT) ;
        } catch(Exception e) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST) ;
        }

    }
}
