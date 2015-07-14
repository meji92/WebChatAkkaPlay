package actors;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import messages.*;

import java.util.HashMap;
import java.util.Map;

public class Chat extends UntypedActor{

    private Map<String, ActorRef> users;
    private ObjectMapper mapper = new ObjectMapper();
    private ActorRef chatManager;
    private String chatName;


    public static Props props(ActorRef manager, String chatName) {
        return Props.create(Chat.class, manager, chatName);
    }

    public Chat() {
        users = new HashMap<String,ActorRef>();
    }

    public Chat(ActorRef chatManager, String chatName) {
        this.chatName = chatName;
        this.chatManager = chatManager;
        users = new HashMap<String,ActorRef>();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        // Normal message from user
        if (message instanceof Message){
            for (Map.Entry<String, ActorRef> entry : users.entrySet()) {
                entry.getValue().tell(message, getSelf());
            }
        }
        //Suscribe message
        else{
            if (message instanceof SubscribeChat){
                if (users.containsKey(((SubscribeChat) message).getUser())){ //If I already have this user
                    getSender().tell(new DuplicatedUser(), getSelf());
                }else{ //If is a new user, I subscribe it
                    users.put(((SubscribeChat) message).getUser(),getSender());
                }
            }else{
                if (message instanceof UnsubscribeChat){
                    users.remove(((UnsubscribeChat) message).getUser());
                    if (users.isEmpty()){ //If there aren't clients in this chat, I remove this chat
                        UnsubscribeChatManager unsubscribeChatManager = new UnsubscribeChatManager(chatName);
                        chatManager.tell(unsubscribeChatManager,getSelf());
                        self().tell(PoisonPill.getInstance(), self());
                    }
                }
            }
        }

    }
}
