package net.oz.holdon.Fragments;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.oz.holdon.MainActivity;
import net.oz.holdon.MessagesActivity;
import net.oz.holdon.R;
import net.oz.holdon.SQLite.ConversationSQL;
import net.oz.holdon.TempInfo.Conversation;
import net.oz.holdon.TempInfo.Relationships;
import net.oz.holdon.TempInfo.User;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class FragmentFriends extends Fragment {

    public static Relationships[] friends;
    ListView listView;

    public static final int IDM_DELM= 101;
    public static final int IDM_DELF = 102;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        listView = (ListView) view.findViewById(R.id.friendsList);

        FriendsAsyncTask friendsAsyncTask = new FriendsAsyncTask();
       startAsyncTaskInParallel(friendsAsyncTask,"getFriends");
        return view;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, IDM_DELM, Menu.NONE, "Удалить переписку");
        menu.add(Menu.NONE, IDM_DELF, Menu.NONE, "Удалить из друзей");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String key = ((TextView) info.targetView).getText().toString();

        switch (item.getItemId())
        {
            case IDM_DELM:{
                FriendsAsyncTask friendsAsyncTask = new FriendsAsyncTask();
                startAsyncTaskInParallel(friendsAsyncTask,"delMess",key);
                break;}
            case IDM_DELF:
                FriendsAsyncTask friendsAsyncTask = new FriendsAsyncTask();
                startAsyncTaskInParallel(friendsAsyncTask,"delFriend",key);
                break;
            default:
                return super.onContextItemSelected(item);
        }

        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startAsyncTaskInParallel(FriendsAsyncTask task,String... params) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params);
    }

    private class FriendsAsyncTask extends AsyncTask<Object,Object,Object>{


        @Override
        protected Object doInBackground(Object... objects) {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            String exec = (String) objects[0];
            String url;
            try {
                switch (exec) {
                    case ("getFriends"): {
                        url = MainActivity.host + "relationship/" + User.user.getUsername();
                        friends = restTemplate.getForObject(url, Relationships[].class);
                        break;
                    }
                    case ("delMess"): {
                        url = MainActivity.host + "deleteAllMessages/" + User.user.getUsername()
                                + "&" + objects[1];
                        restTemplate.delete(url, Conversation.class);
                        ConversationSQL.deleteAll(ConversationSQL.class,"USER_ONE=? AND USER_TWO=?", new String[] {User.user.getUsername(),(String)objects[1]});
                        ConversationSQL.deleteAll(ConversationSQL.class,"USER_ONE=? AND USER_TWO=?", new String[] {(String)objects[1],User.user.getUsername()});
                        break;
                    }
                    case ("delFriend"): {
                        url = MainActivity.host + "deleteFriend/" + User.user.getUsername()
                                + "&" + objects[1];
                        restTemplate.delete(url, Relationships.class);
                        url = MainActivity.host + "relationship/" + User.user.getUsername();
                        friends = restTemplate.getForObject(url, Relationships[].class);
                        break;
                    }
                }
            }catch (Exception e){

            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            setAdapterForListView();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getActivity(), MessagesActivity.class);
                    String name = adapterView.getItemAtPosition(i).toString();
                    intent.putExtra("userTwo",name);
                    startActivity(intent);
                }
            });
            registerForContextMenu(listView);
        }

        private void setAdapterForListView(){
            List<String> nameList = new ArrayList<>();

            for(int i=0;i<friends.length;i++){
                if(!friends[i].getUserOne().equals(User.user.getUsername()))
                    nameList.add(friends[i].getUserOne());
                else nameList.add(friends[i].getUserTwo());
            }

            ArrayAdapter<String> listViewAdapter = new ArrayAdapter<>(
                    getActivity(), R.layout.item_list, nameList
            );
            listView.setAdapter(listViewAdapter);
        }
    }
}
