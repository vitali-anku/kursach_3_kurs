package net.oz.holdon;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.oz.holdon.SQLite.UserSQL;
import net.oz.holdon.TempInfo.User;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class MainActivity extends AppCompatActivity {

  // public static String host = "https://ozik.herokuapp.com/";
    public static String host = "http://192.168.43.133:8081/";
    EditText etUsername;
    EditText etPassword;
    Button btLogIn;
    Button btSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btLogIn = (Button)findViewById(R.id.btLoginIn);
        btSignIn = (Button)findViewById(R.id.btSignIn);
        etUsername = (EditText)findViewById(R.id.etUsername);
        etPassword = (EditText)findViewById(R.id.etPassword);
        btSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
            }
        });

        List<UserSQL> userSQLs = UserSQL.listAll(UserSQL.class);
        if(!userSQLs.isEmpty()){
            etUsername.setText(userSQLs.get(0).username);
            etPassword.setText(userSQLs.get(0).password);
        }
    }

    public void OnClickLogIn(View view){
            if(etUsername.length()>3 && etPassword.length()>3){
                HttpRequestTask hrt = new HttpRequestTask();
                startAsyncTaskInParallel(hrt,"getUser",etUsername.getText().toString(),etPassword.getText().toString(),1);
            }else{
                Toast.makeText(getApplicationContext(),"Введите данные",Toast.LENGTH_SHORT).show();
            }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startAsyncTaskInParallel(HttpRequestTask task, Object... params) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,params);
    }
    public class HttpRequestTask extends AsyncTask<Object, Void, Integer> {

        User user;
        String url;
        boolean login = false;
        String request,requestJson;
        @Override
        protected Integer doInBackground(Object... params) {

            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                switch ((String)params[0]){
                    case "getUser": {
                        url = host+"findUser/"+ params[1] + "&" + params[2];
                        User.user = restTemplate.getForObject(url, User.class);
                        if(User.user != null){
                            login =true;
                            UserSQL.deleteAll(UserSQL.class);
                            UserSQL userSQL = new UserSQL(User.user.getUsername(),User.user.getPassword(),User.user.getEmail());
                            userSQL.save();
                        }
                        request = SOAPRequest((String)params[1],(String)params[2]);
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e("HttpRequestTask", e.getMessage(), e);
            }

            return null;
        }
        @Override
        protected void onPostExecute(Integer i) {
            if (login){
                Toast.makeText(getApplicationContext(),requestJson,Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this, MenuActivity.class));
            }
            else Toast.makeText(getApplicationContext(),"Введены неверные данные",Toast.LENGTH_LONG).show();
        }
        private String SOAPRequest(String username,String pass) {

            String SOAP_ACTION = "http://webservicestest.com/soap/getUserRequest";
            String METHOD_NAME = "getUserRequest";
            String NAMESPACE = "http://webservicestest.com/soap";
            String URL = "https://ozik.herokuapp.com/soap/userInfo.wsdl";

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            PropertyInfo info1 = new PropertyInfo();
            info1.setName("username");
            info1.setValue(username);
            info1.setType(String.class);
            PropertyInfo info2 = new PropertyInfo();
            info2.setName("password");
            info2.setValue(pass);
            info2.setType(String.class);
            request.addProperty(info1);
            request.addProperty(info2);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE httpTransportSE = new HttpTransportSE(URL);
            try {
                httpTransportSE.debug = true;
                httpTransportSE.call(SOAP_ACTION, envelope);
                SoapObject soapObject = (SoapObject) envelope.getResponse();
                requestJson = soapObject.toString();

                return httpTransportSE.responseDump;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }



    }
}
