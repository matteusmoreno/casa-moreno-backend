package br.com.casa_moreno.casa_moreno_backend.repository;

import br.com.casa_moreno.casa_moreno_backend.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsByProductTitle(String productTitle);

    Page<Product> findAllByProductCategory(Pageable pageable, String category);

    boolean existsByMercadoLivreId(String mercadoLivreId);
}
