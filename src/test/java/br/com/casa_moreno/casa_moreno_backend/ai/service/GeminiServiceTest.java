package br.com.casa_moreno.casa_moreno_backend.ai.service;

import com.google.cloud.vertexai.api.Candidate;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("GeminiService Tests")
@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @Mock
    private GenerativeModel generativeModel;
    @Mock
    private ResourceLoader resourceLoader;

    private GeminiService geminiService;

    private final String fakeStoreContext = "Horário de funcionamento: 9h às 18h.";

    @BeforeEach
    void setUp() throws IOException {
        Resource mockResource = new ByteArrayResource(fakeStoreContext.getBytes());
        when(resourceLoader.getResource("classpath:prompts/store_context.txt")).thenReturn(mockResource);
        geminiService = new GeminiService(generativeModel, resourceLoader);
    }

    @Test
    @DisplayName("Should build correct prompt and generate chat response")
    void shouldGenerateChatResponseSuccessfully() throws IOException {
        // Arrange
        String userMessage = "Qual o horário de vocês?";
        String expectedAiResponse = "Nosso horário de funcionamento é das 9h às 18h.";

        Part part = Part.newBuilder().setText(expectedAiResponse).build();
        Content content = Content.newBuilder().addParts(part).build();
        Candidate candidate = Candidate.newBuilder().setContent(content).build();
        GenerateContentResponse mockApiResponse = GenerateContentResponse.newBuilder()
                .addAllCandidates(List.of(candidate))
                .build();

        when(generativeModel.generateContent(anyString())).thenReturn(mockApiResponse);

        String actualResponse = geminiService.generateChatResponse(userMessage);

        assertEquals(expectedAiResponse, actualResponse);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

        verify(generativeModel).generateContent(promptCaptor.capture());

        String capturedPrompt = promptCaptor.getValue();

        assertTrue(capturedPrompt.contains("PERGUNTA DO CLIENTE:"));
        assertTrue(capturedPrompt.contains(userMessage));
        assertTrue(capturedPrompt.contains(fakeStoreContext));
    }

    @Test
    @DisplayName("Should build correct prompt and organize product description")
    void shouldOrganizeProductDescriptionSuccessfully() throws IOException {
        String rawDescription = "Produto novo, cor azul, 128gb";
        String expectedOrganizedDescription = "<p><strong>Produto novo</strong></p>";

        Part part = Part.newBuilder().setText(expectedOrganizedDescription).build();
        Content content = Content.newBuilder().addParts(part).build();
        Candidate candidate = Candidate.newBuilder().setContent(content).build();
        GenerateContentResponse mockApiResponse = GenerateContentResponse.newBuilder()
                .addAllCandidates(List.of(candidate))
                .build();

        when(generativeModel.generateContent(anyString())).thenReturn(mockApiResponse);

        String actualDescription = geminiService.organizeProductDescription(rawDescription);

        assertEquals(expectedOrganizedDescription, actualDescription);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(generativeModel).generateContent(promptCaptor.capture());

        String capturedPrompt = promptCaptor.getValue();
        assertTrue(capturedPrompt.contains("A descrição é:"));
        assertTrue(capturedPrompt.contains(rawDescription));
    }

    @Test
    @DisplayName("Should propagate IOException when AI model fails")
    void shouldPropagateIOExceptionWhenModelFails() throws IOException {
        String userMessage = "Qualquer mensagem";

        when(generativeModel.generateContent(anyString())).thenThrow(new IOException("API communication failed"));

        assertThrows(IOException.class, () -> {
            geminiService.generateChatResponse(userMessage);
        });
    }
}