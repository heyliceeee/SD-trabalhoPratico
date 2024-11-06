package org.example.hierarchy;

public enum Role {
    HIGH("HIGH"),      // Coordenador Regional
    MEDIUM("MEDIUM"),  // Coordenador Local
    LOW("LOW"),        // Utilizador com permissões limitadas
    REGULAR("REGULAR"); // Utilizador comum, sem permissões especiais

    private final String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
