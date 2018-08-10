package ru.myandroid.drebedengi_my;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;


public class PlanCursorAdapter extends SimpleCursorAdapter
{
    private DecimalFormat sumFormat;

    PlanCursorAdapter(Context context, int resource, Cursor cursor, String[] from, int[] to) {
        super(context, resource, cursor, from, to);

        DecimalFormatSymbols sumFormatSymbols = new DecimalFormatSymbols();
        sumFormatSymbols.setGroupingSeparator(' ');
        sumFormat = new DecimalFormat("#,###", sumFormatSymbols); // отделяем тысячные разряды
    }

    @Override
    public void setViewText(TextView v, String text) {
        super.setViewText(v, text);

        switch (v.getId()) {
            case R.id.tvSumFact:
                if (!text.equals("")) {
                    int sum = Integer.parseInt(text);
                    if (sum < 0) v.setTextColor(Color.RED);
                    v.setText(sumFormat.format(sum));
                }
                break;

            case R.id.tvSumPlan:
                if (!text.equals("")) {
                    int sum = Integer.parseInt(text);
                    v.setText(sumFormat.format(sum));
                }
                break;

            default: break;
        }
    }
}
