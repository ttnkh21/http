package http.com.tst;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import api.ApiException;
import callback.ResultCallbackAdapterIs;
import callback.UCallback;
import domain.req.UpdateRequest;
import domain.resp.UpdateResp;
import domain.wrap.HttpCommonObjResp;
import http.HttpRequestFactory;
import loading.ILoading;
import loading.Porgress;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {
    private TextView mTst;
    private TextView mTst2;
    private TextView mTst3;
    private TextView mTst4;
    //copyAssetDBFile("tst-debug.apk");
    private String path = "";

    public void copyAssetFile(final String fileName) {
        final File file = new File(getFilesDir(), fileName);
        if (file.exists() && file.length() > 0) {
        } else {
            new Thread() {
                public void run() {
                    try {
                        InputStream in = getAssets().open(fileName);
                        FileOutputStream out = new FileOutputStream(file);
                        int len = 0;
                        byte buffer[] = new byte[1024];
                        while ((len = in.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                        in.close();
                        out.close();
                        path = file.getAbsolutePath();
                        Log.e("System.out", "copy database successfully!");
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("System.out", "fatal error info----" + e.getMessage());
                    }
                }
            }.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTst = (TextView) findViewById(R.id.tst);
        mTst2 = (TextView) findViewById(R.id.tst2);
        mTst3 = (TextView) findViewById(R.id.tst3);
        mTst4 = (TextView) findViewById(R.id.tst4);
        PackageManager pkgManager = getPackageManager();
        // 读写 sd card 权限非常重要, android6.0默认禁止的, 建议初始化之前就弹窗让用户赋予该权限
        boolean sdCardWritePermission =
                pkgManager.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, getPackageName()) == PackageManager.PERMISSION_GRANTED;

        // read phone state用于获取 imei 设备信息
        boolean phoneSatePermission =
                pkgManager.checkPermission(Manifest.permission.READ_PHONE_STATE, getPackageName()) == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= 23 && !sdCardWritePermission || !phoneSatePermission) {
            requestPermission();
        }
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

        mTst3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downTask();
            }
        });
        // 防止快速点击,建议使用这种方式
        ClickHelper.helper(mTst3, new CommonCallBackII() {
            @Override
            public void doCallback() {
                downTask();
            }
        });

        copyAssetFile("tst-debug.apk");
        mTst4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadTasks();
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
        // data 是 对象 , 泛型是 HttpCommonObjResp<UpdateResp>
        // data 是 数组 , 泛型是 HttpCommonResp<UpdateResp>
        // 要求返回格式一致,否则请在CommonRespWrapI->onNext(String resp) 处理 参考代码 faultTolerance(String resp);
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


    // =====================
    private static final int REQUEST_PERMISSION = 0;

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},
                REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if ((grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                copyAssetFile("tst-debug.apk");
            } else {
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private void downTask() {
        //http://download.fir.im/v2/app/install/595c5959959d6901ca0004ac?download_token=1a9dfa8f248b6e45ea46bc5ed96a0a9e&source=update
        //http://download.fir.im/v2/app/install/595c5959959d6901ca0004ac?download_token=1a9dfa8f248b6e45ea46bc5ed96a0a9e&source=update
        //http://shangjie888.oss-cn-shanghai.aliyuncs.com/qm28v9.apk
        final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置水平进度条
        dialog.setCancelable(true);// 设置是否可以通过点击Back键取消
        dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        // dialog.setIcon(R.drawable.ic_launcher);// 设置提示的title的图标，默认是没有的
        dialog.setTitle("提示");
        dialog.setMax(100);
        dialog.show();
        HttpRequestFactory.downFile("http://download.fir.im/v2/app/install/595c5959959d6901ca0004ac?download_token=1a9dfa8f248b6e45ea46bc5ed96a0a9e&source=update", new HashMap<String, String>(), new UCallback() {
            @Override
            public void onProgress(final long currentLength, final long totalLength, final float percent) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.setProgress((int) percent);
                        if (percent == 100) {
                            dialog.dismiss();
                        }
                        mTst3.setText(currentLength + "|" + totalLength + "|" + percent);
                    }
                });

            }

            @Override
            public void onFail(final int errCode, final String errMsg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        mTst3.setText(errMsg + "" + errCode);
                    }
                });
            }
        }, new ResultCallbackAdapterIs<ResponseBody>() {
            @Override
            public void doOnResponse(final ResponseBody response) {
                super.doOnResponse(response);
                System.out.println("成功了");
                try {
                    mTst3.setText("|" + response.contentLength());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("成功了" + e);
                }
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            InputStream is = response.byteStream();
                            File file = new File(Environment.getExternalStorageDirectory(), "12345.apk");
                            FileOutputStream fos = new FileOutputStream(file);
                            BufferedInputStream bis = new BufferedInputStream(is);
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = bis.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                                fos.flush();
                            }
                            fos.close();
                            bis.close();
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.out.println(e);
                        }
                    }
                }.start();

            }

            @Override
            public void doOnError(ApiException ex) {
                super.doOnError(ex);
                dialog.dismiss();
                mTst3.setText(ex + "");
            }
        });
    }

    /**
     * 可以把loading封装到 HttpFactory中去..
     */
    private void uploadTasks() {
        //
        // final Porgress mPorgress = new Porgress(MainActivity.this);
        final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置水平进度条
        dialog.setCancelable(true);// 设置是否可以通过点击Back键取消
        dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        // dialog.setIcon(R.drawable.ic_launcher);// 设置提示的title的图标，默认是没有的
        dialog.setTitle("上传");
        dialog.setMax(100);
        dialog.show();
        // 内置SD卡路径：/storage/emulated/0
        // 外置SD卡路径：/storage/extSdCard
        if (TextUtils.isEmpty(path)) {
            path = "/sdcard/Downloads/102_c5ce43c969a9684369f673838d84a447.apk";
        }
        // Environment.getExternalStorageDirectory().getPath()
        File file = new File(path);
        mTst4.setText(file.length() + "");
        HttpRequestFactory.uploadFiles("http://server.jeasonlzy.com/OkHttpUtils/upload", null, new UCallback() {
            @Override
            public void onProgress(final long currentLength, final long totalLength, final float percent) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTst4.setText(currentLength + "|" + totalLength + "|" + percent);
                    }
                });
                dialog.show();
                dialog.setProgress((int) percent);
                dialog.setMessage("上传中....");
            }

            @Override
            public void onFail(final int errCode, final String errMsg) {
                dialog.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTst4.setText(errMsg + "" + errCode);
                    }
                });
            }
        }, new ResultCallbackAdapterIs<ResponseBody>() {
            @Override
            public void doOnResponse(ResponseBody response) {
                super.doOnResponse(response);
                try {
                    dialog.dismiss();
                    mTst4.setText(response + "|" + response.contentLength());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void doOnError(ApiException ex) {
                super.doOnError(ex);
                dialog.dismiss();
                mTst4.setText(ex + "#error#");
            }
        }, new File(path));
    }
}

