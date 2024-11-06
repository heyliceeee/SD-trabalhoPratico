package org.example.authentication;


import org.example.communication.ClientHandler;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Gere o registo e a autenticacao dos utilizadores
 *
 *
 * Responsabilidades:
 * - Registo de Utilizadores: Método registerUser para adicionar um novo utilizador.
 * - Autenticação: Método authenticateUser que verifica o email e a password.
 * - Gestão de Perfis: Método para verificar e atualizar o perfil do utilizador.
 */
public class UserManager {
    private Map<String, User> users = new HashMap<>(); // Mapeia emails para utilizadores
    private static final String USERS_FILE = "D:\\githubProjects\\SD-trabalhoPratico\\src\\main\\java\\org\\example\\users.txt"; // Ficheiro para persistência

    public UserManager() {
        loadUsersFromFile();
    }

    // Regista um novo utilizador no sistema
    public boolean registerUser(String email, String password, String role) {
        if (!users.containsKey(email)) {
            User newUser = new User(email, password, role);
            users.put(email, newUser);
            saveUsers();
            return true;
        }
        return false; // Email já está registado
    }

    // Autentica um utilizador verificando email e password
    public boolean authenticateUser(String email, String password) {
        User user = users.get(email);
        return user != null && user.getPassword().equals(password);
    }

    // Obtém o perfil do utilizador (para autorização e permissões)
    public String getUserRole(String email) {
        User user = users.get(email);
        return user != null ? user.getRole() : null;
    }

    // Método que retorna o email associado a um `ClientHandler`
    public String getUserEmail(ClientHandler clientHandler) {
        // Considera que o username do cliente é usado como chave no mapa
        return clientHandler.getUsername();
    }

    // Carrega utilizadores a partir do ficheiro para o mapa
    private void loadUsersFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String email = parts[0];
                    String password = parts[1];
                    String role = parts[2];
                    users.put(email, new User(email, password, role));
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar utilizadores: " + e.getMessage());
        }
    }

    // Guarda os utilizadores no ficheiro para persistência
    private void saveUsers() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (User user : users.values()) {
                bw.write(user.getEmail() + "," + user.getPassword() + "," + user.getRole());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erro ao guardar utilizadores: " + e.getMessage());
        }
    }
}
