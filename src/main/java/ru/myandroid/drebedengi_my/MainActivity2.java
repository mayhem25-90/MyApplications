//package ru.myandroid.drebedengi_my;
//
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.database.Cursor;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ListView;
//import android.widget.SimpleAdapter;
//import android.widget.Spinner;
//import android.widget.TextView;
//
//import java.sql.Date;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Locale;
//import java.util.Map;
//
//public class MainActivity extends AppCompatActivity {
//
//    final String LOG_TAG = "myLogs";
//
//    final int DIALOG_CURRENCIES = 1;
//    final int DIALOG_WALLETS = 2;
//    final int DIALOG_CATEGORIES = 3;
//    final int DIALOG_SOURCES = 4;
//    final int DIALOG_TAGS = 5;
//
//    final int RADIO_SPENDING = 11;
//    final int DIALOG_GAINS = 12;
//    final int DIALOG_MOVES = 13;
//    final int DIALOG_CHANGES = 14;
//
//    final String ATTRIBUTE_NAME_IMAGE = "image";
//    final String ATTRIBUTE_NAME_TEXT = "text";
//
//    DB db;
//    Cursor cursor;
//
////    String data[] = { "one", "two", "three", "four" };
////    boolean chkd[] = { false, true, true, false };
//
//    // виджеты 1-го блока
//    TextView tvHello;
//    //    EditText etDate;
//    Button btnDate, btnCategory, btnSource;
//    Spinner spinner;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.tab_operations_activity);
//
//        // открываем подключение к БД
//        db = new DB(this);
//        db.open();
//        stopManagingCursor(cursor);
//        cursor = db.getAllData();
//
//        initContent();
//    }
//
//
//    protected void onDestroy() {
//        super.onDestroy();
//        db.close();
//    }
//
//
//    private void initContent() {
//        tvHello = (TextView) findViewById(R.id.tvHello);
//        tvHello.setVisibility(View.GONE);
////        tvHello.setVisibility(View.VISIBLE);
//
//        // Первоначальное заполнение даты
//        btnDate = (Button) findViewById(R.id.btnDate);
//        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
//        String currDate = currentDate.format(new Date(System.currentTimeMillis()));
//        btnDate.setText(currDate);
//
//        spinner = (Spinner) findViewById(R.id.spinner);
//
//        btnCategory = (Button) findViewById(R.id.btnCategory);
//
//        btnSource = (Button) findViewById(R.id.btnSource);
//        btnSource.setVisibility(View.GONE);
//    }
//
//
//    public void onButtonClick(View v) {
//        switch (v.getId()) {
//            case R.id.btnCurrency: showDialog(DIALOG_CURRENCIES); break;
//            case R.id.btnWallet: showDialog(DIALOG_WALLETS); break;
//            case R.id.btnCategory: showDialog(DIALOG_CATEGORIES); break;
//            case R.id.btnSource: showDialog(DIALOG_SOURCES); break;
//            case R.id.btnTag: showDialog(DIALOG_TAGS); break;
//
//            case R.id.rb1st:
//                btnCategory.setVisibility(View.VISIBLE);
//                btnSource.setVisibility(View.GONE);
////                showDialog(RADIO_SPENDING);
//                break;
//
//            case R.id.rb2nd:
//                btnCategory.setVisibility(View.GONE);
//                btnSource.setVisibility(View.VISIBLE);
////                showDialog(DIALOG_GAINS);
//                break;
//
//            case R.id.rb3rd: showDialog(DIALOG_MOVES); break;
//            case R.id.rb4th: showDialog(DIALOG_CHANGES); break;
//            default: break;
//        }
//    }
//
//
//    protected Dialog onCreateDialog(int id) {
//        AlertDialog.Builder adb = new AlertDialog.Builder(this);
//        switch (id) {
//            // Валюты
//            case DIALOG_CURRENCIES:
//                adb.setTitle(R.string.currencies);
////                adb.setMultiChoiceItems(data, chkd, myItemsMultiClickListener);
////                adb.setMultiChoiceItems(cursor, DB.COLUMN_CHK, DB.WALLET_COLUMN_NAME, myCursorMultiClickListener);
//                adb.setCursor(cursor, myClickListener, DB.WALLET_COLUMN_NAME);
//                break;
//
//            // курсор
//            case DIALOG_WALLETS:
//                adb.setTitle(R.string.wallets);
////                adb.setMultiChoiceItems(cursor, DB.COLUMN_CHK, DB.WALLET_COLUMN_NAME, myCursorMultiClickListener);
//
//                // упаковываем данные в понятную для адаптера структуру
//                ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(cursor.getCount());
//                Map<String, Object> m;
//                if (cursor.moveToFirst()) {
//                    for (int i = 0; i < cursor.getCount(); i++) {
//                        m = new HashMap<String, Object>();
////                        Log.d(LOG_TAG, "cursor.getString(i) = " + cursor.getString(cursor.getColumnIndex(DB.WALLET_COLUMN_NAME)));
////                        m.put(ATTRIBUTE_NAME_TEXT, "test");
//                        m.put(ATTRIBUTE_NAME_TEXT, cursor.getString(cursor.getColumnIndex(DB.WALLET_COLUMN_NAME)));
////                        m.put(ATTRIBUTE_NAME_IMAGE, cursor.getString(i));
//                        m.put(ATTRIBUTE_NAME_IMAGE, R.mipmap.ic_launcher);
//
//                        data.add(m);
//                        cursor.moveToNext();
//                    }
//                }
//
//                // массив имен атрибутов, из которых будут читаться данные
//                String[] from = { ATTRIBUTE_NAME_IMAGE, ATTRIBUTE_NAME_TEXT };
//                // массив ID View-компонентов, в которые будут вставлять данные
//                int[] to = { R.id.ivImg, R.id.tvText };
//
//                SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.item, from, to);
//                adb.setAdapter(sAdapter, myClickListener);
//                spinner.setAdapter(sAdapter);
//                break;
//        }
////        adb.setPositiveButton(R.string.ok, myBtnClickListener);
//        return adb.create();
//    }
//
//
//    // обработчик нажатия на пункт списка диалога
//    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
//        public void onClick(DialogInterface dialog, int which) {
//            // выводим в лог позицию нажатого элемента
//            Log.d(LOG_TAG, "which = " + which);
//        }
//    };
//
//
//    // обработчик для списка массива
//    DialogInterface.OnMultiChoiceClickListener myItemsMultiClickListener = new DialogInterface.OnMultiChoiceClickListener() {
//        @Override
//        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//            Log.d(LOG_TAG, "which = " + which + ", isChecked = " + isChecked);
//        }
//    };
//
//
//    // обработчик для списка курсора
//    DialogInterface.OnMultiChoiceClickListener myCursorMultiClickListener = new DialogInterface.OnMultiChoiceClickListener() {
//        @Override
//        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//            ListView lv = ((AlertDialog) dialog).getListView();
//            Log.d(LOG_TAG, "which = " + which + ", isChecked = " + isChecked);
//            db.changeRec(which, isChecked);
//            cursor.requery();
//        }
//    };
//}
