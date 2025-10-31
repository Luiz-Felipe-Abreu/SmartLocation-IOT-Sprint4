package br.com.fiap.smartlocation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para resultado da análise Python
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnaliseResultadoDTO {
    
    private String status; // SUCESSO, ERRO, PROCESSANDO
    private String mensagem;
    private String caminhoVideo;
    private String caminhoGrafico;
    private String caminhoLog;
    private Integer totalDeteccoes;
    private LocalDateTime dataAnalise;
}
