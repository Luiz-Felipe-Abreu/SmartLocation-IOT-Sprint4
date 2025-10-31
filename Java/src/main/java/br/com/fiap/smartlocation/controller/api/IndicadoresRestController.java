package br.com.fiap.smartlocation.controller.api;

import br.com.fiap.smartlocation.dto.IndicadoresDTO;
import br.com.fiap.smartlocation.repository.MotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/indicadores")
@RequiredArgsConstructor
@Slf4j
public class IndicadoresRestController {

    private final MotoRepository motoRepository;

    @GetMapping
    public IndicadoresDTO indicadores() {
        try {
            long total = motoRepository.count();
            long disponivel = motoRepository.countByStatus("DISPONIVEL");
            long emUso = motoRepository.countByStatus("EM_USO");
            long manutencao = motoRepository.countByStatus("MANUTENCAO");
            long inativa = motoRepository.countByStatus("INATIVA");

            IndicadoresDTO dto = new IndicadoresDTO();
            dto.setTotalMotos(total);
            dto.setDisponivel(disponivel);
            dto.setEmUso(emUso);
            dto.setManutencao(manutencao);
            dto.setInativa(inativa);
            dto.setAtualizadoEm(LocalDateTime.now());
            return dto;
        } catch (Exception e) {
            log.error("Falha ao calcular indicadores", e);
            // Em falha, retornar zeros para não quebrar o front
            return new IndicadoresDTO(0,0,0,0,0, LocalDateTime.now());
        }
    }
}
