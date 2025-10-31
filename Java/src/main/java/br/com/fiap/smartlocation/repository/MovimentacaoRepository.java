package br.com.fiap.smartlocation.repository;

import br.com.fiap.smartlocation.model.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {
    
    List<Movimentacao> findByMotoId(Long motoId);
    
    void deleteByMotoId(Long motoId);
    
    List<Movimentacao> findByTipoEvento(String tipoEvento);
    
    @Query("SELECT m FROM Movimentacao m ORDER BY m.dtEvento DESC")
    List<Movimentacao> findAllOrderByDtEventoDesc();
    
    @Query("SELECT m FROM Movimentacao m WHERE m.moto.id = :motoId ORDER BY m.dtEvento DESC")
    List<Movimentacao> findByMotoIdOrderByDtEventoDesc(Long motoId);
}
