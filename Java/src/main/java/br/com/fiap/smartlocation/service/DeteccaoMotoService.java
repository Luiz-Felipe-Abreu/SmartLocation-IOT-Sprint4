package br.com.fiap.smartlocation.service;

import br.com.fiap.smartlocation.model.DeteccaoMoto;
import br.com.fiap.smartlocation.repository.DeteccaoMotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeteccaoMotoService {

    private final DeteccaoMotoRepository deteccaoRepository;

    @Transactional(readOnly = true)
    public List<DeteccaoMoto> listarTodas() {
        return deteccaoRepository.findAllOrderByHorarioDesc();
    }

    @Transactional(readOnly = true)
    public Optional<DeteccaoMoto> buscarPorId(Long id) {
        return deteccaoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<DeteccaoMoto> buscarPorMoto(Long idMoto) {
        return deteccaoRepository.findByIdMoto(idMoto);
    }

    @Transactional(readOnly = true)
    public List<DeteccaoMoto> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return deteccaoRepository.findByHorarioRegistroBetween(inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<DeteccaoMoto> buscarPorConfianca(Double minConfianca) {
        return deteccaoRepository.findByConfiancaMaiorQue(minConfianca);
    }

    @Transactional
    public DeteccaoMoto salvar(DeteccaoMoto deteccao) {
        log.info("Salvando detecção: Moto ID {}, Confiança: {}", deteccao.getIdMoto(), deteccao.getConfianca());
        if (deteccao.getHorarioRegistro() == null) {
            deteccao.setHorarioRegistro(LocalDateTime.now());
        }
        return deteccaoRepository.save(deteccao);
    }

    @Transactional
    public List<DeteccaoMoto> salvarVarias(List<DeteccaoMoto> deteccoes) {
        log.info("Salvando {} detecções", deteccoes.size());
        deteccoes.forEach(d -> {
            if (d.getHorarioRegistro() == null) {
                d.setHorarioRegistro(LocalDateTime.now());
            }
        });
        return deteccaoRepository.saveAll(deteccoes);
    }

    @Transactional
    public void deletar(Long id) {
        log.info("Deletando detecção ID: {}", id);
        deteccaoRepository.deleteById(id);
    }

    @Transactional
    public void deletarVarias(List<Long> ids) {
        log.info("Deletando {} detecções", ids.size());
        deteccaoRepository.deleteAllById(ids);
    }

    @Transactional(readOnly = true)
    public Long contarDeteccoes() {
        return deteccaoRepository.count();
    }

    @Transactional(readOnly = true)
    public Long contarDeteccoesAPartirDe(LocalDateTime data) {
        return deteccaoRepository.contarDeteccoesAPartirDe(data);
    }
}
