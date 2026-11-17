package com.example.demo.repository;




import com.example.demo.entity.cards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository 
public interface  CardRepository extends JpaRepository<cards, Long>{

    List<cards> findByDeckIdOrderByOrderIndexAsc(Long deckId);

    long countByDeckId(Long deckId); 

    void deleteByDeckId(Long deckId); 

    boolean existsByIdAndDeck_User_Id(Long id, Long userId);
}
