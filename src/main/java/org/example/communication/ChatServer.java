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
        serverSocket = new ServerSocket(port); // Estabelece conexão TCP com o servidor
        userManager = new UserManager();
        messageService = new MessageService(userManager);
        groupService = new GroupService(messageService);
        System.out.println("Servidor de chat em execução na porta " + port);
    }

    /**
     * Método para iniciar o servidor e aceitar conexões de clientes
     */
    public void start() {
        while (true) {
            try {
                // Aceitar a conexão do cliente
                Socket clientSocket = serverSocket.accept();  // Aceita conexões TCP

                // Criar um ClientHandler para cada novo cliente e iniciar uma nova thread
                ClientHandler clientHandler = new ClientHandler(clientSocket, userManager, messageService, groupService);
                clientHandler.start(); // Inicia o thread de cada cliente

                // Registrar o cliente na MessageService (se desejado)
                messageService.registerClient(clientHandler);

            } catch (IOException e) {
                System.err.println("Erro ao aceitar conexão de cliente: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        try {
            // Inicia o servidor na porta 12345
            ChatServer server = new ChatServer(12345);
            server.start();
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor de chat: " + e.getMessage());
        }
    }
}
