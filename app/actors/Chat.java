package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        if (message instanceof ObjectNode){
            users.put(((ObjectNode) message).get("user").toString(),getSender());
        }else{
            JsonNode json = null;
            try {
                json = mapper.readTree(message.toString());  //message to Json
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Map.Entry<String, ActorRef> entry : users.entrySet()) {
                ObjectNode respuesta = Json.newObject();
                respuesta.put("name", json.get("user").asText());
                respuesta.put("color", json.get("color").asText());
                respuesta.put("message", json.get("message").asText());
                entry.getValue().tell(respuesta, getSelf());

            }
        }

    }
}
