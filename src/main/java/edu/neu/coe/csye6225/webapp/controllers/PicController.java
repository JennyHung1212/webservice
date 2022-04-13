package edu.neu.coe.csye6225.webapp.controllers;

import com.timgroup.statsd.StatsDClient;
import edu.neu.coe.csye6225.webapp.models.Pic;
import edu.neu.coe.csye6225.webapp.models.User;
import edu.neu.coe.csye6225.webapp.utils.CmdRunner;
import edu.neu.coe.csye6225.webapp.repositories.PicRepository;
import edu.neu.coe.csye6225.webapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@RestController
public class PicController {
    @Autowired
    private StatsDClient statsDClient;

    private final PicRepository picRepository;
    private final UserRepository userRepository;


    public PicController(@Qualifier("pic") PicRepository picRepository, @Qualifier("user") UserRepository userRepository) {
        this.picRepository = picRepository;
        this.userRepository = userRepository;
    }

    @PostMapping(value="/v1/user/self/pic",
            consumes=MediaType.MULTIPART_FORM_DATA_VALUE,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createProfilePic(@RequestHeader("Authorization") String token, @RequestParam MultipartFile profilePic) {
        statsDClient.incrementCounter("endpoint.pic.http.post");

        String base64Credentials = token.substring("Basic".length()).trim();
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        String username = credentials.split(":")[0];
        if(userRepository.findByUsername(username).getVerified() == null
                || !userRepository.findByUsername(username).getVerified()) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByUsername(username);
        String userId = user.getId();

        String fileName = profilePic.getOriginalFilename().replaceAll("\\s+","_");
        String contentType = profilePic.getContentType();
        if(!contentType.equals("image/png") && !contentType.equals("image/jpeg") && !contentType.equals("image/jpg")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file type.");
        }
        Long size = profilePic.getSize();
        String filePath = "/home/ec2-user/" + fileName;
        String s3BucketPath = "s3://" + System.getenv("S3_BUCKET_NAME") + "/" + userId + "/";

        try {
            String cmd;
            Process p;
            if(picRepository.findByUserId(userId) != null) {
                cmd = "aws s3 rm " + s3BucketPath + " --recursive";
                CmdRunner.run(cmd);
            }

            File file = new File(filePath);
            profilePic.transferTo(file);
            cmd = "aws s3 cp " + filePath + " " + s3BucketPath;
            CmdRunner.run(cmd);

            file.delete();
        } catch (Exception e) {
            System.out.println(e);
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
        statsDClient.incrementCounter("endpoint.pic.http.get");

        String base64Credentials = token.substring("Basic".length()).trim();
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        String username = credentials.split(":")[0];
        if(userRepository.findByUsername(username).getVerified() == null
                || !userRepository.findByUsername(username).getVerified()) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

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
        statsDClient.incrementCounter("endpoint.pic.http.delete");

        String base64Credentials = token.substring("Basic".length()).trim();
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        String username = credentials.split(":")[0];
        if(userRepository.findByUsername(username).getVerified() == null
                || !userRepository.findByUsername(username).getVerified()) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByUsername(username);
        String userId = user.getId();

        Pic pic = picRepository.findByUserId(userId);
        if (pic == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        String id = pic.getId();
        String url = pic.getUrl();
        int i = url.lastIndexOf('/');
        String s3Bucket = url.substring(0, i);
        System.out.println(s3Bucket);

        try {
            picRepository.deleteById(id);
            String cmd = "aws s3 rm " + s3Bucket + " --recursive";
            CmdRunner.run(cmd);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
