package controllers;

import actors.ChatManager;
import actors.EchoUser;
import actors.User;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.chat;

public class Application extends Controller {

    /**Config config = ConfigFactory.parseString(
            "akka.remote.netty.tcp.port=8000").withFallback(
            ConfigFactory.load());**/

    // Create an Akka system
    //ActorSystem system = ActorSystem.create("ClusterSystem", config);

    private ActorRef chatManager = Akka.system().actorOf(Props.create(ChatManager.class), "ChatManager");
    //private ActorRef chatManager = system.actorOf(Props.create(ChatManager.class),"ChatManager");

    public Result index() {
        return ok(chat.render());
    }

    // Java 8 version of the method to create the user actor
    //public WebSocket<String> socket() {
    //    return WebSocket.withActor(User::props);
    //}

    public WebSocket<String> socket() {
        return WebSocket.withActor(new F.Function<ActorRef, Props>() {
            public Props apply(ActorRef out) throws Throwable {
                return User.props(out,chatManager);
                //return Props.create(User.class, out, chatManager);
                //return Props.create(EchoUser.class, out);
            }
        });
    }

}
