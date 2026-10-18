package org.sabana.proyectofinal_bd1_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ProyectoFinalBd1BackApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProyectoFinalBd1BackApplication.class, args);
    }

}
