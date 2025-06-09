package StableMulticast;

import java.net.*;
import java.util.ArrayList;
import java.io.IOException;

public class MulticastServer extends Thread {
    private final int port;
    private final UnicastServer unicastServer;
    private final ArrayList<String> ipTable;
    private final Object ipTableLock;
    private final MulticastSocket multicastSocket;
    public final InetAddress multicastGroup;

    public MulticastServer(int port, UnicastServer unicastServer, ArrayList<String> ipTable, Object ipTableLock) throws IOException {
        this.port = port;
        this.unicastServer = unicastServer;
        this.ipTable = ipTable;
        this.ipTableLock = ipTableLock;
        this.multicastGroup = InetAddress.getByName("239.1.1.1");
        this.multicastSocket = new MulticastSocket(port);
        this.multicastSocket.joinGroup(multicastGroup);
    }

    public String getAddress() {
        return unicastServer.getAddress();
    }

    public int getPort() {
        return port;
    }

    public MulticastSocket getSocket() {
        return multicastSocket;
    }

    @Override
    public void run() {
        while(true) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);
                
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received multicast message: " + message);
                
                // Process the received message
                if (message.equals("Who's listening")){
                    String signalMessage = unicastServer.getAddress() + ":" + unicastServer.getPort();
                    DatagramPacket sendPacket = new DatagramPacket(signalMessage.getBytes(), signalMessage.length(), multicastGroup, port);
                    multicastSocket.send(sendPacket);
                } else {
                    synchronized (ipTableLock){
                        ipTable.add(message);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error receiving multicast message: " + e.getMessage());
            }
        }
    }
}