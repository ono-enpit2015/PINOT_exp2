package com.example.student11.pinot_exp2;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.net.URL;
import java.util.StringTokenizer;



/**
 * Created by student11 on 2015/09/25.
 */
public class ItemDetailActivity extends Activity {
    private TextView mTitle;
    public static long start = 0;
    //final String LOGDIR = "/sdcard/";           //android6.0に対応
    //final String LOGDIR = Environment.getExternalStorageDirectory().getPath()+"/data/";
    //final String SDFILE1 = LOGDIR + "displayed.txt";
    //final String SDFILE2 = LOGDIR + "tmp.txt";
    //File DISPLAYED = new File(SDFILE1);
    //File TMP = new File(SDFILE2);
    private String line;
    private int touch;
    private int viewcount;         //視認回数
    private int displaycount;      //表示回数
    private String title_displayed;
    private String link_displayed;
    String crlf = System.getProperty("line.separator");

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_detail);

        Intent intent = getIntent();
        String title = intent.getStringExtra("TITLE");
        mTitle = (TextView) findViewById(R.id.item_detail_title);
        mTitle.setText(title);

        MainActivity.progressDialog.dismiss();

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
                    String detailtext1 = "";
                    for (org.jsoup.nodes.Element link1 : links1) {
                        String clas1 = link1.attr("class");			//属性"class"の属性値を取得
                            if (clas1.equals("ynDetailText") || clas1.equals("newsBody") || clas1.equals("yjS ymuiDate")) {            //取得した属性値が"ynDetailText"と一致
                                String str = link1.getElementsByAttribute("class").text();

                                String[] detailtext = str.split("。", 0);
                                for (String text : detailtext) {
                                    detailtext1 += text + "。" + crlf + crlf;
                                }

                                TextView mDetailtext = (TextView) findViewById(R.id.item_detail_text);
                                mDetailtext.setText(detailtext1);
                            }
                    }
                    Elements links2 = null;
                    links2 = document1.getElementsByTag("div");  		//タグ"p"の要素を格納
                    String detailtext2 = "";
                    for (org.jsoup.nodes.Element link2 : links2) {
                        String clas2 = link2.attr("class");			//属性"class"の属性値を取得
                        if(clas2.equals("marB10 clearFix yjMt") || clas2.equals("newsParagraph piL") || clas2.equals("rics-column bd covered") || clas2.equals("mainBody")){
                            String str = link2.getElementsByAttribute("class").text();

                            String[] detailtext = str.split("。", 0);
                            for(String text: detailtext){
                                detailtext2 += text + "。" + crlf + crlf;
                            }

                            TextView mDetailtext = (TextView) findViewById(R.id.item_detail_text);
                            mDetailtext.setText(detailtext2);
                        }
                    }
                    Elements links3 = null;
                    links3 = document1.getElementsByTag("p");  		//タグ"p"の要素を格納
                    for (org.jsoup.nodes.Element link3 : links3) {
                        String clas3 = link3.attr("class");			//属性"class"の属性値を取得
                        if (clas3.equals("ymuiDate") || clas3.equals("source") || clas3.equals("ynLastEditDate yjSt") || clas3.equals("ynLastEditDate yjS")) {
                            String date = "\n"+link3.getElementsByAttribute("class").text()+"\n";
                            TextView mDate = (TextView) findViewById(R.id.date);
                            mDate.setText(date);
                        }
                    }

                    String image_url = "";
                    Elements links4 = document1.select("img[onContextMenu]");
                    image_url = links4.attr("src");            //属性"class"の属性値を取得
                    if(!image_url.isEmpty()) {
                        //imageを取得
                        ImageView image = (ImageView) findViewById(R.id.imageView);
                        //画像取得スレッド起動
                        ImageGetTask task = new ImageGetTask(image);
                        task.execute(image_url);
                    }

                    /*Elements links4 = null;
                    Elements links4_2 = null;
                    links4 = document1.getElementsByTag("div");  		//タグ"p"の要素を格納
                    for (org.jsoup.nodes.Element link4 : links4) {
                        links4_2 = link4.getElementsByTag("img");
                        System.out.println("links4_2：" + links4_2);
                        for (org.jsoup.nodes.Element link4_2 : links4_2) {
                                image_url = link4_2.attr("src");            //属性"class"の属性値を取得
                                System.out.println("画像のURL：" + image_url);
                                Toast.makeText(ItemDetailActivity.this, image_url, Toast.LENGTH_SHORT).show();
                            if(!image_url.isEmpty()) {
                                //imageを取得
                                ImageView image = (ImageView) findViewById(R.id.imageView);
                                //画像取得スレッド起動
                                ImageGetTask task = new ImageGetTask(image);
                                task.execute(image_url);
                            }
                        }
                    }*/

                    /*if(detailtext1.isEmpty()){
                        Toast.makeText(ItemDetailActivity.this, "記事情報を取得できませんでした", Toast.LENGTH_SHORT).show();
                    }*/
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e. printStackTrace();
        }

        /*try {
            DISPLAYED.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            TMP.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(DISPLAYED));
            try {
                BufferedWriter pw = new BufferedWriter(new FileWriter(TMP,true));
                try {
                    while((line = br.readLine()) != null){
                        StringTokenizer tok = new StringTokenizer(line,"\t\t");
                        title_displayed = tok.nextToken();
                        link_displayed = tok.nextToken();
                        displaycount = Integer.parseInt(tok.nextToken());
                        viewcount = Integer.parseInt(tok.nextToken());
                        touch = Integer.parseInt(tok.nextToken());
                        if(title.equals(title_displayed)){
                            if(touch == 0) {
                                pw.write(title_displayed + "\t" + link_displayed + "\t" + displaycount + "\t" + viewcount + "\t" + viewcount);
                                pw.newLine();
                            }else{
                                pw.write(title_displayed+"\t"+link_displayed+"\t"+displaycount+"\t"+viewcount+"\t"+touch);
                                pw.newLine();
                            }
                        }else{
                            pw.write(title_displayed+"\t"+link_displayed+"\t"+displaycount+"\t"+viewcount+ "\t"+touch);
                            pw.newLine();
                        }
                    }

                    pw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                br.close();
                DISPLAYED.delete();
                TMP.renameTo(DISPLAYED);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/

    }

    public boolean dispatchKeyEvent(KeyEvent event){
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){		// 戻るボタンが押された！
            start = System.currentTimeMillis();
        }
        return super.dispatchKeyEvent(event);
    }
}