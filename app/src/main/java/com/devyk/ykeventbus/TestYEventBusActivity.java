package com.devyk.ykeventbus;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.devyk.devykbus.Constants;
import com.devyk.devykbus.core.YEventBus;

/**
 * <pre>
 *     author  : devyk on 2019-07-27 19:58
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is TestYEventBusActivity
 * </pre>
 */
public class TestYEventBusActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        findViewById(R.id.sendMessage1).setOnClickListener(this);
        findViewById(R.id.sendMessage2).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sendMessage1) {
            YEventBus.getDefault().post(Constants.TAG_1, "发送 TAG 为 1 的消息");
        } else if (view.getId() == R.id.sendMessage2) {
            YEventBus.getDefault().post(Constants.TAG_2, "发送 TAG 为 2 的消息");
        }

    }
}
