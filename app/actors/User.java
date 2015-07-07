package actors;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;


public class User extends UntypedActor {

    private final ActorRef out;
    private ActorRef chatManager;
    private ActorRef chat;
    private ObjectMapper mapper = new ObjectMapper();
    private String username;
    private String color;

    public static Props props(ActorRef out) {
        return Props.create(User.class, out);
    }

    public static Props props(ActorRef out, ActorRef chatManager) {
        return Props.create(User.class, out, chatManager);
    }

    public User(ActorRef out, ActorRef chatManager) {
        this.out = out;
        this.chatManager = chatManager;
        this.color = getRandomColor();
    }

    public void onReceive(Object message) throws Exception {
        //Message from the client
        if (message instanceof String) {
            JsonNode json= null;
            try {
                json= mapper.readTree(message.toString());  //message to Json
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Initial message. Send the chat name to ChatManager
            if (!json.has("message")){
                username = json.get("user").asText();
                chatManager.tell(json.get("chat").asText(), getSelf());
            }
            //Normal message. Send message to chat
            else{
                ObjectNode msgdata = Json.newObject();
                msgdata.put("name",json.get("user").asText());
                msgdata.put("message",json.get("message").asText());
                msgdata.put("color",color);
                chat.tell(msgdata, getSelf());
            }
        }else{
            // Message from chat to client
            if (message instanceof ObjectNode){
                out.tell(message.toString(), self());
            }else{
                // Returned message sent by ChatManager. Sends suscribe message
                if (message instanceof ActorRef) {
                    if (message == getSelf()){
                        ObjectNode msg = Json.newObject();
                        msg.put("type", "system");
                        msg.put("message", "This user already exists");
                        chat = null; //To avoid the dead letters when this actor do the postStop and the Chat reply
                        out.tell(msg.toString(),getSelf());
                        self().tell(PoisonPill.getInstance(), self());
                    }else{
                        chat = (ActorRef) message;
                        chat.tell(username, getSelf());
                    }
                }
            }
        }
    }

    // When the websocket are closed
    public void postStop() throws Exception {
        if (chat!=null){ //If I was connected to any chat, I unsubscribe
            chat.tell(username,getSelf());
        }
    }

    public String getRandomColor() {
        String[] letters = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "E", "F"};
        String color = "";
        for (int i = 0; i < 6; i++) {
            color = color.concat(letters[(int) (Math.random() * 15)]);
        }
        return color;
    }
}

