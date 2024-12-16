package org.example;

import org.example.authentication.EncryptionUtil;
import org.example.authentication.UserManager;
import org.example.communication.AlertService;
import org.example.communication.ChatServer;
import org.example.communication.GroupService;
import org.example.communication.MessageService;
import org.example.reports.ReportScheduler;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            // Gera as chaves RSA, apenas se ainda não existirem
            if (!new File("public.key").exists() || !new File("private.key").exists()) {
                EncryptionUtil.generateKeys();
                System.out.println("Chaves RSA geradas.");
            } else {
                System.out.println("Chaves RSA já existentes.");
            }

            // Inicializa o UserManager, MessageService e GroupService
            UserManager userManager = new UserManager();
            MessageService messageService = new MessageService(userManager);
            GroupService groupService = new GroupService(messageService);

            // Inicia o servidor de chat em uma nova thread
            new Thread(() -> {
                try {
                    ChatServer chatServer = new ChatServer(12345);  // Define a porta do servidor
                    chatServer.start();

                    // Carrega grupos de utilizadores após o servidor iniciar
                    groupService.loadUserGroups(); // Chamada após o servidor estar em execução
                } catch (Exception e) {
                    System.err.println("Erro ao iniciar o servidor de chat: " + e.getMessage());
                }
            }).start();

            // Inicia o agendador de relatórios com intervalo de 5 segundos (5000 milissegundos)
            ReportScheduler reportScheduler = new ReportScheduler(5000, messageService);
            reportScheduler.start();

            System.out.println("Servidor iniciado e aguardando conexões...");
        } catch (Exception e) {
            System.err.println("Erro ao iniciar o sistema: " + e.getMessage());
        }
    }
}
