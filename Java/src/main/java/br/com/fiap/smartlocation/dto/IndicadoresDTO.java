package br.com.fiap.smartlocation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicadoresDTO {
    private long totalMotos;
    private long disponivel;
    private long emUso;
    private long manutencao;
    private long inativa;

    public long getAlertasAtivos() {
        // Regra simples: manutenção + inativa compõem alertas
        return manutencao + inativa;
    }

    private LocalDateTime atualizadoEm;
}
