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
import net.oz.holdon.TempInfo.Conversation;
import net.oz.holdon.TempInfo.Relationships;
import net.oz.holdon.TempInfo.User;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;


public class FragmentRequests extends Fragment {

    Relationships[] relRequest;
    ListView listView;

    public static final int IDM_DELR= 101;
    public static final int IDM_ACCEPT= 102;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_requests, container, false);
        listView = (ListView) view.findViewById(R.id.requestList);

        FriendsAsyncTask friendsAsyncTask = new FriendsAsyncTask();
        startAsyncTaskInParallel(friendsAsyncTask,"getRequest");

        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, IDM_DELR, Menu.NONE, "Принять");
        menu.add(Menu.NONE, IDM_ACCEPT, Menu.NONE, "Отклонить");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String key = ((TextView) info.targetView).getText().toString();

        switch (item.getItemId())
        {
            case IDM_DELR:{
                FriendsAsyncTask friendsAsyncTask = new FriendsAsyncTask();
                startAsyncTaskInParallel(friendsAsyncTask,"acceptPerson",key);
                break;
            }
            case IDM_ACCEPT:{
                FriendsAsyncTask friendsAsyncTask = new FriendsAsyncTask();
                startAsyncTaskInParallel(friendsAsyncTask,"delRequest",key);
                break;
            }
            default:
                return super.onContextItemSelected(item);
        }

        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startAsyncTaskInParallel(FriendsAsyncTask task,String... params) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params);
    }

    private class FriendsAsyncTask extends AsyncTask<Object,Object,Object> {


        @Override
        protected Object doInBackground(Object... objects) {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            String exec = (String) objects[0];
            String url;
            try{
                switch (exec){
                    case("getRequest") :{
                        url = MainActivity.host+"relRequest/" + User.user.getUsername();
                        relRequest  = restTemplate.getForObject(url, Relationships[].class);
                        break;
                    }
                    case ("delRequest"):{
                        url = MainActivity.host+"deleteFriend/"+ objects[1]
                                + "&" + User.user.getUsername();
                        restTemplate.delete(url, Conversation.class);
                        break;
                    }
                    case ("acceptPerson"): {
                        url = MainActivity.host+"acceptRequest/"+ objects[1]
                                + "&" + User.user.getUsername();
                        restTemplate.put(url, Relationships.class);
                        url = MainActivity.host+"relRequest/" + User.user.getUsername();
                        relRequest  = restTemplate.getForObject(url, Relationships[].class);
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

            for(int i=0;i<relRequest.length;i++){
                if(!relRequest[i].getUserOne().equals(User.user.getUsername()))
                    nameList.add(relRequest[i].getUserOne());
                else nameList.add(relRequest[i].getUserTwo());
            }

            ArrayAdapter<String> listViewAdapter = new ArrayAdapter<>(
                    getActivity(), R.layout.item_list, nameList
            );
            listView.setAdapter(listViewAdapter);
        }
    }
}
