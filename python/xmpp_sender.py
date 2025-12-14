import asyncio
import aioxmpp
import logging
import sys

#ignore windows policy
if sys.platform == 'win32':
    asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())

# jabber info
SENDER_JID_STR = "shkhrdumyan_andranik@jabber.def.am"
SENDER_PASSWORD = "2004Ando**"
RECIPIENT_JID_STR = "andranik_shkhrdumyan@jabber.def.am"
MESSAGE_BODY = "Hello! This is a test message with certificate verification disabled."

async def send_message():
    print("⏳ Connecting...")
    
    sender_jid = aioxmpp.JID.fromstr(SENDER_JID_STR)
    recipient_jid = aioxmpp.JID.fromstr(RECIPIENT_JID_STR)
    
    client = aioxmpp.PresenceManagedClient(
        sender_jid,
        aioxmpp.make_security_layer(SENDER_PASSWORD, no_verify=True)
    )

    try:
        async with client.connected() as stream:
            print(f"✅ Logged in as {SENDER_JID_STR}")
            
            msg = aioxmpp.Message(
                to=recipient_jid,
                type_=aioxmpp.MessageType.CHAT
            )
            msg.body[None] = MESSAGE_BODY
            
            print(f"📤 Sending to {RECIPIENT_JID_STR}...")
            await client.send(msg)
            print("✅ Message sent!")
            
            await asyncio.sleep(2)
            print("👋 Disconnecting...")

    except Exception as e:
        print(f"❌ An error occurred: {e}")

if __name__ == "__main__":
    try:
        asyncio.run(send_message())
    except KeyboardInterrupt:
        print("Program stopped by user.")