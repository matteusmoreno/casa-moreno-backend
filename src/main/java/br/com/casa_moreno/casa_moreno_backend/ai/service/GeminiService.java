package br.com.casa_moreno.casa_moreno_backend.ai.service;

import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class GeminiService {

    @Value("${GEMINI_MODEL_NAME}")
    private String geminiModelName;

    private final GenerativeModel generativeModel;
    private final ResourceLoader resourceLoader;
    private final String storeContext;

    public GeminiService(GenerativeModel generativeModel, ResourceLoader resourceLoader) throws IOException {
        this.generativeModel = generativeModel;
        this.resourceLoader = resourceLoader;
        this.storeContext = loadStoreContext();
    }

    public String generateChatResponse(String userMessage) throws IOException {
        String prompt = String.format(
                "Você é um assistente virtual da loja Casa Moreno. Use estritamente o CONTEXTO abaixo para responder à pergunta do cliente de forma precisa. " +
                        "Se a resposta não estiver no contexto, diga que você não tem essa informação e peça para o cliente entrar em contato. " +
                        "CONTEXTO:\n---\n%s\n---\nPERGUNTA DO CLIENTE: %s",
                this.storeContext,
                userMessage
        );

        return generateText(geminiModelName, prompt);
    }

    public String organizeProductDescription(String rawDescription) throws IOException {
        String prompt = "A partir desta descrição de produto apenas formate o texto para ficar mais visível e amigável para uma pessoa ler. " +
                "Não invente nenhuma informação. Não tente deixar em negrito pois não é possível. Não utiize emojis apenas utilize as informações do texto e formate-o" +
                "A descrição é: " + rawDescription;

        return generateText(geminiModelName, prompt);
    }

    private String generateText(String modelName, String prompt) throws IOException {
        GenerateContentResponse response = this.generativeModel.generateContent(prompt);
        return ResponseHandler.getText(response);
    }

    private String loadStoreContext() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:ai/contexto-loja.txt");
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}