package StableMulticast;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

public class StableMulticast {
    private final UnicastServer unicastServer;
    private final MulticastServer multicastServer;
    private final Discovery discovery;
    private final String ipPort;

    private final ArrayList<Integer> vectorClock = new ArrayList<>();
    private final ArrayList<Message> buffer = new ArrayList<>();
    private final Object ipLock = new Object();
    private final ArrayList<String> ipList = new ArrayList<>();
    private final Object bufferLock = new Object();
    private final Object vectorClockLock = new Object();
    

    public StableMulticast(String ip, Integer port, IStableMulticast client) throws IOException {
        try {
            this.unicastServer = new UnicastServer(port, InetAddress.getByName(ip), client, this);
            unicastServer.start();
            this.multicastServer = new MulticastServer(3000, unicastServer, ipList, ipLock);
            multicastServer.start();
            this.discovery = new Discovery(multicastServer);
            discovery.start();
            ipPort = ip + ":" + port;
            vectorClock.add(0); // Initialize vector clock with 0 for the local process
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Error initializing UnicastServer", e);
        }
    }

    public void discardStableMessages() {
        /*
         * when P[i] receives a message from P[j]
         * MC[j][*] <- msg.VC
         * if i != j then MC[i][j] <- MC[i][j] + 1
         * deliver msg to the upper layer
         * 
         * when (msg in buffer) AND (msg.VC[msg.sender] <= min[1<=x<=n](MC[x][msg.sender]))
         * discard msg from buffer
         */

        synchronized (bufferLock) {
            ArrayList<Message> toRemove = new ArrayList<>();
            for (Message msg : buffer) {
                int senderIndex = Integer.parseInt(msg.getSenderPort().split(":")[1]) - 1; // Assuming port is in format "ip:port"
                boolean canDiscard = true;

                // Check if the message can be discarded
                for (int i = 0; i < vectorClock.size(); i++) {
                    if (i != senderIndex && vectorClock.get(i) < msg.getVectorClock().get(senderIndex)) {
                        canDiscard = false;
                        break;
                    }
                }

                if (canDiscard) {
                    toRemove.add(msg);
                    System.out.println("Discarding message: " + msg.getContent());
                }
            }
            buffer.removeAll(toRemove);
        }
    }

    public void addToBuffer(Message msg) {
        synchronized (bufferLock) {
            // Add the message to the buffer
            buffer.add(msg);
        }
    }

    public void updateVectorClock() {
        synchronized (vectorClockLock) {
            // Increment the local process's vector clock
            vectorClock.set(0, vectorClock.get(0) + 1);
        }
    }

    public void updateVectorClock(String senderPort, ArrayList<Integer> receivedVectorClock) {
        synchronized (vectorClockLock) {
            // Update the vector clock based on the received vector clock
            for (int i = 0; i < vectorClock.size(); i++) {
                if (i < receivedVectorClock.size()) {
                    vectorClock.set(i, Math.max(vectorClock.get(i), receivedVectorClock.get(i)));
                } else {
                    vectorClock.add(0);
                }
            }
            // Increment the local process's vector clock
            vectorClock.set(0, vectorClock.get(0) + 1);
        }
    }

    private void printMessageBuffer() {
        synchronized (bufferLock) {
            System.out.println("Message Buffer:");
            for (Message msg : buffer) {
                System.out.println(msg.getContent());
            }
        }
    }

    public void msend(String msg, IStableMulticast client) {
        /*
         * msg.VC <- MC[i][*]
         * msg.sender <- i
         * for all P do send(msg) to P[j]
         * MC[i][i] <- MC[i][i] + 1
         */

        synchronized (vectorClockLock) {
            updateVectorClock();
            ArrayList<Integer> currentVectorClock = new ArrayList<>(vectorClock);
            Message message = new Message(msg, ipPort, currentVectorClock);
            addToBuffer(message);
            printMessageBuffer();

            // Send the message to all known IPs
            synchronized (ipLock) {
                for (String ip : ipList) {
                    String[] parts = ip.split(":");
                    String ipAddress = parts[0];
                    String port = parts[1];
                    try {
                        unicastServer.send(message, ipAddress, port);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}