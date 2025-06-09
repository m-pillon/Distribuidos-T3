import StableMulticast.IStableMulticast;
import StableMulticast.StableMulticast;

import java.io.IOException;

public class Client implements IStableMulticast {
    StableMulticast multicast;

    public Client(String ip, String port) throws IOException {
        multicast = new StableMulticast(ip, Integer.parseInt(port), this);
    }

    public StableMulticast getMulticast() {
        return multicast;
    }

    @Override
    public void deliver(String message) {
        System.out.println("Received message: " + message);
    }
}
