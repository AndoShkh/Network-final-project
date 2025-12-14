import asyncio
import aioxmpp
import logging
import sys

# To ignore sa
if sys.platform == 'win32':
    asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())

# Jabber account
MY_JID_STR = "andranik_shkhrdumyan@jabber.def.am"
MY_PASSWORD = "2004Ando**"

async def listen_for_messages():
    print(f"⏳ Connecting as {MY_JID_STR}...")
    
    my_jid = aioxmpp.JID.fromstr(MY_JID_STR)
    client = aioxmpp.PresenceManagedClient(
        my_jid,
        aioxmpp.make_security_layer(MY_PASSWORD, no_verify=True)
    )

    def on_message(msg):
        # Check if the message has a body (text)
        if msg.body:
            # .ignore language
            content = msg.body.any()
            sender = msg.from_.bare()
            
            print(f"\n📩 NEW MESSAGE from {sender}:")
            print(f"   Content > {content}\n")

    async with client.connected() as stream:
        print(f"✅ Online! Waiting for messages...")
        
        dispatcher = client.summon(aioxmpp.dispatcher.SimpleMessageDispatcher)
        
        dispatcher.register_callback(
            aioxmpp.MessageType.CHAT,
            None, 
            on_message
        )

        while True:
            await asyncio.sleep(1)

if __name__ == "__main__":
    try:
        asyncio.run(listen_for_messages())
    except KeyboardInterrupt:
        print("👋 Logged out.")