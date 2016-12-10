package com.example.mymediaplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Vector;

import static com.example.mymediaplayer.R.id.music;

/**
 * Created by 茹丽盈 on 2016/12/06.
 */

public class MyFileActivity extends Activity{
    //支持的媒体格式
    private final String[]FILE_MapTable={
      ".3prgp",".mov",".avi",".rmvb",".wmv","mp3","mp4"
    };
    private Vector<String> items=null;//items 存放显示的名称
    private Vector<String> paths=null;//paths 存放文件路径
    private Vector<String> sizes=null;//sizes 文件大小
    private String rootPath="/mnt/sdcard";//起始文件夹
    private EditText pathEt;//路径
    private Button queryButton;//查询按钮
    private ListView fileLV;//文件列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myfile);
        this.setTitle("多媒体文件浏览");
        pathEt= (EditText) findViewById(R.id.pathExit);
        queryButton= (Button) findViewById(R.id.queryButton);
        fileLV= (ListView) findViewById(R.id.file_listview);

        //查询按钮事件
        queryButton.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                File file=new File(pathEt.getText().toString());
                if (file.exists()){
                    if (file.isFile()){
                        openFile(pathEt.getText().toString());
                    }else {
                        getFilesDir(pathEt.getText().toString());
                    }
                }else {
                    Toast.makeText(MyFileActivity.this,"找不到该位置，情确定位置是否正确！",
                    Toast.LENGTH_SHORT).show();
                }
            }
        });
        //设置ListItem被点击时的操作
        fileLV.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fileOrDir(paths.get(position));
            }
        });
        getFilesDir(rootPath);
    }

    //处理文件或者目录的方法
    private void fileOrDir(String s) {
        File file=new File(s);
        if (file.isDirectory()){
            getFilesDir(file.getPath());
        }else {
            openFile(s);
        }
    }

    //取得文件结构的方法
    private void getFilesDir(String s) {
        //设置目前所在路径
        pathEt.setText(s);
        items=new Vector<String>();
        paths=new Vector<String>();
        sizes=new Vector<String>();
        File file=new File(s);
        File[] files=file.listFiles();
        if (files!=null){
            for (int i=0;i<files.length;i++){
                if (files[i].isDirectory()){
                    items.add(files[i].getName());
                    paths.add(files[i].getPath());
                    sizes.add("");
                }
            }
            for (int i=0;i<files.length;i++){
                if (files[i].isFile()){
                    String name=files[i].getName();
                    int index=name.lastIndexOf(".");
                    if (index>0){
                        String endName=name.substring(index,
                                name.length()).toLowerCase();
                        String type=null;
                        for (int x=0;x<FILE_MapTable.length;x++){
                            if (endName.equals(FILE_MapTable)){
                                type=FILE_MapTable[x];
                                break;
                            }
                        }
                        if (type!=null){
                            items.add(files[i].getName());
                            paths.add(files[i].getPath());
                            sizes.add(files[i].length()+"");
                        }
                    }
                }
            }
        }
        fileLV.setAdapter(new FileListAdapter(this,items));
    }

    private void openFile(String s) {
        Intent intent=new Intent(MyFileActivity.this,MainActivity.class);
        intent.putExtra("path",s);
        startActivity(intent);
        finish();
    }

    private class FileListAdapter extends BaseAdapter {
        private Vector<String> items=null;
        private MyFileActivity myFile;

        public FileListAdapter(MyFileActivity myFileActivity, Vector<String> items) {
            this.items=items;
            this.myFile=myFile;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.elementAt(position);
        }

        @Override
        public long getItemId(int position) {
            return items.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView==null){
                convertView=myFile.getLayoutInflater().inflate(R.layout.file_item,null);
            }
            TextView name= (TextView) convertView.findViewById(R.id.name);
            ImageView music= (ImageView) convertView.findViewById(R.id.music);
            ImageView folder= (ImageView) convertView.findViewById(R.id.folder);
            name.setText(items.elementAt(position));

            if (sizes.elementAt(position).equals("")){
                music.setVisibility(View.GONE);
                folder.setVisibility(View.VISIBLE);
            }else{
                folder.setVisibility(View.GONE);
                music.setVisibility(View.VISIBLE);
            }
            return convertView;
        }
    }
}
