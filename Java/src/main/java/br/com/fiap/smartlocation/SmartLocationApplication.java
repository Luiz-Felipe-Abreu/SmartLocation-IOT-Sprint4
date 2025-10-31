package br.com.fiap.smartlocation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Classe principal do Spring Boot - SmartLocation
 * 
 * Sistema de detecção de motos em tempo real
 * Integração: Spring Boot + Oracle + Python (YOLO)
 * 
 * @author Luiz Felipe Abreu da Conceição
 * @version 1.0.0
 */
@SpringBootApplication
public class SmartLocationApplication {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   SmartLocation - Sistema Iniciando");
        System.out.println("========================================");
        System.out.println("Integração IoT + Database + Spring Boot");
        System.out.println("FIAP - Sprint 4");
        System.out.println("========================================\n");
        
        SpringApplication.run(SmartLocationApplication.class, args);
        
        System.out.println("\n========================================");
        System.out.println("✓ Aplicação iniciada com sucesso!");
        System.out.println("✓ Acesse: http://localhost:8080");
        System.out.println("========================================\n");
    }

    /**
     * Configuração do ObjectMapper para serialização JSON
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
