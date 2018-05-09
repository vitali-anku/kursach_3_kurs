package net.oz.holdon;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.oz.holdon.SQLite.ConversationSQL;
import net.oz.holdon.TempInfo.Conversation;
import net.oz.holdon.TempInfo.User;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MessagesActivity extends AppCompatActivity {

    Conversation[] conversations;
    private EditText messageET;
    private ListView messagesContainer;
    private Button sendBtn;
    private ChatAdapter adapter;
    private boolean cancel = true;
    private ArrayList<Conversation> messagesHistory;
    String userTwo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageET = (EditText) findViewById(R.id.messageEdit);
        sendBtn = (Button) findViewById(R.id.chatSendButton);
        TextView companionLabel = (TextView) findViewById(R.id.friendLabel);

        userTwo = getIntent().getStringExtra("userTwo");
        AsyncTaskMessages asyncTaskMessagesGet = new AsyncTaskMessages();
        startAsyncTaskInParallelATM(asyncTaskMessagesGet,"getMessages",User.user.getUsername(),userTwo);

        companionLabel.setText(userTwo);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageET.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }

                Conversation chatMessage = new Conversation();
                chatMessage.setUserOne(User.user.getUsername());
                chatMessage.setMessage(messageText);
                chatMessage.setUserTwo(userTwo);
                chatMessage.setC_id(100);

                messageET.setText("");
                AsyncTaskMessages asyncTaskMessagesSend = new AsyncTaskMessages();
                startAsyncTaskInParallelATM(asyncTaskMessagesSend,"sendMessage",chatMessage);
            }
        });


    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startAsyncTaskInParallelATM(AsyncTaskMessages task,Object... params) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params);
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startAsyncTaskInParallelLAT(LoopMessAsyncTask task,String... params) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params);
    }
    private void loadMessagesHistory(){

        adapter = new ChatAdapter(MessagesActivity.this,new ArrayList<Conversation>());
        messagesContainer.setAdapter(adapter);

        for(int i=0; i<messagesHistory.size(); i++) {
            Conversation message = messagesHistory.get(i);
            displayMessage(message);
        }

        LoopMessAsyncTask loopMessAsyncTask = new LoopMessAsyncTask();
        startAsyncTaskInParallelLAT(loopMessAsyncTask,User.user.getUsername(),userTwo);
    }

    private void DBSynchronization(){
        messagesHistory = new ArrayList<>();
        if(conversations != null)
        for(int i =0;i<conversations.length;i++){
            ConversationSQL conversationSQL = new ConversationSQL(conversations[i].getC_id(),
                                                                conversations[i].getUserOne(),
                                                                conversations[i].getUserTwo(),
                                                                conversations[i].getMessage());
            conversationSQL.save();
        }

        List<ConversationSQL> cvList1 = ConversationSQL.find(ConversationSQL.class,"USER_ONE=? and USER_TWO=?", new String[] {User.user.getUsername(),userTwo});
        List<ConversationSQL> cvList2 = ConversationSQL.find(ConversationSQL.class,"USER_ONE=? and USER_TWO=?", new String[] {userTwo,User.user.getUsername()});
        List<ConversationSQL> cvList = joinListst(cvList1,cvList2);

        Collections.sort(cvList, new Comparator<ConversationSQL>() {
            @Override
            public int compare(ConversationSQL conversationSQL, ConversationSQL t1) {
                return conversationSQL.getId().compareTo(t1.getId());
            }
        });
        if(!cvList.isEmpty()){
            for(int i=0;i<cvList.size();i++){
                Conversation conversation = new Conversation(cvList.get(i).c_id,cvList.get(i).userOne,
                                                            cvList.get(i).userTwo,cvList.get(i).message);
                messagesHistory.add(conversation);
            }
        }
    }
    public void displayMessage(Conversation message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    private class AsyncTaskMessages extends AsyncTask<Object,Object,Object>{

        boolean postSetConf = true;
        @Override
        protected Object doInBackground(Object... objects) {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            String exec = (String) objects[0];
            String url;
            try {
                switch (exec) {
                    case "getMessages": {
                        int countMessagesInLocalDB = (int)ConversationSQL.count(ConversationSQL.class,
                                "USER_ONE=? and USER_TWO=?",
                                new String[] {User.user.getUsername(),userTwo})+
                                (int)ConversationSQL.count(ConversationSQL.class,
                                        "USER_ONE=? and USER_TWO=?",
                                        new String[] {userTwo,User.user.getUsername()});
                        url = MainActivity.host + "findConversationForUserOne/" + objects[1] + "&" +objects[2]+ "&" + countMessagesInLocalDB;
                        conversations = restTemplate.getForObject(url, Conversation[].class);
                        if(conversations == null){
                            ConversationSQL.deleteAll(ConversationSQL.class,"USER_ONE=? AND USER_TWO=?", new String[] {User.user.getUsername(),(String)objects[1]});
                            ConversationSQL.deleteAll(ConversationSQL.class,"USER_ONE=? AND USER_TWO=?", new String[] {(String)objects[1],User.user.getUsername()});
                        }
                        break;
                    }
                    case "sendMessage": {
                        url = MainActivity.host + "setConversation";
                        postSetConf =false;
                        restTemplate.postForObject(url, objects[1], Conversation.class);

                        break;
                    }
                }
            }catch (Exception e ){

            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(postSetConf){
            DBSynchronization();
            loadMessagesHistory();}
            else postSetConf =true;
        }
    }

    private class LoopMessAsyncTask extends AsyncTask<Object,Object,Object>{

        String url;
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(cancel){
                DBSynchronization();
                loadMessagesHistory();
            }
        }

        @Override
        protected Object doInBackground(Object... objects) {
            if(cancel) {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                for (; ; ) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                        if(!cancel) return null;

                        int countMessagesInLocalDB = (int) ConversationSQL.count(ConversationSQL.class,
                                "USER_ONE=? and USER_TWO=?",
                                new String[]{User.user.getUsername(), userTwo}) +
                                (int) ConversationSQL.count(ConversationSQL.class,
                                        "USER_ONE=? and USER_TWO=?",
                                        new String[]{userTwo, User.user.getUsername()});
                        url = MainActivity.host + "findConversationForUserOne/" + objects[0] + "&" + objects[1] + "&" + countMessagesInLocalDB;
                        conversations = restTemplate.getForObject(url, Conversation[].class);
                        if (conversations.length >= 1) {
                            return null;
                        }
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        return null;
                    }
                }
            }else return null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancel =false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancel =false;
    }

    private static List<ConversationSQL>joinListst(final List<ConversationSQL> listA,
                                                   final List<ConversationSQL> listB){
        boolean aEmpty = (listA == null) || listA.isEmpty();
        boolean bEmpty = (listB == null) || listB.isEmpty();

        if(aEmpty & bEmpty){
            return new ArrayList<>();
        } else if(aEmpty){
            return new ArrayList<>(listB);
        } else if(bEmpty){
            return new ArrayList<>(listA);
        } else {
            ArrayList<ConversationSQL> result = new ArrayList<>(listA.size() + listB.size());
            result.addAll(listA);
            result.addAll(listB);
            return result;
        }
    }
}
