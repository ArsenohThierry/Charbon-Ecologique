package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.MotifSortieModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MotifSortieRepository extends JpaRepository<MotifSortieModel, Integer> {
    Optional<MotifSortieModel> findByLibelle(String libelle);

    @Query(value="SELECT * FROM motif_sortie WHERE libelle NOT IN ('Commande', 'Retour')", nativeQuery = true)
    public List<MotifSortieModel> getMotifs();
}
