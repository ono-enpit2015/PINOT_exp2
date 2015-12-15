package com.example.student11.pinot_exp2;

import android.app.ListActivity;
import android.content.ClipData;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends ListActivity {
    private static final String RSS_FEED_URL =  "http://www.rssmix.com/u/6589813/rss.xml";
    private ArrayList<Item> mItems;
    private RssListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Itemオブジェクトを保持するためのリストを生成し、アダプタに追加する
        mItems = new ArrayList<Item>();
        mAdapter = new RssListAdapter(this, mItems);

        // タスクを起動する
        RssParserTask task = new RssParserTask(this, mAdapter);
        task.execute(RSS_FEED_URL);
    }

    // リストの項目を選択した時の処理
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Item item = mItems.get(position);
        Intent intent = new Intent(this, ItemDetailActivity.class);

        intent.putExtra("TITLE", item.getTitle());
        intent.putExtra("LINK", item.getLink());

        startActivity(intent);

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
