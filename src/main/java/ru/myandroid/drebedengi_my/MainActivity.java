package ru.myandroid.drebedengi_my;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
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

    DB db;
    Cursor cursor;

    TabHost tabHost;

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
        setupTab(getString(R.string.balance_tab), R.id.balanceTab);
        setupTab(getString(R.string.history_tab), R.id.historyTab);
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
    protected void onStop() {
        super.onStop();
        db.setSelectedParameter(history_item_selected_id, db.NOT_SELECTED);
        loadDataForOperationHistory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }


// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 1. Вкладка введения операций
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    final int DIALOG_DATE = 0;
    final int DIALOG_CURRENCIES = 1;
    final int DIALOG_WALLETS = 2;
    final int DIALOG_CATEGORIES = 3;
    final int DIALOG_SOURCES = 4;
    final int DIALOG_TAGS = 5;
    final int DIALOG_CONFIRM = 6;
    final int DIALOG_DELETE = 7;

    public static final int spin_currency = 0,
                            spin_currency_dest = 1,
                            spin_wallet = 2,
                            spin_wallet_dest = 3,
                            spin_spending = 4,
                            spin_source = 5;

    int currentRadioButton = 0;
    int[] radioButtons = {R.id.rbSpend, R.id.rbGain, R.id.rbMove, R.id.rbChange};

    // виджеты 1-го блока
//    TextView tvHello;
    LinearLayout operationsTab;
    TableRow etSumRow, etSumDestRow;
    EditText etSum, etSumDest, etComment;
    Button btnDate, btnConfirm;
    Spinner spinCurrency, spinCurrencyDest, spinWallet, spinWalletDest, spinCategory, spinSource;
    RadioGroup rgOperationChoice;
