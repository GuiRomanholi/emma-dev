package br.com.fiap.emma;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

//http://localhost:8081/swagger-ui/index.html
//http://localhost:8081/register
//http://localhost:8081/h2-console
//JDBC URL:	jdbc:h2:mem:testdb, User Name: sa, Password: deixar em branco
@EnableCaching
@OpenAPIDefinition(info =
@Info(title = "API EMMA", description = "API RESTful com Swagger para EMMA", version = "v1"))
@SpringBootApplication
public class EmmaApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmmaApplication.class, args);
	}

}
