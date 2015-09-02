package controllers;

import actors.User;
import akka.actor.ActorRef;
import akka.actor.Props;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.chat;

import static akka.pattern.Patterns.ask;

public class Application extends Controller {

    public F.Promise<Result> index() {
        ActorRef myActor = Akka.system().actorFor("akka://application/user/ChatManager");
        return F.Promise.wrap(ask(myActor, "GiveMeTheChatIP", 10000)).map(
                new F.Function<Object, Result>() {
                    public Result apply(Object response) {
                        return ok(chat.render(response+":9000"));
                    }
                }
        );
    }

    // Java 8 version of the method to create the user actor
    //public WebSocket<String> socket() {
    //    return WebSocket.withActor(User::props);
    //}

    public WebSocket<String> socket() {
        return WebSocket.withActor(new F.Function<ActorRef, Props>() {
            public Props apply(ActorRef out) throws Throwable {
                return User.props(out, Akka.system().actorFor("akka://application/user/ChatManager"));
                //return Props.create(User.class, out, chatManager);
            }
        });
    }

}
