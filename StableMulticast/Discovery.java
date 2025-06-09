package StableMulticast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class Discovery extends Thread {
    private final MulticastServer server;

    public Discovery(MulticastServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Simulate discovery process
                Thread.sleep(5000); // Wait for 5 seconds before next discovery
                System.out.println("Discovery process running...");
                String message = "Looking";
                MulticastSocket multicastSocket = server.getSocket();
                DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), server.getPort());

                multicastSocket.send(packet);
                System.out.println("Discovery message sent: " + message);

            } catch (InterruptedException e) {
                System.err.println("Discovery thread interrupted: " + e.getMessage());
                break;
            } catch (IOException e) {
                System.err.println("IO error: " + e.getMessage());
                break;
            }
        }
    }
}
