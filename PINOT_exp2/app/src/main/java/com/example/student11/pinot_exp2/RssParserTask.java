package com.example.student11.pinot_exp2;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Xml;
import android.widget.ArrayAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Created by student11 on 2015/09/25.
 */
//「RssParserTask.java」では、RSSフィード（xmlファイル）の中から各要素（記事タイトルや記事概要など）を取得しています。
//その役目を行っているのが「parseXmlメソッド」です。
public class RssParserTask extends AsyncTask<String, Integer, RssListAdapter> {
    private MainActivity mActivity;
    private RssListAdapter mAdapter;
    private ProgressDialog mProgressDialog;
    public static long start;
    final String LOGDIR = "/sdcard/";           //android6.0に対応
    //final String LOGDIR = Environment.getExternalStorageDirectory().getPath()+"/data/";
    final String SDFILE1 = LOGDIR + "displayed.txt";
    final String SDFILE2 = LOGDIR + "received.txt";
    final String SDFILE3 = LOGDIR + "tmp.txt";
    final String SDFILE4 = LOGDIR + "all.txt";
    private String line;		//title_info.txtの先頭から１行ずつ取ってきたものを格納
    private String line2;
    private int count;			//count=-1ならば既読、０以上なら未読で見たと判断した回数を表示
    private int count_line;
    private String title_displayed;
    private String title_received;
    private String title_tmp;
    private String title_all;
    private String link_displayed;
    private String link_received;
    private String link_tmp;
    File DISPLAYED = new File(SDFILE1);       //前回表示した見出し文の一覧（表示回数が3回に達したものは除外）
    File RECEIVED = new File(SDFILE2);    //アプリ起動時に受信した見出し文の一覧（DISPLAYと重複する見出し文有り）
    File TMP = new File(SDFILE3);    //新しく表示する見出し文の一覧を一時格納
    File ALL = new File(SDFILE4);
    String link;
    String title;
    int displaycount;
    int viewcount;
    int touch;
    ArrayList<String> list;
    boolean flag;

    // コンストラクタ
    public RssParserTask(MainActivity activity, RssListAdapter adapter) {
        mActivity = activity;
        mAdapter = adapter;
    }

    // タスクを実行した直後にコールされる
    @Override
    protected void onPreExecute() {
        // プログレスバーを表示する
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setMessage("Now Loading...");
        mProgressDialog.show();
    }

