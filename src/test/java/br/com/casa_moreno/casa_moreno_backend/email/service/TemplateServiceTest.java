package br.com.casa_moreno.casa_moreno_backend.email.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TemplateService Tests")
@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private TemplateService templateService;

    @Test
    @DisplayName("Should process template with correct data and return the resulting HTML")
    void shouldProcessTemplateWithCorrectDataAndPath() {
        String templateName = "welcome-email.html";
        Map<String, Object> variables = Map.of(
                "userName", "Matteus",
                "confirmationLink", "https://example.com/confirm?token=123"
        );
        String expectedHtml = "<h1>Welcome, Matteus!</h1><p>Click here: https://example.com/confirm?token=123</p>";

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);

        when(templateEngine.process(any(String.class), any(Context.class))).thenReturn(expectedHtml);

        String actualHtml = templateService.processTemplate(templateName, variables);


        assertEquals(expectedHtml, actualHtml, "The returned HTML should match the one from the template engine.");

        verify(templateEngine, times(1)).process(pathCaptor.capture(), contextCaptor.capture());

        String capturedPath = pathCaptor.getValue();
        Context capturedContext = contextCaptor.getValue();

        assertEquals("email/" + templateName, capturedPath, "The template path should have the 'email/' prefix.");
        assertEquals("Matteus", capturedContext.getVariable("userName"), "The 'userName' variable in the context should be correct.");
        assertEquals("https://example.com/confirm?token=123", capturedContext.getVariable("confirmationLink"), "The confirmation link variable should be correct.");
    }
}