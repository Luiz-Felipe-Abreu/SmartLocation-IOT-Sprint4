package br.com.fiap.smartlocation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma movimentação de moto
 */
@Entity
@Table(name = "MOVIMENTACAO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "moto_id", nullable = false)
    private Moto moto;

    @Column(name = "tipo_evento", length = 20)
    private String tipoEvento; // ENTRADA, SAIDA, MOVIMENTACAO

    @Column(length = 255)
    private String descricao;

    @Column(name = "dt_evento")
    private LocalDateTime dtEvento;

    @PrePersist
    public void prePersist() {
        if (this.dtEvento == null) {
            this.dtEvento = LocalDateTime.now();
        }
    }
}
