package de.telran.myshop.controllers;

import de.telran.myshop.entity.Card;
import de.telran.myshop.entity.Comment;
import de.telran.myshop.entity.Product;
import de.telran.myshop.errors.CardException;
import de.telran.myshop.repository.CardsRepository;
import de.telran.myshop.repository.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class CardsController {

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private CardsRepository cardsRepository;

    // GET http://localhost:8080/cards
    @GetMapping("/cards")
    public Iterable<Card> getAllCards() {
        return cardsRepository.findAll();
    }
    
    // GET http://localhost:8080/cards/2
    @GetMapping("/cards/{id}")
    public Card getCardById(
        @PathVariable Long id
    ) {
        Card card = cardsRepository.findById(id).orElse(null);
        if(card == null) {
            throw new CardException("Card with id " + id + " not found", id);
        }
        return card;
    }



    // какие продукты есть в карте
    // GET http://localhost:8080/cards/1/products
    // getCardProducts
    @GetMapping("/cards/{id}/products")
    public Iterable<Product> getCardProducts(
        @PathVariable Long id
    ) {
        Card card = cardsRepository.findById(id).orElse(null);
        if (card == null) {
            throw new CardException("Card with id " + id + " not found", id);
        }
        return card.getProducts();
    }

    // создадим карту для продукта
    // POST http://localhost:8080/products/4/cards
    @PostMapping("/products/{productId}/cards")
    public Card addCard(
        @PathVariable Long productId,
        @RequestBody Card cardRequest
    ) {
        Product product = productsRepository.findById(productId).orElse(null);
        if (product == null) {
            throw new IllegalArgumentException("Product with id " + productId + " not found");
        }
        Long cardId = cardRequest.getId();
        if(cardId != null && cardId != 0L) {
            Card existingCard = cardsRepository.findById(cardId).orElseThrow(
                () -> new CardException("Card with id " + cardId + " not found", cardId)
            );
            product.addCard(existingCard);
            productsRepository.save(product);
            return existingCard;
        }
        product.addCard(cardRequest);
        return cardsRepository.save(cardRequest);
    }

    // изменение карты по ее идентификатору
    // PUT http://localhost:8080/cards/1
    @PutMapping("/cards/{id}")
    public Card updateCard(
        @PathVariable Long id,
        @RequestBody Card cardRequest
    ) {
        // загрузите карту по ид из базы
        Card card = cardsRepository.findById(id).orElse(null);
        if (card == null) {
            throw new CardException("Card with id " + id + " not found", id);
        }
        // замените имя в загруженной карте
        card.setName(cardRequest.getName());
        // спасите измененную карту
        return cardsRepository.save(card);
    }

    // удаление карты по идентификатору
    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Card> deleteCardById(
        @PathVariable Long id
    ) {
        Card card = cardsRepository.findById(id).orElse(null);
        if (card == null) {
            throw new CardException("Card with id " + id + " not found", id);
        }
        // удалить эту карту из всех ее продуктов
        List<Product> products = new ArrayList<>(card.getProducts());
        products.forEach(
            p -> p.removeCard(card.getId())
        );
        // продукты сохранить
        productsRepository.saveAll(products);
        // удалить карту
        cardsRepository.delete(card);
        return ResponseEntity.noContent().build();
    }

    // DELETE http://locahost:8080/products/2/cards/1
    // удаление конкретного продукта из карты
    @DeleteMapping("/products/{productId}/cards/{cardId}")
    public ResponseEntity<Product> deleteCardFromProduct(
        @PathVariable Long productId,
        @PathVariable Long cardId
    ) {
        Card card = cardsRepository.findById(cardId).orElse(null);
        if (card == null) {
            throw new CardException("Card with id " + cardId + " not found", cardId);
        }
        Product product = productsRepository.findById(productId).orElse(null);
        if (product == null) {
            throw new IllegalArgumentException("Product with id " + productId + " not found");
        }
        product.removeCard(cardId);
        productsRepository.save(product);
        return ResponseEntity.noContent().build();
    }


}
