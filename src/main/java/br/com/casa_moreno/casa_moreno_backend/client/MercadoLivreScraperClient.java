package br.com.casa_moreno.casa_moreno_backend.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "mercado-livre-scraper", url = "${scraper.client.url}")
public interface MercadoLivreScraperClient {

    @PostMapping("/scrape")
    @CircuitBreaker(name = "mercadoLivreScraper")
    MercadoLivreScraperResponse scrapeProducts(MercadoLivreScraperRequest request);

    @PostMapping("/sync/all")
    String syncProducts();
}
