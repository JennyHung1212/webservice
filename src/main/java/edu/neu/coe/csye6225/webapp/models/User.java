package edu.neu.coe.csye6225.webapp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class User {
    @Id @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    @NotNull(message = "The first name should not be null")
    @NotEmpty(message = "The first name is required.")
    private String firstName;

    @NotNull(message = "The last name should not be null")
    @NotEmpty(message = "The last name is required.")
    private String lastName;

    @NotNull(message = "The password should not be null.")
    @NotEmpty(message = "The password is required.")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(unique=true)
    @Email(message = "The username must be a well-formed email address.")
    @NotNull(message = "The username should not be null.")
    private String username;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String accountCreated;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String accountUpdated;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean verified = false;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String verifiedOn;

    public User() {
        final String now = Instant.now().toString();
        this.accountCreated = now;
        this.accountUpdated = now;
    };
    public User(String id, String firstName, String lastName, String password, String username) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.username = username;

        final String now = Instant.now().toString();
        this.accountCreated = now;
        this.accountUpdated = now;

        this.verified = false;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAccountCreated() {
        return accountCreated;
    }

    public String getAccountUpdated() {
        return accountUpdated;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAccountUpdated(String accountUpdated) {
        this.accountUpdated = accountUpdated;
    }

    public void updateUser(String firstName, String lastName, String password) {
        final String now = Instant.now().toString();
        setFirstName(firstName);
        setLastName(lastName);
        setPassword(password);
        setAccountUpdated(now);
    }

    public Boolean getVerified() {
        return verified;
    }

    public String getVerifiedOn() {
        return verifiedOn;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
        if(verified == true) {
            final String now = Instant.now().toString();
            this.verifiedOn = now;
        }
    }

    public void setVerifiedOn(String verifiedOn) {
        this.verifiedOn = verifiedOn;
    }
}
