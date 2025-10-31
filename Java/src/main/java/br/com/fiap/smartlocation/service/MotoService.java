package br.com.fiap.smartlocation.service;

import br.com.fiap.smartlocation.model.Moto;
import br.com.fiap.smartlocation.repository.DeteccaoMotoRepository;
import br.com.fiap.smartlocation.repository.MovimentacaoRepository;
import br.com.fiap.smartlocation.repository.MotoRepository;
import br.com.fiap.smartlocation.repository.PosicaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@RequiredArgsConstructor
@Slf4j
public class MotoService {

    private final MotoRepository motoRepository;
    private final DeteccaoMotoRepository deteccaoMotoRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final PosicaoRepository posicaoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<Moto> listarTodas() {
        return motoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Moto> buscarPorId(Long id) {
        return motoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Moto> buscarPorPlaca(String placa) {
        return motoRepository.findByPlaca(placa);
    }

    @Transactional(readOnly = true)
    public List<Moto> buscarPorStatus(String status) {
        return motoRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Moto> listarDisponiveis() {
        return motoRepository.findMotosDisponiveis();
    }

    @Transactional
    public Moto salvar(Moto moto) {
        log.info("Salvando moto: {}", moto.getPlaca());
        moto.setUltimaAtualizacao(LocalDateTime.now());
        return motoRepository.save(moto);
    }

    @Transactional
    public Moto atualizar(Long id, Moto motoAtualizada) {
        return motoRepository.findById(id)
                .map(moto -> {
                    moto.setPlaca(motoAtualizada.getPlaca());
                    moto.setModelo(motoAtualizada.getModelo());
                    moto.setStatus(motoAtualizada.getStatus());
                    moto.setPatio(motoAtualizada.getPatio());
                    moto.setUltimaAtualizacao(LocalDateTime.now());
                    log.info("Atualizando moto ID: {}", id);
                    return motoRepository.save(moto);
                })
                .orElseThrow(() -> new RuntimeException("Moto não encontrada com ID: " + id));
    }

    @Transactional
    public void deletar(Long id) {
        log.info("Deletando moto ID: {}", id);
        // Remover registros dependentes para evitar ORA-02292
        try {
        // 0. Tabelas não mapeadas como entidades (FKs no script SQL): SENSOR e MANUTENCAO
        int delSensor = entityManager.createNativeQuery("DELETE FROM SENSOR WHERE moto_id = :id")
            .setParameter("id", id)
            .executeUpdate();
        if (delSensor > 0) log.info("Removidos {} sensores vinculados à moto {}", delSensor, id);
        entityManager.flush();

        int delManut = entityManager.createNativeQuery("DELETE FROM MANUTENCAO WHERE moto_id = :id")
            .setParameter("id", id)
            .executeUpdate();
        if (delManut > 0) log.info("Removidas {} manutenções vinculadas à moto {}", delManut, id);
        entityManager.flush();

            int qtdDeteccoes = deteccaoMotoRepository.findByIdMoto(id).size();
            int qtdMovs = movimentacaoRepository.findByMotoId(id).size();
            int qtdPos = posicaoRepository.findByMotoId(id).size();

            if (qtdDeteccoes > 0) {
                log.info("Removendo {} detecções relacionadas à moto {}", qtdDeteccoes, id);
                deteccaoMotoRepository.deleteByIdMoto(id);
                entityManager.flush();
            }
            if (qtdMovs > 0) {
                log.info("Removendo {} movimentações relacionadas à moto {}", qtdMovs, id);
                movimentacaoRepository.deleteByMotoId(id);
                entityManager.flush();
            }
            if (qtdPos > 0) {
                log.info("Removendo {} posições relacionadas à moto {}", qtdPos, id);
                posicaoRepository.deleteByMotoId(id);
                entityManager.flush();
            }

            motoRepository.deleteById(id);
            entityManager.flush();
        } catch (Exception e) {
            log.error("Falha ao deletar moto {} com limpeza de dependências", id, e);
            throw e;
        }
    }

    @Transactional
    public Moto atualizarStatus(Long id, String novoStatus) {
        return motoRepository.findById(id)
                .map(moto -> {
                    moto.setStatus(novoStatus);
                    moto.setUltimaAtualizacao(LocalDateTime.now());
                    log.info("Atualizando status da moto ID {} para: {}", id, novoStatus);
                    return motoRepository.save(moto);
                })
                .orElseThrow(() -> new RuntimeException("Moto não encontrada com ID: " + id));
    }
}
