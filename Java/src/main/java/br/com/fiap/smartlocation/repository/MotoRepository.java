package br.com.fiap.smartlocation.repository;

import br.com.fiap.smartlocation.model.Moto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MotoRepository extends JpaRepository<Moto, Long> {
    
    Optional<Moto> findByPlaca(String placa);
    
    List<Moto> findByStatus(String status);
    
    long countByStatus(String status);
    
    List<Moto> findByPatioId(Long patioId);
    
    @Query("SELECT m FROM Moto m WHERE m.status = 'DISPONIVEL'")
    List<Moto> findMotosDisponiveis();
    
    @Query("SELECT m FROM Moto m WHERE m.placa LIKE 'DET-%'")
    List<Moto> findMotosComPlacaVirtual();
}
