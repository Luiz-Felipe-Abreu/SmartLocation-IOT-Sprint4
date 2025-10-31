package br.com.fiap.smartlocation.controller.api;

import br.com.fiap.smartlocation.model.DeteccaoMoto;
import br.com.fiap.smartlocation.service.DeteccaoMotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller REST para gerenciamento de detecções
 */
@RestController
@RequestMapping("/api/deteccoes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DeteccaoRestController {

    private final DeteccaoMotoService deteccaoService;

    @GetMapping
    public ResponseEntity<List<DeteccaoMoto>> listarTodas() {
        log.info("GET /api/deteccoes - Listando todas as detecções");
        return ResponseEntity.ok(deteccaoService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeteccaoMoto> buscarPorId(@PathVariable Long id) {
        log.info("GET /api/deteccoes/{} - Buscando detecção por ID", id);
        return deteccaoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/moto/{idMoto}")
    public ResponseEntity<List<DeteccaoMoto>> buscarPorMoto(@PathVariable Long idMoto) {
        log.info("GET /api/deteccoes/moto/{} - Buscando detecções por moto", idMoto);
        return ResponseEntity.ok(deteccaoService.buscarPorMoto(idMoto));
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<DeteccaoMoto>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        log.info("GET /api/deteccoes/periodo - Buscando detecções entre {} e {}", inicio, fim);
        return ResponseEntity.ok(deteccaoService.buscarPorPeriodo(inicio, fim));
    }

    @GetMapping("/confianca")
    public ResponseEntity<List<DeteccaoMoto>> buscarPorConfianca(@RequestParam Double minConfianca) {
        log.info("GET /api/deteccoes/confianca - Buscando detecções com confiança >= {}", minConfianca);
        return ResponseEntity.ok(deteccaoService.buscarPorConfianca(minConfianca));
    }

    @PostMapping
    public ResponseEntity<DeteccaoMoto> criar(@RequestBody DeteccaoMoto deteccao) {
        log.info("POST /api/deteccoes - Criando nova detecção");
        DeteccaoMoto deteccaoCriada = deteccaoService.salvar(deteccao);
        return ResponseEntity.status(HttpStatus.CREATED).body(deteccaoCriada);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<DeteccaoMoto>> criarVarias(@RequestBody List<DeteccaoMoto> deteccoes) {
        log.info("POST /api/deteccoes/batch - Criando {} detecções", deteccoes.size());
        List<DeteccaoMoto> deteccoesCriadas = deteccaoService.salvarVarias(deteccoes);
        return ResponseEntity.status(HttpStatus.CREATED).body(deteccoesCriadas);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        log.info("DELETE /api/deteccoes/{} - Deletando detecção", id);
        try {
            deteccaoService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deletarVarias(@RequestBody List<Long> ids) {
        log.info("DELETE /api/deteccoes/batch - Deletando {} detecções", ids.size());
        try {
            deteccaoService.deletarVarias(ids);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> contar() {
        log.info("GET /api/deteccoes/count - Contando detecções");
        return ResponseEntity.ok(deteccaoService.contarDeteccoes());
    }

    @GetMapping("/count/desde")
    public ResponseEntity<Long> contarDesde(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime data) {
        log.info("GET /api/deteccoes/count/desde - Contando detecções desde {}", data);
        return ResponseEntity.ok(deteccaoService.contarDeteccoesAPartirDe(data));
    }
}
