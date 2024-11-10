package org.example.communication;

import org.example.authentication.UserManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * O servidor do chat aceita conexoes de clientes e cria uma nova ClienteHandler para cada um.
 * Cada cliente é gerido numa thread separada.
 */
public class ChatServer {
    private ServerSocket serverSocket;
    private MessageService messageService;
    private GroupService groupService;
    private UserManager userManager;

    public ChatServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        userManager = new UserManager();
        messageService = new MessageService(userManager);
        groupService = new GroupService(messageService);
        System.out.println("Servidor de chat em execução na porta " + port);
    }

    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket, userManager, messageService, groupService).start();
            } catch (IOException e) {
                System.err.println("Erro ao aceitar conexão de cliente: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        try {
            new ChatServer(12345).start();
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor de chat: " + e.getMessage());
        }
    }
}
