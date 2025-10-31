package br.com.fiap.smartlocation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa a posição de uma moto
 */
@Entity
@Table(name = "POSICAO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Posicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "moto_id")
    private Moto moto;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "dt_posicao")
    private LocalDateTime dtPosicao;

    @PrePersist
    public void prePersist() {
        if (this.dtPosicao == null) {
            this.dtPosicao = LocalDateTime.now();
        }
    }
}
