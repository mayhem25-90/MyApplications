package ru.myandroid.drebedengi_my;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;


public class SpinnerCursorAdapter extends SimpleCursorAdapter
{

    SpinnerCursorAdapter(Context context, int resource, Cursor cursor, String[] from, int[] to) {
        super(context, resource, cursor, from, to);
    }

    @Override
    public void setViewImage(ImageView v, String res_id) { // метод супер-класса, который вставляет текст
        super.setViewImage(v, res_id);

//        switch (v.getId()) {
//            case R.id.ivImg:
//                if (!res_id.equals("")) {
//                    v.setImageResource(Integer.parseInt(res_id));
//                }
//                break;
//
//            case R.id.ivGroup:
//                if (!res_id.equals("")) {
//                    int groupNumber = Integer.parseInt(res_id);
//                    if (groupNumber % 1000 != 0) {
//                        v.setImageResource(R.mipmap.empty42);
//                    }
//                }
//                break;
//
//            default: break;
//        }
    }
}
