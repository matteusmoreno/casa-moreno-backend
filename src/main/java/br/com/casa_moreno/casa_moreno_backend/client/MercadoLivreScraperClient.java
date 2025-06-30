package br.com.casa_moreno.casa_moreno_backend.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "mercado-livre-scraper", url = "${scraper.client.url}")
public interface MercadoLivreScraperClient {

    @GetMapping("/mercado-livre/product-info")
    @CircuitBreaker(name = "mercadoLivreScraper")
    MercadoLivreScraperResponse getProductInfo(@RequestParam("url") String url);

    @GetMapping("/mercado-livre/sync/stream")
    @CircuitBreaker(name = "mercadoLivreScraper")
    ResponseEntity<String> startFullSync();
}
