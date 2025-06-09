import java.io.IOException;
import java.util.Scanner;

import StableMulticast.StableMulticast;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(args[0]);
        System.out.println(args[1]);
        Client client = new Client(args[0], args[1]);
        StableMulticast multicast = client.getMulticast();
        while (true){
            System.out.println("Digite a mensagem. Mensagens que começam em debug: nao enviam para um cliente, sendo necessario input do teclado p enviar");
            try (Scanner scanner = new Scanner(System.in)) {
                String message = scanner.nextLine();
                multicast.msend(message, client);
            }
        }
    }
}