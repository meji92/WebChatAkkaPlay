package actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.cluster.pubsub.DistributedPubSub;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.GetChat;
import messages.UnsubscribeChatManager;
import play.libs.Akka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatManager extends UntypedActor{

    private Map<String,ActorRef> chats;
    private List <String> chatManagers;
    Cluster cluster = Cluster.get(getContext().system());
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef mediator;

    public ChatManager() {
        chatManagers = new ArrayList<String>();
        chats = new HashMap<String,ActorRef>();
        mediator = DistributedPubSub.get(getContext().system()).mediator();
    }

    //subscribe to cluster changes
    @Override
    public void preStart() {
        //#subscribe
        cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(),
                MemberEvent.class, UnreachableMember.class);
        //#subscribe
    }

    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof GetChat) {
            if (!chats.containsKey(((GetChat) message).getChatname())) { //If i don't  have this chat, I create it
                chats.put(((GetChat) message).getChatname(), Akka.system().actorOf(Chat.props(getSelf(), ((GetChat) message).getChatname(), mediator)));
                ((GetChat) message).setChat(chats.get(((GetChat) message).getChatname()));
                getSender().tell(message, getSelf());
            } else { //If I already have this chat, only I send it back the ActorRef of the chat (inside the same message I've received)
                ((GetChat) message).setChat(chats.get(((GetChat) message).getChatname()));
                getSender().tell(message, getSelf());
            }
        } else {
            if (message instanceof UnsubscribeChatManager) {
                chats.remove(((UnsubscribeChatManager) message).getChat());
            } else {
                if (message instanceof MemberUp) {
                    MemberUp mUp = (MemberUp) message;
                    log.info("Member is Up: {}", mUp.member());
                        /**chatManagers.add(mUp.member().address() + "/user/ChatManager");
                        if (!mUp.member().equals(getSelf())){
                            getContext().actorSelection(mUp.member().address() + "/user/ChatManager").tell(new GetMediator(), getSelf());
                        }**/

                } else if (message instanceof UnreachableMember) {
                    UnreachableMember mUnreachable = (UnreachableMember) message;
                    log.info("Member detected as unreachable: {}", mUnreachable.member());

                } else if (message instanceof MemberRemoved) {
                    MemberRemoved mRemoved = (MemberRemoved) message;
                    log.info("Member is Removed: {}", mRemoved.member());

                } else if (message instanceof MemberEvent) {
                    // ignore

                } else if (message instanceof CurrentClusterState) {
                    CurrentClusterState state = (CurrentClusterState) message;
                    //for (Member member : state.getMembers()) {
                        //if (member.status().equals(MemberStatus.up())) {
                            //getContext().actorSelection(member.address() + "/user/ChatManager").tell("Hola", getSelf());
                        //}
                    //}
                }else{
                    unhandled(message);
                }
            }
        }
    }

}
