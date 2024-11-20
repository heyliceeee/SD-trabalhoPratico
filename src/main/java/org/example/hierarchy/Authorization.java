package org.example.hierarchy;


import java.util.HashMap;
import java.util.Map;

/**
 * Verifica  se um utilizador possui a permissão necessária para realizar uma operação específica.
 *
 *
 * Responsabilidades:
 * - Definir Permissões: Configurar o nível de perfil mínimo necessário para cada operação.
 * - Verificar Permissões: Método que compara o perfil do utilizador com o nível exigido para a operação.
 */
public class Authorization {
    private Map<String, Role> requiredRoles; // Mapeia operações aos papéis necessários

    public Authorization() {
        requiredRoles = new HashMap<>();
        definePermissions();
    }

    // Define os níveis de permissão para cada operação
    private void definePermissions() {
        requiredRoles.put("evacuateMass", Role.HIGH);       // Apenas Coordenador Regional pode aprovar
        requiredRoles.put("activateEmergency", Role.MEDIUM); // Coordenador Local ou superior
        requiredRoles.put("distributeResources", Role.LOW);  // Permissão para utilizadores com nível LOW ou superior
    }

    // Verifica se o perfil do utilizador tem permissão para a operação
    public boolean hasPermission(String operation, Role userRole) {
        Role requiredRole = requiredRoles.get(operation);
        if (requiredRole == null) {
            System.out.println("Operação desconhecida: " + operation);
            return false; // A operação não foi definida
        }

        return isRoleSufficient(userRole, requiredRole);
    }

    // Compara os níveis de permissão
    private boolean isRoleSufficient(Role userRole, Role requiredRole) {
        if (userRole == Role.HIGH) return true;   // Coordenador Regional tem permissão para todas as operações
        if (userRole == Role.MEDIUM && (requiredRole == Role.MEDIUM || requiredRole == Role.LOW)) return true;
        return userRole == Role.LOW && requiredRole == Role.LOW;
    }
}
