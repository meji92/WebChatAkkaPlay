package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import org.json.simple.JSONObject;


public class User extends UntypedActor {

    private final ActorRef out;
    private ActorRef chatManager;
    private ActorRef chat;
    private ObjectMapper mapper = new ObjectMapper();
    private String username;

    public static Props props(ActorRef out) {
        return Props.create(User.class, out);
    }

    public User(ActorRef out, ActorRef chatManager) {
        this.out = out;
        this.chatManager = chatManager;
    }

    public void onReceive(Object message) throws Exception {
        if (message instanceof String) {
            JsonNode json= null;
            try {
                json= mapper.readTree(message.toString());  //message to Json
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Initial message
            if (!json.has("message")){
                username = json.get("user").asText();
                chatManager.tell(json.get("chat").asText(), getSelf());
                System.out.println(json);
                System.out.println(chatManager.toString());
            }
            //Normal message
            else{
                chat.tell(message, getSelf());
                System.out.println("Le envio al chat: "+ message);
            }
        }else{
            if (message instanceof ActorRef){
                chat = (ActorRef) message;
                System.out.println("El chat es: " + message);
                JSONObject userdata = new JSONObject();
                userdata.put("user", username);
                userdata.put("actorRef", getSelf());
                //UserData userdata = new UserData(username,getSelf());
                chat.tell(userdata, getSelf());
            }

            //if (message  instanceof JsonObject){

            //}
        }
    }
}

