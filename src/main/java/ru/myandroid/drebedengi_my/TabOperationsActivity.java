package ru.myandroid.drebedengi_my;
/*
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TableRow;
import android.widget.Toast;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class TabOperationsActivity extends AppCompatActivity
{
//    private final Context mCtx;

    public TabOperationsActivity(Context ctx, DB ext_db) {
//        mCtx = ctx;
        db = ext_db;
    }

    final String LOG_TAG = "myLogs";

    final int DIALOG_DATE = 0;
    final int DIALOG_CURRENCIES = 1;
    final int DIALOG_WALLETS = 2;
    final int DIALOG_CATEGORIES = 3;
    final int DIALOG_SOURCES = 4;
    final int DIALOG_TAGS = 5;
    final int DIALOG_CONFIRM = 6;

    final int SPENDING = 1, GAIN = 2, MOVE = 3, CHANGE = 4;

    final String ATTRIBUTE_NAME_IMAGE = "image";
    final String ATTRIBUTE_NAME_TEXT = "text";

    DB db;
    Cursor cursor;


    // виджеты 1-го блока
    TableRow etSumRow, etSumDestRow;
    //    TextView tvHello;
//    EditText etDate;
    EditText etSum, etSumDest, etComment;
    Button btnDate, btnConfirm;
    //    btnCategory, btnSource;
    Spinner spinCurrency, spinCurrencyDest, spinWallet, spinWalletDest, spinCategory, spinSource;
    RadioGroup rgOperationChoice;
    TabHost tabHost;

    SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_operations_activity);

        // открываем подключение к БД
//        db = new DB(this);
//        db.open();
//        stopManagingCursor(cursor);
//        cursor = db.getAllData(DB.WALLET_TABLE);

        // Инициализация вкладок
//        tabHost = (TabHost) findViewById(android.R.id.tabhost);
//        tabHost.setup();
//        setupTab(getString(R.string.categories), R.id.operationsTab);
//        setupTab(getString(R.string.buy), R.id.secondTab);

        initContent();
    }


//        // обработчик переключения вкладок
//        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
//            public void onTabChanged(String tabId) {
//                Toast.makeText(getBaseContext(), "tabId = " + tabId, Toast.LENGTH_SHORT).show();
//            }
//        });


    private void setupTab(String title, int id) {
        TabHost.TabSpec spec = tabHost.newTabSpec(title);
        spec.setContent(id);
        spec.setIndicator(title);
        tabHost.addTab(spec);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        db.close();
    }


    private void initContent() {
//        tvHello = (TextView) findViewById(R.id.tvHello);
//        tvHello.setVisibility(View.GONE);
//        tvHello.setVisibility(View.VISIBLE);

        etSumRow = (TableRow) findViewById(R.id.etSumRow);
        etSumRow.setBackgroundColor(getResources().getColor(R.color.colorSpend));

        etSumDestRow = (TableRow) findViewById(R.id.etSumDestRow);
        etSumDestRow.setBackgroundColor(getResources().getColor(R.color.colorMove));
        etSumDestRow.setVisibility(View.GONE);

        // Первоначальное заполнение даты
        btnDate = (Button) findViewById(R.id.btnDate);
//        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        String currDate = currentDateFormat.format(new Date(System.currentTimeMillis()));
        btnDate.setText(currDate);

        etSum = (EditText) findViewById(R.id.etSum);

        spinCurrency = (Spinner) findViewById(R.id.spinCurrency);
        loadDataForSpinner(DB.CURRENCY_TABLE, spinCurrency, R.string.currency);

        etSumDest = (EditText) findViewById(R.id.etSumDest);

        spinCurrencyDest = (Spinner) findViewById(R.id.spinCurrencyDest);
        loadDataForSpinner(DB.CURRENCY_TABLE, spinCurrencyDest, R.string.currency);

        spinWallet = (Spinner) findViewById(R.id.spinWallet);
//        loadDataForSpinWallet();
        loadDataForSpinner(DB.WALLET_TABLE, spinWallet, R.string.wallet);

        spinWalletDest = (Spinner) findViewById(R.id.spinWalletDest);
        spinWalletDest.setVisibility(View.GONE);
        loadDataForSpinner(DB.WALLET_TABLE, spinWalletDest, R.string.wallet);

//        btnCategory = (Button) findViewById(R.id.btnCategory);
        spinCategory = (Spinner) findViewById(R.id.spinCategory);
//        loadDataForSpinCategory();
        loadDataForSpinner(DB.SPENDING_TABLE, spinCategory, R.string.category);

//        btnSource = (Button) findViewById(R.id.btnSource);
//        btnSource.setVisibility(View.GONE);
        spinSource = (Spinner) findViewById(R.id.spinSource);
        spinSource.setVisibility(View.GONE);
//        loadDataForSpinSource();
        loadDataForSpinner(DB.SOURCES_TABLE, spinSource, R.string.source);

        etComment = (EditText) findViewById(R.id.etComment);

        btnConfirm = (Button) findViewById(R.id.btnConfirm);

        rgOperationChoice = (RadioGroup) findViewById(R.id.rgOperationChoice);
//        rgExerciseChoice.check(R.id.rb1st);
    }


    public void onEditTextClick(View v) {
//        Log.d(LOG_TAG, "etSum: " + etSum.getText().toString());
        if (etSum.getText().toString().equals("")) {
            etSum.setText(getResources().getString(R.string.sum));
        }
        if (etComment.getText().toString().equals("")) {
            etComment.setText(getResources().getString(R.string.comment));
        }
        switch (v.getId()) {
            case R.id.etSum:
                if (etSum.getText().toString().equals(getResources().getString(R.string.sum))) {
//                    Log.d(LOG_TAG, "etSum: строки совпадают");
                    etSum.setText("");
//                    etSum.setSelection(0);
                }
                break;

            case R.id.etComment:
                if (etComment.getText().toString().equals(getResources().getString(R.string.comment))) {
                    etComment.setText("");
                }
                break;

            default: break;
        }
    }

    public void onButtonClick(View v) {
        if (etSum.getText().toString().equals("")) {
            etSum.setText(getResources().getString(R.string.sum));
        }
        if (etComment.getText().toString().equals("")) {
            etComment.setText(getResources().getString(R.string.comment));
        }
        switch (v.getId()) {
            case R.id.btnDate: showDialog(DIALOG_DATE); break;

//            case R.id.btnCurrency: showDialog(DIALOG_CURRENCIES); break;
//            case R.id.spinCurrency: showDialog(DIALOG_CURRENCIES); break;
//            case R.id.btnWallet: showDialog(DIALOG_WALLETS); break;
//            case R.id.spinWallet: showDialog(DIALOG_WALLETS); break;
//            case R.id.btnCategory: showDialog(DIALOG_CATEGORIES); break;
//            case R.id.btnSource: showDialog(DIALOG_SOURCES); break;
            case R.id.btnTag: showDialog(DIALOG_TAGS); break;

            case R.id.btnConfirm:
//                showDialog(DIALOG_CONFIRM);
                onConfirmOperation();
                break;

            case R.id.rbSpend:
                etSumRow.setBackgroundColor(getResources().getColor(R.color.colorSpend));
                etSumDestRow.setVisibility(View.GONE);
                spinWalletDest.setVisibility(View.GONE);
//                btnCategory.setVisibility(View.VISIBLE);
                spinCategory.setVisibility(View.VISIBLE);
//                btnSource.setVisibility(View.GONE);
                spinSource.setVisibility(View.GONE);
//                showDialog(RADIO_SPENDING);
                break;

            case R.id.rbGain:
                etSumRow.setBackgroundColor(getResources().getColor(R.color.colorGain));
                etSumDestRow.setVisibility(View.GONE);
                spinWalletDest.setVisibility(View.GONE);
//                btnCategory.setVisibility(View.GONE);
                spinCategory.setVisibility(View.GONE);
//                btnSource.setVisibility(View.VISIBLE);
                spinSource.setVisibility(View.VISIBLE);
//                showDialog(DIALOG_GAINS);
                break;

            case R.id.rbMove:
                etSumRow.setBackgroundColor(getResources().getColor(R.color.colorMove));
                etSumDestRow.setVisibility(View.GONE);
                spinWalletDest.setVisibility(View.VISIBLE);
                spinCategory.setVisibility(View.GONE);
                spinSource.setVisibility(View.GONE);
                break;

            case R.id.rbChange:
                etSumRow.setBackgroundColor(getResources().getColor(R.color.colorMove));
                etSumDestRow.setVisibility(View.VISIBLE);
                spinWalletDest.setVisibility(View.VISIBLE);
                spinCategory.setVisibility(View.GONE);
                spinSource.setVisibility(View.GONE);
//                showDialog(DIALOG_CHANGES);
                break;

//            case R.id.etSum:
//                if (etSum.getText().toString().equals(getResources().getString(R.string.sum))) {
////                    Log.d(LOG_TAG, "etSum: строки совпадают");
////                    etSum.setText("");
//                    etSum.setSelection(0);
//                }
//                break;

            default: break;
        }
    }


    DatePickerDialog.OnDateSetListener dateDialogCallBack = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//            myYear = year - 1900;
//            myMonth = monthOfYear;
//            myDay = dayOfMonth;
            Date chosenDate = new Date(year - 1900, monthOfYear, dayOfMonth);
//            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
//            tvDate.setText("Today is " + myDay + "." + myMonth + "." + myYear);
//            btnDate.setText(myDay + "-" + myMonth + "-" + myYear);
            btnDate.setText(currentDateFormat.format(chosenDate));
        }
    };


    public void testDB() {
        Log.d(LOG_TAG, "testDB");
        db.addTransaction(5, 5, 5, 5,
                25.0, "t1", "t2");
    }

    public void onConfirmOperation() {
        if ( !etSum.getText().toString().equals(getResources().getString(R.string.sum))
                && !etSum.getText().toString().equals("")
                && Double.parseDouble(etSum.getText().toString()) > 0 ) {
            // Подготавливаем данные для записи в базу
            String[] date = btnDate.getText().toString().split("-");
            String currentDate = date[2] + "-" + date[1] + "-" + date[0];
            double sum = Double.parseDouble(etSum.getText().toString());
            int currency_id = spinCurrency.getSelectedItemPosition();
            int wallet_id = spinWallet.getSelectedItemPosition();
            String comment = !etComment.getText().toString().equals(getResources().getString(R.string.comment)) ?
                    etComment.getText().toString() : "";

            int wallet_id_dest = -1;
            int category_id = -1;
            int operation_type = -1;
            int currency_id_dest = -1;
            double sumDest = -1.0;
            String confirmText = "";
            switch (rgOperationChoice.getCheckedRadioButtonId()) {
                case R.id.rbSpend:
                    operation_type = SPENDING;
                    confirmText = "Трата сохранена";
                    category_id = spinCategory.getSelectedItemPosition();
                    break;

                case R.id.rbGain:
                    operation_type = GAIN;
                    confirmText = "Доход сохранён";
                    category_id = spinSource.getSelectedItemPosition();
                    break;

                case R.id.rbMove:
                    operation_type = MOVE;
                    confirmText = "Перемещение сохранено";
                    wallet_id_dest = spinWalletDest.getSelectedItemPosition();
                    break;

                case R.id.rbChange:
                    operation_type = CHANGE;
                    confirmText = "Обмен валют сохранён";
                    currency_id_dest = spinCurrencyDest.getSelectedItemPosition();
                    sumDest = Double.parseDouble(etSumDest.getText().toString());
                    break;

                default: break;
            }

            Log.d(LOG_TAG, "currentDate = " + currentDate);
            Log.d(LOG_TAG, "sum = " + sum);
            Log.d(LOG_TAG, "sum_dest = " + sumDest);
            Log.d(LOG_TAG, "currency_id = " + currency_id);
            Log.d(LOG_TAG, "currency_id_dest = " + currency_id_dest);
            Log.d(LOG_TAG, "wallet_id = " + wallet_id);
            Log.d(LOG_TAG, "wallet_id_dest = " + wallet_id_dest);
            Log.d(LOG_TAG, "category_id = " + category_id);
            Log.d(LOG_TAG, "comment = " + comment);
            Log.d(LOG_TAG, "operation_type = " + operation_type);

            // Добаляем запись в базу
            try {
                if (operation_type == SPENDING || operation_type == GAIN) {
                    db.addTransaction(operation_type, currency_id, wallet_id, category_id,
                            sum, currentDate, comment);
                }
                else if (operation_type == MOVE) {
                    db.addTransaction(SPENDING, currency_id, wallet_id, category_id,
                            sum, currentDate, comment);
                    db.addTransaction(GAIN, currency_id, wallet_id_dest, category_id,
                            sum, currentDate, comment);
                }
                else if (operation_type == CHANGE) {
                    db.addTransaction(SPENDING, currency_id, wallet_id, category_id,
                            sum, currentDate, comment);
                    db.addTransaction(GAIN, currency_id_dest, wallet_id_dest, category_id,
                            sumDest, currentDate, comment);
                }
            }
            catch (Exception ex) {
                Log.d(LOG_TAG, ex.getClass() + " db write error: " + ex.getMessage());
            }
            finally {
                Toast.makeText(this, confirmText, Toast.LENGTH_LONG).show();
                etSum.setText(getResources().getString(R.string.sum));
                etComment.setText(getResources().getString(R.string.comment));
            }
        }
        else {
            Toast.makeText(this, "Сумма должна быть больше 0", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        switch (id) {
            // Дата
            case DIALOG_DATE:
//                DatePickerDialog tpd = new DatePickerDialog(this, myCallBack, myYear, myMonth, myDay);
//                return tpd;
//                String currDate = btnDate.getText().toString();
//                String[] date = currDate.split("-");
                String[] date = btnDate.getText().toString().split("-");
//                String currD = "123";
//                java.sql.date currentDate = currentDateFormat.parse(currD);
//                Log.d(LOG_TAG, "date = " + date[0] + " " + date[1] + " " + date[2]);
//                return new DatePickerDialog(this, dateDialogCallBack, myYear, myMonth, myDay);
                return new DatePickerDialog(this, dateDialogCallBack, Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[0]));

            // Валюты
//            case DIALOG_CURRENCIES:
//                adb.setTitle(R.string.currencies);
////                adb.setMultiChoiceItems(data, chkd, myItemsMultiClickListener);
////                adb.setMultiChoiceItems(cursor, DB.COLUMN_CHK, DB.WALLET_COLUMN_NAME, myCursorMultiClickListener);
//                adb.setCursor(cursor, myClickListener, DB.CURRENCY_COLUMN_NAME);
//                break;

            // Кошельки
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
//                        m.put(ATTRIBUTE_NAME_IMAGE, R.mipmap.olm);
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
//                spinWallet.setAdapter(sAdapter);
//                break;

            default: return adb.create();
        }
//        adb.setPositiveButton(R.string.ok, myBtnClickListener);
//        return adb.create();
    }


    // обработчик нажатия на пункт списка диалога
//    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
//        public void onClick(DialogInterface dialog, int which) {
//            // выводим в лог позицию нажатого элемента
//            Log.d(LOG_TAG, "which = " + which);
//        }
//    };


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


    // Загрузка данных в спиннеры из БД
    public void loadDataForSpinner(String TABLE_NAME, Spinner currentSpinner, int string_id) {
        cursor = db.getAllData(TABLE_NAME);
        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(cursor.getCount());
        Map<String, Object> m;
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                m = new HashMap<String, Object>();
                m.put(ATTRIBUTE_NAME_TEXT, cursor.getString(cursor.getColumnIndex(DB.TABLE_COLUMN_NAME)));
                m.put(ATTRIBUTE_NAME_IMAGE, cursor.getString(cursor.getColumnIndex(DB.TABLE_COLUMN_IMAGE)));

                data.add(m);
                cursor.moveToNext();
            }
        }

        String[] from = { ATTRIBUTE_NAME_IMAGE, ATTRIBUTE_NAME_TEXT };
        int[] to = { R.id.ivImg, R.id.tvText };

        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.item, from, to);
        currentSpinner.setAdapter(sAdapter);
        currentSpinner.setPrompt(getResources().getString(string_id));
    }


//    public void loadDataForSpinWallet() {
//
////        final int test[] = {R.mipmap.ic_launcher, R.mipmap.olm, R.mipmap.ic_launcher, R.mipmap.ic_launcher};
//        cursor = db.getAllData(DB.WALLET_TABLE);
//
//        // упаковываем данные в понятную для адаптера структуру
//        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(cursor.getCount());
//        Map<String, Object> m;
//        if (cursor.moveToFirst()) {
//            for (int i = 0; i < cursor.getCount(); i++) {
//                m = new HashMap<String, Object>();
////                        Log.d(LOG_TAG, "cursor.getString(i) = " + cursor.getString(cursor.getColumnIndex(DB.WALLET_COLUMN_NAME)));
////                        m.put(ATTRIBUTE_NAME_TEXT, "test");
//                m.put(ATTRIBUTE_NAME_TEXT, cursor.getString(cursor.getColumnIndex(DB.WALLET_COLUMN_NAME)));
////                        m.put(ATTRIBUTE_NAME_IMAGE, cursor.getString(i));
////                m.put(ATTRIBUTE_NAME_IMAGE, test[i]);
//                m.put(ATTRIBUTE_NAME_IMAGE, cursor.getString(cursor.getColumnIndex(DB.WALLET_COLUMN_IMAGE)));
//
//                data.add(m);
//                cursor.moveToNext();
//            }
//        }
//
//        // массив имен атрибутов, из которых будут читаться данные
//        String[] from = { ATTRIBUTE_NAME_IMAGE, ATTRIBUTE_NAME_TEXT };
//        // массив ID View-компонентов, в которые будут вставлять данные
//        int[] to = { R.id.ivImg, R.id.tvText };
//
//        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.item, from, to);
//        spinWallet.setAdapter(sAdapter);
//        spinWallet.setPrompt(getResources().getString(R.string.wallet));
//    }
//
//
//    public void loadDataForSpinCategory() {
//        cursor = db.getAllData(DB.SPENDING_TABLE);
//        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(cursor.getCount());
//        Map<String, Object> m;
//        if (cursor.moveToFirst()) {
//            for (int i = 0; i < cursor.getCount(); i++) {
//                m = new HashMap<String, Object>();
//                m.put(ATTRIBUTE_NAME_TEXT, cursor.getString(cursor.getColumnIndex(DB.SPENDING_COLUMN_NAME)));
//                m.put(ATTRIBUTE_NAME_IMAGE, cursor.getString(cursor.getColumnIndex(DB.SPENDING_COLUMN_IMAGE)));
//
//                data.add(m);
//                cursor.moveToNext();
//            }
//        }
//
//        String[] from = { ATTRIBUTE_NAME_IMAGE, ATTRIBUTE_NAME_TEXT };
//        int[] to = { R.id.ivImg, R.id.tvText };
//
//        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.item, from, to);
//        spinCategory.setAdapter(sAdapter);
//        spinCategory.setPrompt(getResources().getString(R.string.category));
//    }
//
//
//    public void loadDataForSpinSource() {
//        cursor = db.getAllData(DB.SOURCES_TABLE);
//        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(cursor.getCount());
//        Map<String, Object> m;
//        if (cursor.moveToFirst()) {
//            for (int i = 0; i < cursor.getCount(); i++) {
//                m = new HashMap<String, Object>();
//                m.put(ATTRIBUTE_NAME_TEXT, cursor.getString(cursor.getColumnIndex(DB.SOURCES_COLUMN_NAME)));
//                m.put(ATTRIBUTE_NAME_IMAGE, cursor.getString(cursor.getColumnIndex(DB.SOURCES_COLUMN_IMAGE)));
//
//                data.add(m);
//                cursor.moveToNext();
//            }
//        }
//
//        String[] from = { ATTRIBUTE_NAME_IMAGE, ATTRIBUTE_NAME_TEXT };
//        int[] to = { R.id.ivImg, R.id.tvText };
//
//        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.item, from, to);
//        spinSource.setAdapter(sAdapter);
//        spinSource.setPrompt(getResources().getString(R.string.source));
//    }
}
*/