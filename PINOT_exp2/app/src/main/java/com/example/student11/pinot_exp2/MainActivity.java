package com.example.student11.pinot_exp2;

import android.Manifest;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class MainActivity extends ListActivity {
    private static final String RSS_FEED_URL =  "http://www.rssmix.com/u/6589813/rss.xml";
    private ArrayList<Item> mItems;
    private RssListAdapter mAdapter;
    //final String LOGDIR = "/sdcard/";           //  /sdcard/data/がない場合
    final String LOGDIR = Environment.getExternalStorageDirectory().getPath()+"/data/";
    final String SDFILE1 = LOGDIR + "displayed.txt";
    final String SDFILE2 = LOGDIR + "tmp.txt";
    final String SDFILE2_2 = LOGDIR + "tmp2.txt";
    final String SDFILE3 = LOGDIR + "all.txt";
    final String SDFILE4 = LOGDIR + "username.txt";
    File DISPLAYED = new File(SDFILE1);
    File TMP = new File(SDFILE2);
    File TMP2 = new File(SDFILE2_2);
    File ALL = new File(SDFILE3);
    File NAME = new File(SDFILE4);
    long start;
    long stop;
    long diff=0;
    int second;
    int comma;
    int x;
    String line;
    private String title_line;
    private String link_line;
    int displaycount_line;
    int viewcount_line;
    int touchflag_line;
    int allcount;
    AsyncTask<Void, Void, String> task;
    private ProgressDialog progressDialog;
    static String path;
    String resultFileName;
    String username;
    ArrayList<String> list;
    static ArrayList<String> list2;
    private int touch;
    private int viewcount;         //視認回数
    private int displaycount;      //表示回数
    private String title_displayed;
    private String link_displayed;
    static Set<String> set;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!NAME.exists()){
            namedialog();
        }

        // Itemオブジェクトを保持するためのリストを生成し、アダプタに追加する
        mItems = new ArrayList<Item>();
        mAdapter = new RssListAdapter(this,mItems);
        list = new ArrayList<String>();

        // タスクを起動する
        RssParserTask task = new RssParserTask(this, mAdapter);
        task.execute(RSS_FEED_URL);

        alert();
    }

    // リストの項目を選択した時の処理
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        timer();

        Item item = mItems.get(position);
        list.add(String.valueOf(item.getTitle()));
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra("TITLE", item.getTitle());
        intent.putExtra("LINK", item.getLink());

        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(item.getLink())));  //指定したURLを表示

        startActivity(intent);
    }

    public void namedialog() {
        try {
            NAME.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //テキスト入力を受け付けるビューを作成します。
        final EditText editView = new EditText(MainActivity.this);
        new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("名前を入力してください。")
                        //setViewにてビューを設定します。
                .setView(editView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            BufferedWriter name = new BufferedWriter(new FileWriter(NAME));
                            name.write(editView.getText().toString());
                            name.close();
                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .show();
    }

    public static void distinct(List<String> slist) {       //要素の重複を削除
        /*set = new HashSet<String>();
        for (Iterator<String> i = slist.iterator(); i.hasNext();) {
            String s = i.next();
            if (set.contains(s)) {
                i.remove();
            } else {
                set.add(s);
            }
        }*/
        list2 = new ArrayList<String>();
        for (Iterator<String> i = slist.iterator(); i.hasNext();) {
            String s = i.next();
            if (list2.contains(s)) {
                i.remove();
            } else {
                list2.add(s);
            }
        }
    }

    private void DataSend(){
        // タスク
        task = new AsyncTask<Void, Void, String>() {

            /**
             * 準備
             */
            @Override
            protected void onPreExecute() {

                // 進捗ダイアログを開始
                MainActivity.this.progressDialog = new ProgressDialog(MainActivity.this);
                MainActivity.this.progressDialog.setMessage("Now Loading ...");
                MainActivity.this.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                MainActivity.this.progressDialog.setCancelable(true);
                MainActivity.this.progressDialog.show();
            }

            /**
             * 実行
             */
            @Override
            protected String doInBackground(Void... params) {

                FTPClient ftp = null;
                FileInputStream fis = null;
                FileOutputStream fos = null;

                try {

                    ftp = new FTPClient();

                    // エンコーディング
                    ftp.setControlEncoding("SJIS");     //コネクトの前に設定     Windowsサーバー:"Windows-31J"or"SJIS"

                    // 接続前タイムアウト：15秒
                    ftp.setDefaultTimeout(15000);
                    Log.e("デバック", "0");
                    // 接続
                    ftp.connect("133.71.201.164", 21);       //ホスト名「koblab.cs.ehime-u.ac.jp」に対して、ポート「21」に接続する   133.71.201.142
                    Log.e("デバック", "1");
                    // 接続エラーの場合
                    if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {

                        return "サーバーに接続できません";
                    }
                    // 接続後タイムアウト：10秒
                    ftp.setSoTimeout(15000);

                    // ログイン
                    if (!ftp.login("ono", "qt7gn089ebdonx2")) {

                        return "サーバーの認証に失敗しました";
                    }
                    Log.e("デバック", "2");
                    // ファイル種別：アスキーモード
                    ftp.setFileType(FTP.ASCII_FILE_TYPE);//BINARY_FILE_TYPE
                    // PASVモード
                    ftp.enterLocalPassiveMode();
                    // タイムアウト：10秒
                    ftp.setDataTimeout(20000);
                    Log.e("デバック", "3");

                    // 受信元のディレクトリを作成
                    //String path = Environment.getExternalStorageDirectory().getPath() + "/SAMPLE/";
                    //new File(path).mkdir();

                    // 受信   サーバーから「hoge1.txt」を、Android端末に「hoge2.txt」としてダウンロードする
                    /*fos = new FileOutputStream(path + "hoge2.txt");
                    if (!ftp.retrieveFile("/TEST/hoge1.txt", fos)) {

                        return "ファイルの受信に失敗しました";
                    }*/
                    //Log.e("デバック", "3");
                    // 送信   Android端末から「hoge2.txt」を、サーバーに「hoge3.txt」としてアップロードする
                    fis = new FileInputStream(path);
                    Log.e("デバック", ""+path);
                    Log.e("デバック", ""+resultFileName);
                    if (!ftp.storeFile("/home/ono/result0615/"+resultFileName, fis)) {

                        return "ファイルの送信に失敗しました";
                    }
                    Log.e("デバック", "4");
                } catch (SocketException e) {

                    return "FTP通信に失敗しました（１）";

                } catch (IOException e) {

                    return "FTP通信に失敗しました（２）";

                } finally {

                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                        }
                    }

                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                        }
                    }

                    if (ftp != null) {
                        try {
                            // ログアウト
                            ftp.logout();
                        } catch (IOException e) {
                        }
                        try {
                            // 切断
                            ftp.disconnect();
                        } catch (IOException e) {
                        }
                    }
                }

                return "送受信に成功しました";
            }

            /**
             * 完了
             */
            @Override
            protected void onPostExecute(String param) {

                if (MainActivity.this.progressDialog.isShowing()) {

                    // 進捗ダイアログを終了
                    MainActivity.this.progressDialog.dismiss();
                }

                Toast.makeText(MainActivity.this, param, Toast.LENGTH_LONG).show();
            }
        };
    }

    public void alert(){
        allcount=0;
        try {
            ALL.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader all = new BufferedReader(new FileReader(ALL));
            while((line = all.readLine()) != null){
                allcount++;
            }
            all.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        if(allcount>1000){
            new AlertDialog.Builder(this)
                    .setTitle("実験終了です")
                    .setMessage("ご協力ありがとうございました。結果を送信します。")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // OK button pressed
                            try {
                                NAME.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                BufferedReader name = new BufferedReader(new FileReader(NAME));
                                username = name.readLine();
                                name.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }

                            path = LOGDIR + "/all.txt";
                            resultFileName = username + ".txt";
                            DataSend();
                            task.execute();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
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

    public void touchinfo(){
        distinct(list);
        try {
            DISPLAYED.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            TMP2.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(DISPLAYED));
            try {
                BufferedWriter pw = new BufferedWriter(new FileWriter(TMP2,true));
                    while ((line = br.readLine()) != null) {
                        StringTokenizer tok = new StringTokenizer(line, "\t");
                        title_displayed = tok.nextToken();
                        link_displayed = tok.nextToken();
                        displaycount = Integer.parseInt(tok.nextToken());
                        viewcount = Integer.parseInt(tok.nextToken());
                        touch = Integer.parseInt(tok.nextToken());
                        boolean f = true;
                        for ( int i = 0; i < list2.size(); i++ ) {
                            if (list2.get(i).equals(title_displayed)) {
                                if (touch == -1) {//未タップの場合
                                    pw.write(title_displayed + "\t" + link_displayed + "\t" + displaycount + "\t" + viewcount + "\t" + viewcount);
                                    pw.newLine();
                                } else if (touch >= 0) {        //タップ済みの場合
                                    pw.write(title_displayed + "\t" + link_displayed + "\t" + displaycount + "\t" + viewcount + "\t" + touch);
                                    pw.newLine();
                                }
                                f=false;
                                break;
                            }
                        }
                        if(f) {
                            pw.write(title_displayed + "\t" + link_displayed + "\t" + displaycount + "\t" + viewcount + "\t" + touch);
                            pw.newLine();
                        }
                    }
                pw.close();
                br.close();
                if(list2.size()>=1) {
                    DISPLAYED.delete();
                    TMP2.renameTo(DISPLAYED);
                }
                if(TMP2.exists()){
                    TMP2.delete();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void viewcount(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(DISPLAYED));
            try {
                BufferedWriter pw = new BufferedWriter(new FileWriter(TMP, true));
                while ((line = br.readLine()) != null) {
                    StringTokenizer tok = new StringTokenizer(line, "\t");
                    title_displayed = tok.nextToken();
                    link_displayed = tok.nextToken();
                    displaycount = Integer.parseInt(tok.nextToken());
                    viewcount = Integer.parseInt(tok.nextToken());
                    touch = Integer.parseInt(tok.nextToken());
                    if (x > 0) {
                        viewcount++;
                        x--;
                    }
                    pw.write(title_displayed + "\t" + link_displayed + "\t" + displaycount + "\t" + viewcount + "\t" + touch);
                    pw.newLine();
                }
                pw.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
            br.close();
        }catch (IOException e1){
            e1.printStackTrace();
        }
        DISPLAYED.delete();
        TMP.renameTo(DISPLAYED);
    }

    public void displaycount(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(DISPLAYED));
            try {
                BufferedWriter all = new BufferedWriter(new FileWriter(ALL, true));
                while ((line = br.readLine()) != null) {
                    StringTokenizer tok = new StringTokenizer(line, "\t");
                    title_displayed = tok.nextToken();
                    link_displayed = tok.nextToken();
                    displaycount = Integer.parseInt(tok.nextToken());
                    viewcount = Integer.parseInt(tok.nextToken());
                    touch = Integer.parseInt(tok.nextToken());
                    if (displaycount >= 3) {
                        all.write(title_displayed + "\t" + displaycount + "\t" + viewcount + "\t" + touch);
                        all.newLine();
                    }
                }
                all.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            br.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){				// 戻るボタンが押された！
            timer();
            x = (int) (diff/2500);                  //ユーザ毎に設定する
            Log.e("終了",second+"."+comma);
            Log.e("視認件数",""+x);
            start = stop = RssParserTask.start = ItemDetailActivity.start = 0;

            try {
                ALL.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                TMP.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            viewcount();
            touchinfo();
            displaycount();
            /*try {
                BufferedReader br = new BufferedReader(new FileReader(DISPLAYED));
                try {
                    BufferedWriter pw = new BufferedWriter(new FileWriter(TMP,true));
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
                            if(displaycount_line >= 3){
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
                    DISPLAYED.delete();
                    TMP.renameTo(DISPLAYED);
                    } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }*/


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
