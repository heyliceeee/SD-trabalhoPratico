package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

public class MainController {
    @FXML
    private ListView<String> contactList;
    @FXML
    private TextArea chatArea;
    @FXML
    private TextArea messageInput;

    @FXML
    public void initialize() {
        // Inicializar a lista de contatos e outras configurações
    }

    @FXML
    public void sendMessage() {
        String message = messageInput.getText();
        if (!message.isEmpty()) {
            chatArea.appendText("You: " + message + "\n");
            messageInput.clear();
            // Enviar a mensagem usando MessageService
        }
    }
}