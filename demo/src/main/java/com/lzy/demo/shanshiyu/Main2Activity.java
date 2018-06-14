package com.lzy.demo.shanshiyu;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lzy.demo.R;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.cookie.store.CookieStore;
import com.lzy.okgo.model.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import www.thl.com.utils.ToastUtils;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {

    private ScrollView scrollView;
    private EditText cookies;
    private EditText key;
    private EditText num;
    private Button run;
    private TextView msg;

    private int dNum = 0;
    private int mNum = 0;
    private Handler handler;
    private StringBuffer buf;

    private String strKey;


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        cookies = (EditText) findViewById(R.id.cookies);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        key = (EditText) findViewById(R.id.key);
        num = (EditText) findViewById(R.id.num);
        run = (Button) findViewById(R.id.run);
        msg = (TextView) findViewById(R.id.msg);
        run.setOnClickListener(this);

        CookieStore cookieStore = OkGo.getInstance().getCookieJar().getCookieStore();
        cookieStore.removeAllCookie();
        buf = new StringBuffer();
        handler = new Handler() {
            @Override
            public void handleMessage(Message mMessage) {
                super.handleMessage(mMessage);
                if (mMessage.arg2 == 200 && mMessage.arg1 == 100) {
                    page = page + 1;
                    dzRun();
                } else {
//                    buf.append(mMessage.obj + "");
                    buf.insert(0, mMessage.obj);
                    msg.setText(buf.toString());
                    scrollView.pageScroll(ScrollView.FOCUS_UP);
                }
            }
        };
    }


    @Override
    public void onClick(View view) {

        mNum = 0;
        dNum = TextUtils.isEmpty(num.getText().toString()) ? 10 : Integer.parseInt(num.getText().toString());
        String value = cookies.getText().toString();
        String value1 = key.getText().toString();
        if (TextUtils.isEmpty(value)) {
            ToastUtils.showLong("请输入账号");
            return;
        }
        if (TextUtils.isEmpty(value1)) {
            ToastUtils.showLong("请输入密码");
            return;
        }
        OkGo.<String>post("http://api.ybzha.com/wapmall/login")
                .tag(this)
                .params("UserLogin[username]", value)
                .params("UserLogin[password]", value1)
                .params("UserLogin[rememberMe]", "1")
                .params("UserLogin[channel_key]", "")
                .params("key", "")
                .execute(new StringDialogCallback(this) {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {

                            Message message = new Message();
                            message.arg1 = 1;
                            message.arg2 = 2;
                            message.obj = "登录成功" + "\n";
                            handler.sendMessage(message);
                            Gson gson = new Gson();
                            Login data = gson.fromJson(response.body(), Login.class);
                            strKey = data.result.key;
                            dzRun();
                        } catch (Exception e) {
                            Message message = new Message();
                            message.arg1 = 1;
                            message.arg2 = 2;
                            message.obj = "账号异常，登录失败" + "\n";
                            handler.sendMessage(message);
                        }
                    }
                });
    }

    private int page = 1;

    private void dzRun() {
        OkGo.<String>post("http://api.ybzha.com/bbs/HomeList")
                .tag(this)
                .params("page", page)
                .params("sort", "create_at")
                .params("is_elite", "0")
                .params("maxid", "0")
                .params("key", "")
                .execute(new StringCallback() {

                    @Override
                    public void onError(Response<String> response) {
                        Message message = new Message();
                        message.arg1 = 1;
                        message.arg2 = 2;
                        message.obj = "获取数据失败" + "\n";
                        handler.sendMessage(message);
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        Message message = new Message();
                        message.arg1 = 1;
                        message.arg2 = 2;
                        message.obj = "获取数据成功" + "\n";
                        handler.sendMessage(message);
                        Gson gson = new Gson();
                        Data data = gson.fromJson(response.body(), Data.class);
                        if (data != null) {
                            dianzan(data);
                        }
                    }
                });
    }

    private void dianzan(Data data) {

        final List<Data.ResultBean.ListBean> list = data.result.list;
        if (list != null && list.size() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (Data.ResultBean.ListBean item : list) {
                        if (item.is_up.equals("1")) {
                            Message message = new Message();
                            message.arg1 = 1;
                            message.arg2 = 2;
                            message.obj = "id:" + item.id + "已点赞" + "\n";
                            handler.sendMessage(message);

                        } else {
                            try {
                                if (mNum >= dNum) {
                                    Message message = new Message();
                                    message.arg1 = 1;
                                    message.arg2 = 2;
                                    message.obj = "完成点赞，共计：" + mNum + "\n";
                                    handler.sendMessage(message);
                                    return;
                                }
                                okhttp3.Response execute = OkGo.<String>post("http://api.ybzha.com/bbs/Up")
                                        .params("id", item.id)
                                        .params("key", strKey)
                                        .execute();
                                String data = execute.body().string();
                                JSONObject json = new JSONObject(data);
                                int code = json.optInt("code");
                                if (code == 200) {
                                    Message message = new Message();
                                    message.arg1 = 1;
                                    message.arg2 = 2;
                                    message.obj = "帖子id:" + item.id + "点赞成功,第" + mNum + "个赞" + "\n";
                                    handler.sendMessage(message);
                                } else {
                                    Message message = new Message();
                                    message.arg1 = 1;
                                    message.arg2 = 2;
                                    message.obj = "id:" + item.id + "点赞失败" + "\n";
                                    handler.sendMessage(message);
                                }
                                mNum = mNum + 1;
                                {
                                    Message message = new Message();
                                    message.arg1 = 1;
                                    message.arg2 = 2;
                                    message.obj = "使劲刷10个阅读量！！！！！！" + "\n";
                                    handler.sendMessage(message);
                                }
                                for (int i = 0; i < 10; i++) {
                                    OkGo.<String>post("http://m.ybzha.com/post/detail/" + item.id)
                                            .execute();
                                    okhttp3.Response execute2 = OkGo.<String>post("http://api.ybzha.com/bbs/PostDetail")
                                            .params("id", item.id)
                                            .params("page", "1")
                                            .params("maxid", "1")
                                            .params("key", strKey)
                                            .execute();
                                    String data2 = execute2.body().string();
                                    JSONObject mJSONObject = new JSONObject(data2);
                                    int code2 = mJSONObject.optInt("code");
                                    if (code2 == 200) {
                                        try {
                                            JSONObject dataJson = mJSONObject.optJSONObject("result").optJSONObject("data");
                                            String view_num = dataJson.optString("view_num");
                                            Message message = new Message();
                                            message.arg1 = 1;
                                            message.arg2 = 2;
                                            message.obj = "阅读量+1=" + view_num + "\n";
                                            handler.sendMessage(message);
                                        } catch (Exception e) {

                                        }
                                    } else {
                                        Message message = new Message();
                                        message.arg1 = 1;
                                        message.arg2 = 2;
                                        message.obj = "刷阅读量失败" + "\n";
                                        handler.sendMessage(message);
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                Message message = new Message();
                                message.arg1 = 1;
                                message.arg2 = 2;
                                message.obj = "休息三秒。。。" + "\n";
                                handler.sendMessage(message);
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    if (mNum < dNum) {

                        Message message = new Message();
                        message.arg1 = 100;
                        message.arg2 = 200;
                        message.obj = "获取数据" + "\n";
                        handler.sendMessage(message);
                    } else {
                        Message message = new Message();
                        message.arg1 = 1;
                        message.arg2 = 2;
                        message.obj = "完成点赞，共计：" + mNum + "\n";
                        handler.sendMessage(message);
                    }

                }
            }).start();
        }
    }


}
