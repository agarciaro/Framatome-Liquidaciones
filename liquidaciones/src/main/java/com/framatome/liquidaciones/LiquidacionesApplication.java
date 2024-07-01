package com.framatome.liquidaciones;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.framatome.liquidaciones.model.UserData;
import com.framatome.liquidaciones.service.ExcelProcessorService;
import com.framatome.liquidaciones.service.WebDriverService;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class LiquidacionesApplication {
	
	@Autowired
	WebDriverService webDriverService;
	
	@Autowired
	ExcelProcessorService excelProcessorService;
	
	public static void main(String[] args) {
		log.info("Iniciando la aplicación de liquidaciones");
		SpringApplication.run(LiquidacionesApplication.class, args);
		log.info("Aplicación de liquidaciones finalizada");
	}
	
	@Bean
    CommandLineRunner run() {
        return args -> {
            procesarUsuarios();
        };
    }
	
	public void procesarUsuarios() {
		try {
			webDriverService.init();
			webDriverService.goHome();
			webDriverService.login();
			
//			webDriverService.downloadFiles();
			excelProcessorService.loadExcel();
			excelProcessorService.createCsv();
			UserData userData = null;
//			userData = new UserData("00832501Q", "Vicente Esteban", "Soler Crespo", "00832501Q - Vicente Esteban Soler Crespo");
//			userData = new UserData("03141190B", "Abraham", "Ecija Castaño", "03141190B - Abraham Ecija Castaño");
//			excelProcessorService.createUserFolder(userData);
//			webDriverService.openUserView(userData);
			
			int userCount = 0;
			while ((userData = excelProcessorService.getNextUser()) != null) {
				log.info("Usuario: {}", userData);
				excelProcessorService.createUserFolder(userData);
				webDriverService.openUserView(userData);
				userCount++;
			}
			
			log.info("Usuarios procesados: {}", userCount);
			
		} catch (Exception e) {
			log.error("Error en el proceso: {}", e.getMessage());
		} finally {
			excelProcessorService.closeExcel();
			webDriverService.close();
			excelProcessorService.closeCsv();
		}
    }

}
