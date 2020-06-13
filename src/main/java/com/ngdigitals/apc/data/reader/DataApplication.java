package com.ngdigitals.apc.data.reader;

import java.io.File;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ngdigitals.apc.data.reader.service.ExtractService;

@Slf4j
@SpringBootApplication
public class DataApplication {

	public static void main(String[] args) {

		SpringApplication.run(DataApplication.class, args);

		File directoryPath = new File(args.length > 0 ? args[0] : "/Users/abrahamoyelaran/Desktop/18  - OWO");
		ExtractService extractService = new ExtractService();
		System.out.println("Path " + directoryPath.getPath());
		extractService.listDirectory(directoryPath.getPath(), true);
		log.info("*** Data extraction complete ***");
	}

}
