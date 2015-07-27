package actors;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import messages.*;

import java.util.HashMap;
import java.util.Map;

public class Chat extends UntypedActor{

    private Map<String, ActorRef> users;
    private ObjectMapper mapper = new ObjectMapper();
    private ActorRef chatManager;
    private String chatName;
    LoggingAdapter log;
    ActorRef mediator;


    public static Props props(ActorRef manager, String chatName, ActorRef mediator) {
        return Props.create(Chat.class, manager, chatName, mediator);
    }

    public Chat() {
        users = new HashMap<String,ActorRef>();
    }

    public Chat(ActorRef chatManager, String chatName, ActorRef mediator) {
        this.chatName = chatName;
        this.chatManager = chatManager;
        users = new HashMap<String,ActorRef>();
        this.mediator = mediator;
        //mediator = DistributedPubSubExtension.get(getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Subscribe(chatName, getSelf()), getSelf());
        log = Logging.getLogger(getContext().system(), this);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        // Normal message from user
        if (message instanceof Message){
            if(getSender().equals(users.get(((Message) message).getName()))){ //If the message isn't from other node
                mediator.tell(new DistributedPubSubMediator.Publish(chatName, message), getSelf());
            }else{
                for (Map.Entry<String, ActorRef> entry : users.entrySet()) {
                    entry.getValue().tell(message, getSelf());
                }
            }
        }
        //Suscribe message
        else{
            if (message instanceof SubscribeChat){
                if (users.containsKey(((SubscribeChat) message).getUser())){ //If I already have this user
                    if (!getSender().equals(getSelf())) {
                        getSender().tell(new DuplicatedUser(((SubscribeChat) message).getUser()), getSelf());
                    }
                }else{ //If is a new user, I subscribe it
                    mediator.tell(new DistributedPubSubMediator.Publish(chatName, new CheckUser(((SubscribeChat) message).getUser())), getSelf());
                    users.put(((SubscribeChat) message).getUser(),getSender());
                }
            }else{
                if (message instanceof UnsubscribeChat){
                    users.remove(((UnsubscribeChat) message).getUser());
                    if (users.isEmpty()){ //If there aren't clients in this chat, I remove this chat
                        UnsubscribeChatManager unsubscribeChatManager = new UnsubscribeChatManager(chatName);
                        chatManager.tell(unsubscribeChatManager, getSelf());
                        self().tell(PoisonPill.getInstance(), self());
                    }
                }else{
                    if (message instanceof CheckUser){
                        if (users.containsKey(((CheckUser) message).getUser())) { //If I already have this user
                            if (!getSender().equals(getSelf())) {
                                getSender().tell(new DuplicatedUser(((CheckUser) message).getUser()), getSelf());
                            }
                        }
                    }else{
                        if (message instanceof DuplicatedUser) {
                            users.get(((DuplicatedUser) message).getUser()).tell(message, getSelf());
                            users.remove(((DuplicatedUser) message).getUser());
                            if (users.isEmpty()) { //If there aren't clients in this chat, I remove this chat
                                UnsubscribeChatManager unsubscribeChatManager = new UnsubscribeChatManager(chatName);
                                chatManager.tell(unsubscribeChatManager, getSelf());
                                self().tell(PoisonPill.getInstance(), self());
                            }
                        }
                    }
                }
            }
        }

    }
}