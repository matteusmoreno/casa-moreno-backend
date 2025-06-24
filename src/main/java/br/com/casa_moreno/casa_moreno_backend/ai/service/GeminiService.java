package br.com.casa_moreno.casa_moreno_backend.ai.service;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class GeminiService {

    // Adicionamos um campo para guardar o contexto da loja em memória.
    private String storeContext;

    public GeminiService() {
        // Carrega o contexto do arquivo quando o serviço é inicializado.
        try {
            this.storeContext = loadStoreContext();
        } catch (IOException e) {
            this.storeContext = "Erro ao carregar contexto da loja.";
            e.printStackTrace();
        }
    }

    public String generateChatResponse(String userMessage) throws IOException {
        String modelName = "gemini-2.0-flash-lite-001";

        // --- PROMPT ATUALIZADO COM O CONTEXTO DA LOJA ---
        String prompt = String.format(
                "Você é um assistente virtual da loja Casa Moreno. Use estritamente o CONTEXTO abaixo para responder à pergunta do cliente de forma precisa. " +
                        "Se a resposta não estiver no contexto, diga que você não tem essa informação e peça para o cliente entrar em contato. " +
                        "CONTEXTO:\n---\n%s\n---\nPERGUNTA DO CLIENTE: %s",
                this.storeContext,
                userMessage
        );

        return generateText(modelName, prompt);
    }

    public String organizeProductDescription(String rawDescription) throws IOException {
        // Este método não precisa do contexto da loja, então permanece o mesmo.
        String modelName = "gemini-2.0-flash-lite-001";
        String prompt = "A partir desta descrição de produto apenas formate o texto para ficar mais visível e amigável para uma pessoa ler. " +
                "Não invente nenhuma informação. Não tente deixar em negrito pois não é possível. Não utiize emojis apenas utilize as informações do texto e formate-o" +
                "A descrição é: " + rawDescription;

        return generateText(modelName, prompt);
    }

    /**
     * Carrega o conteúdo do arquivo de contexto do classpath.
     */
    private String loadStoreContext() throws IOException {
        ClassPathResource resource = new ClassPathResource("ai/contexto-loja.txt");
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private String generateText(String modelName, String prompt) throws IOException {
        try (VertexAI vertexAi = new VertexAI("casa-moreno-project-463821", "us-east5")) {
            GenerativeModel model = new GenerativeModel(modelName, vertexAi);
            GenerateContentResponse response = model.generateContent(prompt);
            return ResponseHandler.getText(response);
        }
    }
}