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
    //private TextView mDescr;
    //private TextView mDate;
    //private WebView mWeb;
    //private TextView mDetailtext;
    public static long start = 0;
    final String LOGDIR = Environment.getExternalStorageDirectory().getPath()+"/data/";
    final String SDFILE3 = LOGDIR + "title_info.txt";
    final String SDFILE4 = LOGDIR + "title_info_new.txt";
    File Title = new File(SDFILE3);
    File Title_w = new File(SDFILE4);
    private String line;		//title_info.txtの先頭から１行ずつ取ってきたものを格納
    private int count;			//count=-1ならば既読、０以上なら未読で見たと判断した回数を表示
    private int count_line;
    private String title_line;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_detail);

        Intent intent = getIntent();
		/*String link = intent.getStringExtra("LINK");
        mWeb = (WebView)findViewById(R.id.item_detail_web);
        mWeb.loadUrl(link);*/
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

        try {						//ファイルへ書き込み：count=-1(既読)にする
            Title.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(Title));
            try {
                BufferedWriter pw = new BufferedWriter(new FileWriter(Title_w,true));
                try {
                    while((line = br.readLine()) != null){
                        StringTokenizer tok = new StringTokenizer(line,"\t\t");
                        title_line = tok.nextToken();
                        count_line = Integer.parseInt(tok.nextToken());
                        if(title.equals(title_line)){
                            count = -1;
                            pw.write(title_line+"\t\t"+count);
                            pw.newLine();
                        }else{
                            pw.write(title_line+"\t\t"+count_line);
                            pw.newLine();
                        }
                    }

                    pw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                br.close();
                Title.delete();
                Title_w.renameTo(Title);
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