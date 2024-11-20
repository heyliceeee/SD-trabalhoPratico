package org.example.logging;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Regista eventos importantes (mensagens trocadas, tentativas de autenticacao e acoes autorizadas) em um ficheiro de log
 *
 *
 * Responsabilidades:
 * - Registar Mensagens: Método para registar mensagens trocadas entre clientes.
 * - Registar Autenticação: Método para registar tentativas de login bem-sucedidas e falhas.
 * - Registar Ações Autorizadas e Não Autorizadas: Método para registar quando uma ação é executada ou negada devido à falta de permissão.
 * - Estrutura de Log: Cada entrada de log contém o timestamp, o tipo de evento, e a mensagem associada.
 */
public class Logger {
    private static final String LOG_FILE = "D:\\githubProjects\\SD-trabalhoPratico\\src\\main\\java\\files\\system.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_RECENT_OPERATIONS = 10;
    private static final Queue<String> recentOperations = new LinkedList<>();

    private static void logEvent(String eventType, String message) {
        String logEntry = String.format("[%s] %s: %s", LocalDateTime.now().format(formatter), eventType, message);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(logEntry);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Erro ao escrever no log: " + e.getMessage());
        }
        synchronized (recentOperations) {
            if (recentOperations.size() >= MAX_RECENT_OPERATIONS) {
                recentOperations.poll();
            }
            recentOperations.add(logEntry);
        }
    }

    public static void logMessage(String fromUser, String toUser, String message) {
        logEvent("MESSAGE", "Mensagem de " + fromUser + " para " + toUser + ": " + message);
    }

    // Método para registrar ações com permissões
    public static void logAction(String user, String action, boolean authorized) {
        String logMessage = String.format("Ação %s para o utilizador %s: %s", action, user, authorized ? "AUTORIZADA" : "NÃO AUTORIZADA");
        logEvent("ACTION", logMessage);
    }

    // Método para registrar tentativas de autenticação
    public static void logAuthentication(String email, boolean success) {
        String logMessage = String.format("Tentativa de login %s para o email: %s", success ? "bem-sucedida" : "falhou", email);
        logEvent("AUTHENTICATION", logMessage);
    }

    public static void logActiveUsers(List<String> activeUsers) {
        String activeUsersList = String.join(", ", activeUsers);
        String logMessage = String.format("%s", activeUsersList);
        logEvent("ACTIVE_USERS", logMessage);
    }

    public static String getRecentOperations() {
        StringBuilder operations = new StringBuilder();
        synchronized (recentOperations) {
            for (String operation : recentOperations) {
                operations.append(operation).append("\n");
            }
        }
        return operations.toString();
    }
}
