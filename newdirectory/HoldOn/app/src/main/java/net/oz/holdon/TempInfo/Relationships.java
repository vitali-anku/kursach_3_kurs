package net.oz.holdon.TempInfo;


/**
 * Created by Vlad Ozik on 11.05.2017.
 */

public class Relationships {

    private String userOne;
    private String userTwo;
    private int status;


    public Relationships(String userOne, String userTwo,int status) {
        this.userOne = userOne;
        this.userTwo = userTwo;
        this.status = status;
    }

    public Relationships() {
    }

    public String getUserOne() {
        return userOne;
    }

    public void setUserOne(String userOne) {
        this.userOne = userOne;
    }

    public String getUserTwo() {
        return userTwo;
    }

    public void setUserTwo(String userTwo) {
        this.userTwo = userTwo;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
