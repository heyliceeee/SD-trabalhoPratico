package org.example.communication;


import org.example.authentication.UserManager;
import org.example.logging.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Gere o envio de mensagens, incluindo o armazenamento de mensagens para entrega posterior (quando o cliente estiver offline)
 *
 * Responsabilidades:
 * - Envio de Mensagens: Enviar mensagens diretas entre clientes.
 * - Mensagens Offline: Armazenar mensagens para entrega quando o utilizador estiver online.
 * - Histórico de Mensagens: Opcionalmente, fazer o log das mensagens enviadas para fins de auditoria.
 */
public class MessageService {
    private Map<String, Queue<String>> offlineMessages = new HashMap<>();
    private Map<String, ClientHandler> onlineClients = new HashMap<>();
    private UserManager userManager;

    public MessageService(UserManager userManager) {
        this.userManager = userManager;
    }

    public void sendMessage(String toUser, String message) {
        ClientHandler recipient = onlineClients.get(toUser);
        if (recipient != null) {
            recipient.sendMessage(message);
            Logger.logMessage(userManager.getUserEmail(recipient), toUser, message);
        } else {
            offlineMessages.computeIfAbsent(toUser, k -> new LinkedList<>()).add(message);
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
                    client.sendMessage(messages.poll());
                }
            }
        }
    }

    public void registerOnlineClient(String username, ClientHandler client) {
        onlineClients.put(username, client);
    }

    public void unregisterOnlineClient(String username) {
        onlineClients.remove(username);
    }

    public Map<String, ClientHandler> getOnlineClients() {
        return onlineClients;
    }
}
