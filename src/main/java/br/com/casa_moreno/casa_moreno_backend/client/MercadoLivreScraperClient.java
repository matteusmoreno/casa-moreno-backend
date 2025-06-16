package br.com.casa_moreno.casa_moreno_backend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "mercado-livre-scraper", url = "http://localhost:8080/scrape")
public interface MercadoLivreScraperClient {

    @PostMapping
    MercadoLivreScraperResponse scrapeProducts(MercadoLivreScraperRequest request);
}
