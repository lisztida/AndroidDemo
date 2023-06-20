package com.example.map;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private Button login;
    private EditText name,password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        find();
    }

    private void find() {
        login = findViewById(R.id.login);
        name = findViewById(R.id.edname);
        password = findViewById(R.id.edpassword);

        login.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        Thread t = new Thread(() -> {
            int id = v.getId();
            if (id == R.id.login) {
                String edusername = name.getText().toString();
                String edpassword = password.getText().toString();
                String sendUrl = "http://192.168.10.23:8030/login?username="+edusername+"&password="+edpassword;
                try {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create("{\"username\":\"aaa\", \"password\":\"as\"}", MediaType.get("application/json; charset=utf-8"));
                    Request request = new Request.Builder()
                            .url(sendUrl)
                            .post(body)
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        String res = response.body().string();
                        System.out.println(res);
                        if(!res.equals("111")){
                            LoginActivity.this.runOnUiThread(() -> {
                                Toast.makeText(this,"登录成功",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this,MainActivity.class);
                                intent.putExtra("username",res);
                                startActivity(intent);
                            });
                        }
                        else{
                            LoginActivity.this.runOnUiThread(() -> {
                                Toast.makeText(this,"用户名或密码错误",Toast.LENGTH_SHORT).show();
                            });

                        }
                    } catch (Exception e) {

                        System.out.println(e);
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
        t.start();
    }
}
