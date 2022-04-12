package edu.neu.coe.csye6225.webapp;

import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@ComponentScan(basePackages = {"edu.neu.coe.csye6225.webapp"})
@RestController
public class WebappApplication {
	@Autowired
	private StatsDClient statsDClient;

	public static void main(String[] args) {
		SpringApplication.run(WebappApplication.class, args);
	}

	@GetMapping(value = {"/healthzzzzz", "/"})
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity health() {
		statsDClient.incrementCounter("endpoint.home.http.get");
		return ResponseEntity.ok().build();
	}
}
