package br.com.casa_moreno.casa_moreno_backend.product.repository;

import br.com.casa_moreno.casa_moreno_backend.product.domain.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ProductRepository Tests")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Cenário 1: Deve retornar categorias distintas em ordem alfabética")
    void shouldReturnDistinctProductCategoriesInAlphabeticalOrder() {
        entityManager.persist(Product.builder().productCategory("Eletrônicos").build());
        entityManager.persist(Product.builder().productCategory("Smartphones").build());
        entityManager.persist(Product.builder().productCategory("Eletrônicos").build()); // Duplicado
        entityManager.persist(Product.builder().productCategory("Acessórios").build());

        List<String> categories = productRepository.findDistinctProductCategories();

        assertThat(categories)
                .isNotNull()
                .hasSize(3)
                .containsExactly("Acessórios", "Eletrônicos", "Smartphones");
    }

    @Test
    @DisplayName("Cenário 2: Deve retornar uma lista vazia quando não há produtos")
    void shouldReturnEmptyListWhenNoProductsExist() {
        List<String> categories = productRepository.findDistinctProductCategories();

        assertThat(categories).isNotNull();
        assertThat(categories).isEmpty();
    }
}