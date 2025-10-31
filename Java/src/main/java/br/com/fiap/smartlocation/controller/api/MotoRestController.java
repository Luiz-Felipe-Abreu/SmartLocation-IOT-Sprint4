package br.com.fiap.smartlocation.controller.api;

import br.com.fiap.smartlocation.model.Moto;
import br.com.fiap.smartlocation.service.MotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gerenciamento de motos
 */
@RestController
@RequestMapping("/api/motos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MotoRestController {

    private final MotoService motoService;

    @GetMapping
    public ResponseEntity<List<Moto>> listarTodas() {
        log.info("GET /api/motos - Listando todas as motos");
        return ResponseEntity.ok(motoService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Moto> buscarPorId(@PathVariable Long id) {
        log.info("GET /api/motos/{} - Buscando moto por ID", id);
        return motoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/placa/{placa}")
    public ResponseEntity<Moto> buscarPorPlaca(@PathVariable String placa) {
        log.info("GET /api/motos/placa/{} - Buscando moto por placa", placa);
        return motoService.buscarPorPlaca(placa)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Moto>> buscarPorStatus(@PathVariable String status) {
        log.info("GET /api/motos/status/{} - Buscando motos por status", status);
        return ResponseEntity.ok(motoService.buscarPorStatus(status));
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<Moto>> listarDisponiveis() {
        log.info("GET /api/motos/disponiveis - Listando motos disponíveis");
        return ResponseEntity.ok(motoService.listarDisponiveis());
    }

    @PostMapping
    public ResponseEntity<Moto> criar(@RequestBody Moto moto) {
        log.info("POST /api/motos - Criando nova moto: {}", moto.getPlaca());
        Moto motoCriada = motoService.salvar(moto);
        return ResponseEntity.status(HttpStatus.CREATED).body(motoCriada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Moto> atualizar(@PathVariable Long id, @RequestBody Moto moto) {
        log.info("PUT /api/motos/{} - Atualizando moto", id);
        try {
            Moto motoAtualizada = motoService.atualizar(id, moto);
            return ResponseEntity.ok(motoAtualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Moto> atualizarStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("PATCH /api/motos/{}/status - Atualizando status para: {}", id, status);
        try {
            Moto motoAtualizada = motoService.atualizarStatus(id, status);
            return ResponseEntity.ok(motoAtualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        log.info("DELETE /api/motos/{} - Deletando moto", id);
        try {
            motoService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
