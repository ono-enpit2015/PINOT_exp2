package com.example.student11.pinot_exp2;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.widget.TextView;

import com.example.student11.pinot_exp2.R;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created by student11 on 2015/09/25.
 */
public class ItemDetailActivity extends Activity {
    private TextView mTitle;
    public static long start = 0;
    final String LOGDIR = Environment.getExternalStorageDirectory().getPath()+"/data/";
    final String SDFILE1 = LOGDIR + "display.txt";
    final String SDFILE2 = LOGDIR + "newdisplay.txt";
    File DISPLAY = new File(SDFILE1);
    File NewDISPLAY = new File(SDFILE2);
    private String line;		//title_info.txtの先頭から１行ずつ取ってきたものを格納
    private int touchflag_line;			//1ならば既読、０なら未読
    private int viewcount_line;         //視認回数
    private int displaycount_line;      //表示回数
    private String title_line;
    private String link_line;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_detail);

        Intent intent = getIntent();
        String title = intent.getStringExtra("TITLE");
        mTitle = (TextView) findViewById(R.id.item_detail_title);
        mTitle.setText(title);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        try {

            String url = intent.getStringExtra("LINK");
            // HTMLのドキュメントを取得
            org.jsoup.nodes.Document document = Jsoup.connect(url).get();

            Elements links = null;
            links = document.getElementsByTag("a");

            for (org.jsoup.nodes.Element link : links) {
                String lin = link.attr("id");
                if(lin.equals("link")){
                    String url1 = link.attr("href");

                    org.jsoup.nodes.Document document1 = Jsoup.connect(url1).get();
                    Elements links1 = null;
                    links1 = document1.getElementsByTag("p");  		//タグ"p"の要素を格納
                    for (org.jsoup.nodes.Element link1 : links1) {
                        String clas1 = link1.attr("class");			//属性"class"の属性値を取得
                        if(clas1.equals("ynDetailText") || clas1.equals("newsBody")){			//取得した属性値が"ynDetailText"と一致
                            String detailtext1 = link1.getElementsByAttribute("class").text();
                            TextView mDetailtext = (TextView) findViewById(R.id.item_detail_text);
                            mDetailtext.setText(detailtext1);
                        }
                    }
                    Elements links2 = null;
                    links2 = document1.getElementsByTag("div");  		//タグ"p"の要素を格納
                    for (org.jsoup.nodes.Element link2 : links2) {
                        String clas2 = link2.attr("class");			//属性"class"の属性値を取得
                        if(clas2.equals("marB10 clearFix yjMt") || clas2.equals("newsParagraph piL") || clas2.equals("rics-column bd covered")){
                            String detailtext2 = link2.getElementsByAttribute("class").text();
                            TextView mDetailtext = (TextView) findViewById(R.id.item_detail_text);
                            mDetailtext.setText(detailtext2);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e. printStackTrace();
        }

        try {						//ファイルへ書き込み：touchflag=1(既読)にする
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
            BufferedReader br = new BufferedReader(new FileReader(DISPLAY));
            try {
                BufferedWriter pw = new BufferedWriter(new FileWriter(NewDISPLAY,true));
                try {
                    while((line = br.readLine()) != null){
                        StringTokenizer tok = new StringTokenizer(line,"\t\t");
                        title_line = tok.nextToken();
                        link_line = tok.nextToken();
                        displaycount_line = Integer.parseInt(tok.nextToken());
                        viewcount_line = Integer.parseInt(tok.nextToken());
                        touchflag_line = Integer.parseInt(tok.nextToken());
                        if(title.equals(title_line)){
                            touchflag_line = 1;
                            pw.write(title_line+"\t"+link_line+"\t"+displaycount_line+"\t"+viewcount_line+"\t"+touchflag_line);
                            pw.newLine();
                        }else{
                            pw.write(title_line+"\t"+link_line+"\t"+displaycount_line+"\t"+viewcount_line+"\t"+touchflag_line);
                            pw.newLine();
                        }
                    }

                    pw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                br.close();
                DISPLAY.delete();
                NewDISPLAY.renameTo(DISPLAY);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public boolean dispatchKeyEvent(KeyEvent event){
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){		// 戻るボタンが押された！
            start = System.currentTimeMillis();
        }
        return super.dispatchKeyEvent(event);
    }
}