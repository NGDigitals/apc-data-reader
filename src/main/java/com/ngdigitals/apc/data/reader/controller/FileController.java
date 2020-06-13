package com.ngdigitals.apc.data.reader.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ngdigitals.apc.data.reader.service.ExtractService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import com.ngdigitals.apc.data.reader.service.FileService;
import com.ngdigitals.apc.data.reader.payload.response.ApiResponse;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/file")
public class FileController {

//    private File rootDir = null;
//    private File currentDir = null;

    @Autowired
    private FileService fileService;

    @Autowired
    private ExtractService extractService;

    @GetMapping(path = "/ping")
    public ResponseEntity getServerTime() {
        return ResponseEntity.ok("Received ping on "+new Date().toString());
    }

    @PostMapping(path = "/import")
    public ResponseEntity<?> importFile(@RequestPart("file") List<MultipartFile> files){
        for (MultipartFile file : files) {
            fileService.importFile(file);
        }
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("File import successful");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping(path = "/extract")
    public ResponseEntity<?> readFile(@RequestPart("path") String path){
        File directoryPath = new File(path);
        //List of all files and directories
//		File files[] = directoryPath.listFiles();
        extractService.listDirectory(directoryPath.getPath(), true);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("File import successful");
        return ResponseEntity.ok(apiResponse);
    }
}