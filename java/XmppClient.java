import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;

import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

public class XmppClient {

    private AbstractXMPPConnection connection;

    public void connect(String username, String password, String domain, String host) throws Exception {
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain(domain)
                .setHost(host)
                .setPort(5222)
                .setUsernameAndPassword(username, password)
                .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled)
                .build();

        connection = new XMPPTCPConnection(config);
        connection.connect();
        connection.login();
    }

    public void sendMessage(String toJid, String text) throws Exception {
        EntityBareJid jid = JidCreate.entityBareFrom(toJid);
        ChatManager cm = ChatManager.getInstanceFor(connection);
        Chat chat = cm.chatWith(jid);

        Message msg = new Message();
        msg.setBody(text);

        chat.send(msg);
    }

    public AbstractXMPPConnection getConnection() {
        return connection;
    }
}
