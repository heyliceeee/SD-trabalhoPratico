package org.example.communication;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Para envio de notificacaoes em broadcast
 */
public class AlertService {
    private MulticastSocket socket;
    private static final String ALERT_GROUP = "224.0.0.1";

    public AlertService() throws IOException {
        socket = new MulticastSocket();
    }

    public void sendAlert(String message) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ALERT_GROUP), 12345);
        socket.send(packet);
    }
}
