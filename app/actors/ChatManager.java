package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import play.libs.Akka;

import java.util.HashMap;
import java.util.Map;

public class ChatManager extends UntypedActor{

    private Map chats;

    //public static Props props(ActorRef out) {
    //    return Props.create(ChatManager.class, out);
    //}

    public ChatManager() {
        chats = new HashMap<String,ActorRef>();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof String) {
            if (!chats.containsKey(message)){ //If i don't  have this chat, I create it
                //chats.put(message, Akka.system().actorOf(Props.create(Chat.class)));
                chats.put(message, Akka.system().actorOf(Chat.props(getSelf(),(String)message)));
                getSender().tell(chats.get(message), getSelf());
            }else{
                if (chats.get(message)!=getSender()) { // I have the chat and the sender is the chat -> I delete it
                    getSender().tell(chats.get(message), getSelf());
                }else{ // The sender is other client -> I send it the chat
                    chats.remove(message);
                }
            }
        }
    }
}
