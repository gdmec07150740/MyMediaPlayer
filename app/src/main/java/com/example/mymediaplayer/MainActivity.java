package com.example.mymediaplayer;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MainActivity extends Activity {
    private Display currDisplay;
    private SurfaceView sv;
    private SurfaceHolder holder;
    private MediaPlayer player;
    private int vWidth,vHeight;
    private Timer timer;
    private ImageButton rew;//快退
    private ImageButton pause;//暂停
    private ImageButton start;//开始
    private ImageButton ff;//快进
    private TextView playTime;//已播放的时间
    private TextView allTime;//总播放时间
    private TextView title;//播放文件名称
    private SeekBar seekBar;//进度条

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //获取传过来的媒体路径
        Intent intent =getIntent();
        Uri uri=intent.getData();
        String mPath="";
        if (uri!=null){
            mPath=uri.getPath();//外部程序调用该程序，获得媒体路径
        }else{
            //从程序内部的文件夹传过来的媒体路径
            Bundle localBundle=getIntent().getExtras();
            if (localBundle!=null){
                String t_path=localBundle.getString("path");
                if (t_path!=null && !"".equals(t_path)){
                    mPath=t_path;
                }
            }
        }
        //加载当前布局文件控件操作
        title= (TextView) findViewById(R.id.title1);
        sv= (SurfaceView) findViewById(R.id.surfaceView);
        rew= (ImageButton) findViewById(R.id.rew);
        pause= (ImageButton) findViewById(R.id.pause);
        start= (ImageButton) findViewById(R.id.start);
        ff= (ImageButton) findViewById(R.id.ff);
        playTime= (TextView) findViewById(R.id.playTime);
        allTime= (TextView) findViewById(R.id.allTime);
        seekBar= (SeekBar) findViewById(R.id.seekbar);

        //添加监听
        holder=sv.getHolder();
        holder.addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                player.setDisplay(holder);
                player.prepareAsync();
            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //下面实例化MediaPlayer对象
        player=new MediaPlayer();
        //设置播放完成监听器
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){

            @Override
            public void onCompletion(MediaPlayer mp) {
                //当MediaPlayer播放完成后触发
                if (timer!=null){
                    timer.cancel();
                    timer=null;
                }
            }
        });
        //设置prepare完成监听器
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
            @Override
            public void onPrepared(MediaPlayer mp) {
                //当prepare完成后，该方法触发，在这里我们播放视频
                //首先取得video的宽和高
                vWidth=player.getVideoWidth();
                vHeight=player.getVideoHeight();

                if (vWidth>currDisplay.getWidth() || vHeight>currDisplay.getHeight()){
                    //如果video的宽或者高超出了当前屏幕的大小，则进行缩放
                    float wRedio=(float) vWidth/(float) currDisplay.getWidth();
                    float hRedio=(float) vHeight/(float) currDisplay.getHeight();
                    //选择大的一个进行缩放
                    float ratio=Math.max(wRedio,hRedio);
                    vWidth= (int) Math.ceil((float) vWidth/ratio);
                    vHeight= (int) Math.ceil((float) vHeight/ratio);
                    //设置sv的布局参数
                    sv.setLayoutParams(new LinearLayout.LayoutParams(vWidth,vHeight));
                    //然后开始播放视频
                    player.start();
                }else{
                    player.start();
                }
                if (timer!=null){
                    timer.cancel();
                    timer=null;
                }
                //启动时间更新及进度条更新任务，5秒更新一次
                timer=new Timer();
                timer.schedule(new MyTask(),50,500);
            }
        });
        rew.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //判定是否正在播放
                if (player.isPlaying()){
                    int currentPosition=player.getCurrentPosition();
                    if (currentPosition-10000>0){
                        player.seekTo(currentPosition-10000);
                    }
                }
            }
        });
        //快速操作，每次快进10s
        ff.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //判定是否正在播放
                if (player.isPlaying()){
                    int currentPosition=player.getCurrentPosition();
                    if (currentPosition+10000<player.getDuration()){
                        player.seekTo(currentPosition+10000);
                    }
                }
            }
        });
        //取得当前display对象
        currDisplay =this.getWindowManager().getDefaultDisplay();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,1,0,"文件夹");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==1){
            Intent intent=new Intent(this,MyFileActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //进度栏任务
    public class MyTask extends TimerTask{

        @Override
        public void run() {
            Message message=new Message();
            message.what=1;
            //发生消息更新进度栏和时间显示

        }
    }

    //处理进度栏和时间显示
    private final android.os.Handler handler=new android.os.Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    Time progress=new Time(player.getCurrentPosition());
                    Time allTime=new Time(player.getDuration());
                    String str1=progress.toString();
                    String str2=allTime.toString();
                    //已播放时间
                    playTime.setText(str1.substring(str1.indexOf(":")+1));
                    //总时间

                    int pv=0;
                    if (player.getDuration()>0){
                        pv=seekBar.getMax()
                                *player.getCurrentPosition()/player.getDuration();
                    }
                    //进度栏进度
                    seekBar.setProgress(pv);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player!=null){
            player.stop();
        }
    }
}
