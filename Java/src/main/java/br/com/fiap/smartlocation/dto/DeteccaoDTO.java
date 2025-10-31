package br.com.fiap.smartlocation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para detecção de moto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeteccaoDTO {
    
    private Long idDeteccao;
    private Long idMoto;
    private String placaVirtual;
    private Double posicaoX;
    private Double posicaoY;
    private Double confianca;
    private LocalDateTime horarioRegistro;
    private String status;
}
