package br.com.fiap.smartlocation.repository;

import br.com.fiap.smartlocation.model.Patio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatioRepository extends JpaRepository<Patio, Long> {
    
    List<Patio> findByClasse(String classe);
    
    List<Patio> findByNomeContainingIgnoreCase(String nome);
}
