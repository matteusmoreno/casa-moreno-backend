package br.com.casa_moreno.casa_moreno_backend.ai.controller;

import br.com.casa_moreno.casa_moreno_backend.ai.dto.ChatRequest;
import br.com.casa_moreno.casa_moreno_backend.ai.dto.DescriptionRequest;
import br.com.casa_moreno.casa_moreno_backend.ai.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final GeminiService geminiService;

    public AiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        try {
            String response = geminiService.generateChatResponse(request.message());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro ao comunicar com a IA.");
        }
    }

    @PostMapping("/organize-description")
    public ResponseEntity<String> organizeDescription(@RequestBody DescriptionRequest request) {
        try {
            String response = geminiService.organizeProductDescription(request.description());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro ao comunicar com a IA.");
        }
    }
}