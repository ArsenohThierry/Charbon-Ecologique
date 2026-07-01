package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.MouvementSortieDetailModel;
import com.example.charbonecolo.model.MouvementStockModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MouvementSortieDetailRepository extends JpaRepository<MouvementSortieDetailModel, Integer> {

    List<MouvementSortieDetailModel> findByMouvementSortie(MouvementStockModel mouvementSortie);

    void deleteByMouvementSortie(MouvementStockModel mouvementSortie);
}
