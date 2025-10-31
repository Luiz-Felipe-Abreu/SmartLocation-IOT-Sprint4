package br.com.fiap.smartlocation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma Moto
 */
@Entity
@Table(name = "MOTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Moto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    @Column(nullable = false, length = 50)
    private String modelo;

    @Column(length = 20)
    private String status; // DISPONIVEL, EM_USO, MANUTENCAO, INATIVA

    @ManyToOne
    @JoinColumn(name = "patio_id")
    private Patio patio;

    @Column(name = "ultima_atualizacao")
    private LocalDateTime ultimaAtualizacao;

    @PrePersist
    @PreUpdate
    public void prePersist() {
        this.ultimaAtualizacao = LocalDateTime.now();
    }
}
