import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

public class Receiver {

    public static void startListening(XmppClient client) {
        ChatManager cm = ChatManager.getInstanceFor(client.getConnection());

        cm.addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid from, Message message, org.jivesoftware.smack.chat2.Chat chat) {
                System.out.println("\n[Message from " + from + "]: " + message.getBody());
                System.out.print("> ");
            }
        });
    }
}
