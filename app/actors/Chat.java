package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;

public class Chat extends UntypedActor{

    private Map<String, ActorRef> users;
    private ObjectMapper mapper = new ObjectMapper();


    public static Props props(ActorRef out) {
        return Props.create(Chat.class, out);
    }

    public Chat() {
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
                if (users.containsKey((String)message)){ //If I already have this user
                    if (users.get((String)message)==getSender()){ //If the sender was already subscribed, unsubscribe it
                        users.remove((String)message);
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
