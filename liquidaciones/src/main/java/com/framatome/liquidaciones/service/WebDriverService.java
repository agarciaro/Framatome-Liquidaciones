package com.framatome.liquidaciones.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

	ChromeOptions options;

	WebDriver webDriver;

	public void init() {
		// Configurar WebDriverManager para manejar ChromeDriver
		WebDriverManager.chromedriver().setup();

		// Configurar opciones de Chrome
		options = new ChromeOptions();
		options.addArguments("--start-maximized");
//		options.addArguments("download.default_directory=" + downloadFolder);
//		options.addArguments("plugins.always_open_pdf_externally=true");
		
		Map<String, Object> prefs = new HashMap<String, Object>();
		prefs.put("download.default_directory", downloadFolder);
		prefs.put("plugins.always_open_pdf_externally", true);
		options.setExperimentalOption("prefs", prefs);

		// Crear una instancia de WebDriver
		webDriver = new ChromeDriver(options);
	}

	public void close() {
		if (webDriver != null) {
			webDriver.quit();
		}
	}

	public void goHome() {
		webDriver.get(urlHome);
		webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
	}

	public void login() {
		// Iniciar sesión
		WebElement usernameField = webDriver.findElement(By.id("userNameBox"));
		WebElement passwordField = webDriver.findElement(By.id("passWordBox"));

		usernameField.sendKeys(loginUser);
		passwordField.sendKeys(loginPassword);
		passwordField.submit();

		webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		WebElement loginError = webDriver.findElement(By.className("error"));
		if (loginError != null) {
			log.error("Error en login:{}", loginError.getText());
			throw new RuntimeException("Error al iniciar sesión");
		}
	}
	
	public void downloadFiles() {
		WebElement table = webDriver.findElement(By.className("field-items"));
		List<WebElement> links = new ArrayList<>(table.findElements(By.tagName("a")));
		for (WebElement row : links) {
			log.info("Row: {}", row.getText());
			row.click();
			webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
		}
		
		try {
			Thread.sleep(5000);
			LocalDate today = LocalDate.now();

			File sourceDir = new File(downloadFolder); //this directory already exists
			File destDir = new File(liquidacionesFolder + today.toString()); //this is a new directory
			destDir.mkdirs(); // make sure that the dest directory exists

			Path destPath = destDir.toPath();
			for (File sourceFile : sourceDir.listFiles()) {
			    Path sourcePath = sourceFile.toPath();
			    Files.copy(sourcePath, destPath.resolve(sourcePath.getFileName()));
			}
		} catch (InterruptedException e) {
			log.error("Error al esperar la descarga: {}", e.getMessage());
		} catch (IOException e) {
			log.error("Error al crear el directorio: {}", e.getMessage());
		}
	}

}
