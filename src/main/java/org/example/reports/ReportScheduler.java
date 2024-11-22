package org.example.reports;


import org.example.communication.MessageService;
import org.example.logging.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Uma thread para gerir e enviar relatorios automaticamente em intervalos definidos periodicos sobre o estado do sistema e as operacoes realizadas
 *
 *
 * Responsabilidades:
 * - Gerar Relatórios Periódicos: Criar relatórios em intervalos regulares.
 * - Enviar Relatórios aos Clientes: Enviar o conteúdo do relatório para todos os clientes ativos.
 * - Registar os Relatórios: Opcionalmente, guardar os relatórios em um ficheiro para consulta futura.
 */
public class ReportScheduler extends Thread {
    private final long interval;
    private final MessageService messageService;
    private static final String REPORT_FILE = "C:\\Users\\andre\\OneDrive\\Ambiente de Trabalho\\es2\\SD-trabalhoPratico\\src\\main\\java\\files\\system_reports.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReportScheduler(long interval, MessageService messageService) {
        this.interval = interval;
        this.messageService = messageService;
    }

    @Override
    public void run() {
        while (true) {
            generateAndSendReport();
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateAndSendReport() {
        String report = generateReport();
        messageService.broadcastMessage(report);
        saveReportToFile(report);
    }

    private String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("Relatório do Sistema - ").append(LocalDateTime.now().format(formatter)).append("\n");
        report.append("Operações Recentes:\n");
        report.append(Logger.getRecentOperations());
        report.append("\n---------------------------\n");
        return report.toString();
    }

    private void saveReportToFile(String report) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(REPORT_FILE, true))) {
            writer.write(report);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Erro ao escrever no relatório: " + e.getMessage());
        }
    }
}
