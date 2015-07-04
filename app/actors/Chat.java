package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;

public class Chat extends UntypedActor{

    //private final ActorRef out;
    private Map<String, ActorRef> users;
    private ObjectMapper mapper = new ObjectMapper();


    public static Props props(ActorRef out) {
        return Props.create(Chat.class, out);
    }

    public Chat() {
        users = new HashMap<String,ActorRef>();
        //this.out = out;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        //if (message instanceof UserData){
        //    users.put(((UserData) message).getName(), ((UserData) message).getActorRef());
        if (message instanceof JSONObject){
            users.put((String) ((JSONObject) message).get("user"), (ActorRef) ((JSONObject) message).get("actorRef"));
        }else{
            JsonNode json = null;
            try {
                json = mapper.readTree(message.toString());  //message to Json
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Map.Entry<String, ActorRef> entry : users.entrySet()) {
                entry.getValue().tell(json.get("message"), getSelf());
                System.out.println(message + entry.getKey());
            }
        }

    }
}
