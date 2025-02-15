package com.example.practice.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

@Service
public class RPAService {
	private static final Logger log = LoggerFactory.getLogger(RPAService.class);

	public void startChallenge() {
		try (FileInputStream file = new FileInputStream("doc/challenge.xlsx");
				Workbook workbook = new XSSFWorkbook(file);
				Playwright playwright = Playwright.create()) {
			Sheet sheet = workbook.getSheetAt(0);
			Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
			Page page = browser.newPage();
			page.navigate("https://rpachallenge.com/?lang=EN");
			page.click("button:has-text('Start')");
			Row headerRow = sheet.getRow(0);
			boolean isEnd = false;
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				Map<String, String> data = new HashMap<>();
				isEnd = excelToMap(data, headerRow, row);
				if (isEnd) break;
				fillForm(data, page);
			}
			Thread.sleep(2000);
			browser.close();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean excelToMap(Map<String, String> data, Row headerRow, Row row) {
		boolean isEnd = false;
		for (int j = 0; j < headerRow.getLastCellNum(); j++) {
			Cell headerCell = headerRow.getCell(j);
			String header = headerCell != null ? headerCell.getStringCellValue() : "";
			if (header == null || header.isEmpty()) {
				continue;
			}
			Cell cell = row.getCell(j);
			String value = "";
			if (cell != null) {
				switch (cell.getCellType()) {
				case STRING:
					value = cell.getStringCellValue();
					break;
				case NUMERIC:
					value = String.valueOf(cell.getNumericCellValue());
					break;
				case BOOLEAN:
					value = String.valueOf(cell.getBooleanCellValue());
					break;
				default:
					value = cell.toString();	
					isEnd = true;
					break;
				}
			} 
			data.put(header, value);
		}
		return isEnd;
	}

	public void fillForm(Map<String, String> data, Page page) {
		for (Map.Entry<String, String> entry : data.entrySet()) {
			String labelText = entry.getKey();
			if (labelText == null || labelText.isEmpty()) {
				continue;
			}
			String selector = "label:has-text('" + labelText + "') + input";
			Locator inputField = page.locator(selector);
			inputField.fill(entry.getValue());
		}
		log.info("SUBMIT => {} ", data);
		page.locator("input[value='Submit']").click();
	}
}