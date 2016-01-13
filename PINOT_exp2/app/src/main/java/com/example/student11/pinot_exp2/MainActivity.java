package com.example.student11.pinot_exp2;

import android.app.ListActivity;
import android.content.ClipData;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class MainActivity extends ListActivity {
    private static final String RSS_FEED_URL =  "http://www.rssmix.com/u/6589813/rss.xml";
    private ArrayList<Item> mItems;
    private RssListAdapter mAdapter;
    final String LOGDIR = Environment.getExternalStorageDirectory().getPath()+"/data/";
    final String SDFILE1 = LOGDIR + "display.txt";
    final String SDFILE2 = LOGDIR + "tmpdisplay.txt";
    final String SDFILE3 = LOGDIR + "all.txt";
    File DISPLAY = new File(SDFILE1);
    File TmpDISPLAY = new File(SDFILE2);
    File ALL = new File(SDFILE3);
    long start;
    long stop;
    long diff=0;
    int second;
    int comma;
    String line;
    private String title_line;
    private String link_line;
    int displaycount_line;
    int viewcount_line;
    int touchflag_line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Itemオブジェクトを保持するためのリストを生成し、アダプタに追加する
        mItems = new ArrayList<Item>();
        mAdapter = new RssListAdapter(this,mItems);

        // タスクを起動する
        RssParserTask task = new RssParserTask(this, mAdapter);
        task.execute(RSS_FEED_URL);
    }

    // リストの項目を選択した時の処理
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        timer();

        Item item = mItems.get(position);
        Intent intent = new Intent(this, ItemDetailActivity.class);

        intent.putExtra("TITLE", item.getTitle());
        intent.putExtra("LINK", item.getLink());

        startActivity(intent);
    }

    public void timer(){
        if(ItemDetailActivity.start == 0){
            start = RssParserTask.start;
        }else{
            start = ItemDetailActivity.start;
        }
        stop = System.currentTimeMillis();
        diff = diff + (stop - start);
        second = (int) (diff / 1000);
        comma = (int) (diff % 1000);
        Log.e("一時停止", second + "." + comma);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){				// 戻るボタンが押された！
            timer();
            int x = (int) (diff/1570);                  //ユーザ毎に設定する
            Log.e("終了",second+"."+comma);
            Log.e("視認件数",""+x);
            start = stop = RssParserTask.start = ItemDetailActivity.start = 0;

            try {
                ALL.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                TmpDISPLAY.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                BufferedReader br = new BufferedReader(new FileReader(DISPLAY));
                try {
                    BufferedWriter pw = new BufferedWriter(new FileWriter(TmpDISPLAY,true));
                    try {
                        BufferedWriter pw_all = new BufferedWriter(new FileWriter(ALL,true));
                        while((line = br.readLine()) != null){
                            StringTokenizer tok = new StringTokenizer(line,"\t");
                            title_line = tok.nextToken();
                            link_line = tok.nextToken();
                            displaycount_line = Integer.parseInt(tok.nextToken());
                            viewcount_line = Integer.parseInt(tok.nextToken());
                            touchflag_line = Integer.parseInt(tok.nextToken());
                            if(x>0){
                                viewcount_line++;
                                x--;
                            }
                            if(displaycount_line == 3){
                                pw_all.write(title_line+"\t\t"+displaycount_line+"\t\t"+viewcount_line+"\t\t"+touchflag_line);
                                pw_all.newLine();
                            }else {
                                pw.write(title_line + "\t\t" + link_line + "\t\t" + displaycount_line + "\t\t" + viewcount_line + "\t\t" + touchflag_line);
                                pw.newLine();
                            }
                        }
                        pw_all.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    pw.close();
                    br.close();
                    DISPLAY.delete();
                    TmpDISPLAY.renameTo(DISPLAY);
                    } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
