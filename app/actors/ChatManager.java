package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import messages.GetChat;
import messages.UnsubscribeChatManager;
import play.libs.Akka;

import java.util.HashMap;
import java.util.Map;

public class ChatManager extends UntypedActor{

    private Map<String,ActorRef> chats;

    //public static Props props(ActorRef out) {
    //    return Props.create(ChatManager.class, out);
    //}

    public ChatManager() {
        chats = new HashMap<String,ActorRef>();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof GetChat) {
            if (!chats.containsKey(((GetChat) message).getChatname())){ //If i don't  have this chat, I create it
                chats.put(((GetChat) message).getChatname(), Akka.system().actorOf(Chat.props(getSelf(),((GetChat) message).getChatname())));
                ((GetChat)message).setChat(chats.get(((GetChat) message).getChatname()));
                getSender().tell(message, getSelf());
            }else{
                ((GetChat)message).setChat(chats.get(((GetChat) message).getChatname()));
                getSender().tell(message, getSelf());
            }
        }else{
            if (message instanceof UnsubscribeChatManager){
                chats.remove(((UnsubscribeChatManager) message).getChat());
            }
        }
    }
}
