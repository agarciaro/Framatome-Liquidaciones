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
//			webDriverService.init();
//			webDriverService.goHome();
//			webDriverService.login();
			excelProcessorService.loadExcel();
			UserData userData;
			do {
				userData = excelProcessorService.getNextUser();
				log.info("Usuario: {}", userData);
			} while (userData != null);
			
			excelProcessorService.closeExcel();
		} catch (Exception e) {
			log.error("Error en el proceso: {}", e.getMessage());
		} finally {
			webDriverService.close();
		}
    }

//    public void procesarUsuario(WebDriver driver, String username, String password) {
//        try {
//            // Navegar a la página web
//            driver.get(urlHome);
//
//            // Esperar a que la página cargue completamente
//            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
//
//            // Iniciar sesión
//            WebElement usernameField = driver.findElement(By.id("userNameBox"));
//            WebElement passwordField = driver.findElement(By.id("passWordBox"));
//
//            usernameField.sendKeys(username);
//            passwordField.sendKeys(password);
//            passwordField.submit();
//
//            // Esperar a que la sesión inicie
//            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

//            // Navegar a la sección específica del usuario
//            driver.get("URL_DE_LA_SECCION_DE_USUARIO");
//
//            // Esperar a que la sección cargue completamente
//            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
//
//            // Acceder a la sección "liquidaciones"
//            WebElement liquidaciones = driver.findElement(By.xpath("XPATH_DE_LA_SECCION_LIQUIDACIONES"));
//            liquidaciones.click();
//
//            // Esperar a que cargue la sección de liquidaciones
//            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
//
//            // Descargar los documentos
//            List<WebElement> documentos = driver.findElements(By.className("CLASE_DE_LOS_ENLACES_DE_DESCARGA"));
//            for (WebElement documento : documentos) {
//                documento.click();
//                // Esperar unos segundos para que la descarga se complete
//                Thread.sleep(3000);
//            }
//
//            // Cerrar sesión
//            WebElement logoutButton = driver.findElement(By.id("ID_DEL_BOTON_DE_CERRAR_SESION"));
//            logoutButton.click();
//
//            // Esperar a que cierre sesión
//            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

//        } catch (Exception e) {
//            log.error("Error al procesar el usuario: " + username, e);
//        }
//    }

}
