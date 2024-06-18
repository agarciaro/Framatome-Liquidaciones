package com.framatome.liquidaciones.service;

import java.time.Duration;

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

	ChromeOptions options;

	WebDriver webDriver;

	public void init() {
		// Configurar WebDriverManager para manejar ChromeDriver
		WebDriverManager.chromedriver().setup();

		// Configurar opciones de Chrome
		options = new ChromeOptions();
		options.addArguments("--start-maximized");
		options.addArguments("download.default_directory=/path/to/descargas"); // Cambiar el directorio de descargas

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

}