    // バックグラウンドにおける処理を担う。タスク実行時に渡された値を引数とする
    @Override
    protected RssListAdapter doInBackground(String... params) {
        RssListAdapter result = null;
        try {
            // HTTP経由でアクセスし、InputStreamを取得する
            URL url = new URL(params[0]);
            InputStream is = url.openConnection().getInputStream();		//コネクションを開き、接続先のデータを取得
            result = parseXml(is);

            //読み込んだ全ての見出し文をtitle_all.txtに書き込む
            /*BufferedReader br = new BufferedReader(new FileReader(Title));
            BufferedWriter pw2 = new BufferedWriter(new FileWriter(Title_all,true));
            while((line = br.readLine()) != null){
                StringTokenizer tok = new StringTokenizer(line,"\t\t");
                title_line = tok.nextToken();
                count_line = Integer.parseInt(tok.nextToken());
                Flag = true;
                BufferedReader br2 = new BufferedReader(new FileReader(Title_w));
                while((line2 = br2.readLine()) != null){
                    StringTokenizer tok2 = new StringTokenizer(line2,"\t\t");
                    title_line2 = tok2.nextToken();
                    if(title_line.equals(title_line2)){		//一致した場合何もしない
                        Flag = false;
                        break;
                    }
                }
                br2.close();
                if(Flag){				//一致しなかったとき（title_info.txtにあってtitle_info_new.txtにない見出し文）title_all.txtに書き込む
                    pw2.write(title_line+"\t\t"+count_line);
                    pw2.newLine();
                }
            }
            pw2.close();
            br.close();

            Title.delete();
            Title_w.renameTo(Title);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ここで返した値は、onPostExecuteメソッドの引数として渡される
        return result;
    }

    // メインスレッド上で実行される
    @Override
    protected void onPostExecute(RssListAdapter result) {
        mProgressDialog.dismiss();
        mActivity.setListAdapter(result);       //ArrayAdapterクラスをsetListAdapterメソッドでアクティビティに設定
        start = System.currentTimeMillis();
    }

    //XmlPullParser.START_DOCUMENT 	ドキュメントの開始
    //XmlPullParser.END_DOCUMENT 	ドキュメントの終わり
    //XmlPullParser.START_TAG 	開始タグ<~>
    //XmlPullParser.END_TAG 	終了タグ<~/>
    //XmlPullParser.TEXT 	要素

    // XMLをパースする
    public RssListAdapter parseXml(InputStream is) throws IOException, XmlPullParserException {		//inputStream:XMLストリームを指定する
        XmlPullParser parser = Xml.newPullParser();
        try {		//XMLパーサー解析開始
            parser.setInput(is, null);						//XMLのストリームを渡す
            int eventType = parser.getEventType();			//イベントタイプを取得。今読み込んでいる場所がどの状態かを知る
            Item currentItem = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tag = null;
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();				//XMLのタグ名称を取得する
                        /*if (tag.equals("item")) {
                            currentItem = new Item("");     //コンストラタ作成
                        } else if (currentItem != null) {*/
                            if (tag.equals("title")) {
                                title = parser.nextText();
                            }
                            else if (tag.equals("link")) {
                                link = parser.nextText();                            }
                        //}
                        break;
                    case XmlPullParser.END_TAG:				//itemタグが終わったら、そこで１記事のセットが終了したとしてlistに追加。
                        tag = parser.getName();
                        if (tag.equals("item")) {
                            try {
                                DISPLAYED.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                RECEIVED.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                TMP.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                ALL.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            BufferedWriter received = new BufferedWriter(new FileWriter(RECEIVED,true));//trueは追記
                            BufferedReader all = new BufferedReader(new FileReader(ALL));
                            //System.out.println(title+":"+link);
                            boolean flag=false;         //false:all.txtにない ,true:all.txtにある
                            while((line = all.readLine()) != null) {
                                StringTokenizer tok = new StringTokenizer(line, "\t");
                                title_all = tok.nextToken();
                                if(title.equals(title_all)) {
                                    flag = true;
                                    break;
                                }
                            }
                            if(!flag){              //3回表示された見出し文が再度表示されるのを防ぐ
                                received.write(title + "\t" + link);
                                received.newLine();
                            }
                            all.close();
                            received.close();
                        }
                        break;
                }
                eventType = parser.next();      //nextメソッドで一行ずつイベントタイプを取得
            }
            //受信した見出し文と前回の見出し文を比較
            try {
                BufferedReader received = new BufferedReader(new FileReader(RECEIVED));
                list = new ArrayList<String>();
                while ((line = received.readLine()) != null) {
                    StringTokenizer tok = new StringTokenizer(line, "\t");
                    title_received = tok.nextToken();
                    link_received = tok.nextToken();
                /*if (line == null) {               //display.txtの中身が空の場合
                    //System.out.println("display.txtは空と判定");
                    while ((line2 = received.readLine()) != null) {
                        list.add(line2 + "\t" + 1 + "\t" + 0 + "\t" + 0);
                     StringTokenizer tok = new StringTokenizer(line2, "\t");
                     title_line2 = tok.nextToken();
                     link_line2 = tok.nextToken();
                     bw_tmpdisplay.write(title_line2+"\t"+link_line2+"\t"+1+"\t"+0+"\t"+0);         //タイトル・URL・表示回数・視認回数・タップ情報（０：未，１～３：初タップ時の視認回数）
                     bw_tmpdisplay.newLine();
                        Log.i("a", "display.txtの中身が空");
                    }
                } else if (line != null) {         //display.txtに書き込まれている場合*/
                    //System.out.println("display.txtの内容ありと判定");
                    try {
                        BufferedReader displayed = new BufferedReader(new FileReader(DISPLAYED));
                        flag = true;             //tmp.txtに書き込みをしたか否かのフラグ
                        while ((line2 = displayed.readLine()) != null) {
                            StringTokenizer tok2 = new StringTokenizer(line2, "\t");
                            title_displayed = tok2.nextToken();
                            link_displayed = tok2.nextToken();
                            displaycount = Integer.parseInt(tok2.nextToken());
                            viewcount = Integer.parseInt(tok2.nextToken());
                            touch = Integer.parseInt(tok2.nextToken());
                            //while (line != null) {
                        /*StringTokenizer tok3 = new StringTokenizer(line, "\t");
                        title_displayed = tok3.nextToken();
                        link_displayed = tok3.nextToken();
                        displaycount = Integer.parseInt(tok3.nextToken());
                        viewcount = Integer.parseInt(tok3.nextToken());
                        touch = Integer.parseInt(tok3.nextToken());
                        System.out.println(title_displayed + ":" + title_received);*/
                            if (title_displayed.equals(title_received)) {     // 前回表示した見出し文との比較
                                displaycount++;
                                list.add(title_displayed + "\t" + link_displayed + "\t" + displaycount + "\t" + viewcount + "\t" + touch);
                                Log.i("1", "既出" + ":" + title_displayed + "\t" + link_displayed + "\t" + displaycount + "\t" + viewcount + "\t" + touch);
                                /*try{
                                    BufferedWriter tmp = new BufferedWriter(new FileWriter(TMP,true));
                                    tmp.write(title_displayed + "\t" + link_displayed + "\t" + displaycount + "\t" + viewcount + "\t" + touch);
                                    tmp.newLine();
                                    tmp.close();
                                }catch (IOException e){
                                    e.printStackTrace();
                                }*/
                                flag = false;
                                break;
                            }
                            //line = displayed.readLine();
                            //}
                        }
                        if(flag){            //新出見出し文
                            //System.out.println("新出");
                            //bw_tmpdisplay.write(title_line2+"\t"+link_line2+"\t"+1+"\t"+0+"\t"+0);
                            //bw_tmpdisplay.newLine();
                            list.add(title_received + "\t" + link_received + "\t" + 1 + "\t" + 0 + "\t" + -1);
                            Log.i("2", "新出" + ":" + title_received);
                            /*try{
                                BufferedWriter tmp = new BufferedWriter(new FileWriter(TMP,true));
                                tmp.write(title_received + "\t" + link_received + "\t" + displaycount + "\t" + viewcount + "\t" + touch);
                                tmp.newLine();
                                tmp.close();
                            }catch (IOException e){
                                e.printStackTrace();
                            }*/
                        }
                        displayed.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                received.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for ( int i = 0; i < list.size(); i++ ) {
                System.out.println( list.get( i ) );
            }
            //}
            //displayed.close();
            //received.close();
            Collections.shuffle(list);
            BufferedWriter tmp = new BufferedWriter(new FileWriter(TMP));
            for ( int i = 0; i < list.size(); i++ ) {
                tmp.write(list.get(i));
                tmp.newLine();
            }
            tmp.close();
            /*Collections.shuffle(list);
            Iterator<String> it = list.iterator();
            BufferedWriter tmp = new BufferedWriter(new FileWriter(TMP));
            while (it.hasNext()) {
                //String e = it.next();
                tmp.write(it.next());
                tmp.newLine();
                //System.out.println(e);
            }
            tmp.close();*/
            BufferedReader br2 = new BufferedReader(new FileReader(TMP));
            while((line = br2.readLine()) != null){
                currentItem = new Item("");
                StringTokenizer tok = new StringTokenizer(line, "\t");
                title_tmp = tok.nextToken();
                link_tmp = tok.nextToken();
                currentItem.setTitle(title_tmp);
                currentItem.setLink(link_tmp);
                mAdapter.add(currentItem);
            }
            br2.close();
            DISPLAYED.delete();
            RECEIVED.delete();
            TMP.renameTo(DISPLAYED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mAdapter;
    }
}