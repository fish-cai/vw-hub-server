package com.fish.vwhub.util;

import com.alibaba.fastjson2.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelUtil {

    private static List<String> readExcelTitle(File file) throws Exception {
        List<String> result = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(Files.newInputStream(file.toPath()));
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(0);
        Iterator<Cell> iterator = row.cellIterator();
        while (iterator.hasNext()) {
            Cell cell = (Cell) iterator.next();
            String value = cell.getStringCellValue();
            result.add(value);
        }
        return result;
    }

    public static List<JSONObject> readExcelData(File file) throws Exception {
        List<JSONObject> result = new ArrayList<>();
        List<String[]> data = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(Files.newInputStream(file.toPath()));
        Sheet sheet = workbook.getSheetAt(0);
        Row hRow = sheet.getRow(0);
        Iterator<Cell> iterator = hRow.cellIterator();
        while (iterator.hasNext()) {
            Cell cell = (Cell) iterator.next();
            String value = cell.getStringCellValue();
            headers.add(value);
        }
        // 循环获取每一行数据
        for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            int numberOfCells = row.getPhysicalNumberOfCells();
            String[] tmp = new String[numberOfCells];
            // 读取每一格内容
            for (int index = 0; index < numberOfCells; index++) {
                Cell cell = row.getCell(index);
                if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    tmp[index] = cell.getStringCellValue();
                } else {
                    tmp[index] = cell.getStringCellValue();
                }
            }
            data.add(tmp);
        }
        for (String[] s : data) {
            JSONObject jsonObject = new JSONObject();
            for (int i = 0; i < headers.size(); i++) {
                jsonObject.put(headers.get(i), s[i]);
            }
            result.add(jsonObject);
        }
        return result;
    }

}
