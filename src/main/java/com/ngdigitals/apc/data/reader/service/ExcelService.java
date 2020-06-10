package com.ngdigitals.apc.data.reader.service;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.io.FileOutputStream;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.springframework.stereotype.Service;

import com.ngdigitals.apc.data.reader.model.Voter;

@Service
@Slf4j
public class ExcelService {
    private static String[] columns = {"VIN", "LAST NAME", "OTHER NAMES", "OCCUPATION", "GENDER", "AGE",
            "STATE", "LGA", "REGISTRATION AREA", "POLLING UNIT", "PICTURE"};

    public void writeExcel(List<Voter> voters, List<File> pictures, String fileName) throws IOException {

        Workbook workbook = new XSSFWorkbook();

//        CreationHelper createHelper = workbook.getCreationHelper();
        Sheet sheet = workbook.createSheet("Voters");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.RED.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }
        int rowNum = 1;
        int index = 0;
        for (Voter voter : voters) {
            System.out.println("Processing... " + voter);
            Row row = sheet.createRow(rowNum++);
            Font bodyFont = workbook.createFont();
            bodyFont.setFontHeightInPoints((short) 15);

            // Create a CellStyle with the font
            CellStyle bodyCellStyle = workbook.createCellStyle();
            bodyCellStyle.setFont(bodyFont);
            row.setHeight((short) 2000);
            Cell cell = row.createCell(0);
            cell.setCellStyle(bodyCellStyle);
            cell.setCellValue(voter.getVIN());

            cell = row.createCell(1);
            cell.setCellStyle(bodyCellStyle);
            cell.setCellValue(voter.getLastName());

            cell = row.createCell(2);
            cell.setCellStyle(bodyCellStyle);
            cell.setCellValue(voter.getOtherNames());

            cell = row.createCell(3);
            cell.setCellStyle(bodyCellStyle);
            cell.setCellValue(voter.getOccupation());

            cell = row.createCell(4);
            cell.setCellStyle(bodyCellStyle);
            cell.setCellValue(voter.getGender());

            cell = row.createCell(5);
            cell.setCellStyle(bodyCellStyle);
            cell.setCellValue(voter.getAge());

            cell = row.createCell(6);
            cell.setCellStyle(bodyCellStyle);
            cell.setCellValue(voter.getState());

            cell = row.createCell(7);
            cell.setCellStyle(bodyCellStyle);
            cell.setCellValue(voter.getLga());

            cell = row.createCell(8);
            cell.setCellStyle(bodyCellStyle);
            cell.setCellValue(voter.getRegArea());

            cell = row.createCell(9);
            cell.setCellStyle(bodyCellStyle);
            cell.setCellValue(voter.getPollingUnit());

            //InputStream image = new FileInputStream("D:\\PB_PROJECT\\NFC School Card\\NFCREST\\web\\photo_student\\4566.png");
            //image.close();
            File picture = pictures.get(index);
            byte[] bytes = Files.readAllBytes(picture.toPath());//IOUtils.toByteArray(image);
            picture.delete();
            int pictureID = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);

            XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
            XSSFClientAnchor xssfClientAnchor = new XSSFClientAnchor();
            xssfClientAnchor.setCol1(10);
            xssfClientAnchor.setRow1(rowNum-1);
            xssfClientAnchor.setCol2(11);
            xssfClientAnchor.setRow2(rowNum);

            drawing.createPicture(xssfClientAnchor, pictureID);
            index++;
        }

        for (int i = 0; i < columns.length; i++)
            sheet.autoSizeColumn(i);

        FileOutputStream fileOut = new FileOutputStream("import/" + fileName + ".xlsx");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }
}
