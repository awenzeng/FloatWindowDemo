package com.awen.floatwindowdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ContactWindowUtil contactWindowUtil = new ContactWindowUtil(this);
        contactWindowUtil.setDialogListener(new ContactWindowUtil.ContactWindowListener() {
            @Override
            public void onDataCallBack(String str) {
                //拨打电话代码（略）
                Intent intent = new Intent(MainActivity.this,KotlinActivity.class);
                startActivity(intent);
            }
        });
        contactWindowUtil.showContactView();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }
}
