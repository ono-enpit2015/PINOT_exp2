package com.example.student11.pinot_exp2;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Xml;
import android.widget.ArrayAdapter;

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
    final String SDFILE1 = LOGDIR + "display.txt";
    final String SDFILE2 = LOGDIR + "newdisplay.txt";
    final String SDFILE3 = LOGDIR + "tmpdisplay.txt";
    final String SDFILE4 = LOGDIR + "all.txt";
    private String line;		//title_info.txtの先頭から１行ずつ取ってきたものを格納
    private String line2;
    private int count;			//count=-1ならば既読、０以上なら未読で見たと判断した回数を表示
    private int count_line;
    private String title_line;
    private String title_line2;
    private String link_line;
    private String link_line2;
    File DISPLAY = new File(SDFILE1);       //前回表示した見出し文の一覧（表示回数が3回に達したものは除外）
    File NewDISPLAY = new File(SDFILE2);    //アプリ起動時に新しく受信した見出し文の一覧（DISPLAYと重複する見出し文有り）
    File TmpDISPLAY = new File(SDFILE3);    //新しく表示する見出し文の一覧
    File ALL = new File(SDFILE4);
    String link;
    String title;
    int displaycount_line;
    int viewcount_line;
    int touchflag_line;

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
                                DISPLAY.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                NewDISPLAY.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                TmpDISPLAY.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                ALL.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            BufferedWriter bw_newdisplay = new BufferedWriter(new FileWriter(NewDISPLAY,true));//trueは追記
                            BufferedReader br_all = new BufferedReader(new FileReader(ALL));
                            System.out.println(title+":"+link);
                            boolean flag=false;         //false:all.txtにない ,true:all.txtにある
                            while((line = br_all.readLine()) != null) {
                                StringTokenizer tok = new StringTokenizer(line, "\t");
                                title_line = tok.nextToken();
                                if(title.equals(title_line)) {
                                    flag = true;
                                    break;
                                }
                            }
                            if(!flag){
                                bw_newdisplay.write(title + "\t" + link);
                                bw_newdisplay.newLine();
                            }
                            br_all.close();
                            bw_newdisplay.close();
                        }
                        break;
                }
                eventType = parser.next();      //nextメソッドで一行ずつイベントタイプを取得
            }
            //新しい見出し文と前回の見出し文を比較
            BufferedReader br_newdisplay = new BufferedReader(new FileReader(NewDISPLAY));
            BufferedReader br_display = new BufferedReader(new FileReader(DISPLAY));
            BufferedWriter bw_tmpdisplay = new BufferedWriter(new FileWriter(TmpDISPLAY,true));
            line = br_display.readLine();
            if(line == null){               //display.txtの中身が空の場合
                //System.out.println("display.txtは空と判定");
                 while((line2 = br_newdisplay.readLine()) != null){
                     StringTokenizer tok = new StringTokenizer(line2, "\t");
                     title_line2 = tok.nextToken();
                     link_line2 = tok.nextToken();
                     bw_tmpdisplay.write(title_line2+"\t"+link_line2+"\t"+1+"\t"+0+"\t"+0);
                     bw_tmpdisplay.newLine();
                 }
            }else if(line != null){         //display.txtに書き込まれている場合
                //System.out.println("display.txtの内容ありと判定");
                while((line2 = br_newdisplay.readLine()) != null){
                    StringTokenizer tok2 = new StringTokenizer(line2, "\t");
                    title_line2 = tok2.nextToken();
                    link_line2 = tok2.nextToken();
                    boolean flag = false;               //tmpdisplay.txtに書き込みをしたか否かのフラグ
                    //System.out.println("title2:"+title_line2);
                    while(line != null){
                        StringTokenizer tok = new StringTokenizer(line, "\t");
                        title_line = tok.nextToken();
                        link_line = tok.nextToken();
                        displaycount_line = Integer.parseInt(tok.nextToken());
                        viewcount_line = Integer.parseInt(tok.nextToken());
                        touchflag_line = Integer.parseInt(tok.nextToken());
                        //System.out.println("title:"+title_line);
                        if(title_line.equals(title_line2)){     // 前回表示した見出し文
                            //System.out.println("既出");
                            displaycount_line++;
                            bw_tmpdisplay.write(title_line + "\t" + link_line + "\t" + displaycount_line + "\t" + viewcount_line + "\t" + touchflag_line);
                            bw_tmpdisplay.newLine();
                            flag = true;
                            break;
                        }
                        line = br_display.readLine();
                    }
                    if(!flag){            //新出見出し文
                        //System.out.println("新出");
                        bw_tmpdisplay.write(title_line2+"\t"+link_line2+"\t"+1+"\t"+0+"\t"+0);
                        bw_tmpdisplay.newLine();
                    }
                }
            }
            br_display.close();
            br_newdisplay.close();
            bw_tmpdisplay.close();
            DISPLAY.delete();
            NewDISPLAY.delete();
            TmpDISPLAY.renameTo(DISPLAY);
            BufferedReader br = new BufferedReader(new FileReader(DISPLAY));
            while((line = br.readLine()) != null){
                currentItem = new Item("");
                StringTokenizer tok = new StringTokenizer(line, "\t");
                title_line = tok.nextToken();
                link_line = tok.nextToken();
                currentItem.setTitle(title_line);
                currentItem.setLink(link_line);
                mAdapter.add(currentItem);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mAdapter;
    }
}