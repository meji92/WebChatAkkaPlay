package messages;

import java.io.Serializable;

public class DuplicatedUser implements Serializable{

    private String user;

    public DuplicatedUser (String user){
        this.user = user;
    }

    public String getUser() {
        return user;
    }
}
