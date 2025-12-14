import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Simple P2P messenger (no external libraries).
 *
 * Usage:
 *  - Run one instance as "listener" (it always listens on the listenPort).
 *  - From same or other machine run another instance and send messages to that listener.
 *
 * Each instance both listens and can send. Works on localhost or over LAN.
 */
public class Peer {

    // default listen port
    private static final int DEFAULT_PORT = 5000;

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter your display name: ");
        String myName = sc.nextLine().trim();
        if (myName.isEmpty()) myName = "anon";

        System.out.print("Listen on port (ENTER for " + DEFAULT_PORT + "): ");
        String p = sc.nextLine().trim();
        int listenPort = p.isEmpty() ? DEFAULT_PORT : Integer.parseInt(p);

        // Start listener thread
        Thread listener = new Thread(() -> {
            try {
                runListener(listenPort);
            } catch (IOException e) {
                System.err.println("Listener error: " + e.getMessage());
            }
        }, "listener-thread");
        listener.setDaemon(true);
        listener.start();

        System.out.println("Listening on port " + listenPort + ". Type commands:");
        System.out.println("  send <ip> <port> <message>");
        System.out.println("  exit");

        // command loop
        while (true) {
            System.out.print("> ");
            String line = sc.nextLine();
            if (line == null) break;
            line = line.trim();
            if (line.isEmpty()) continue;
            if (line.equalsIgnoreCase("exit")) {
                System.out.println("Exiting...");
                break;
            }
            if (line.startsWith("send ")) {
                // send <ip> <port> <message>
                String[] parts = splitN(line, 4);
                if (parts.length < 4) {
                    System.out.println("Usage: send <ip> <port> <message>");
                    continue;
                }
                String ip = parts[1];
                int port = Integer.parseInt(parts[2]);
                String msg = parts[3];
                try {
                    sendMessage(ip, port, myName, msg);
                    System.out.println("SENT to " + ip + ":" + port);
                } catch (IOException ex) {
                    System.out.println("Send failed: " + ex.getMessage());
                }
            } else {
                System.out.println("Unknown command. Use: send <ip> <port> <message> | exit");
            }
        }

        sc.close();
    }

    // listener: accepts connections and prints received messages
    private static void runListener(int port) throws IOException {
        ServerSocket server = new ServerSocket(port);
        while (true) {
            Socket sock = server.accept();
            // handle each client in its own thread so we can accept more
            new Thread(() -> {
                try (Socket s = sock;
                     InputStream in = s.getInputStream();
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                    byte[] buf = new byte[4096];
                    int read;
                    // read until peer closes connection
                    while ((read = in.read(buf)) != -1) {
                        baos.write(buf, 0, read);
                    }
                    String xml = baos.toString(StandardCharsets.UTF_8.name());
                    Parsed p = parseMessage(xml);
                    if (p != null) {
                        System.out.println("\n<<< MESSAGE RECEIVED >>>");
                        System.out.println("From: " + p.from);
                        System.out.println("Body: " + p.body);
                        System.out.print("> ");
                    } else {
                        System.out.println("\n<<< UNKNOWN DATA RECEIVED >>>");
                        System.out.println(xml);
                        System.out.print("> ");
                    }
                } catch (IOException e) {
                    System.err.println("Error handling incoming connection: " + e.getMessage());
                }
            }, "handler-" + sock.getRemoteSocketAddress()).start();
        }
    }

    // send a single message (connect, send, close)
    private static void sendMessage(String host, int port, String fromName, String body) throws IOException {
        String xml = buildMessage(host /*to*/, fromName /*from*/, body);
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, port), 5000);
            OutputStream out = s.getOutputStream();
            out.write(xml.getBytes(StandardCharsets.UTF_8));
            out.flush();
            // close -> receiver readsEOF and displays
        }
    }

    // build a minimal XML message (simple, not full XMPP)
    private static String buildMessage(String to, String from, String body) {
        // escape minimal XML entities
        String b = xmlEscape(body);
        String f = xmlEscape(from);
        return "<message to=\"" + xmlEscape(to) + "\" from=\"" + f + "\" type=\"chat\">" +
                "<body>" + b + "</body>" +
                "</message>";
    }

    // parse the simple XML created above (very small parser)
    private static Parsed parseMessage(String xml) {
        if (xml == null) return null;
        int bstart = xml.indexOf("<body>");
        int bend = xml.indexOf("</body>");
        if (bstart < 0 || bend < 0) return null;
        String body = xml.substring(bstart + "<body>".length(), bend);
        // from attribute
        String from = extractAttr(xml, "from=\"");
        return new Parsed(from, body);
    }

    private static String extractAttr(String xml, String key) {
        int i = xml.indexOf(key);
        if (i < 0) return "";
        i += key.length();
        int j = xml.indexOf('"', i);
        if (j < 0) return xml.substring(i);
        return xml.substring(i, j);
    }

    private static String xmlEscape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    private static class Parsed {
        final String from;
        final String body;
        Parsed(String f, String b) { from = f; body = b; }
    }

    // split into at most N parts (keeps message with spaces as last part)
    private static String[] splitN(String s, int n) {
        String[] out = new String[n];
        int idx = 0;
        int pos = 0;
        for (int i = 0; i < n - 1; i++) {
            int sp = s.indexOf(' ', pos);
            if (sp == -1) {
                out[idx++] = s.substring(pos);
                pos = s.length();
                break;
            } else {
                out[idx++] = s.substring(pos, sp);
                pos = sp + 1;
            }
        }
        if (pos <= s.length()) out[idx++] = s.substring(pos);
        // compact to actual length
        int real = 0;
        for (int i = 0; i < out.length; i++) if (out[i] != null) real++;
        String[] res = new String[real];
        System.arraycopy(out, 0, res, 0, real);
        return res;
    }
}
