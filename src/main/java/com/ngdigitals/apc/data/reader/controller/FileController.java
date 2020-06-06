package com.ngdigitals.apc.data.reader.controller;

import java.util.Date;

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

    @Autowired
    private FileService fileService;

    @GetMapping(path = "/ping")
    public ResponseEntity getServerTime() {
        return ResponseEntity.ok("Received ping on "+new Date().toString());
    }

    @PostMapping(path = "/import")
    public ResponseEntity<?> importFile(@RequestPart("file") MultipartFile file){
        fileService.importFile(file);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("File import successful");
        return ResponseEntity.ok(apiResponse);
    }
}