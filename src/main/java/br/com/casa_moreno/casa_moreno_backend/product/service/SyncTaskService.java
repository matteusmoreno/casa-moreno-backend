package br.com.casa_moreno.casa_moreno_backend.product.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class SyncTaskService {

    private final Map<String, CompletableFuture<String>> runningTasks = new ConcurrentHashMap<>();

    // Inicia uma nova tarefa e retorna seu ID
    public String submitTask(Supplier<CompletableFuture<String>> taskSupplier) {
        String taskId = UUID.randomUUID().toString();
        CompletableFuture<String> taskFuture = taskSupplier.get();
        runningTasks.put(taskId, taskFuture);

        // Limpa a tarefa do mapa quando ela for concluída (para não consumir memória)
        taskFuture.whenComplete((result, throwable) -> {
            // Pode-se adicionar um pequeno delay aqui se quiser manter o resultado por mais tempo
            // Por enquanto, vamos manter simples. Em um cenário real, o resultado seria persistido.
        });

        return taskId;
    }

    // Retorna o status e o resultado de uma tarefa
    public TaskResult getTaskResult(String taskId) {
        CompletableFuture<String> taskFuture = runningTasks.get(taskId);

        if (taskFuture == null) {
            return new TaskResult("NOT_FOUND", null, "Tarefa não encontrada ou já expirou.");
        }

        if (taskFuture.isDone()) {
            try {
                String report = taskFuture.get(); // Pega o resultado
                runningTasks.remove(taskId); // Remove após a consulta
                return new TaskResult("COMPLETED", report, null);
            } catch (Exception e) {
                runningTasks.remove(taskId); // Remove em caso de erro
                return new TaskResult("FAILED", null, e.getCause().getMessage());
            }
        } else {
            return new TaskResult("RUNNING", null, null);
        }
    }

    // DTO simples para encapsular a resposta do status
    public record TaskResult(String status, String report, String error) {}
}