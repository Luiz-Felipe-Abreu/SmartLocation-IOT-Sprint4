package br.com.fiap.smartlocation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa um Pátio de motos
 */
@Entity
@Table(name = "PATIO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false)
    private String endereco;

    @Column(nullable = false)
    private Integer capacidade;

    @Column(length = 20)
    private String classe; // POP, SPORT, ELETRICA
}
