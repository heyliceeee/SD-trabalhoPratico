package org.example.communication;

import org.example.hierarchy.Role;

public class ApprovalRequest {
    private String action;
    private ClientHandler requester;
    private Role requiredRole;  // Define o role necessário para aprovar a ação

    public ApprovalRequest(String action, ClientHandler requester, Role requiredRole) {
        this.action = action;
        this.requester = requester;
        this.requiredRole = requiredRole;
    }

    public String getAction() { return action; }
    public ClientHandler getRequester() { return requester; }
    public Role getRequiredRole() { return requiredRole; }
}
