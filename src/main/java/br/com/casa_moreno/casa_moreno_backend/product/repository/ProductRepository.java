package br.com.casa_moreno.casa_moreno_backend.product.repository;

import br.com.casa_moreno.casa_moreno_backend.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsByProductTitle(String productTitle);

    Page<Product> findAllByProductCategory(Pageable pageable, String category);

    boolean existsByMercadoLivreId(String mercadoLivreId);

    @Query("SELECT DISTINCT p.productCategory FROM Product p ORDER BY p.productCategory ASC")
    List<String> findDistinctProductCategories();
}
