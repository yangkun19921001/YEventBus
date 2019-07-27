package com.devyk.ykeventbus;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.devyk.devykbus.Constants;
import com.devyk.devykbus.core.YEventBus;
import com.devyk.devykbus.core.YSubscribe;
import com.devyk.devykbus.core.YThreadMode;

public class MainActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //开始注册事件。模仿 EventBus
        YEventBus.getDefault().register(this);

        findViewById(R.id.btn_test).setOnClickListener(this);
    }


    /**
     * 这里是自定义的注册，最后通过反射来获取当前类里面的订阅者
     *
     * @param meg
     */
    @YSubscribe(threadMode = YThreadMode.MAIN, tag = Constants.TAG_1)
    public void onEvent(String meg) {
        Toast.makeText(getApplicationContext(), "收到：" + meg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 这里是自定义的注册，最后通过反射来获取当前类里面的订阅者
     *
     * @param meg
     */
    @YSubscribe(threadMode = YThreadMode.MAIN, tag = Constants.TAG_2)
    public void onEvent2(String meg) {
        Toast.makeText(getApplicationContext(), "收到：" + meg, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销事件
        YEventBus.getDefault().unRegister(this);
    }


    @Override
    public void onClick(View view) {
        startActivity(new Intent(this, TestYEventBusActivity.class));
    }
}
