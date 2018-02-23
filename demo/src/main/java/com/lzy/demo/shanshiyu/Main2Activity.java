package com.lzy.demo.shanshiyu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lzy.demo.R;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cookie.store.CookieStore;
import com.lzy.okgo.model.Response;

import www.thl.com.utils.MD5Utils;
import www.thl.com.utils.PhoneUtils;
import www.thl.com.utils.ToastUtils;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        CookieStore cookieStore = OkGo.getInstance().getCookieJar().getCookieStore();
        cookieStore.removeAllCookie();
        long time = System.currentTimeMillis() / 1000;

        OkGo.<String>post("http://api.3sy.com/app3" + "/Login")
                .tag(this)
                .headers("St", time + "")
                .headers("Sign", getSign("/Login", time + ""))
                .params("UserLogin[username]", "lqf")
                .params("UserLogin[password]", "123456")
                .execute(new StringDialogCallback(this) {

                    @Override
                    public void onError(Response<String> response) {

                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        ToastUtils.showLong(response.body());
                    }
                });
    }

    private String getSign(String uri, String time) {
        StringBuilder sb = new StringBuilder("/app3" + uri);
        sb.append("?st=").append("PE5xnw5JIInFmZMf8tbJodN6Y").append(time).append(PhoneUtils.getIMEI());
        return  MD5Utils.MD5(sb.toString()).toLowerCase();
    }
}
