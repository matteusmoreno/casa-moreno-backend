package br.com.casa_moreno.casa_moreno_backend.ai.service;

import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class GeminiService {

    private final GenerativeModel generativeModel;
    private final ResourceLoader resourceLoader;
    private final String storeContext;

    public GeminiService(GenerativeModel generativeModel, ResourceLoader resourceLoader) throws IOException {
        this.generativeModel = generativeModel;
        this.resourceLoader = resourceLoader;
        this.storeContext = loadStoreContext();
    }

    public String generateChatResponse(String userMessage) throws IOException {
        String prompt = """
            Você é um assistente virtual da loja Casa Moreno, uma loja de utilidades para casa e presentes. Responda APENAS em português do Brasil.
            Sua personalidade deve ser amigável, prestativa e um pouco informal.
            Se a pergunta for sobre a loja, use o CONTEXTO DA LOJA para responder. Se a pergunta for sobre qualquer outro assunto, recuse-se educadamente a responder.
            Não invente informações que não estão no contexto. Se a resposta não estiver no contexto, diga que não tem essa informação.
            
            CONTEXTO DA LOJA:
            %s
            
            PERGUNTA DO CLIENTE:
            %s
            """.formatted(this.storeContext, userMessage);

        return generateText("gemini-1.0-pro", prompt);
    }

    public String organizeProductDescription(String rawDescription) throws IOException {
        String prompt = """
            Você é um especialista em marketing para e-commerce. Sua tarefa é pegar uma descrição de produto "crua" e formatá-la usando tags HTML simples (como <strong>, <ul>, <li>, <p>, <br>) para torná-la atraente e fácil de ler.
            - Destaque os pontos principais com <strong>.
            - Use listas (<ul> e <li>) para especificações técnicas ou listas de itens.
            - Não use estilos complexos, apenas formatação de texto básica.
            - A resposta deve conter APENAS o HTML formatado, sem nenhuma outra palavra ou frase de introdução.
            
            A descrição é:
            %s
            """.formatted(rawDescription);

        return generateText("gemini-1.5-flash", prompt);
    }

    private String generateText(String modelName, String prompt) throws IOException {
        GenerateContentResponse response = this.generativeModel.generateContent(prompt);
        return ResponseHandler.getText(response);
    }

    private String loadStoreContext() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:prompts/store_context.txt");
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}