package br.com.fiap.smartlocation.repository;

import br.com.fiap.smartlocation.model.DeteccaoMoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeteccaoMotoRepository extends JpaRepository<DeteccaoMoto, Long> {
    
    List<DeteccaoMoto> findByIdMoto(Long idMoto);
    
    void deleteByIdMoto(Long idMoto);
    
    List<DeteccaoMoto> findByHorarioRegistroBetween(LocalDateTime inicio, LocalDateTime fim);
    
    @Query("SELECT d FROM DeteccaoMoto d ORDER BY d.horarioRegistro DESC")
    List<DeteccaoMoto> findAllOrderByHorarioDesc();
    
    @Query("SELECT d FROM DeteccaoMoto d WHERE d.confianca >= :minConfianca")
    List<DeteccaoMoto> findByConfiancaMaiorQue(Double minConfianca);
    
    @Query("SELECT COUNT(d) FROM DeteccaoMoto d WHERE d.horarioRegistro >= :data")
    Long contarDeteccoesAPartirDe(LocalDateTime data);
}
