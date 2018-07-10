package ru.myandroid.drebedengi_my;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    final String LOG_TAG = "myLogs";

    final int DIALOG_DATE = 0;
    final int DIALOG_CURRENCIES = 1;
    final int DIALOG_WALLETS = 2;
    final int DIALOG_CATEGORIES = 3;
    final int DIALOG_SOURCES = 4;
    final int DIALOG_TAGS = 5;
    final int DIALOG_CONFIRM = 6;

    final String ATTRIBUTE_NAME_IMAGE = "image";
    final String ATTRIBUTE_NAME_TEXT = "text";

    DB db;
    Cursor cursor;

    SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // открываем подключение к БД
        db = new DB(this);
        db.open();

        // Инициализация вкладок
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();
        setupTab(getString(R.string.operations_tab), R.id.operationsTab);
        setupTab(getString(R.string.history_tab), R.id.historyTab);
        setupTab(getString(R.string.balance_tab), R.id.balanceTab);
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(getResources().getColor(R.color.colorTextTab));
        }

        getSupportActionBar().hide();

        initOperationsTabContent();
        initHistoryTabContent();
        initBalanceTabContent();
    }


    private void setupTab(String title, int id) {
        TabHost.TabSpec spec = tabHost.newTabSpec(title);
        spec.setContent(id);
        spec.setIndicator(title);
        tabHost.addTab(spec);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }


// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 1. Вкладка введения операций
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

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


    private void initOperationsTabContent() {
        etSumRow = (TableRow) findViewById(R.id.etSumRow);
        etSumRow.setBackgroundColor(getResources().getColor(R.color.colorSpend));

        etSumDestRow = (TableRow) findViewById(R.id.etSumDestRow);
        etSumDestRow.setBackgroundColor(getResources().getColor(R.color.colorMove));
        etSumDestRow.setVisibility(View.GONE);

        // Первоначальное заполнение даты
        btnDate = (Button) findViewById(R.id.btnDate);
        String currDate = currentDateFormat.format(new Date(System.currentTimeMillis()));
        btnDate.setText(currDate);

        etSum = (EditText) findViewById(R.id.etSum);

        spinCurrency = (Spinner) findViewById(R.id.spinCurrency);
        loadDataForSpinner(DB.CURRENCY_TABLE, spinCurrency, R.string.currency);

        etSumDest = (EditText) findViewById(R.id.etSumDest);

        spinCurrencyDest = (Spinner) findViewById(R.id.spinCurrencyDest);
        loadDataForSpinner(DB.CURRENCY_TABLE, spinCurrencyDest, R.string.currency);

        spinWallet = (Spinner) findViewById(R.id.spinWallet);
        loadDataForSpinner(DB.WALLET_TABLE, spinWallet, R.string.wallet);

        spinWalletDest = (Spinner) findViewById(R.id.spinWalletDest);
        spinWalletDest.setVisibility(View.GONE);
        loadDataForSpinner(DB.WALLET_TABLE, spinWalletDest, R.string.wallet);

        spinCategory = (Spinner) findViewById(R.id.spinCategory);
        loadDataForSpinner(DB.SPENDING_TABLE, spinCategory, R.string.category);

        spinSource = (Spinner) findViewById(R.id.spinSource);
        spinSource.setVisibility(View.GONE);
        loadDataForSpinner(DB.SOURCES_TABLE, spinSource, R.string.source);

        etComment = (EditText) findViewById(R.id.etComment);

        btnConfirm = (Button) findViewById(R.id.btnConfirm);

        rgOperationChoice = (RadioGroup) findViewById(R.id.rgOperationChoice);
    }


    public void onEditTextClick(View v) {
//        Log.d(LOG_TAG, "etSum: " + etSum.getText().toString());
        if (etSum.getText().toString().equals("")) {
            etSum.setText(getResources().getString(R.string.sum));
        }
        if (etSumDest.getText().toString().equals("")) {
            etSumDest.setText(getResources().getString(R.string.buy));
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

            case R.id.etSumDest:
                if (etSumDest.getText().toString().equals(getResources().getString(R.string.buy))) {
                    etSumDest.setText("");
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

            case R.id.btnTag: showDialog(DIALOG_TAGS); break;

            case R.id.btnConfirm:
                onConfirmOperation();
                break;

            case R.id.rbSpend:
                etSumRow.setBackgroundColor(getResources().getColor(R.color.colorSpend));
                etSumDestRow.setVisibility(View.GONE);
                spinWalletDest.setVisibility(View.GONE);
                spinCategory.setVisibility(View.VISIBLE);
                spinSource.setVisibility(View.GONE);
                break;

            case R.id.rbGain:
                etSumRow.setBackgroundColor(getResources().getColor(R.color.colorGain));
                etSumDestRow.setVisibility(View.GONE);
                spinWalletDest.setVisibility(View.GONE);
                spinCategory.setVisibility(View.GONE);
                spinSource.setVisibility(View.VISIBLE);
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
                break;

            default: break;
        }
    }


    DatePickerDialog.OnDateSetListener dateDialogCallBack = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Date chosenDate = new Date(year - 1900, monthOfYear, dayOfMonth);
            btnDate.setText(currentDateFormat.format(chosenDate));
        }
    };


    public void onConfirmOperation() {
        if ( !etSum.getText().toString().equals(getResources().getString(R.string.sum))
                && !etSum.getText().toString().equals("")
                && Double.parseDouble(etSum.getText().toString()) > 0 ) {
            // Подготавливаем данные для записи в базу
            String[] date = btnDate.getText().toString().split("-");
            String currentDate = date[2] + "-" + date[1] + "-" + date[0];
//            double sum = Double.parseDouble(etSum.getText().toString());
            int currency_id = spinCurrency.getSelectedItemPosition();
            int wallet_id = spinWallet.getSelectedItemPosition();
            String comment = !etComment.getText().toString().equals(getResources().getString(R.string.comment)) ?
                    etComment.getText().toString() : "";

            int wallet_id_dest = -1;
            int category_id = -1;
            int operation_type = -1;
            int currency_id_dest = -1;
            double sum = 0.0;
            double sumDest = 0.0;
            String confirmText = "";
            switch (rgOperationChoice.getCheckedRadioButtonId()) {
                case R.id.rbSpend:
                    operation_type = db.SPENDING;
                    confirmText = "Трата сохранена";
                    category_id = spinCategory.getSelectedItemPosition();
                    sum = - Double.parseDouble(etSum.getText().toString());
                    break;

                case R.id.rbGain:
                    operation_type = db.GAIN;
                    confirmText = "Доход сохранён";
                    category_id = spinSource.getSelectedItemPosition();
                    sum = Double.parseDouble(etSum.getText().toString());
                    break;

                case R.id.rbMove:
                    operation_type = db.MOVE;
                    confirmText = "Перемещение сохранено";
                    wallet_id_dest = spinWalletDest.getSelectedItemPosition();
                    sum = Double.parseDouble(etSum.getText().toString());
                    break;

                case R.id.rbChange:
                    operation_type = db.CHANGE;
                    confirmText = "Обмен валют сохранён";
                    currency_id_dest = spinCurrencyDest.getSelectedItemPosition();
                    sum = Double.parseDouble(etSum.getText().toString());
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
                if (operation_type == db.SPENDING || operation_type == db.GAIN) {
                    db.addTransaction(operation_type, currency_id, wallet_id, category_id,
                            sum, currentDate, comment);
                }
                else if (operation_type == db.MOVE) {
                    db.addTransaction(db.SPENDING, currency_id, wallet_id, category_id,
                            -sum, currentDate, comment);
                    db.addTransaction(db.GAIN, currency_id, wallet_id_dest, category_id,
                            sum, currentDate, comment);
                }
                else if (operation_type == db.CHANGE) {
                    db.addTransaction(db.SPENDING, currency_id, wallet_id, category_id,
                            -sum, currentDate, comment);
                    db.addTransaction(db.GAIN, currency_id_dest, wallet_id_dest, category_id,
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

                // А ещё обновим историю операций и баланс
                loadDataForOperationHistory();
                loadDataForBalance();
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
            case DIALOG_DATE: // Дата
                String[] date = btnDate.getText().toString().split("-");
                return new DatePickerDialog(this, dateDialogCallBack, Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[0]));

            default: return adb.create();
        }
    }


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


// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 2. Вкладка истории операций
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    ListView lvHistory;

    private void initHistoryTabContent() {
        lvHistory = (ListView) findViewById(R.id.lvHistory);

        loadDataForOperationHistory();
    }


    // Загрузка истории операций из БД
    public void loadDataForOperationHistory() {
        cursor = db.getAllHistoryData();
        db.logCursor(cursor);

        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(cursor.getCount());
        Map<String, Object> m;
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                m = new HashMap<String, Object>();
                m.put(DB.WALLET_COLUMN_IMAGE, cursor.getString(cursor.getColumnIndex(DB.WALLET_COLUMN_IMAGE)));
//                m.put(DB.RECORD_COLUMN_CATEGORY_ID, cursor.getString(cursor.getColumnIndex(DB.RECORD_COLUMN_CATEGORY_ID)));
                m.put(DB.SPENDING_COLUMN_NAME, cursor.getString(cursor.getColumnIndex(DB.SPENDING_COLUMN_NAME)));
                m.put(DB.CURRENCY_COLUMN_TITLE, cursor.getString(cursor.getColumnIndex(DB.CURRENCY_COLUMN_TITLE)));
                m.put(DB.RECORD_COLUMN_SUM, cursor.getString(cursor.getColumnIndex(DB.RECORD_COLUMN_SUM)));
                m.put(DB.RECORD_COLUMN_DATE, cursor.getString(cursor.getColumnIndex(DB.RECORD_COLUMN_DATE)));

                data.add(m);
                cursor.moveToNext();
            }
        }

        String[] from = { DB.WALLET_COLUMN_IMAGE, DB.SPENDING_COLUMN_NAME, DB.RECORD_COLUMN_DATE, DB.RECORD_COLUMN_SUM, DB.CURRENCY_COLUMN_TITLE };
        int[] to = { R.id.ivImg, R.id.tvCategory, R.id.tvDate, R.id.tvSum, R.id.tvCurrency };

        lvHistory.setAdapter(new SimpleAdapter(this, data, R.layout.item_history, from, to));
    }


// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 3. Вкладка баланса
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    ListView lvBalance;

    private void initBalanceTabContent() {
        lvBalance = (ListView) findViewById(R.id.lvBalance);
        loadDataForBalance();
    }

    // Загрузка баланса из БД
    public void loadDataForBalance() {
        cursor = db.getAllBalanceData();
        db.logCursor(cursor);

        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(cursor.getCount());
        Map<String, Object> m;
//        if (cursor.moveToFirst()) {
        if (cursor.moveToFirst() && (cursor.getCount() > 0)) {
            for (int i = 0; i < cursor.getCount(); i++) {
                m = new HashMap<String, Object>();
                m.put(DB.WALLET_COLUMN_IMAGE, cursor.getString(cursor.getColumnIndex(DB.WALLET_COLUMN_IMAGE)));
                m.put(DB.WALLET_COLUMN_NAME, cursor.getString(cursor.getColumnIndex(DB.WALLET_COLUMN_NAME)));
                m.put(DB.CURRENCY_COLUMN_TITLE, cursor.getString(cursor.getColumnIndex(DB.CURRENCY_COLUMN_TITLE)));
                m.put(DB.RECORD_COLUMN_SUM, cursor.getString(cursor.getColumnIndex(DB.RECORD_COLUMN_SUM)));

                data.add(m);
                cursor.moveToNext();
            }
        }

        String[] from = { DB.WALLET_COLUMN_IMAGE, DB.WALLET_COLUMN_NAME, DB.RECORD_COLUMN_SUM, DB.CURRENCY_COLUMN_TITLE };
        int[] to = { R.id.ivImg, R.id.tvCategory, R.id.tvSum, R.id.tvCurrency };

        lvBalance.setAdapter(new SimpleAdapter(this, data, R.layout.item_balance, from, to));
    }
}
