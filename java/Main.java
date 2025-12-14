import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        System.out.print("XMPP username (WITHOUT domain): ");
        String user = sc.nextLine().trim();

        System.out.print("Password: ");
        String pass = sc.nextLine().trim();

        String domain = "jabber.def.am";      // your domain
        String host   = "jabber.def.am";      // same server host

        XmppClient client = new XmppClient();
        client.connect(user, pass, domain, host);
        Receiver.startListening(client);

        System.out.println("Connected. Type: send <jid> <message>");
        System.out.println("Example: send student@jabber.def.am hello");
        System.out.println("Type exit to quit.");

        while (true) {
            System.out.print("> ");
            String line = sc.nextLine();

            if (line.equalsIgnoreCase("exit"))
                break;

            if (line.startsWith("send")) {
                String[] p = line.split(" ", 3);
                if (p.length < 3) {
                    System.out.println("Usage: send <jid> <message>");
                    continue;
                }
                client.sendMessage(p[1], p[2]);
            }
        }

        client.getConnection().disconnect();
        System.out.println("Disconnected.");
    }
}
