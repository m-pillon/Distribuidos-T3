package StableMulticast;

import java.net.DatagramSocket;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class UnicastServer extends Thread {
    private final int port;
    private final IStableMulticast client;
    private final StableMulticast stableMulticast;
    private final DatagramSocket socket;
    private final InetAddress address;
    private byte[] buffer = new byte[1024];

    public UnicastServer(int port, java.net.InetAddress address, IStableMulticast client, StableMulticast stableMulticast) throws java.io.IOException {
        this.port = port;
        this.address = address;
        this.client = client;
        this.stableMulticast = stableMulticast;
        System.out.println("UnicastServer initialized on " + address.getHostAddress() + ':' + port);
        this.socket = new DatagramSocket(port, address);
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public void send(Message msg, String ip, String port) throws java.io.IOException {
        byte[] data;
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream)) {
            // Serialize the message object
            objectOutputStream.writeObject(msg.getContent());
            data = byteStream.toByteArray();
        }

        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(ip), Integer.parseInt(port));
        socket.send(packet);
    }

    @Override
    public void run() {
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getData());
                ObjectInputStream objectInputStream = new ObjectInputStream(byteStream);
                Message msg = (Message) objectInputStream.readObject();
                stableMulticast.addToBuffer(msg);
                stableMulticast.updateVectorClock(msg.getSenderPort(), msg.getVectorClock());

                client.deliver(msg.getContent());
                stableMulticast.discardStableMessages();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
