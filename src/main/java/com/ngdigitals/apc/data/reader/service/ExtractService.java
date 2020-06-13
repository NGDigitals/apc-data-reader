package com.ngdigitals.apc.data.reader.service;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@Slf4j
public class ExtractService {

    @Autowired
    FileService fileService;

    private File rootDir = null;
    private File currentDir = null;

    public List<File> listDirectory(String path, boolean setAsRoot) {
        File directory = new File(path);
        List<File> fileList = new ArrayList<>();
        File[] files = directory.listFiles();
//		fileList.addAll(Arrays.asList(files));
        FileService fileService = new FileService();
        boolean directoryChange = false;
        if(/*setAsRoot ||*/ rootDir == null) {
            rootDir = new File(directory.getName()).getAbsoluteFile();
            if (rootDir.exists() || rootDir.mkdirs())
                ;
            System.out.println("Setting Root is null" + rootDir.getAbsolutePath());
        }else{
            System.out.println("Root is not null");
        }
        System.setProperty("user.dir", rootDir.getAbsolutePath());
        for (File file : files) {
            if (file.isFile()) {
//                System.out.println("Putting File: " + currentDir.getAbsolutePath() + "YYYYY" + file.getAbsolutePath());
                if(FilenameUtils.isExtension(file.getName(), "pdf"))
                    fileService.importFile(currentDir.getAbsolutePath(), file);
//				System.out.println("Reset CMD" + rootDir);
//				System.setProperty("user.dir", rootDir);
//				directoryChange = false;
            } else if (file.isDirectory()) {
//				if(fileList.size() > 0) {

//				}
//				directoryChange = false;
                File newDir = new File(file.getName()).getAbsoluteFile();
                if (newDir.exists() || newDir.mkdirs()) {
//					if(!directoryChange){
                    String [] split = newDir.getAbsolutePath().split("/");
                    System.out.println(rootDir.getName() + " Root & New: " + split[split.length-1]);
                    if(!rootDir.getName().equals(split[split.length-1])) {
                        System.out.println("Creating image dir...");
                        File imagesDir = new File(newDir.getName() + "/images").getAbsoluteFile();
                        if (imagesDir.exists() || imagesDir.mkdirs())
                            ;
                    }
                    directoryChange = (System.setProperty("user.dir", newDir.getAbsolutePath()) != null);
//					}
                    //currentDir = newDir.getAbsolutePath();
                    currentDir = newDir;
                    System.out.println(rootDir.getName() + " Create/Change Dire: " + newDir.getName());
                }
//				if(!file.getName().equals("images"))
                listDirectory(file.getAbsolutePath(), false);
//				System.out.println("Dire: " + file.getAbsolutePath());
                //fileList.addAll(listDirectory(file.getAbsolutePath()));
            }
        }
        return fileList;
    }
}