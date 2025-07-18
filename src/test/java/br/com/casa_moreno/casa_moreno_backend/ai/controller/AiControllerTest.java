package br.com.casa_moreno.casa_moreno_backend.ai.controller;

import br.com.casa_moreno.casa_moreno_backend.ai.dto.ChatRequest;
import br.com.casa_moreno.casa_moreno_backend.ai.dto.DescriptionRequest;
import br.com.casa_moreno.casa_moreno_backend.ai.service.GeminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AI Controller Tests")
@ExtendWith(MockitoExtension.class)
class AiControllerTest {

    @Mock
    private GeminiService geminiService;

    @InjectMocks
    private AiController aiController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiController).build();
    }

    @Test
    @DisplayName("Should return chat response successfully")
    void shouldReturnChatResponseSuccessfully() throws Exception {
        ChatRequest request = new ChatRequest("Qual o seu horário de funcionamento?");
        String expectedResponse = "Nosso horário é das 9h às 18h.";

        when(geminiService.generateChatResponse(request.message())).thenReturn(expectedResponse);

        mockMvc.perform(
                        post("/ai/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));

        verify(geminiService, times(1)).generateChatResponse(request.message());
    }

    @Test
    @DisplayName("Should return 500 when chat service throws IOException")
    void shouldReturn500WhenChatServiceFails() throws Exception {
        ChatRequest request = new ChatRequest("Uma pergunta qualquer");
        String errorMessage = "Erro ao comunicar com a IA.";

        when(geminiService.generateChatResponse(request.message()))
                .thenThrow(new IOException("Falha na API da IA"));

        mockMvc.perform(
                        post("/ai/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(errorMessage));

        verify(geminiService, times(1)).generateChatResponse(request.message());
    }

    @Test
    @DisplayName("Should return organized description successfully")
    void shouldReturnOrganizedDescriptionSuccessfully() throws Exception {
        DescriptionRequest request = new DescriptionRequest("Produto novo, azul, 128gb");
        String expectedResponse = "<h2>Descrição</h2><p>Produto novo</p><ul><li>Cor: azul</li><li>Capacidade: 128gb</li></ul>";

        when(geminiService.organizeProductDescription(request.description())).thenReturn(expectedResponse);

        mockMvc.perform(
                        post("/ai/organize-description")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));

        verify(geminiService, times(1)).organizeProductDescription(request.description());
    }

    @Test
    @DisplayName("Should return 500 when organize description service throws IOException")
    void shouldReturn500WhenOrganizeDescriptionFails() throws Exception {
        DescriptionRequest request = new DescriptionRequest("Uma descrição qualquer");
        String errorMessage = "Erro ao comunicar com a IA.";

        when(geminiService.organizeProductDescription(request.description()))
                .thenThrow(new IOException("Falha na API da IA"));

        mockMvc.perform(
                        post("/ai/organize-description")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(errorMessage));

        verify(geminiService, times(1)).organizeProductDescription(request.description());
    }
}