package qianfeng.handler_application;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 子线程更新UI的几种方式：
 * 1.runOnUiThread
 * 2.mHandler.sendMessage()
 * 3.
 * <p/>
 * <p/>
 * <p/>
 * Handler使用步骤：
 * 1.创建一个Handler实例,并重写handleMessage方法，该方法用来处理各个线程发送来的消息
 * 2.在获取数据的时候发送Handler消息
 * 3.在handleMessage方法中处理各线程发送来的消息
 */

public class MainActivity extends AppCompatActivity {
    private ImageView iv;
    private ImageView iv2;
    private int position = 0;

    private int[] imgs = new int[]{R.drawable.tab1,R.drawable.tab2,R.drawable.tab3,R.drawable.tab4};

    private static final int DOWNLOADSUCCESSFUL = 0;
    private static final int DOWNLOADFAILT = 1;


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what)
            {
                case DOWNLOADSUCCESSFUL:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    iv.setImageBitmap(bitmap);
                    break;
                case DOWNLOADFAILT:
                    Toast.makeText(MainActivity.this,"下载失败",Toast.LENGTH_SHORT).show();

                    break;
                case 2:
                    iv2.setImageResource(imgs[(position++)%4]);
                    mHandler.sendEmptyMessageDelayed(2,2000);
                    break;
            }

        }
    };
    private TextView tv;
    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = ((ImageView) findViewById(R.id.iv));
        iv2 = ((ImageView) findViewById(R.id.iv2));

        tv = ((TextView) findViewById(R.id.tv));

        mHandler.sendEmptyMessageDelayed(2,2000);
        // Message message = mHandler.obtainMessage(); 这种方式用到了 消息池里面自带的50条消息，这种方法更节省内存，不用自己new的！它不够会自己new。

        /*
          /*Message的创建方式*/
        //获取一个Message对象，推荐方式
     //   Message msg = mHandler.obtainMessage();
        //获取一个Message对象，推荐方式
//                        Message msg = Message.obtain();
        //不推荐
//                        Message msg = new Message();

                        /*Message可以携带的数据类型*/
        //msg携带的消息对象
     //   msg.obj = bitmap;
        //给每条消息添加标识符
   //     msg.what = DOWNLOADSUCCESSFUL;
        //携带int类型数据
//                        msg.arg1
//                                msg.arg2
        //携带一个bundle类型的数据
//                        msg.setData();


                        /*消息的发送方式*/
        //发送一条普通消息
     //   mHandler.sendMessage(msg);
        //延迟发送一条消息，第二个参数表示消息发送的延迟时间
//                        mHandler.sendMessageDelayed()
        //定时发送一条消息，第二个参数表示定时时间
//                        mHandler.sendMessageAtTime()
        //延迟发送一条空消息
//                        mHandler.sendEmptyMessageDelayed()
        //发送一条空消息
//                        mHandler.sendEmptyMessage()
        //定时发送一条空消息
//                        mHandler.sendEmptyMessageAtTime()
        //android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
//                        iv.setImageBitmap(bitmap);
      //   */
    }

    public void click(View view) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                try {
                    URL url = new URL("http://tnfs.tngou.net/image/cook/150802/1340f07baad474a757825191701d5e1e.jpg");
                    con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(5*1000);
                    if(con.getResponseCode() == 200)
                    {
                        Bitmap bitmap = BitmapFactory.decodeStream(con.getInputStream());
                        Message msg = mHandler.obtainMessage();
                        msg.obj = bitmap;
                        msg.what = DOWNLOADSUCCESSFUL;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = DOWNLOADFAILT;
                    mHandler.sendMessage(msg);
                    // e.printStackTrace();
                }

            }
        }).start();

    }

    public void click2(View view) {  // 点击加载文本

        new Thread(new Runnable() {
            @Override
            public void run() {

                HttpURLConnection con = null;
                try {
                    URL url = new URL("http://www.baidu.com");
                    con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(5000);
                    con.connect();
                    if(con.getResponseCode() == 200)
                    {
                        final StringBuffer buffer = new StringBuffer();
                        String str = "";
                        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        while((str = br.readLine())!=null)
                        {
                            buffer.append(str);
                        }

                        mHandler.post(new Runnable() {// 这是main线程中执行的
                            @Override
                            public void run() {
                                tv.setText(buffer.toString());
                            }
                        });
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }).start();

    }

    public void click3(View view) { // 在子线程中创建一个Handler

        new Thread(new Runnable() {



            @Override
            public void run() {
                //往当前线程存储一个Looper对象
                Looper.prepare();

                handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        Log.d("google-my:", "handleMessage: ++++++++++++++++");
                    }
                };
                //不断的从消息队列中读取消息出来交给handler中的dispatchMessage去处理
                Looper.loop();
            }
        }).start();




    }

    public void click4(View view) { //把Message读到子线程的Handler中

        handler.sendEmptyMessage(2);

    }
}
