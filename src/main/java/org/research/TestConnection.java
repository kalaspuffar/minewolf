package org.research;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class TestConnection {
    public static void main(String[] args) {
        String serverAddress = "localhost"; // Server address
        int port = 4711; // Server port number

        try (Socket socket = new Socket(serverAddress, port)) {
            // Create output stream to send data to the server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // Create input stream to receive data from the server
            out.println("player.setTile(100,200,100)");
            System.out.println("Sent message to server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
