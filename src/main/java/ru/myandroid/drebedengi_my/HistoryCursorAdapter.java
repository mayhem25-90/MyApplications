package ru.myandroid.drebedengi_my;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static ru.myandroid.drebedengi_my.DB.dbDateFormat;
import static ru.myandroid.drebedengi_my.DB.dbTimeFormat;


public class HistoryCursorAdapter extends SimpleCursorAdapter
{
    private SimpleDateFormat historyDateFormat = new SimpleDateFormat("dd MMM,", Locale.US);
    private SimpleDateFormat historyTimeFormat = new SimpleDateFormat("HH:mm", Locale.US);
    private DecimalFormat sumFormat;

    HistoryCursorAdapter(Context context, int resource, Cursor cursor, String[] from, int[] to) {
        super(context, resource, cursor, from, to);

        DecimalFormatSymbols sumFormatSymbols = new DecimalFormatSymbols();
        sumFormatSymbols.setGroupingSeparator(' ');
        sumFormat = new DecimalFormat("#,###", sumFormatSymbols); // отделяем тысячные разряды
    }

    @Override
    public void setViewText(TextView v, String text) { // метод супер-класса, который вставляет текст
        super.setViewText(v, text);

        switch (v.getId()) {
            case R.id.tvSum:
                double sum = Double.parseDouble(text);
                if (sum < 0) v.setTextColor(Color.RED);
                else if (sum > 0) v.setTextColor(Color.rgb(0, 127, 0));
                v.setText(sumFormat.format(sum));
                break;

            case R.id.tvCategory:
                v.setTextColor(Color.BLACK);
                if (text.equals("")) {
                    v.setVisibility(View.GONE);
                }
                else v.setVisibility(View.VISIBLE);
                break;

            case R.id.tvWalletFrom:
                v.setTextColor(Color.BLACK);
                if (text.equals("")) {
                    v.setVisibility(View.GONE);
                }
                else v.setVisibility(View.VISIBLE);
                break;

            case R.id.tvWalletTo:
                v.setTextColor(Color.BLACK);
                if (text.equals("")) {
                    v.setVisibility(View.GONE);
                }
                else v.setVisibility(View.VISIBLE);
                break;

            case R.id.tvDate:
                final Calendar calendar = Calendar.getInstance();
                String today = dbDateFormat.format(calendar.getTime());
                calendar.add(Calendar.DATE, -1);
                String yesterday = dbDateFormat.format(calendar.getTime());

                if (text.equals(today)) {
                    v.setText("Сегодня,");
                }
                else if (text.equals(yesterday)) {
                    v.setText("Вчера,");
                }
                else {
                    try {
                        v.setText(historyDateFormat.format(dbDateFormat.parse(text)));
                    }
                    catch (Exception exception) {
                        Log.d("myLogs", exception.toString());
                    }
                }
                break;

            case R.id.tvTime:
                try {
                    v.setText(historyTimeFormat.format(dbTimeFormat.parse(text)));
                }
                catch (Exception exception) {
                    Log.d("myLogs", exception.toString());
                }
                break;

            case R.id.tvDelete:
            case R.id.tvEdit:
                int select = Integer.parseInt(text);
                if (select == 1) {
                    v.setPadding(0, 13, 0, 13);
                    v.setTextSize(14);
                    if (v.getId() == R.id.tvDelete) v.setText("Удалить");
                    if (v.getId() == R.id.tvEdit) v.setText("Редактировать");
                }
                else if (select == 0) {
                    v.setPadding(0, 0, 0, 0);
                    v.setTextSize(0);
                }
                break;

            default: break;
        }
    }
}
