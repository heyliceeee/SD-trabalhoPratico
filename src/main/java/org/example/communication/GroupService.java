package org.example.communication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gere a criação e a participação em grupos multicast, permitindo que os utilizadores se comuniquem em canais dedicados.
 *
 * Responsabilidades:
 * - Criar Grupos: Registar novos grupos e garantir que o mesmo grupo não seja criado várias vezes.
 * - Participação: Permitir que utilizadores entrem e saiam de grupos específicos.
 * - Envio de Mensagens para Grupos: Lógica para enviar mensagens a todos os membros de um grupo.
 */
public class GroupService {
    private Map<String, List<ClientHandler>> groups = new HashMap<>(); // Mapa de grupos e membros

    // Cria um novo grupo
    public void createGroup(String groupName) {
        groups.putIfAbsent(groupName, new ArrayList<>());
    }

    // Adiciona um cliente ao grupo
    public void joinGroup(String groupName, ClientHandler clientHandler) {
        groups.getOrDefault(groupName, new ArrayList<>()).add(clientHandler);
    }

    // Remove um cliente do grupo
    public void leaveGroup(String groupName, ClientHandler clientHandler) {
        groups.getOrDefault(groupName, new ArrayList<>()).remove(clientHandler);
    }

    // Envia uma mensagem para todos os membros de um grupo
    public void sendMessageToGroup(String groupName, String message) {
        List<ClientHandler> members = groups.get(groupName);
        if (members != null) {
            for (ClientHandler client : members) {
                client.sendMessage(message);
            }
        }
    }
}
