package net.oz.holdon;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.oz.holdon.TempInfo.User;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class RegistrationActivity extends AppCompatActivity {

    EditText etUsername;
    EditText etEmail;
    EditText etPassword;
    Button btSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        btSignUp = (Button) findViewById(R.id.btSignUp);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);

        btSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(etPassword.length()<4 && etEmail.length()<4 && etUsername.length()<4)
                    Toast.makeText(getApplicationContext(),"Во всех полях должно быть не менее 4 символов", Toast.LENGTH_LONG).show();
                else{
                    RegistrationAsyncTask registrationAsynkTask = new RegistrationAsyncTask();
                    User.user = new User(etUsername.getText().toString(),
                                            etPassword.getText().toString(),
                                            etEmail.getText().toString());
                    startAsyncTaskInParallel(registrationAsynkTask,User.user);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startAsyncTaskInParallel(RegistrationAsyncTask task, Object... params) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params);
    }

    private class RegistrationAsyncTask extends AsyncTask<Object, Object, Object>{

        @Override
        protected Object doInBackground(Object... objects) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                String url = MainActivity.host + "users";
                restTemplate.postForObject(url, objects[0], User.class);
            }catch (Exception we){

            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Toast.makeText(getApplicationContext(),"Вы зарегистрированы", Toast.LENGTH_LONG).show();
            startActivity(new Intent(RegistrationActivity.this, MenuActivity.class));
        }
    }
}
