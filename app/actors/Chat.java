package actors;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
        if (message instanceof ObjectNode){
            for (Map.Entry<String, ActorRef> entry : users.entrySet()) {
                entry.getValue().tell(message, getSelf());
            }
        }
        //Suscribe message
        else{
            if (message instanceof String){
                if (users.containsKey(message)){ //If I already have this user
                    if (users.get(message)==getSender()){ //If the sender was already subscribed, unsubscribe it
                        users.remove(message);
                        if (users.isEmpty()){ //If there aren't clients in this chat, I remove this chat
                            chatManager.tell(chatName,getSelf());
                            self().tell(PoisonPill.getInstance(), self());
                        }
                    }else{ //If is a new user with a already taken username, I reject it
                        getSender().tell(getSender(),getSelf());
                    }
                }else{ //If is a new user, I subscribe it
                    users.put((String)message,getSender());
                }
            }
        }

    }
}
