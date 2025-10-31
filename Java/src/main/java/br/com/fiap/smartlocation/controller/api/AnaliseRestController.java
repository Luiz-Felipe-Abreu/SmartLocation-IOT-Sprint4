package br.com.fiap.smartlocation.controller.api;

import br.com.fiap.smartlocation.dto.AnaliseResultadoDTO;
import br.com.fiap.smartlocation.dto.DeteccaoDTO;
import br.com.fiap.smartlocation.service.PythonIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Controller REST para integração com Python
 */
@RestController
@RequestMapping("/api/analise")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnaliseRestController {

    private final PythonIntegrationService pythonService;

    @PostMapping("/iniciar")
    public ResponseEntity<String> iniciarAnalise() {
        log.info("POST /api/analise/iniciar - Iniciando análise Python");
        
        if (pythonService.isAnaliseEmExecucao()) {
            return ResponseEntity.badRequest().body("Já existe uma análise em execução");
        }

        CompletableFuture<AnaliseResultadoDTO> futureResultado = pythonService.executarAnalise();
        
        return ResponseEntity.accepted().body("Análise iniciada com sucesso");
    }

    @GetMapping("/status")
    public ResponseEntity<String> verificarStatus() {
        log.info("GET /api/analise/status - Verificando status da análise");
        
        if (pythonService.isAnaliseEmExecucao()) {
            return ResponseEntity.ok("EM_EXECUCAO");
        } else {
            return ResponseEntity.ok("CONCLUIDA");
        }
    }

    @PostMapping("/cancelar")
    public ResponseEntity<String> cancelarAnalise() {
        log.info("POST /api/analise/cancelar - Cancelando análise");
        pythonService.cancelarAnalise();
        return ResponseEntity.ok("Análise cancelada");
    }

    @GetMapping("/deteccoes-pendentes")
    public ResponseEntity<List<DeteccaoDTO>> carregarDeteccoesPendentes() {
        log.info("GET /api/analise/deteccoes-pendentes - Carregando detecções pendentes");
        
        try {
            List<DeteccaoDTO> deteccoes = pythonService.carregarDeteccoesPendentes();
            return ResponseEntity.ok(deteccoes);
        } catch (Exception e) {
            log.error("Erro ao carregar detecções pendentes", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
