package StableMulticast;

import java.util.ArrayList;;

public class Message {
    private final String content;
    private final String senderPort;
    ArrayList<Integer> vectorClock;

    public Message(String content, String senderPort, ArrayList<Integer> vectorClock) {
        this.content = content;
        this.senderPort = senderPort;
        this.vectorClock = vectorClock;
    }

    public String getContent() {
        return content;
    }

    public String getSenderPort() {
        return senderPort;
    }

    public ArrayList<Integer> getVectorClock() {
        return vectorClock;
    }
}
