package net.oz.holdon.TempInfo;

public class Conversation {

    private int c_id;
    private String userOne;
    private String userTwo;
    private String message;

    public Conversation(int c_id, String userOne, String userTwo,  String message) {
        this.c_id = c_id;
        this.userOne = userOne;
        this.userTwo = userTwo;
        this.message = message;
    }

    public Conversation() {
    }

    public int getC_id() {
        return c_id;
    }

    public void setC_id(int c_id) {
        this.c_id = c_id;
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


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "c_id=" + c_id +
                ", userOne='" + userOne + '\'' +
                ", userTwo='" + userTwo + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
