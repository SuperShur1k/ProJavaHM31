package de.telran.myshop.repository;

import de.telran.myshop.entity.Card;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardsRepository extends CrudRepository<Card, Long> {
}
