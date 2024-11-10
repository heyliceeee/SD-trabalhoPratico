package org.example.communication;

import org.example.hierarchy.Role;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

import static java.lang.System.out;

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
    public static final String GROUPS_FILE = "D:\\githubProjects\\SD-trabalhoPratico\\src\\main\\java\\files\\user_groups.txt";
    public static final String GROUPMESSAGES_FILE = "D:\\githubProjects\\SD-trabalhoPratico\\src\\main\\java\\files\\group_messages.txt";

    private MessageService messageService;



    public GroupService(MessageService messageService) {
        // Inicializa grupos default
        createGroup("GRUPO-GERAL");
        createGroup("GRUPO-HIGH");
        createGroup("GRUPO-MEDIUM");
        createGroup("GRUPO-LOW");
        createGroup("GRUPO-REGULAR");

        this.messageService = messageService;
    }

    // Cria um novo grupo
    public void createGroup(String groupName) {
        groups.putIfAbsent(groupName, new ArrayList<>());
    }

    // Adiciona um cliente ao grupo
    public void joinGroup(String groupName, ClientHandler clientHandler) {
        groups.getOrDefault(groupName, new ArrayList<>()).add(clientHandler);

        // Guardar grupos do utilizador após adicioná-lo
        saveUserGroups(clientHandler.getEmail());
    }

    // Remove um cliente do grupo
    public void leaveGroup(String groupName, ClientHandler clientHandler) {
        groups.getOrDefault(groupName, new ArrayList<>()).remove(clientHandler);

        // Remove o cliente do grupo
        List<ClientHandler> members = groups.get(groupName);
        if (members != null) {
            members.remove(clientHandler);
        }

        // Atualiza o arquivo user_groups.txt para remover a associação
        removeUserFromGroupFile(clientHandler.getEmail(), groupName);
    }

    // Método para remover o usuário do arquivo
    private void removeUserFromGroupFile(String email, String groupName) {
        List<String> lines = new ArrayList<>();

        // Ler as linhas atuais do arquivo
        try (BufferedReader br = new BufferedReader(new FileReader(GROUPS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Adicionar linha à lista se não for a que queremos remover
                if (!line.equals(email + "," + groupName)) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler grupos de usuários: " + e.getMessage());
        }

        // Reescrever o arquivo sem a linha removida
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(GROUPS_FILE))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar grupos de usuários: " + e.getMessage());
        }
    }

    public void addUserToDefaultGroups(ClientHandler clientHandler, Role userRole) {
        joinGroup("GRUPO-GERAL", clientHandler);  // Todos pertencem ao grupo "geral"

        // Adiciona o utilizador ao seu grupo e aos grupos de roles inferiores
        if (userRole == Role.HIGH) {
            joinGroup("GRUPO-HIGH", clientHandler);
            joinGroup("GRUPO-MEDIUM", clientHandler);
            joinGroup("GRUPO-LOW", clientHandler);
            joinGroup("GRUPO-REGULAR", clientHandler);

        } else if (userRole == Role.MEDIUM) {
            joinGroup("GRUPO-MEDIUM", clientHandler);
            joinGroup("GRUPO-LOW", clientHandler);
            joinGroup("GRUPO-REGULAR", clientHandler);

        } else if (userRole == Role.LOW) {
            joinGroup("GRUPO-LOW", clientHandler);
            joinGroup("GRUPO-REGULAR", clientHandler);

        } else {
            joinGroup("GRUPO-REGULAR", clientHandler);
        }
    }

    // membros online do grupo
    public List<ClientHandler> getOnlineMembersOfGroup(String groupName) {
        // Obter os membros do grupo
        List<ClientHandler> members = loadMembersGroup(groupName);

        // Obter os utilizadores online
        Collection<ClientHandler> onlineUsers = messageService.getOnlineClients().values(); // Retorna os valores (ClientHandler) do mapa

        // Filtrar membros online
        List<ClientHandler> onlineMembers = new ArrayList<>();

        // Verifica se o grupo existe
        if (members != null) {
            // Cria um Set para armazenar os emails dos utilizadores online para facilitar a busca
            Set<String> onlineEmails = new HashSet<>();
            for (ClientHandler onlineUser : onlineUsers) {
                onlineEmails.add(onlineUser.getEmail()); // Adiciona os emails dos utilizadores online
            }

            // Percorrer os membros do grupo e verificar se estão online
            for (ClientHandler member : members) {
                if (onlineEmails.contains(member.getEmail())) {
                    onlineMembers.add(member); // Adicionar à lista de membros online
                }
            }
        }

        return onlineMembers; // Retorna a lista de membros online
    }


    //membros offline do grupo
    public List<ClientHandler> getOfflineMembersOfGroup(String groupName) {
        List<ClientHandler> members = loadMembersGroup(groupName); // Obter membros do grupo
        Collection<ClientHandler> onlineUsers = messageService.getOnlineClients().values(); // Usuários online

        List<ClientHandler> offlineMembers = new ArrayList<>();

        // Percorrer os membros do grupo e verificar se estão offline
        if (members != null) {
            for (ClientHandler member : members) {
                boolean isOnline = false;
                // Verificar se o membro está online
                for (ClientHandler onlineUser : onlineUsers) {
                    if (member.getEmail().equals(onlineUser.getEmail())) {
                        isOnline = true; // Se encontrado, o membro está online
                        break;
                    }
                }

                // Se o membro não está online, adicioná-lo à lista de membros offline
                if (!isOnline) {
                    offlineMembers.add(member);
                }
            }
        }

        return offlineMembers; // Retornar membros offline
    }


    public List<ClientHandler> loadMembersGroup(String groupName) {
        List<ClientHandler> members = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(GROUPS_FILE))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String email = parts[0];
                    String groupNameFile = parts[1];

                    if (groupNameFile.equals(groupName)) { // Verifica se o grupo corresponde
                        ClientHandler member = messageService.getClientHandlerByEmail(email); // obter o ClientHandler com base no email
                        if (member != null) {
                            members.add(member); // Adiciona o ClientHandler à lista
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar membros do grupos: " + e.getMessage());
        }

        return members;
    }



    // Envia uma mensagem para todos os membros de um grupo
    public void sendMessageToGroup(String groupName, String message, ClientHandler sender) {
        // Obter os membros do grupo
        List<ClientHandler> members = loadMembersGroup(groupName);

        // Obter os utilizadores online e offline
        List<ClientHandler> onlineMembers = getOnlineMembersOfGroup(groupName);
        List<ClientHandler> offlineMembers = getOfflineMembersOfGroup(groupName);

        if (members != null && members.contains(sender)) { // Verifica se o remetente pertence ao grupo

            // Armazenar a mensagem para todos os membros offline, independentemente de estarem registrados
            for (ClientHandler offlineMember : offlineMembers) {
                messageService.storeGroupMessage(offlineMember.getEmail(), groupName, message); // Armazenar a mensagem para membros offline
            }

            // Enviar a mensagem para os membros online
            for (ClientHandler onlineMember : onlineMembers) {
                String messageWithRecipient = "";

                if (onlineMember.getEmail().equals(sender.getEmail())) { // Se o remetente for o próprio
                    messageWithRecipient = String.format("(%s): %s", groupName, message); // Exibir mensagem simples
                } else {
                    messageWithRecipient = String.format("%s (%s): %s", sender.getEmail(), groupName, message); // Mensagem para outros membros
                }

                onlineMember.sendMessage(messageWithRecipient); // Enviar mensagem para o membro online
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
            sendMessageToGroup("GRUPO-GERAL", "Ação aprovada: " + request.getAction() + " por " + approver.getEmail(), approver);
            pendingApprovals.remove(request);
        } else {
            approver.sendMessage("Você não tem permissão para aprovar esta ação ou é o próprio requisitante.");
        }
    }

    // Método para salvar grupos de um usuário em um arquivo
    public void saveUserGroups(String email) {
        // Obter a lista de grupos do usuário
        List<String> userGroups = getGroupsForUser(email);

        // Carregar grupos existentes do arquivo para verificar duplicatas
        Set<String> existingEntries = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(GROUPS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                existingEntries.add(line);
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar grupos existentes: " + e.getMessage());
        }

        // Adicionar apenas grupos que ainda não estão no arquivo
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(GROUPS_FILE, true))) {
            for (String groupName : userGroups) {
                String entry = email + "," + groupName.trim(); // Use trim() para remover espaços em branco
                if (!existingEntries.contains(entry)) {
                    bw.write(entry);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar grupos de usuários: " + e.getMessage());
        }
    }


    // Método que retorna os grupos de um utilizador
    public List<String> getGroupsForUser(String email) {
        List<String> userGroups = new ArrayList<>();
        for (Map.Entry<String, List<ClientHandler>> entry : groups.entrySet()) {
            for (ClientHandler member : entry.getValue()) {
                if (member.getEmail().equals(email)) {
                    userGroups.add(entry.getKey());
                }
            }
        }
        // Retornar uma lista sem duplicatas
        return new ArrayList<>(new HashSet<>(userGroups));
    }

    public void loadUserGroups() {
        try (BufferedReader br = new BufferedReader(new FileReader(GROUPS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String email = parts[0];
                    String groupName = parts[1];
                    // Adicione o utilizador ao grupo
                    joinGroup(groupName, messageService.getClientHandlerByEmail(email));
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar grupos de utilizadores: " + e.getMessage());
        }
    }
}
