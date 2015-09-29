package actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.*;
import akka.cluster.pubsub.DistributedPubSub;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;
import messages.GetChat;
import messages.GetIP;
import messages.UnsubscribeChatManager;
import play.libs.Akka;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class ChatManager extends UntypedActor{

    private Map<String,ActorRef> chats;
    private List <String> chatManagers;
    Cluster cluster = Cluster.get(getContext().system());
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    ActorRef router = getContext().actorOf(FromConfig.getInstance().props(), "router");
    ActorRef mediator;


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
    }

    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof String) {
            //Runtime runtime = Runtime.getRuntime();
            //System.out.print("Heap: " + (runtime.maxMemory()-(runtime.totalMemory()-runtime.freeMemory())));
            //System.out.println(" / " + runtime.maxMemory());
            router.tell(new GetIP(), getSender());
        } else {
            if (message instanceof GetIP) {
                /**Runtime runtime = Runtime.getRuntime();
                 System.out.println("##### Heap utilization statistics [MB] #####");
                 //Print used memory
                 System.out.println("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()));
                 //Print free memory
                 System.out.println("Free Memory:" + runtime.freeMemory());
                 //Print total available memory
                 System.out.println("Total Memory:" + runtime.totalMemory());
                 //Print Maximum available memory
                 System.out.println("Max Memory:" + runtime.maxMemory());**/
                //heap HeapMetricsSelector - Used and max JVM heap memory. Weights based on remaining heap capacity; (max - used) / max
                //System.out.print("Heap: " + (runtime.maxMemory()-(runtime.totalMemory()-runtime.freeMemory())));
                //System.out.println(" / " + runtime.maxMemory());
                //getSender().tell(Play.application().configuration().getString("akka.remote.netty.tcp.hostname"), getSelf());
                getSender().tell(getAmazonIP(), getSelf());
            } else {
                if (message instanceof GetChat) {
                    if (!chats.containsKey(((GetChat) message).getChatname())) { //If i don't  have this chat, I create it
                        chats.put(((GetChat) message).getChatname(), Akka.system().actorOf(Chat.props(((GetChat) message).getChatname(), mediator)));
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
                        } else {
                            unhandled(message);
                        }
                    }
                }
            }
        }
    }



    private String getPublicIpAddress() {
        String res = null;
        try {
            String localhost = InetAddress.getLocalHost().getHostAddress();
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) e.nextElement();
                if(ni.isLoopback())
                    continue;
                if(ni.isPointToPoint())
                    continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress address = (InetAddress) addresses.nextElement();
                    if(address instanceof Inet4Address) {
                        String ip = address.getHostAddress();
                        if(!ip.equals(localhost)) {
                            //System.out.println((res = ip));
                            res = ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    private String getAmazonIP(){
        String dir = "ec2-";
        String ip = getEC2InstancePublicIP();
        ip = ip.replace(".","-");
        dir = dir + ip;
        dir = dir + ".eu-west-1.compute.amazonaws.com";
        return dir;
    }

    public String getEC2InstancePublicIP(){
        URL url = null;
        URLConnection conn = null;
        Scanner s = null;
        try {
            //url = new URL("http://169.254.169.254/latest/meta-data/instance-id");
            url = new URL("http://instance-data/latest/meta-data/public-ipv4");
            conn = url.openConnection();
            s = new Scanner(conn.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String aux = null;
        if (s.hasNext()) {
            aux = s.next();
        }
        return aux;
    }


}
