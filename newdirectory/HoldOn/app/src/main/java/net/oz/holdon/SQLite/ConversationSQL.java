package net.oz.holdon.SQLite;

import com.orm.SugarRecord;

/**
 * Created by Vlad Ozik
 */

public class ConversationSQL extends SugarRecord{

    public int c_id;
    public String userOne;
    public String userTwo;
    public String message;

    public ConversationSQL(int c_id, String userOne, String userTwo,  String message) {
        this.c_id = c_id;
        this.userOne = userOne;
        this.userTwo = userTwo;
        this.message = message;
    }

    public ConversationSQL() {
    }

}
