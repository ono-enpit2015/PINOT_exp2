package com.example.student11.pinot_exp2;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
    final String LOGDIR = Environment.getExternalStorageDirectory().getPath()+"/data/";
    final String SDFILE3 = LOGDIR + "title_info.txt";
    final String SDFILE4 = LOGDIR + "title_info_new.txt";
    final String SDFILE5 = LOGDIR + "title_all.txt";
    private String line;		//title_info.txtの先頭から１行ずつ取ってきたものを格納
    private String line2;
    private int count;			//count=-1ならば既読、０以上なら未読で見たと判断した回数を表示
    private int count_line;
    private String title_line;
    private String title_line2;
    private Boolean Flag;
    File Title = new File(SDFILE3);
    File Title_w = new File(SDFILE4);
    File Title_all = new File(SDFILE5);
    String title;
    private int val;

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
            BufferedReader br = new BufferedReader(new FileReader(Title));
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
            Title_w.renameTo(Title);
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
        mActivity.setListAdapter(result);
        start = System.currentTimeMillis();
    }



    // XMLをパースする
    public RssListAdapter parseXml(InputStream is) throws IOException, XmlPullParserException {		//inputStream:XMLストリームを指定する
        XmlPullParser parser = Xml.newPullParser();
        try {		//XMLパーサー解析開始
            parser.setInput(is, null);						//XMLのストリームを渡す
            int eventType = parser.getEventType();			//今読み込んでいる場所がどの状態かを知る
            Item currentItem = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tag = null;
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();				//XMLのタグ名称を取得する
                        if (tag.equals("item")) {
                            currentItem = new Item("");
                        } else if (currentItem != null) {
                            if (tag.equals("title")) {
                                title = parser.nextText();
                                    currentItem.setTitle(title);
                                    try {
                                        Title.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        BufferedReader br = new BufferedReader(new FileReader(Title));
                                        try {
                                            Title_w.createNewFile();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            Title_all.createNewFile();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        BufferedWriter pw = new BufferedWriter(new FileWriter(Title_w,true));
                                        try {
                                            if((line = br.readLine()) != null){			//前回の見出し文情報（title_info.txt）が存在する場合（２回目以降）
                                                Flag = true;
                                                while(line != null){
                                                    StringTokenizer tok = new StringTokenizer(line,"\t\t");
                                                    title_line = tok.nextToken();
                                                    count_line = Integer.parseInt(tok.nextToken());
                                                    //flag = Boolean.valueOf(tok.nextToken());
                                                    if(title.equals(title_line)){		//今回の見出し文と前回の見出し文で同じものがある場合
                                                        pw.write(title_line+"\t\t"+count_line);
                                                        pw.newLine();
                                                        Flag = false;
                                                        break;
                                                    }
                                                    line = br.readLine();
                                                }
                                                if(Flag){								//新出見出し文の場合
                                                    count = 0;
                                                    pw.write(title+"\t\t"+count);
                                                    pw.newLine();
                                                }
                                            }else{										//title_info.txtが存在しない場合（アプリ起動一回目）
                                                count = 0;
                                                pw.write(title+"\t\t"+count);
                                                pw.newLine();
                                            }
                                            pw.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        br.close();
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                            }
                            else if (tag.equals("link")) {
                                if(val == 1){
                                    currentItem.setLink((parser.nextText()));
                                }
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:				//itemタグが終わったら、そこで１記事のセットが終了したとしてlistに追加。
                        tag = parser.getName();
                        if (tag.equals("item")) {
                            if(val == 1){
                                mAdapter.add(currentItem);
                            }
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mAdapter;
    }
}