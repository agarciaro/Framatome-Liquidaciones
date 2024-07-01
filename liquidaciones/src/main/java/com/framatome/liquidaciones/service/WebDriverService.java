package com.framatome.liquidaciones.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.framatome.liquidaciones.model.UserData;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WebDriverService {

	@Value("${web.url.home}")
	String urlHome;

	@Value("${login.username}")
	String loginUser;

	@Value("${login.password}")
	String loginPassword;
	
	@Value("${temp.download.folder}")
	String downloadFolder;
	
	@Value("${liquidaciones.folder}")
	String liquidacionesFolder;
	
	@Autowired
	ExcelProcessorService excelProcessorService;

	ChromeOptions options;

	WebDriver webDriver;
	
	Map<String, Object> prefs = new HashMap<String, Object>();

	public void init() {
		// Configurar WebDriverManager para manejar ChromeDriver
		WebDriverManager.chromedriver().setup();

		// Configurar opciones de Chrome
		options = new ChromeOptions();
		options.addArguments("--start-maximized");
		
		
		prefs.put("download.default_directory", downloadFolder);
		prefs.put("plugins.always_open_pdf_externally", true);
		prefs.put("download.prompt_for_download", false);
        prefs.put("profile.default_content_settings.popups", 0);
		options.setExperimentalOption("prefs", prefs);

		// Crear una instancia de WebDriver
		webDriver = new ChromeDriver(options);
		webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		webDriver.manage().timeouts().scriptTimeout(Duration.ofSeconds(10));
	}

	public void close() {
		if (webDriver != null) {
			webDriver.quit();
		}
	}

	public void goHome() {
		webDriver.get(urlHome);
	}

	public void login() {
		// Iniciar sesión
		WebElement usernameField = webDriver.findElement(By.id("userNameBox"));
		WebElement passwordField = webDriver.findElement(By.id("passWordBox"));

		usernameField.sendKeys(loginUser);
		passwordField.sendKeys(loginPassword);
		passwordField.submit();

	}
	
	public void openUserView(UserData userData) {
		webDriver.navigate().to("https://framatome.csod.com/admin/Users.aspx?tab_page_id=-38");
		
		WebElement userIdSearchField = webDriver.findElement(By.id("userIdText"));
		userIdSearchField.clear();
		userIdSearchField.sendKeys(userData.getUserId());
		WebElement searchButton = webDriver.findElement(By.cssSelector("#btnSearchUser > .fa-icon-search"));
		searchButton.click();
		WebElement userLink = webDriver.findElement(By.cssSelector(".csod-ellipsis"));
		userLink.click();
		
		WebElement instantaneaLink = webDriver.findElement(By.linkText("Instantánea"));
		instantaneaLink.click();
		
		WebElement documentosTextElement = webDriver.findElement(By.id("y0_cf"));
		String documentosText = documentosTextElement.getText();
		log.info("Documentos: {}", documentosText);
		if (documentosText.equals("0")) {
			log.info("No hay documentos para el usuario: {}", userData);
			try {
				excelProcessorService.writeCsvRow(userData, "", 0);
			} catch (IOException e) {
				log.error("Error al escribir en CSV", e);
			}
			return;
		}
		
		documentosTextElement.click();
		
		try {
			WebElement liquidacionesLink = webDriver.findElement(By.linkText("Liquidaciones"));
			liquidacionesLink.click();
			downloadFiles(webDriver.getCurrentUrl(), userData);
		} catch (NoSuchElementException e) {
			log.warn("No hay liquidaciones para el usuario: {}", userData);
			return;
		}
	}
	
	public void downloadFiles(String url, UserData userData) {
		int liquidacionNumero = 1;
		WebElement liquidacionLink = null;
		
		do {
			try {
				webDriver.navigate().to(url);
				liquidacionLink = webDriver.findElement(By.id("ba" + (liquidacionNumero - 1) + "f_i"));
				WebElement liquidacionFecha = webDriver.findElement(By.id("ba" + (liquidacionNumero - 1) + "f_s"));
				String fecha = liquidacionFecha.getText().replaceAll("/", "-");
				log.info("Fecha liquidación: {}", fecha);
				
				liquidacionLink.click();
//				Thread.sleep(2000);
				
				WebElement botonSiguiente = webDriver.findElement(By.xpath("//a[contains(text(), 'Siguiente')]"));
				botonSiguiente.click();
				Thread.sleep(2000);
				botonSiguiente = webDriver.findElement(By.xpath("//a[contains(text(), 'Siguiente')]"));
				botonSiguiente.click();
				Thread.sleep(2000);
				
				downloadFile(userData, fecha, liquidacionNumero);
				
				liquidacionNumero++;
			} catch (Exception e) {
//				log.error("Error al descargar liquidación", e);
				liquidacionLink = null;
			}
		} while (liquidacionLink != null);
		
	}
	
	protected void downloadFile(UserData userData, String fecha, int liquidacionNumero ) throws IOException {
		String liquidacionUsuarioFolder = userData.getCarpeta() + "\\" + fecha + "_" + String.format("%02d", liquidacionNumero);
		try {
			File sourceDir = new File(downloadFolder); 
			File destDir = new File(liquidacionesFolder + liquidacionUsuarioFolder);
			sourceDir.mkdirs();
			destDir.mkdirs();
			
			WebElement imprimirBtn = webDriver.findElement(By.linkText("Imprimir en PDF"));
			imprimirBtn.click();
			
			WebElement link = null;
			int linkNumero = 1;
			
			do {
				try {
					link = webDriver.findElement(By.cssSelector(".cso-uploaded:nth-child(" + linkNumero + ") .cso-hyper-link"));
//					log.info("Downloading:{}", link.getAttribute("alt"));
					linkNumero++;
					link.click();
				} catch (Exception e) {
					link = null;
				}
			} while (link != null);
			
//			Thread.sleep(2000);
			WebElement hechoBtn =webDriver.findElement(By.linkText("Hecho"));
			hechoBtn.click();
			
			Thread.sleep(2500);
			Path destPath = destDir.toPath();
			for (File sourceFile : sourceDir.listFiles()) {
				Path sourcePath = sourceFile.toPath();
				log.info("Moviendo archivo: {}", sourcePath.getFileName());
				Files.copy(sourcePath, destPath.resolve(sourcePath.getFileName()));
				sourceFile.delete();
			}
		} catch (Exception e) {
			log.error("Error al descargar", e);
		} finally {
			File sourceDir = new File(downloadFolder);
			if(sourceDir.listFiles() != null ) {
				for (File sourceFile : sourceDir.listFiles()) {
					sourceFile.delete();
				}
			}
			
			File destDir = new File(liquidacionesFolder + liquidacionUsuarioFolder);
			
			if (destDir.listFiles() == null) {
				log.warn("No se han descargado archivos");
				excelProcessorService.writeCsvRow(userData, destDir.getName(), 0);
				return;
			}

			excelProcessorService.writeCsvRow(userData, destDir.getName(), destDir.listFiles().length);
		}
	}

}
