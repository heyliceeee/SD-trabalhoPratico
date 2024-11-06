package org.example.hierarchy;

public enum Role {
    HIGH("high"),      // Coordenador Regional
    MEDIUM("medium"),  // Coordenador Local
    LOW("low"),        // Utilizador com permissões limitadas
    REGULAR("regular"); // Utilizador comum, sem permissões especiais

    private final String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
