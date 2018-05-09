package net.oz.holdon.SQLite;

import com.orm.SugarRecord;

/**
 * Created by Vlad Ozik
 */

public class UserSQL extends SugarRecord<UserSQL>{
    public String username;
   public String password;
   public String email;


    public UserSQL() {
    }

    public UserSQL(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }


}
