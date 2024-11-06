package org.example.communication;

import org.example.hierarchy.Role;

import java.util.*;

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
    private Queue<ApprovalRequest> pendingApprovals = new LinkedList<>(); //pedidos de aprovacao de mensagens

    public GroupService() {
        // Inicializa grupos default
        createGroup("Grupo-geral");
        createGroup("Grupo-HIGH");
        createGroup("Grupo-MEDIUM");
        createGroup("Grupo-LOW");
        createGroup("Grupo-REGULAR");
    }

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

    public void addUserToDefaultGroups(ClientHandler clientHandler, Role userRole) {
        joinGroup("Grupo-geral", clientHandler);  // Todos pertencem ao grupo "geral"

        // Adiciona o utilizador ao seu grupo e aos grupos de roles inferiores
        if (userRole == Role.HIGH) {
            joinGroup("Grupo-HIGH", clientHandler);
            joinGroup("Grupo-MEDIUM", clientHandler);
            joinGroup("Grupo-LOW", clientHandler);
            joinGroup("Grupo-REGULAR", clientHandler);

        } else if (userRole == Role.MEDIUM) {
            joinGroup("Grupo-MEDIUM", clientHandler);
            joinGroup("Grupo-LOW", clientHandler);
            joinGroup("Grupo-REGULAR", clientHandler);

        } else if (userRole == Role.LOW) {
            joinGroup("Grupo-LOW", clientHandler);
            joinGroup("Grupo-REGULAR", clientHandler);

        } else {
            joinGroup("Grupo-REGULAR", clientHandler);
        }
    }

    // Envia uma mensagem para todos os membros de um grupo
    public void sendMessageToGroup(String groupName, String message, ClientHandler sender) {
        // Verifica se o remetente pertence ao grupo antes de enviar a mensagem
        List<ClientHandler> members = groups.get(groupName);

        if (members != null && members.contains(sender)) {
            for (ClientHandler client : members) {

                if(client.getEmail() == sender.getEmail()) //se fui eu que enviei
                {
                    // Adiciona o email do destinatário na mensagem enviada
                    String messageWithRecipient = String.format("(%s): %s", groupName, message);
                    client.sendMessage(messageWithRecipient);
                }
                else
                {
                    // Adiciona o email do destinatário na mensagem enviada
                    String messageWithRecipient = String.format("%s (%s): %s", sender.getEmail(), groupName, message);
                    client.sendMessage(messageWithRecipient);
                }
            }
        } else {
            sender.sendMessage("Erro: Você não tem permissão para enviar mensagens para o grupo " + groupName);
        }
    }

    /**
     * quando um pedido de acao e feito, ele e enviado para o grupo correto para aprovacao e, apos a aprovacao, e enviado para o grupo geral
     */
    public void requestApproval(String action, ClientHandler requester, String requiredGroup) {
        Role requiredRole;
        switch (action) {
            case "EVACUATE":
                requiredRole = Role.HIGH;
                break;
            case "ACTIVATE":
                requiredRole = Role.MEDIUM;
                break;
            case "DISTRIBUTE":
                requiredRole = Role.LOW;
                break;
            default:
                requester.sendMessage("Ação desconhecida: " + action);
                return;
        }

        ApprovalRequest request = new ApprovalRequest(action, requester, requiredRole);
        pendingApprovals.add(request);

        // Notifica o grupo apropriado para aprovação, excluindo o próprio pedinte
        List<ClientHandler> members = groups.get(requiredGroup);
        if (members != null) {
            for (ClientHandler client : members) {
                if (!client.equals(requester)) {
                    client.sendMessage("-- Aprovação necessária para " + action + " iniciada por " + requester.getEmail() + " --");
                }
            }
        }
        requester.sendMessage("-- Pedido de " + action + " enviada para aprovação. --");
    }

    public ApprovalRequest getPendingApproval(String action) {
        for (ApprovalRequest request : pendingApprovals) {
            if (request.getAction().equalsIgnoreCase(action)) {
                return request;
            }
        }
        return null;
    }

    public void approveAction(ClientHandler approver, ApprovalRequest request) {
        if (!request.getRequester().equals(approver) && approver.getRole().ordinal() <= request.getRequiredRole().ordinal()) {
            // Aprovação válida, envia confirmação para o grupo "geral"
            sendMessageToGroup("Grupo-geral", "Ação aprovada: " + request.getAction() + " por " + approver.getEmail(), approver);
            pendingApprovals.remove(request);
        } else {
            approver.sendMessage("Você não tem permissão para aprovar esta ação ou é o próprio requisitante.");
        }
    }
}
