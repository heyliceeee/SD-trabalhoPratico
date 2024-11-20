package org.example.communication;

public class ClientHandlerStub extends ClientHandler {
    private final String email;

    public ClientHandlerStub(String email) {
        super(null, null, null, null); // Não inicializa as dependências
        this.email = email;
    }

    @Override
    public String getEmail() {
        return email;
    }
}