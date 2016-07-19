package com.example.student11.pinot_exp2;
import android.content.ClipData;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by student11 on 2015/09/25.
 */
//「RssListAdapter.java」では、スマホ画面への表示を行ってます。
public class RssListAdapter extends ArrayAdapter<Item> {
    private LayoutInflater mInflater;		//レイアウトのxmlからviewを作成してくれるファイル
    private TextView mTitle;

    public RssListAdapter(Context context, ArrayList<Item> objects) {
        super(context, 0, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    static class ViewHolder {//追加
        TextView labelText;
    }

    // 1行ごとのビューを生成する
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;//追加

        if (convertView == null){
            view = mInflater.inflate(R.layout.item_row, null);		//item_row.xmlの内容を取得
            //view = mInflater.inflate(android.R.layout.simple_list_item_1, null);
            //Log.i("a",""+view);
            TextView label = (TextView)view.findViewById(R.id.item_title);//追加
            holder = new ViewHolder();
            holder.labelText = label;
            view.setTag(holder);
        }else {//追加
            holder = (ViewHolder) view.getTag();
        }

        // 現在参照しているリストの位置からItemを取得する
        Item item = this.getItem(position);
        if (item != null) {
            // Itemから必要なデータを取り出し、それぞれTextViewにセットする
            String title = item.getTitle().toString();
            //mTitle = (TextView) view.findViewById(R.id.item_title);
            //mTitle = (TextView) view.findViewById(android.R.id.text1);
            //mTitle.setText(title);

            if (!TextUtils.isEmpty(title)) {//追加
                // テキストビューにラベルをセット
                holder.labelText.setText(title);
            }
        }

        // XMLで定義したアニメーションを読み込む
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.item_motion);//追加
        // リストアイテムのアニメーションを開始
        view.startAnimation(anim);//追加

        return view;
    }
}