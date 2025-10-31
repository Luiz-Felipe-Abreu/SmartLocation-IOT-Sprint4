package br.com.fiap.smartlocation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma detecção de moto pela visão computacional
 */
@Entity
@Table(name = "DETECCOES_MOTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeteccaoMoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DETECCAO")
    private Long idDeteccao;

    @Column(name = "ID_MOTO")
    private Long idMoto;

    @Column(name = "POSICAO_X")
    private Double posicaoX;

    @Column(name = "POSICAO_Y")
    private Double posicaoY;

    @Column(name = "CONFIANCA")
    private Double confianca;

    @Column(name = "HORARIO_REGISTRO")
    private LocalDateTime horarioRegistro;

    // Campos adicionais para integração com Python
    @Transient
    private String placaVirtual; // DET-0001, DET-0002, etc.

    @Transient
    private String status; // PENDENTE, SALVA, DESCARTADA

    @PrePersist
    public void prePersist() {
        if (this.horarioRegistro == null) {
            this.horarioRegistro = LocalDateTime.now();
        }
    }
}