//    ListView lvTestData;

    SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);


    private void initOperationsTabContent() {
        Log.d(LOG_TAG, "initOperationsTabContent");

        operationsTab = (LinearLayout) findViewById(R.id.operationsTab);

//        tvHello = (TextView) findViewById(R.id.tvHello);
//        tvHello.setOnTouchListener(new View.OnSwipeTouchListener() {

        operationsTab.setOnTouchListener(new OnSwipeTouchListener(this) {
            public void onSwipeLeft() {
                if (currentRadioButton < radioButtons.length - 1) {
                    currentRadioButton++;
                    rgOperationChoice.check(radioButtons[currentRadioButton]);
                    onButtonClick(findViewById(radioButtons[currentRadioButton]));
//                    Toast.makeText(MainActivity.this, "Move right", Toast.LENGTH_SHORT).show();
                }
            }

            public void onSwipeRight() {
                if (currentRadioButton > 0) {
                    currentRadioButton--;
                    rgOperationChoice.check(radioButtons[currentRadioButton]);
                    onButtonClick(findViewById(radioButtons[currentRadioButton]));
//                    Toast.makeText(MainActivity.this, "Move left", Toast.LENGTH_SHORT).show();
                }
            }
        });

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

        etSumDest = (EditText) findViewById(R.id.etSumDest);

        spinCurrency = (Spinner) findViewById(R.id.spinCurrency);

        spinCurrencyDest = (Spinner) findViewById(R.id.spinCurrencyDest);

        spinWallet = (Spinner) findViewById(R.id.spinWallet);

        spinWalletDest = (Spinner) findViewById(R.id.spinWalletDest);
        spinWalletDest.setVisibility(View.GONE);

        spinCategory = (Spinner) findViewById(R.id.spinCategory);

        spinSource = (Spinner) findViewById(R.id.spinSource);
        spinSource.setVisibility(View.GONE);

        loadDataForSpinner(DB.CURRENCY_TABLE, spinCurrency,     R.string.currency, spin_currency);
        loadDataForSpinner(DB.CURRENCY_TABLE, spinCurrencyDest, R.string.currency, spin_currency_dest);
        loadDataForSpinner(DB.WALLET_TABLE,   spinWallet,       R.string.wallet,   spin_wallet);
        loadDataForSpinner(DB.WALLET_TABLE,   spinWalletDest,   R.string.wallet,   spin_wallet_dest);
        loadDataForSpinner(DB.CATEGORY_TABLE, spinCategory,     R.string.category, spin_spending);
        loadDataForSpinner(DB.CATEGORY_TABLE, spinSource,       R.string.source,   spin_source);

        etComment = (EditText) findViewById(R.id.etComment);

        btnConfirm = (Button) findViewById(R.id.btnConfirm);

//        lvTestData = (ListView) findViewById(R.id.lvTestData);
//        loadDataForTestList();

        rgOperationChoice = (RadioGroup) findViewById(R.id.rgOperationChoice);
        rgOperationChoice.check(radioButtons[currentRadioButton]);
    }


    public void onButtonClick(View v) {
        switch (v.getId()) {
            case R.id.tvDelete: showDialog(DIALOG_DELETE); break;

            case R.id.btnDate: showDialog(DIALOG_DATE); break;

            case R.id.btnTag: showDialog(DIALOG_TAGS); break;

            case R.id.btnConfirm: onConfirmOperation(); break;

            case R.id.rbSpend:
                etSumRow.setBackgroundColor(getResources().getColor(R.color.colorSpend));
                etSumDestRow.setVisibility(View.GONE);
                spinWalletDest.setVisibility(View.GONE);
                spinCategory.setVisibility(View.VISIBLE);
                spinSource.setVisibility(View.GONE);
                currentRadioButton = 0;
                break;

            case R.id.rbGain:
                etSumRow.setBackgroundColor(getResources().getColor(R.color.colorGain));
                etSumDestRow.setVisibility(View.GONE);
                spinWalletDest.setVisibility(View.GONE);
                spinCategory.setVisibility(View.GONE);
                spinSource.setVisibility(View.VISIBLE);
                currentRadioButton = 1;
                break;

            case R.id.rbMove:
                etSumRow.setBackgroundColor(getResources().getColor(R.color.colorMove));
                etSumDestRow.setVisibility(View.GONE);
                spinWalletDest.setVisibility(View.VISIBLE);
                spinCategory.setVisibility(View.GONE);
                spinSource.setVisibility(View.GONE);
                currentRadioButton = 2;
                break;

            case R.id.rbChange:
                etSumRow.setBackgroundColor(getResources().getColor(R.color.colorMove));
                etSumDestRow.setVisibility(View.VISIBLE);
                spinWalletDest.setVisibility(View.VISIBLE);
                spinCategory.setVisibility(View.GONE);
                spinSource.setVisibility(View.GONE);
                currentRadioButton = 3;
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


    long m_spending_category_id = -1;
    long m_gain_category_id = -1;
    long m_currency_id = -1;
    long m_wallet_id = -1;
    long m_currency_id_dest = -1;
    long m_wallet_id_dest = -1;


    public void onConfirmOperation() {
        if ( !etSum.getText().toString().equals(getResources().getString(R.string.sum_hint))
                && !etSum.getText().toString().equals("")
                && Double.parseDouble(etSum.getText().toString()) > 0 ) {
            // Подготавливаем данные для записи в базу
            String[] date = btnDate.getText().toString().split("-");
            String currentDate = date[2] + "-" + date[1] + "-" + date[0];
            String comment = !etComment.getText().toString().equals(getResources().getString(R.string.comment_hint)) ?
                    etComment.getText().toString() : "";

            long category_id = -1;
            int operation_type = -1;
            double sum = 0.0;
            double sumDest = 0.0;
            String confirmText = "";
            switch (rgOperationChoice.getCheckedRadioButtonId()) {
                case R.id.rbSpend:
                    operation_type = db.SPENDING;
                    confirmText = "Трата сохранена";
                    category_id = m_spending_category_id;
                    sum = - Double.parseDouble(etSum.getText().toString());
                    break;

                case R.id.rbGain:
                    operation_type = db.GAIN;
                    confirmText = "Доход сохранён";
                    category_id = m_gain_category_id;
                    sum = Double.parseDouble(etSum.getText().toString());
                    break;

                case R.id.rbMove:
                    operation_type = db.MOVE;
                    confirmText = "Перемещение сохранено";
                    sum = Double.parseDouble(etSum.getText().toString());
                    break;

                case R.id.rbChange:
                    operation_type = db.CHANGE;
                    confirmText = "Обмен валют сохранён";
                    sum = Double.parseDouble(etSum.getText().toString());
                    sumDest = Double.parseDouble(etSumDest.getText().toString());
                    break;

                default: break;
            }

            Log.d(LOG_TAG, "currentDate = " + currentDate);
            Log.d(LOG_TAG, "sum = " + sum);
            Log.d(LOG_TAG, "sum_dest = " + sumDest);
            Log.d(LOG_TAG, "currency_id = " + m_currency_id);
            Log.d(LOG_TAG, "currency_id_dest = " + m_currency_id_dest);
            Log.d(LOG_TAG, "wallet_id = " + m_wallet_id);
            Log.d(LOG_TAG, "wallet_id_dest = " + m_wallet_id_dest);
            Log.d(LOG_TAG, "category_id = " + category_id);
            Log.d(LOG_TAG, "comment = " + comment);
            Log.d(LOG_TAG, "operation_type = " + operation_type);

            // Добаляем запись в базу
            try {
                if (operation_type == db.SPENDING || operation_type == db.GAIN) {
                    db.addTransaction(operation_type, m_currency_id, m_wallet_id, category_id,
                            sum, currentDate, comment);
                }
                else if (operation_type == db.MOVE) {
                    db.addTransaction(db.SPENDING, m_currency_id, m_wallet_id, category_id,
                            -sum, currentDate, comment);
                    db.addTransaction(db.GAIN, m_currency_id, m_wallet_id_dest, category_id,
                            sum, currentDate, comment);
                }
                else if (operation_type == db.CHANGE) {
                    db.addTransaction(db.SPENDING, m_currency_id, m_wallet_id, category_id,
                            -sum, currentDate, comment);
                    db.addTransaction(db.GAIN, m_currency_id_dest, m_wallet_id_dest, category_id,
                            sumDest, currentDate, comment);
                }
            }
            catch (Exception ex) {
                Log.d(LOG_TAG, ex.getClass() + " db write error: " + ex.getMessage());
            }
            finally {
                Toast.makeText(this, confirmText, Toast.LENGTH_SHORT).show();
                etSum.setText("");
                etSumDest.setText("");
                etComment.setText("");

                // А ещё обновим историю операций и баланс
                loadDataForOperationHistory();
                loadDataForBalance();
            }
        }
        else {
            Toast.makeText(this, "Сумма должна быть больше 0", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        switch (id) {
            case DIALOG_DATE: // Дата
                String[] date = btnDate.getText().toString().split("-");
                return new DatePickerDialog(this, dateDialogCallBack, Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[0]));

            case DIALOG_DELETE: // Удаление операции
                adb.setMessage(R.string.delete_message);
                adb.setPositiveButton(R.string.yes, deleteButtonClickListener);
                adb.setNeutralButton(R.string.no, deleteButtonClickListener);

            default: return adb.create();
        }
    }


    DialogInterface.OnClickListener deleteButtonClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_POSITIVE: // Положительная кнопка
                    db.deleteTransaction(history_item_selected_id);
                    Toast.makeText(MainActivity.this, R.string.record_deleted, Toast.LENGTH_SHORT).show();

                    // А ещё обновим историю операций и баланс
                    loadDataForOperationHistory();
                    loadDataForBalance();
                    break;

                case Dialog.BUTTON_NEUTRAL: // Нейтральная кнопка
                    break;
            }
        }
    };


    // Загрузка данных в спиннеры из БД
    public void loadDataForSpinner(final String TABLE_NAME, Spinner currentSpinner, int string_id, final int type) {
//        Log.d(LOG_TAG, "load data for spinner: " + type);
        if (TABLE_NAME.equals(DB.CATEGORY_TABLE)) {
            cursor = db.getAllData(TABLE_NAME, string_id);
        }
        else
            cursor = db.getAllData(TABLE_NAME);
//        db.logCursor(cursor);

        // формируем столбцы сопоставления
        String[] from = { DB.TABLE_COLUMN_IMAGE, DB.TABLE_COLUMN_NAME };
        int[] to = { R.id.ivImg, R.id.tvText };

        // создааем адаптер и настраиваем список
        SimpleCursorAdapter scAdapter = new SimpleCursorAdapter(this, R.layout.item_spinner, cursor, from, to);
        currentSpinner.setAdapter(scAdapter);
        currentSpinner.setPrompt(getResources().getString(string_id));

        // устанавливаем обработчик нажатия
        currentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (type) {
                    case spin_currency:      m_currency_id = id;          break;
                    case spin_currency_dest: m_currency_id_dest = id;     break;
                    case spin_wallet:        m_wallet_id = id;            break;
                    case spin_wallet_dest:   m_wallet_id_dest = id;       break;
                    case spin_spending:      m_spending_category_id = id; break;
                    case spin_source:        m_gain_category_id = id;     break;
                    default: break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
    }


// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 2. Вкладка истории операций
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    long history_item_selected_id = -1;

    ListView lvHistory;

    private void initHistoryTabContent() {
        Log.d(LOG_TAG, "initHistoryTabContent");
        lvHistory = (ListView) findViewById(R.id.lvHistory);
        loadDataForOperationHistory();

        lvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Log.d(LOG_TAG, "itemClick: position = " + position + ", id = " + id);
                db.setSelectedParameter(id, db.AUTO_SELECT);
                if (history_item_selected_id != id) { // Убираем меню со старого элемента
                    db.setSelectedParameter(history_item_selected_id, db.NOT_SELECTED);
                    history_item_selected_id = id;
                }

                // А ещё обновим историю операций и баланс
                loadDataForOperationHistory();
//                loadDataForBalance();
            }
        });
    }


    // Загрузка истории операций из БД
    public void loadDataForOperationHistory() {
        cursor = db.getAllHistoryData();
//        db.logCursor(cursor);

        String[] from = { DB.WALLET_COLUMN_IMAGE, DB.CATEGORY_COLUMN_NAME, DB.RECORD_COLUMN_COMMENT, DB.RECORD_COLUMN_DATE, DB.RECORD_COLUMN_SUM, DB.CURRENCY_COLUMN_TITLE,
                DB.RECORD_COLUMN_SELECTED };
        int[] to = { R.id.ivImg, R.id.tvCategory, R.id.tvComment, R.id.tvDate, R.id.tvSum, R.id.tvCurrency,
                R.id.tvDelete };

        lvHistory.setAdapter(new HistoryCursorAdapter(this, R.layout.item_history, cursor, from, to));
    }


// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 3. Вкладка баланса
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    ListView lvBalance;

    private void initBalanceTabContent() {
        Log.d(LOG_TAG, "initBalanceTabContent");
        lvBalance = (ListView) findViewById(R.id.lvBalance);
        loadDataForBalance();
    }

    // Загрузка баланса из БД
    public void loadDataForBalance() {
        cursor = db.getAllBalanceData();
//        db.logCursor(cursor);

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
