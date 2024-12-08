package org.example.communication;


import org.example.authentication.UserManager;
import org.example.logging.Logger;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.example.communication.GroupService.GROUPMESSAGES_FILE;

/**
 * Gere o envio de mensagens, incluindo o armazenamento de mensagens para entrega posterior (quando o cliente estiver offline)
 *
 * Responsabilidades:
 * - Envio de Mensagens: Enviar mensagens diretas entre clientes.
 * - Mensagens Offline: Armazenar mensagens para entrega quando o utilizador estiver online.
 * - Histórico de Mensagens: Opcionalmente, fazer o log das mensagens enviadas para fins de auditoria.
 */
public class MessageService {
    private Map<String, Queue<String>> offlineMessages = new HashMap<>(); // Armazena mensagens para utilizadores offline
    private Map<String, ClientHandler> onlineClients = new HashMap<>(); // Armazena os clientes online
    public static final String INDIVIDUALMESSAGES_FILE = "C:\\Users\\andre\\OneDrive\\Ambiente de Trabalho\\es2\\SD-trabalhoPratico\\src\\main\\java\\files\\individual_messages.txt";

    // Este mapa mantém todos os clientes, online ou offline
    private Map<String, ClientHandler> allClients = new HashMap<>();

    private UserManager userManager;

    public MessageService(UserManager userManager) {
        this.userManager = userManager;
    }

   public void sendMessage(String toUser, String message) {
        ClientHandler recipient = onlineClients.get(toUser);
        if (recipient != null) {
            recipient.sendMessage(message); // Envia a mensagem diretamente se o destinatário está online
        } else {
            offlineMessages.computeIfAbsent(toUser, k -> new LinkedList<>()).add(message); // Armazena a mensagem se o destinatário está offline

            // Armazena a mensagem se o destinatário está offline
            storeIndividualMessage(toUser, message);
        }
    }

    private void storeIndividualMessage(String toUser, String message) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(INDIVIDUALMESSAGES_FILE, true))) {
            bw.write(String.format("%s,%s,%s", toUser, message, LocalDateTime.now()));
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Erro ao armazenar mensagem individual: " + e.getMessage());
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
                    //client.sendMessage("ENQUANTO TEVE OFFLINE: "+ messages.poll()); // Envia todas as mensagens pendentes
                    messages.poll(); //remove da lista
                }
            }
        }

        loadIndividualMessages(user);
        loadGroupMessages(user);
    }

    private void loadIndividualMessages(String user) {
        File tempFile = new File(INDIVIDUALMESSAGES_FILE + ".tmp");
        File originalFile = new File(INDIVIDUALMESSAGES_FILE);

        try (BufferedReader br = new BufferedReader(new FileReader(originalFile));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 3); // Email, mensagem, timestamp
                if (parts.length == 3 && parts[0].equals(user)) {
                    // Entregar mensagem ao utilizador
                    ClientHandler client = onlineClients.get(user);

                    if (client != null) {
                        //converter timestamp
                        String formattedDate = convertTimestamp(parts[2]);

                        client.sendMessage("ENQUANTO TEVE OFFLINE: " + parts[1] + " [" + formattedDate + "]");
                    }
                    // Não escreve a mensagem no novo ficheiro (removendo-a do ficheiro original)
                } else {
                    // Mensagem não pertence ao utilizador ou é inválida, mantém no ficheiro
                    bw.write(line);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar mensagens individuais: " + e.getMessage());
        }

        // Substitui o ficheiro original pelo ficheiro temporário
        if (!originalFile.delete()) {
            System.err.println("Erro ao remover o ficheiro original.");
        }
        if (!tempFile.renameTo(originalFile)) {
            System.err.println("Erro ao renomear o ficheiro temporário.");
        }
    }

    // Registra todos os clientes, tanto online quanto offline
    public void registerClient(ClientHandler clientHandler) {
        allClients.put(clientHandler.getEmail(), clientHandler);
    }

    // Retorna o ClientHandler pelo email, mesmo que ele esteja offline
    public ClientHandler getClientHandlerByEmail(String email) {
        return allClients.get(email);
    }

    public void registerOnlineClient(String username, ClientHandler clientHandler) {
        onlineClients.put(username, clientHandler); // Regista o cliente como online
        deliverPendingMessages(username); // Entrega mensagens pendentes ao cliente que acabou de se conectar

        // Logar users ativos
        Logger.logActiveUsers(new ArrayList<>(onlineClients.keySet()));
    }

    public void unregisterOnlineClient(String username) {
        onlineClients.remove(username); // Remove o cliente da lista de online

        // Logar users ativos após a remoção
        Logger.logActiveUsers(new ArrayList<>(onlineClients.keySet()));
    }

    public Map<String, ClientHandler> getOnlineClients() {
        return onlineClients;
    }

    private void loadGroupMessages(String userEmail) {
        File tempFile = new File(GROUPMESSAGES_FILE + ".tmp");
        File originalFile = new File(GROUPMESSAGES_FILE);

        try (BufferedReader br = new BufferedReader(new FileReader(originalFile));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 5); // email destinatário, grupo, email remetente, mensagem, timestamp
                if (parts.length == 5 && !parts[2].equals(userEmail) && parts[0].equals(userEmail)) {
                    // Entregar mensagem ao utilizador
                    ClientHandler client = onlineClients.get(userEmail);
                    if (client != null) {
                        //converter timestamp
                        String formattedDate = convertTimestamp(parts[4]);

                        client.sendMessage("ENQUANTO TEVE OFFLINE: [" + parts[1] + "] " + parts[3] + " [" + formattedDate + "]");
                    }
                    // Não escreve a mensagem no novo ficheiro (removendo-a do ficheiro original)
                } else {
                    // Mensagem não pertence ao utilizador ou é inválida, mantém no ficheiro
                    bw.write(line);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar mensagens de grupo: " + e.getMessage());
        }

        // Substitui o ficheiro original pelo ficheiro temporário
        if (!originalFile.delete()) {
            System.err.println("Erro ao remover o ficheiro original.");
        }
        if (!tempFile.renameTo(originalFile)) {
            System.err.println("Erro ao renomear o ficheiro temporário.");
        }
    }

    //converter data para o formato desejado
    private String convertTimestamp(String originalDateTime) {

        // Converter o texto para LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(originalDateTime);

        // Criar um formatador com o formato desejado
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy");

        return dateTime.format(formatter);
    }

    //guardar as mensagens de grupo para os utilizadores offline
    void storeGroupMessage(String receiver, String groupName, String sender, String message) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(GROUPMESSAGES_FILE, true))) {
            bw.write(String.format("%s,%s,%s,%s,%s", receiver, groupName, sender, message, LocalDateTime.now()));
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Erro ao armazenar mensagem de grupo: " + e.getMessage());
        }
    }
}
