package com.framatome.liquidaciones.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.framatome.liquidaciones.model.UserData;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ExcelProcessorService {
	
	@Value("${excel.file.path}")
	Resource resource;
	
	@Value("${excel.column.userId}")
	int userIdColumn;
	
	@Value("${excel.column.userNombre}")
	int nombreColumn;
	
	@Value("${excel.column.userApellidos}")
	int apellidosColumn;

	private FileInputStream fis;
	private Workbook workbook;
	private Sheet sheet;
	private Iterator<Row> rowIterator;

	public void loadExcel() {
		try {
			log.info("Cargando archivo Excel: {}", resource.getFile().getAbsolutePath());
			fis = new FileInputStream(resource.getFile());
			workbook = new XSSFWorkbook(fis);
			sheet = workbook.getSheetAt(0);
			rowIterator = sheet.iterator();
			rowIterator.next(); // Ignorar la primera fila (cabecera)
		} catch (IOException e) {
			log.error("Error al cargar el archivo Excel:", e);
		}
	}

	public void closeExcel() {
		try {
			if (fis != null) {
				fis.close();
				fis = null;
			}
			if (workbook != null) {
				workbook.close();
				workbook = null;
			}
			log.info("Archivo Excel cerrado");
		} catch (IOException e) {
			log.error("Error al cerrar el archivo Excel:", e);
		}
	}

	public UserData getNextUser() {

		if (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			UserData userData = new UserData();
			userData.setUserId(row.getCell(userIdColumn).getStringCellValue());
			userData.setNombre(row.getCell(nombreColumn).getStringCellValue());
			userData.setApellidos(row.getCell(apellidosColumn).getStringCellValue());
			if(userData.getUserId() == null || userData.getUserId().isEmpty()) {
				log.warn("Row sin ID, finalizando proceso...");
				return null;
			}
			return userData;
		}
		
		return null; // Fin del archivo
	}
}
