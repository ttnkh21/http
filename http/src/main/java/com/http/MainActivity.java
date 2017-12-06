package com.http;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import api.ApiException;
import callback.ResultCallbackAdapterIs;
import domain.req.UpdateRequest;
import domain.resp.UpdateResp;
import domain.wrap.HttpCommonObjResp;
import http.HttpRequestFactory;
import loading.ILoading;
import loading.Porgress;

public class MainActivity extends AppCompatActivity {
    private TextView mTst;
    private TextView mTst2;
    private TextView mTst3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTst = (TextView) findViewById(R.id.tst);
        mTst2 = (TextView) findViewById(R.id.tst2);
        mTst3 = (TextView) findViewById(R.id.tst3);
/**
 {
 "code": 1,
 "msg": "",
 "data": {
 "id": "2",
 "channel": "xiongdi",
 "vnumber": "9",
 "url": "http://shangjie888.oss-cn-shanghai.aliyuncs.com/qm28v9.apk",
 "content": "1.修改升级提示界面效果\n2.修正游客登录cid获取失败问题\n3.修正点击忘记密码去修改密码提交失败问题\n4.修改已知崩溃问题\n5.更改替换部分ui\n6.修改公式失败修正\n",
 "platform": "android",
 "force": 0,
 "name": null,
 "switch": "1",
 "versionnum": "1.6.4"
 }
 }
 */
        mTst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                req001();
            }
        });
        mTst2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                req002();
            }
        });
    }

    private void req002() {
        Porgress mPorgress = new Porgress(this);
        mPorgress.setProgress(100);
        mPorgress.setMessage("加载呢");
        HttpRequestFactory.doPost(new UpdateRequest(), new ResultCallbackAdapterIs<String>(this) {
            @Override
            public void doOnError(ApiException ex) {
                super.doOnError(ex);
                mTst2.setText(ex + "#error#");
            }

            @Override
            public void doOnResponse(String response) {
                super.doOnResponse(response);
                mTst2.setText(response + "");
                // 自己手动解析
            }
        }, mPorgress);
    }

    private void req001() {
        ILoading ll = new ILoading(this);
        ll.setMessage("网络请求");
// data 是 对象 , 这样调用
        HttpRequestFactory.doPost(new UpdateRequest(), new ResultCallbackAdapterIs<HttpCommonObjResp<UpdateResp>>(this) {
            @Override
            public void doOnError(ApiException ex) {
                super.doOnError(ex);
                mTst.setText(ex + "#error#");
            }

            @Override
            public void doOnResponse(HttpCommonObjResp<UpdateResp> response) {
                super.doOnResponse(response);
                if (response.isSuccess()) {
                    UpdateResp resp = response.getData();
                    mTst.setText(resp + "");
                } else {
                    mTst.setText(response + "#结果#");
                }
            }
        }, ll);
    }


}
