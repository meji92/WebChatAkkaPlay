package actors;

import akka.actor.ActorRef;

public class UserData{
    private String name;
    private ActorRef actorRef;

    public UserData (String name, ActorRef actorRef){
        this.name = name;
        this.actorRef = actorRef;
    }

    public String getName (){
        return name;
    }

    public ActorRef getActorRef(){
        return actorRef;
    }
}
