package org.example.communication;

import org.example.authentication.UserManager;
import org.example.hierarchy.Authorization;
import org.example.hierarchy.Role;
import org.example.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


/**
 * Trata de cada cliente logado, gerindo a comunicação entre o cliente e o servidor, e processa os comandos enviados pelos clientes.
 *
 *
 * Responsabilidades:
 * - Comunicação com o Cliente: Enviar e receber mensagens.
 * - Processar Permissoes
 * - Processar Comandos: Identificar e processar comandos do cliente (como joinGroup, leaveGroup, sendMessage).
 * - Interação com Outros Serviços: Chamar MessageService para enviar mensagens ou GroupService para participar em grupos.
 */
public class ClientHandler extends Thread {
    private Socket socket;
    private UserManager userManager;
    private MessageService messageService;
    private GroupService groupService;
    private Authorization authorization;
    private PrintWriter out;
    private BufferedReader in;
    private String email;
    private Role userRole;


    public ClientHandler(Socket socket, UserManager userManager, MessageService messageService, GroupService groupService) {
        this.socket = socket;
        this.userManager = userManager;
        this.messageService = messageService;
        this.groupService = groupService;
        this.authorization = new Authorization();
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Autenticação do Utilizador
            if (!authenticateUser()) {
                out.println("Autenticação falhou. Conexão encerrada.");
                return; // Encerrar conexão se a autenticação falhar
            }

            String input;
            while ((input = in.readLine()) != null) {
                processCommand(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Remove o cliente e encerra a conexão
            messageService.unregisterOnlineClient(email);
            closeConnections();
        }
    }

    // Método para autenticar o utilizador
    private boolean authenticateUser() throws IOException {
        out.println("Introduza o seu email:");
        email = in.readLine();
        out.println("Introduza a sua palavra-passe:");
        String password = in.readLine();

        boolean success = userManager.authenticateUser(email, password);
        Logger.logAuthentication(email, success);

        if (success) {
            userRole = Role.valueOf(userManager.getUserRole(email).toUpperCase());
            messageService.registerOnlineClient(email, this); // Registra o cliente como online e entrega mensagens pendentes

            // Adiciona o utilizador aos grupos padrão com base no seu role
            groupService.addUserToDefaultGroups(this, userRole);

            out.println("Autenticação bem-sucedida. Pertence aos grupos: geral, " + userRole + " e roles inferiores.");
            return true;
        }
        return false;
    }

    // Processa os comandos enviados pelo cliente
    private void processCommand(String command) {
        String[] parts = command.split(" ", 3);
        String action = parts[0];

        switch (action) {
            case "SEND":
                sendMessage(parts[1], parts[2]);
                break;
            case "JOIN":
                joinGroup(parts[1]);
                break;
            case "LEAVE":
                leaveGroup(parts[1]);
                break;
            case "GROUPMSG":
                if (parts.length < 3) {
                    sendMessage("Uso: GROUPMSG [grupo] [mensagem]");
                } else {
                    String groupName = parts[1];
                    String message = parts[2];

                    // Envia a mensagem para o grupo apenas se o utilizador for membro do grupo
                    sendMessageToGroup(groupName, message);
                }
                break;
            case "EVACUATE":
                groupService.requestApproval("EVACUATE", this, "GRUPO-HIGH");
                break;
            case "ACTIVATE":
                groupService.requestApproval("ACTIVATE", this, "GRUPO-MEDIUM");
                break;
            case "DISTRIBUTE":
                groupService.requestApproval("DISTRIBUTE", this, "GRUPO-LOW");
                break;

            case "APPROVE":
                if (parts.length < 2) {
                    sendMessage("Uso: APPROVE [ação]");
                } else {
                    String approvalAction = parts[1];
                    ApprovalRequest request = groupService.getPendingApproval(approvalAction);
                    if (request != null) {
                        groupService.approveAction(this, request);
                    } else {
                        sendMessage("Nenhum pedido de aprovação encontrado para " + approvalAction);
                    }
                }
                break;
            default:
                sendMessage("Comando desconhecido.");
        }
    }

    // Envia uma mensagem direta para outro utilizador
    private void sendMessage(String toUser, String message) {
        messageService.sendMessage(toUser, email + ": " + message);
        out.println("Mensagem enviada para " + toUser);
    }

    // Permite que o cliente se junte a um grupo específico
    private void joinGroup(String groupName) {
        groupService.createGroup(groupName); // Cria o grupo se não existir
        groupService.joinGroup(groupName, this);
        out.println("Você entrou no grupo: " + groupName);
    }

    // Permite que o cliente saia de um grupo
    private void leaveGroup(String groupName) {
        groupService.leaveGroup(groupName, this);
        out.println("Você saiu do grupo: " + groupName);
    }

    // Envia uma mensagem para todos os membros de um grupo
    private void sendMessageToGroup(String groupName, String message) {
        groupService.sendMessageToGroup(groupName, email + ": " + message, this);
    }

    // Executa uma ação que requer autorização, como evacuar ou ativar emergência
    private void executeAction(String operation) {
        boolean authorized = authorization.hasPermission(operation, userRole);
        Logger.logAction(email, operation, authorized);

        if (authorized) {
            out.println("Ação autorizada: " + operation);
            // Executa a ação
        } else {
            out.println("Permissão insuficiente para realizar a ação: " + operation);
        }
    }

    // Envia uma mensagem para o cliente
    public void sendMessage(String message) {
        out.println(message);
    }

    // Encerra a conexão e libera os recursos
    private void closeConnections() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() { return userRole; }
}