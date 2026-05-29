package com.ingenieria.taskflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Clase principal de la aplicacion TaskFlow CM.
 * <p>
 * Punto de entrada de Spring Boot. Inicializa el contexto de la aplicacion,
 * configura JPA/Hibernate contra PostgreSQL (Neon) y levanta el servidor embebido Tomcat.
 * Extiende {@code SpringBootServletInitializer} para soporte de despliegue en WAR externo.
 * </p>
 *
 * @author TaskFlow CM
 * @version 1.0
 */
@SpringBootApplication
public class TaskflowApplication extends SpringBootServletInitializer {

	/**
	 * Configuracion para despliegue en contenedor de servlets externo (WAR).
	 *
	 * @param builder constructor de la aplicacion Spring Boot
	 * @return el builder configurado con la clase principal
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(TaskflowApplication.class);
	}

	/**
	 * Metodo principal que inicia la aplicacion Spring Boot.
	 *
	 * @param args argumentos de linea de comandos (no requeridos)
	 */
	public static void main(String[] args) {
		SpringApplication.run(TaskflowApplication.class, args);
	}
}
