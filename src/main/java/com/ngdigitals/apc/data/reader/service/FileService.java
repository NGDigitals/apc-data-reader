package com.ngdigitals.apc.data.reader.service;

import java.io.File;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.io.FileInputStream;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import com.ngdigitals.apc.data.reader.model.Voter;
import com.ngdigitals.apc.data.reader.exception.FileImportException;

@Service
@Slf4j
public class FileService {
    @Autowired
    ExcelService excelService;

    public void importFile(String path, File file) {
        InputStream inputStream = null;
        ExcelService excelService = new ExcelService();
        try {
            long start = System.currentTimeMillis();
            inputStream = new FileInputStream(file);
            try (PDDocument document = PDDocument.load(inputStream)) {
                List<File> pictures = readImages(path, document);
                document.getClass();
                if (!document.isEncrypted()) {
                    List<Voter> voters = readText(document);
                    System.out.println("About Excel File : " + path + " && " + file.getName());
                    excelService.writeExcel(voters, pictures, path, file.getName());
                    document.close();
                    long end = System.currentTimeMillis();
                    log.info("*** Import done in {} ms *** \n", (end - start));
                }
            }
        } catch (IOException | RuntimeException ex) {
            ex.printStackTrace();
            //throw new FileImportException(ex.getMessage());
        }finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                throw new FileImportException(e.getMessage());
            }
        }
    }

    public void importFile(MultipartFile file) {
        InputStream inputStream = null;
        try {
            long start = System.currentTimeMillis();
            inputStream = file.getInputStream();
            try (PDDocument document = PDDocument.load(inputStream)) {
                List<File> pictures = readImages(null, document);
                document.getClass();
                if (!document.isEncrypted()) {
                    List<Voter> voters = readText(document);
                    excelService.writeExcel(voters, pictures, null, file.getOriginalFilename());
                    document.close();
                    long end = System.currentTimeMillis();
                    log.info("*** Import done in {} ms *** \n", (end - start));
                }
            }
        } catch (IOException | RuntimeException ex) {
            throw new FileImportException(ex.getMessage());
        }finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                throw new FileImportException(e.getMessage());
            }
        }
    }

    private List<Voter> readText(PDDocument document) throws IOException{
        List<Voter> voters = new ArrayList<>();
        int totalPages = document.getNumberOfPages();
        PDFTextStripper textStripper = new PDFTextStripper();
        for(int pageCount = 2; pageCount < totalPages; pageCount++){
            textStripper.setStartPage(pageCount);
            textStripper.setEndPage(pageCount);
            String text = textStripper.getText(document);
            String[] lines = text.split("[\\r\\n]");
//            boolean skipped = false;
            if(lines.length >= 24) {
                String state = lines[6];
                String lga = lines[7];
                String regArea = lines[8];
                int lineIndex = 9;
                String pollingUnit = lines[lineIndex];
                while(++lineIndex < lines.length && !lines[lineIndex].equals("Time Printed:")){
                    pollingUnit += ", " + lines[lineIndex];
                }
                lineIndex += 9;
                int imageIndex = 3;
                for (String line : lines) {
//                    if(pageCount == 2 && skipped == false){
//                        index = 28;
//                        skipped = true;
//                    }
                    System.out.println(lineIndex + " : Reading... " + line + " & " + lines[lineIndex]);
                    Voter voter = new Voter();
                    voter.setVIN(lines[lineIndex]);
                    String[] names = lines[++lineIndex].split(",");
                    if(names[0].isEmpty()){
                        lineIndex += 7;
                        System.out.println("Skipping...");
                        continue;
                    }else {
                        System.out.println(lineIndex + " : Reading... " + line + " & " + lines[lineIndex]);
                        voter.setLastName(names[0]);
                        voter.setOtherNames(names[1]);
                        String optional = lines[++lineIndex];
                        String[] optionalSplit = optional.split(" ");
                        if (optionalSplit[0].equals("Male") || optionalSplit[0].equals("Female")) {
                            //                        System.out.println(++index + " : No occupation... " + line);
                            voter.setOccupation("Other");
                            voter.setGender(optionalSplit[0]);
                            voter.setAge(optionalSplit[1]);
                        } else {
                            //                        System.out.println(++index + " : Occupation available... " + line);
                            voter.setOccupation(optional);
                            String[] string = lines[++lineIndex].split(" ");
                            voter.setGender(string[0]);
                            voter.setAge(string[1]);
                        }
                        voter.setState(state);
                        voter.setLga(lga);
                        voter.setRegArea(regArea);
                        voter.setPollingUnit(pollingUnit);
                        voter.setPicture(pageCount + "-" + imageIndex);
                        System.out.println("Reading... " + voter);
                        voters.add(voter);
                        imageIndex++;
                        if ((lineIndex += 6) >= lines.length)
                            break;
                    }
                }
            }
        }
        return voters;
    }

    private List<File> readImages(String path, PDDocument document) throws IOException {
        List<File> pictures = new ArrayList<>();
        int totalPages = document.getNumberOfPages();
        for(int pageCount = 1; pageCount < totalPages; pageCount++) {
            PDPage page = document.getPage(pageCount);
            PDResources pdResources = page.getResources();
            int index = 1;
            for (COSName cosName : pdResources.getXObjectNames()) {
                PDXObject pdxObject = pdResources.getXObject(cosName);
                if (pdxObject instanceof PDImageXObject) {
                    if(index > 2) {
                        File picture;
                        if(path == null)
                             picture = new File("import/images/" + pageCount + "-" + index + ".png");
                        else
                            picture = new File(path + "/images/" + pageCount + "-" + index + ".png");
                        PDStream pdStream = pdxObject.getStream();
                        PDImageXObject pdImage = new PDImageXObject(pdStream, pdResources);
                        ImageIO.write(pdImage.getImage(), "png", picture);
                        pictures.add(picture);
                    }
                    index++;
                }
            }
        }
        return pictures;
    }
}