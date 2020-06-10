package com.ngdigitals.apc.data.reader.service;

import java.io.File;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

    public void importFile(MultipartFile file) {
        InputStream inputStream = null;
        try {
            long start = System.currentTimeMillis();
            inputStream = file.getInputStream();
            try (PDDocument document = PDDocument.load(inputStream)) {
                List<File> pictures = readImages(document);
                document.getClass();
                if (!document.isEncrypted()) {
                    List<Voter> voters = readText(document);
                    excelService.writeExcel(voters, pictures, file.getOriginalFilename());
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
            if(lines.length >= 24) {
                String state = lines[6];
                String lga = lines[7];
                String regArea = lines[8];
                String pollingUnit = lines[9] + " " + lines[10];
                int index = 20, count = 3;
                for (String line : lines) {
                    Voter voter = new Voter();
                    voter.setVIN(lines[index]);
                    String[] names = lines[++index].split(",");
                    voter.setLastName(names[0]);
                    voter.setOtherNames(names[1]);
                    voter.setOccupation(lines[++index]);
                    String[] string = lines[++index].split(" ");
                    voter.setGender(string[0]);
                    voter.setAge(Integer.parseInt(string[1]));
                    voter.setState(state);
                    voter.setLga(lga);
                    voter.setRegArea(regArea);
                    voter.setPollingUnit(pollingUnit);
                    voter.setPicture(pageCount + "-" + count);
                    System.out.println("Reading... " + voter);
                    voters.add(voter);
                    count++;
                    if((index += 6) >= lines.length)
                        break;
                }
            }
        }
        return voters;
    }

    private List<File> readImages(PDDocument document) throws IOException {
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
                        File picture = new File("import/images/" + pageCount + "-" + index + ".png");
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