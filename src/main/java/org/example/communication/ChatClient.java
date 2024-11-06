package org.example.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


/**
 * Repreesenta o cliente de chat, que se loga ao servidor e permite o envio e receber mensagens
 */
public class ChatClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ChatClient(String serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Thread para receber mensagens do servidor
        new Thread(this::receiveMessages).start();

        // Enviar comandos e mensagens
        sendCommands();
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            System.err.println("Conexão com o servidor perdida.");
        }
    }

    private void sendCommands() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String message = scanner.nextLine();
            out.println(message);
            if (message.equalsIgnoreCase("EXIT")) {
                break;
            }
        }
        closeConnection();
    }

    private void closeConnection() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            new ChatClient("localhost", 12345);
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
}
