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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import static akka.pattern.Patterns.ask;

public class Application extends Controller {

    public F.Promise<Result> index() {
        ActorRef chatManager = Akka.system().actorFor("akka://application/user/ChatManager");
        return F.Promise.wrap(ask(chatManager, "GiveMeTheChatIP", 10000)).map(
                new F.Function<Object, Result>() {
                    public Result apply(Object response) {
                        return ok(chat.render(response+":9000", getAmazonIP()+":9000"));
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
