package StableMulticast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class Discovery extends Thread {
    private final MulticastServer server;
    private static int bufferSize = 1024;
    private byte[] buffer = new byte[bufferSize];

    public Discovery(MulticastServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Simulate discovery process
                Thread.sleep(5000); // Wait for 5 seconds before next discovery
                // System.out.println("Discovery process running...");
                String message = "Who's listening";
                MulticastSocket socket = server.getSocket();
                DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), server.getAddress(), server.getPort());
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } catch (InterruptedException e) {
                System.err.println("Discovery thread interrupted: " + e.getMessage());
                break;
            }
        }
    }
}
