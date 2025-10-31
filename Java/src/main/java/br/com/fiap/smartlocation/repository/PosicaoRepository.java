package br.com.fiap.smartlocation.repository;

import br.com.fiap.smartlocation.model.Posicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PosicaoRepository extends JpaRepository<Posicao, Long> {
    
    List<Posicao> findByMotoId(Long motoId);
    
    void deleteByMotoId(Long motoId);
    
    @Query("SELECT p FROM Posicao p WHERE p.moto.id = :motoId ORDER BY p.dtPosicao DESC")
    List<Posicao> findByMotoIdOrderByDtPosicaoDesc(Long motoId);
    
    @Query("SELECT p FROM Posicao p WHERE p.moto.id = :motoId ORDER BY p.dtPosicao DESC")
    Optional<Posicao> findUltimaPosicaoByMotoId(Long motoId);
}
