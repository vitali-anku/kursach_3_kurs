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
import android.widget.SearchView;
import android.widget.TextView;

import net.oz.holdon.MainActivity;
import net.oz.holdon.MessagesActivity;
import net.oz.holdon.R;
import net.oz.holdon.TempInfo.Relationships;
import net.oz.holdon.TempInfo.User;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class FragmentSearch extends Fragment {

    SearchView searchView;
    String[] users;
    ListView listView;

    public static final int IDM_ADDF= 101;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        listView = (ListView) view.findViewById(R.id.list_view);
        searchView = (SearchView) view.findViewById(R.id.search);
        searchView.setQueryHint("Поиск пользователей");
        FriendsAsyncTask friendsAsyncTask = new FriendsAsyncTask();
        startAsyncTaskInParallel(friendsAsyncTask,"getAllUsers");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                FriendsAsyncTask friendsAsyncTask = new FriendsAsyncTask();
                if(query.equals(""))
                    startAsyncTaskInParallel(friendsAsyncTask,"getAllUsers");
                else
                    startAsyncTaskInParallel(friendsAsyncTask,"getUsers",query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                FriendsAsyncTask friendsAsyncTask = new FriendsAsyncTask();
                if(newText.equals(""))
                    startAsyncTaskInParallel(friendsAsyncTask,"getAllUsers");
                else
                    startAsyncTaskInParallel(friendsAsyncTask,"getUsers",newText);
                return false;
            }
        });
        return view;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startAsyncTaskInParallel(FriendsAsyncTask task, String... params) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, IDM_ADDF, Menu.NONE, "Подать заявку в друзья");

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String key = ((TextView) info.targetView).getText().toString();

        switch (item.getItemId())
        {
            case IDM_ADDF:{
                FriendsAsyncTask friendsAsyncTask = new FriendsAsyncTask();
                startAsyncTaskInParallel(friendsAsyncTask,"setRequest",key);
                break;
            }
            default:
                return super.onContextItemSelected(item);
        }

        return true;
    }

    private class FriendsAsyncTask extends AsyncTask<Object,Object,Object> {


        @Override
        protected Object doInBackground(Object... objects) {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            String exec = (String) objects[0];
            String url;
            try {
                switch (exec) {
                    case ("getAllUsers"): {
                        url = MainActivity.host + "usersSearch/all";
                        users = restTemplate.getForObject(url, String[].class);
                        break;
                    }
                    case ("getUsers"): {
                        url = MainActivity.host + "usersSearch/" + objects[1];
                        users = restTemplate.getForObject(url, String[].class);
                        break;
                    }
                    case ("setRequest"): {
                        url = MainActivity.host + "setRequest";
                        Relationships r = new Relationships(User.user.getUsername(), (String) objects[1], 0);
                        restTemplate.postForObject(url, r, Relationships.class);
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

            for(int i=0;i<users.length;i++){
                if(!users[i].equals(User.user.getUsername()))
                    nameList.add(users[i]);
            }

            ArrayAdapter<String> listViewAdapter = new ArrayAdapter<>(
                    getActivity(), R.layout.item_list, nameList
            );
            listView.setAdapter(listViewAdapter);
        }
    }

}
