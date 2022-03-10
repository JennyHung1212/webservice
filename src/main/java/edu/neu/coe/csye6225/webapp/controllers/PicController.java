package edu.neu.coe.csye6225.webapp.controllers;

import edu.neu.coe.csye6225.webapp.models.Pic;
import edu.neu.coe.csye6225.webapp.models.User;
import edu.neu.coe.csye6225.webapp.repositories.PicRepository;
import edu.neu.coe.csye6225.webapp.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Executable;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@RestController
public class PicController {
    private final PicRepository picRepository;
    private final UserRepository userRepository;


    public PicController(PicRepository picRepository, UserRepository userRepository) {
        this.picRepository = picRepository;
        this.userRepository = userRepository;
    }

    @PostMapping(value="/v1/user/self/pic",
            consumes=MediaType.MULTIPART_FORM_DATA_VALUE,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Pic> createProfilePic(@RequestHeader("Authorization") String token, @RequestParam MultipartFile profilePic) {
        String base64Credentials = token.substring("Basic".length()).trim();
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        String username = credentials.split(":")[0];
        User user = userRepository.findByUsername(username);
        String userId = user.getId();

        String fileName = profilePic.getOriginalFilename();
        String contentType = profilePic.getContentType();
        Long size = profilePic.getSize();
        String filePath = "/home/ec2-user/" + fileName;
        String s3BucketPath = "s3://" + System.getenv("S3_BUCKET_NAME") + "/" + userId + "/";

        try {
            String cmd;
            Process p;
            if(picRepository.findByUserId(userId) != null) {
                cmd = "aws s3 rm " + s3BucketPath + " --recursive";
                p = Runtime.getRuntime().exec(cmd);

                p.waitFor();
                BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(p.getErrorStream()));
                String s = null;
                while ((s = stdError.readLine()) != null) {
                    System.out.println(s);
                    throw new Exception(s);
                }
            }

            File file = new File(filePath);
            profilePic.transferTo(file);
            cmd = "aws s3 cp " + filePath + " " + s3BucketPath;
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String s = null;
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
                throw new Exception(s);
            }
            file.delete();
        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        Pic newProfilePic;
        if(picRepository.findByUserId(userId) != null) {
            newProfilePic = picRepository.findByUserId(userId);
        } else {
            newProfilePic = new Pic();
        }
        newProfilePic.setUserId(userId);
        newProfilePic.setUrl(s3BucketPath + fileName);
        newProfilePic.setUploadDate(Instant.now().toString());
        newProfilePic.setFileName(fileName);
        newProfilePic.setContentType(contentType);
        newProfilePic.setSize(size);

        picRepository.save(newProfilePic);

        return ResponseEntity.status(HttpStatus.CREATED).body(newProfilePic);
    }

    @GetMapping("/v1/user/self/pic")
    public ResponseEntity<Pic> getProfilePic(@RequestHeader("Authorization") String token) {
        String base64Credentials = token.substring("Basic".length()).trim();
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        String username = credentials.split(":")[0];
        User user = userRepository.findByUsername(username);
        String userId = user.getId();

        Pic pic = picRepository.findByUserId(userId);
        System.out.println(pic);
        if (pic == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(picRepository.findByUserId(userId));
    }

    @DeleteMapping("/v1/user/self/pic")
    public ResponseEntity deleteProfilePic(@RequestHeader("Authorization") String token) {
        String base64Credentials = token.substring("Basic".length()).trim();
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        String username = credentials.split(":")[0];
        User user = userRepository.findByUsername(username);
        String userId = user.getId();

        Pic pic = picRepository.findByUserId(userId);
        if (pic == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        String id = pic.getId();
        String url = pic.getUrl();
        String fileName = pic.getFileName();
        int i = url.lastIndexOf('/');
        String s3Bucket = url.substring(0, i);
        System.out.println(s3Bucket);



        try {
            picRepository.deleteById(id);
            String cmd = "aws s3 rm " + s3Bucket + " --recursive";
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            String s = null;
            while ((s = stdError.readLine()) != null) {
                throw new Exception(s);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
