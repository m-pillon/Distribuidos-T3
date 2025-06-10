import java.io.IOException;
import java.util.Scanner;

import StableMulticast.StableMulticast;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(args[0]);
        System.out.println(args[1]);
        Client client = new Client(args[0], args[1]);
        StableMulticast multicast = client.getMulticast();
        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.println("Digite a mensagem:");
            String message = scanner.nextLine();
            multicast.msend(message, client);
        }
    }
}