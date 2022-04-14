package edu.neu.coe.csye6225.webapp.controllers;

import com.timgroup.statsd.StatsDClient;
import edu.neu.coe.csye6225.webapp.models.User;
import edu.neu.coe.csye6225.webapp.repositories.UserRepository;
import edu.neu.coe.csye6225.webapp.utils.CmdRunner;
import edu.neu.coe.csye6225.webapp.utils.TokenGenerator;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@RestController
public class UserController {
    @Autowired
    private StatsDClient statsDClient;

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserController(@Qualifier("user") UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/v1/user")
    public ResponseEntity createUser(@Valid @RequestBody User user) {
        statsDClient.incrementCounter("endpoint.user.http.post");

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        String cmd;
        try {
            String token = TokenGenerator.token();
            String msg = String.format("{\"default\":\"default\",\"email\":\"%s\",\"token\":\"%s\"}", user.getUsername(), token);
            long unixTime = Instant.now().getEpochSecond();
            long expireTime = unixTime + 300L;
            String item = String.format("{\"Email\":{\"S\":\"%s\"},\"Token\":{\"S\":\"%s\"},\"ExpireTime\":{\"N\":\"%s\"}}", user.getUsername(), token, expireTime);

            cmd = "aws dynamodb put-item " +
                    "--table-name csye6225_webapp " +
                    "--item " + item +
                    " --region us-east-1";
            CmdRunner.run(cmd);

            cmd = "aws sns publish " +
                    "--topic-arn arn:aws:sns:us-east-1:567984459938:csye6225-webapp-email-verification " +
                    "--message " + msg +
                    " --region us-east-1";
            CmdRunner.run(cmd);

            return new ResponseEntity<User>(repository.save(user), HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/v1/user")
    public List<User> getAllUsers() {
        statsDClient.incrementCounter("endpoint.user.http.get");
        return repository.findAll();
    }

    @GetMapping("/v1/user/self")
    public ResponseEntity getSelf(@RequestHeader("Authorization") String token) {
        statsDClient.incrementCounter("endpoint.user.self.http.get");

        String base64Credentials = token.substring("Basic".length()).trim();
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        String username = credentials.split(":")[0];

        if(repository.findByUsername(username).getVerified() == null
                || !repository.findByUsername(username).getVerified()) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<User>(repository.findByUsername(username), HttpStatus.OK);
    }

    @PutMapping("/v1/user/self")
    public ResponseEntity updateSelf(@RequestHeader("Authorization") String token, @RequestBody User user) {
        statsDClient.incrementCounter("endpoint.user.self.http.put");

        try {
            String base64Credentials = token.substring("Basic".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            String username = credentials.split(":")[0];
            if(repository.findByUsername(username).getVerified() == null
                    || !repository.findByUsername(username).getVerified()) {
                return new ResponseEntity(HttpStatus.UNAUTHORIZED);
            }

            User updatedUser = repository.findByUsername(username);

            updatedUser.updateUser(user.getFirstName(), user.getLastName(), passwordEncoder.encode(user.getPassword()));
            repository.save(updatedUser);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch(Exception e) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/v1/user/self")
    public ResponseEntity deleteSelf(@RequestHeader("Authorization") String token) {
        statsDClient.incrementCounter("endpoint.user.self.http.delete");

        String base64Credentials = token.substring("Basic".length()).trim();
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        String username = credentials.split(":")[0];

        try {
            repository.deleteByUsername(username);
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/v1/verifyUserEmail")
    public ResponseEntity verifyUserEmail(@RequestParam("email") String email, @RequestParam("token") String token) {
        statsDClient.incrementCounter("endpoint.verify.http.get");

        String key = String.format("{\"Email\":{\"S\":\"%s\"}}", email);
        String cmd = "aws dynamodb get-item --consistent-read " +
                "--table-name csye6225_webapp " +
                "--key " + key +
                " --region us-east-1";
        try {
            String output = CmdRunner.run(cmd);
            JSONObject obj = new JSONObject(output);
            String t = obj.getJSONObject("Item").getJSONObject("Token").getString("S");

            if (t.equals(token)) {
                User user = repository.findByUsername(email);
                user.setVerified(true);
                repository.save(user);

                return new ResponseEntity("Verify email successfully!", HttpStatus.OK);
            } else {
                return new ResponseEntity("Failed to verify email.", HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

    }
}
