package org.example.communication;


import org.example.authentication.UserManager;
import org.example.logging.Logger;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Gere o envio de mensagens, incluindo o armazenamento de mensagens para entrega posterior (quando o cliente estiver offline)
 *
 * Responsabilidades:
 * - Envio de Mensagens: Enviar mensagens diretas entre clientes.
 * - Mensagens Offline: Armazenar mensagens para entrega quando o utilizador estiver online.
 * - Histórico de Mensagens: Opcionalmente, fazer o log das mensagens enviadas para fins de auditoria.
 */
public class MessageService {
    private Map<String, Queue<String>> offlineMessages = new HashMap<>(); // Armazena mensagens para utilizadores offline
    private Map<String, ClientHandler> onlineClients = new HashMap<>(); // Armazena os clientes online
    public static final String INDIVIDUALMESSAGES_FILE = "D:\\githubProjects\\SD-trabalhoPratico\\src\\main\\java\\org\\example\\group_messages.txt";

    private UserManager userManager;

    public MessageService(UserManager userManager) {
        this.userManager = userManager;
    }

    public void sendMessage(String toUser, String message) {
        ClientHandler recipient = onlineClients.get(toUser);
        if (recipient != null) {
            recipient.sendMessage(message); // Envia a mensagem diretamente se o destinatário está online
        } else {
            offlineMessages.computeIfAbsent(toUser, k -> new LinkedList<>()).add(message); // Armazena a mensagem se o destinatário está offline

            // Armazena a mensagem se o destinatário está offline
            storeIndividualMessage(toUser, message);
        }
    }

    private void storeGroupMessage(String groupName, String message) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("group_messages.txt", true))) {
            bw.write(String.format("%s,%s,%s", groupName, message, LocalDateTime.now()));
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Erro ao armazenar mensagem de grupo: " + e.getMessage());
        }
    }

    private void storeIndividualMessage(String toUser, String message) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("individual_messages.txt", true))) {
            bw.write(String.format("%s,%s,%s", toUser, message, LocalDateTime.now()));
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Erro ao armazenar mensagem individual: " + e.getMessage());
        }
    }

    public void broadcastMessage(String message) {
        onlineClients.values().forEach(client -> client.sendMessage(message));
    }

    public void deliverPendingMessages(String user) {
        Queue<String> messages = offlineMessages.remove(user);
        if (messages != null) {
            ClientHandler client = onlineClients.get(user);
            if (client != null) {
                while (!messages.isEmpty()) {
                    client.sendMessage(messages.poll()); // Envia todas as mensagens pendentes
                }
            }
        }

        loadIndividualMessages(user);
    }

    private void loadIndividualMessages(String user) {
        try (BufferedReader br = new BufferedReader(new FileReader(INDIVIDUALMESSAGES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 3); // Email, mensagem, timestamp
                if (parts.length == 3 && parts[0].equals(user)) {
                    // Entregar mensagem ao usuário
                    ClientHandler client = onlineClients.get(user);
                    if (client != null) {
                        client.sendMessage("Mensagem recebida: " + parts[1] + " [" + parts[2] + "]");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar mensagens individuais: " + e.getMessage());
        }
    }

    public void registerOnlineClient(String username, ClientHandler clientHandler) {
        onlineClients.put(username, clientHandler); // Regista o cliente como online
        deliverPendingMessages(username); // Entrega mensagens pendentes ao cliente que acabou de se conectar

        // Logar users ativos
        Logger.logActiveUsers(new ArrayList<>(onlineClients.keySet()));
    }

    public void unregisterOnlineClient(String username) {
        onlineClients.remove(username); // Remove o cliente da lista de online

        // Logar users ativos após a remoção
        Logger.logActiveUsers(new ArrayList<>(onlineClients.keySet()));
    }

    public Map<String, ClientHandler> getOnlineClients() {
        return onlineClients;
    }

    public ClientHandler getClientHandlerByEmail(String email) {
        return onlineClients.get(email); // Assume que o email é usado como chave
    }
}
