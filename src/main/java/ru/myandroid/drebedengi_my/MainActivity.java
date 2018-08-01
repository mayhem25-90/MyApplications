package ru.myandroid.drebedengi_my;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

import static java.lang.Math.abs;
import static ru.myandroid.drebedengi_my.DB.dbDateFormat;
import static ru.myandroid.drebedengi_my.DB.dbTimeFormat;


public class MainActivity extends AppCompatActivity {

    final String LOG_TAG = "myLogs";

    DB db;
    Cursor cursor;

    int tabCount = 3;
    int[] tabName = { R.string.operations_tab, R.string.balance_tab, R.string.history_tab };
    int[] tabID = { R.id.operationsTab, R.id.balanceTab, R.id.historyTab };
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
        for (int i = 0; i < tabCount; i++) {
            setupTab(getString(tabName[i]), tabID[i]);
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(getResources().getColor(R.color.colorTextTab));
        }

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
//                Log.d(LOG_TAG, "onTabChanged: " + s);
                // Скрываем клавиатуру:
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        });

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
    final int DIALOG_TIME = 1;
    final int DIALOG_TAGS = 2;
    final int DIALOG_DELETE = 3;

    public static final int spin_currency = 0,
                            spin_currency_dest = 1,
                            spin_wallet = 2,
                            spin_wallet_dest = 3,
                            spin_spending = 4,
                            spin_source = 5;

    int currentRadioButton = 0;
    int[] radioButtons = {R.id.rbSpend, R.id.rbGain, R.id.rbMove, R.id.rbChange};

    // виджеты 1-го блока
    LinearLayout operationsTab;
    TableRow etSumRow, etSumDestRow;
    EditText etSum, etSumDest, etComment;
    Button btnDateLeft, btnDateRight, btnDate, btnTime, btnConfirm, btnEdit, btnCancel;
    Spinner spinCurrency, spinCurrencyDest, spinWallet, spinWalletDest, spinCategory, spinSource;
    RadioGroup rgOperationChoice;
//    ListView lvTestData;

    Calendar operationDate = Calendar.getInstance();

    SimpleDateFormat btnDateFormat = new SimpleDateFormat("dd MMM, EEE", Locale.US);
    SimpleDateFormat btnTimeFormat = new SimpleDateFormat("HH:mm", Locale.US);

    Vector<Integer> spinCurrencyBind = new Vector<>();
    Vector<Integer> spinCurrencyDestBind = new Vector<>();
    Vector<Integer> spinWalletBind = new Vector<>();
    Vector<Integer> spinWalletDestBind = new Vector<>();
    Vector<Integer> spinCategoryBind = new Vector<>();
    Vector<Integer> spinSourceBind = new Vector<>();


    private void initOperationsTabContent() {
        Log.d(LOG_TAG, "initOperationsTabContent");

        operationsTab = (LinearLayout) findViewById(R.id.operationsTab);

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

        // Первоначальное заполнение даты
        btnDate = (Button) findViewById(R.id.btnDate);
        btnDate.setText(checkDateForToday(operationDate.getTime()));

        btnTime = (Button) findViewById(R.id.btnTime);
        btnTime.setText(btnTimeFormat.format(operationDate.getTime()));

        btnDateLeft = (Button) findViewById(R.id.btnDateLeft);

        btnDateRight = (Button) findViewById(R.id.btnDateRight);

        etSumRow = (TableRow) findViewById(R.id.etSumRow);
        etSumRow.setBackgroundColor(getResources().getColor(R.color.colorSpend));

        etSumDestRow = (TableRow) findViewById(R.id.etSumDestRow);
        etSumDestRow.setBackgroundColor(getResources().getColor(R.color.colorMove));
        etSumDestRow.setVisibility(View.GONE);

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

        loadDataForSpinner(DB.CURRENCY_TABLE, spinCurrency,     spinCurrencyBind,     R.string.currency, spin_currency);
        loadDataForSpinner(DB.CURRENCY_TABLE, spinCurrencyDest, spinCurrencyDestBind, R.string.currency, spin_currency_dest);
        loadDataForSpinner(DB.WALLET_TABLE,   spinWallet,       spinWalletBind,       R.string.wallet,   spin_wallet);
        loadDataForSpinner(DB.WALLET_TABLE,   spinWalletDest,   spinWalletDestBind,   R.string.wallet,   spin_wallet_dest);
        loadDataForSpinner(DB.CATEGORY_TABLE, spinCategory,     spinCategoryBind,     R.string.category, spin_spending);
        loadDataForSpinner(DB.CATEGORY_TABLE, spinSource,       spinSourceBind,       R.string.source,   spin_source);

        etComment = (EditText) findViewById(R.id.etComment);

        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        btnEdit = (Button) findViewById(R.id.btnEdit);
        btnEdit.setVisibility(View.GONE);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setVisibility(View.GONE);

//        lvTestData = (ListView) findViewById(R.id.lvTestData);
//        loadDataForTestList();

        rgOperationChoice = (RadioGroup) findViewById(R.id.rgOperationChoice);
        rgOperationChoice.check(radioButtons[currentRadioButton]);


//        Log.d(LOG_TAG, "spinCurrencyBind:");
//        for (int i = 0; i < spinCurrencyBind.size(); ++i) {
//            Log.d(LOG_TAG, i + ": " + spinCurrencyBind.get(i));
//        }
//        Log.d(LOG_TAG, "spinWalletBind:");
//        for (int i = 0; i < spinWalletBind.size(); ++i) {
//            Log.d(LOG_TAG, i + ": " + spinWalletBind.get(i));
//        }
//        Log.d(LOG_TAG, "spinCategoryBind:");
//        for (int i = 0; i < spinCategoryBind.size(); ++i) {
//            Log.d(LOG_TAG, i + ": " + spinCategoryBind.get(i));
//        }
//        Log.d(LOG_TAG, "spinSourceBind:");
//        for (int i = 0; i < spinSourceBind.size(); ++i) {
//            Log.d(LOG_TAG, i + ": " + spinSourceBind.get(i));
//        }
    }


    public String checkDateForToday(Date checkedDate) {
        final Calendar currentDate = Calendar.getInstance();
        String today = dbDateFormat.format(currentDate.getTime());
        currentDate.add(Calendar.DATE, -1);
        String yesterday = dbDateFormat.format(currentDate.getTime());
        String dayOfWeek = new SimpleDateFormat(", EEE", Locale.US).format(checkedDate);

        // Если дата сегодняшнего или вчерашнего дня, выводим текстом:
        if (dbDateFormat.format(checkedDate).equals(today)) {
            return getResources().getString(R.string.today) + dayOfWeek;
        }
        else if (dbDateFormat.format(checkedDate).equals(yesterday)) {
            return getResources().getString(R.string.yesterday) + dayOfWeek;
        }
        else {
            // А если год не совпадает с текущим, меняем формат:
            if (operationDate.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)) {
                btnDateFormat = new SimpleDateFormat("dd MMM, EEE", Locale.US);
            }
            else btnDateFormat = new SimpleDateFormat("dd MMM yyyy, EEE", Locale.US);
            return btnDateFormat.format(checkedDate);
        }
    }


    public void onButtonClick(View v) {
        switch (v.getId()) {
            case R.id.btnDateLeft:
                operationDate.add(Calendar.DATE, -1);
                btnDate.setText(checkDateForToday(operationDate.getTime()));
                break;

            case R.id.btnDateRight:
                operationDate.add(Calendar.DATE, 1);
                btnDate.setText(checkDateForToday(operationDate.getTime()));
                break;

            case R.id.tvDelete: showDialog(DIALOG_DELETE); break;

            case R.id.tvEdit:
                tabHost.setCurrentTabByTag(getString(R.string.operations_tab));
                loadOperationDataToOperationTab();
                break;

            case R.id.btnDate: showDialog(DIALOG_DATE); break;

            case R.id.btnTime: showDialog(DIALOG_TIME); break;

            case R.id.btnTag: showDialog(DIALOG_TAGS); break;

            case R.id.btnConfirm: onConfirmOperation(db.CONFIRM_SAVE); break;

            case R.id.btnEdit: onConfirmOperation(db.CONFIRM_EDIT); break;

            case R.id.btnCancel: onCancelEdit(); break;

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
            Date currentDate = operationDate.getTime();
            currentDate.setYear(year - 1900);
            currentDate.setMonth(monthOfYear);
            currentDate.setDate(dayOfMonth);
            operationDate.setTime(currentDate);
            btnDate.setText(checkDateForToday(operationDate.getTime()));
        }
    };


    TimePickerDialog.OnTimeSetListener timeDialogCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hour, int minute) {
            Date currentDate = operationDate.getTime();
            currentDate.setHours(hour);
            currentDate.setMinutes(minute);
            operationDate.setTime(currentDate);
            btnTime.setText(btnTimeFormat.format(currentDate));
        }
    };


    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        switch (id) {
            case DIALOG_DATE: // Дата
                return new DatePickerDialog(this, dateDialogCallBack,
                        operationDate.get(Calendar.YEAR), operationDate.get(Calendar.MONTH), operationDate.get(Calendar.DATE));

            case DIALOG_TIME: // Время
                return new TimePickerDialog(this, timeDialogCallBack,
                        operationDate.get(Calendar.HOUR_OF_DAY), operationDate.get(Calendar.MINUTE), true);

            case DIALOG_DELETE: // Удаление операции
                adb.setMessage(R.string.delete_message);
                adb.setPositiveButton(R.string.yes, deleteButtonClickListener);
                adb.setNeutralButton(R.string.no, deleteButtonClickListener);
                return adb.create();

            default: return adb.create();
        }
    }


    long m_spending_category_id = -1;
    long m_gain_category_id = -1;
    long m_currency_id = -1;
    long m_wallet_id = -1;
    long m_currency_id_dest = -1;
    long m_wallet_id_dest = -1;


    public void onConfirmOperation(int mode) {
        if ( !etSum.getText().toString().equals(getResources().getString(R.string.sum_hint))
                && !etSum.getText().toString().equals("")
                && Double.parseDouble(etSum.getText().toString()) > 0 ) {
            // Подготавливаем данные для записи в базу
            String currentDate = dbDateFormat.format(operationDate.getTime());
            String currentTime = dbTimeFormat.format(operationDate.getTime());
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
            Log.d(LOG_TAG, "currentTime = " + currentTime);
            Log.d(LOG_TAG, "sum = " + sum);
            Log.d(LOG_TAG, "sum_dest = " + sumDest);
            Log.d(LOG_TAG, "currency_id = " + m_currency_id);
            Log.d(LOG_TAG, "currency_id_dest = " + m_currency_id_dest);
            Log.d(LOG_TAG, "wallet_id = " + m_wallet_id);
            Log.d(LOG_TAG, "wallet_id_dest = " + m_wallet_id_dest);
            Log.d(LOG_TAG, "category_id = " + category_id);
            Log.d(LOG_TAG, "comment = " + comment);
            Log.d(LOG_TAG, "operation_type = " + operation_type);
            Log.d(LOG_TAG, "history_item_selected_id = " + history_item_selected_id);

            // Добаляем запись в базу
            try {
                if (operation_type == db.SPENDING || operation_type == db.GAIN) {
                    db.addTransaction(history_item_selected_id, operation_type, m_currency_id, m_wallet_id, category_id,
                            sum, currentDate, currentTime, comment, mode);
                }
                else if (operation_type == db.MOVE) {
                    db.addTransaction(history_item_selected_id, db.SPENDING, m_currency_id, m_wallet_id, category_id,
                            -sum, currentDate, currentTime, comment, mode);
                    db.addTransaction(history_item_selected_id, db.GAIN, m_currency_id, m_wallet_id_dest, category_id,
                            sum, currentDate, currentTime, comment, mode);
                }
                else if (operation_type == db.CHANGE) {
                    db.addTransaction(history_item_selected_id, db.SPENDING, m_currency_id, m_wallet_id, category_id,
                            -sum, currentDate, currentTime, comment, mode);
                    db.addTransaction(history_item_selected_id, db.GAIN, m_currency_id_dest, m_wallet_id_dest, category_id,
                            sumDest, currentDate, currentTime, comment, mode);
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

                if (mode == db.CONFIRM_EDIT) {
                    tabHost.setCurrentTabByTag(getString(R.string.history_tab));

                    btnConfirm.setVisibility(View.VISIBLE);
                    btnEdit.setVisibility(View.GONE);
                    btnCancel.setVisibility(View.GONE);
                }
            }
        }
        else {
            Toast.makeText(this, "Сумма должна быть больше 0", Toast.LENGTH_SHORT).show();
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
    public void loadDataForSpinner(final String TABLE_NAME, Spinner currentSpinner, Vector<Integer> spinnerBind, int string_id, final int type) {
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

        // связка индекса спиннера с id записи из базы
        if (cursor.moveToFirst()) {
            int column = cursor.getColumnIndex(DB.CATEGORY_COLUMN_ID);
//            Log.d(LOG_TAG, "spinner: " + getResources().getString(string_id));
            do {
                int id = cursor.getInt(column);
//                Log.d(LOG_TAG, "spin id: " + id);
                spinnerBind.add(id);
            } while (cursor.moveToNext());
        }

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


    // Загрузка операции из БД для редактирования
    void loadOperationDataToOperationTab() {
        cursor = db.loadTransactionDataById(history_item_selected_id);
//        db.logCursor(cursor);

        btnConfirm.setVisibility(View.GONE);
        btnEdit.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);

        if (cursor.moveToFirst()) {
            int operationType = cursor.getInt(cursor.getColumnIndex(DB.RECORD_COLUMN_OPERATION_TYPE));
            int currencyID = cursor.getInt(cursor.getColumnIndex(DB.RECORD_COLUMN_CURRENCY_ID));
            int walletID = cursor.getInt(cursor.getColumnIndex(DB.RECORD_COLUMN_WALLET_ID));
            int categoryID = cursor.getInt(cursor.getColumnIndex(DB.RECORD_COLUMN_CATEGORY_ID));
            int sum = cursor.getInt(cursor.getColumnIndex(DB.RECORD_COLUMN_SUM));
            String operationDate = cursor.getString(cursor.getColumnIndex(DB.RECORD_COLUMN_DATE));
            String comment = cursor.getString(cursor.getColumnIndex(DB.RECORD_COLUMN_COMMENT));

//            Log.d(LOG_TAG, "operationType = " + operationType);
//            Log.d(LOG_TAG, "currencyID = " + currencyID);
//            Log.d(LOG_TAG, "walletID = " + walletID);
//            Log.d(LOG_TAG, "categoryID = " + categoryID);
//            Log.d(LOG_TAG, "sum = " + sum);
//            Log.d(LOG_TAG, "comment = " + comment);
//            Log.d(LOG_TAG, "operationDate = " + operationDate);

            rgOperationChoice.check(radioButtons[operationType - 1]);
            onButtonClick(findViewById(radioButtons[operationType - 1]));

            spinCurrency.setSelection(getSpinnerIndexByID(currencyID, spinCurrencyBind));
            spinCurrencyDest.setSelection(getSpinnerIndexByID(currencyID, spinCurrencyDestBind));
            spinWallet.setSelection(getSpinnerIndexByID(walletID, spinWalletBind));
            spinWalletDest.setSelection(getSpinnerIndexByID(walletID, spinWalletDestBind));
            if (categoryID < db.div_category_gain) {
                spinCategory.setSelection(getSpinnerIndexByID(categoryID, spinCategoryBind));
            }
            else spinSource.setSelection(getSpinnerIndexByID(categoryID, spinSourceBind));

            etSum.setText(String.valueOf(abs(sum)));
            etComment.setText(comment);
            try {
                btnDate.setText(btnDateFormat.format(dbDateFormat.parse(operationDate)));
            }
            catch (Exception exception) {
                Log.d(LOG_TAG, exception.toString());
            }
        }
    }


    // Выбор индекса спиннера по ID из БД
    int getSpinnerIndexByID(int id, Vector<Integer> spinnerBind) {
        int spinnerIndex = 0;
        for (int i = 0; i < spinnerBind.size(); ++i) {
            if (spinnerBind.get(i) == id) {
                spinnerIndex = i;
                break;
            }
        }
        return spinnerIndex;
    }


    // Отмена редактирования
    void onCancelEdit() { // Сбрасываем состояние виджетов
        rgOperationChoice.check(radioButtons[0]);
        onButtonClick(findViewById(radioButtons[0]));

        btnDate.setText(btnDateFormat.format(new Date(System.currentTimeMillis())));

        etSum.setText("");
        etSumDest.setText("");
        etComment.setText("");

        spinCurrency.setSelection(0);
        spinCurrencyDest.setSelection(0);
        spinWallet.setSelection(0);
        spinWalletDest.setSelection(0);
        spinCategory.setSelection(0);
        spinSource.setSelection(0);

        tabHost.setCurrentTabByTag(getString(R.string.history_tab));

        btnConfirm.setVisibility(View.VISIBLE);
        btnEdit.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
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

                // А ещё обновим историю операций (чтобы контекстное меню элемента отобразилось)
                loadDataForOperationHistory();
            }
        });
    }


    // Загрузка истории операций из БД
    public void loadDataForOperationHistory() {
        cursor = db.getAllHistoryData();
//        db.logCursor(cursor);

        String[] from = { DB.WALLET_COLUMN_IMAGE, DB.CATEGORY_COLUMN_NAME, DB.RECORD_COLUMN_COMMENT,
                DB.RECORD_COLUMN_DATE, DB.RECORD_COLUMN_TIME, DB.RECORD_COLUMN_SUM, DB.CURRENCY_COLUMN_TITLE,
                DB.RECORD_COLUMN_SELECTED, DB.RECORD_COLUMN_SELECTED };
        int[] to = { R.id.ivImg, R.id.tvCategory, R.id.tvComment,
                R.id.tvDate, R.id.tvTime, R.id.tvSum, R.id.tvCurrency,
                R.id.tvDelete, R.id.tvEdit };

        lvHistory.setAdapter(new HistoryCursorAdapter(this, R.layout.item_history, cursor, from, to));
    }


// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 3. Вкладка баланса
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    LinearLayout llBalanceList;

    private void initBalanceTabContent() {
        Log.d(LOG_TAG, "initBalanceTabContent");
        llBalanceList = (LinearLayout) findViewById(R.id.llBalanceList);
        loadDataForBalance();
    }


    public void loadDataForBalance() {
        LayoutInflater ltInflater = getLayoutInflater();

        for (int i = 0; i < db.getNumberOfRecords(DB.WALLET_TABLE); i++) {
//            Log.d(LOG_TAG, "walletData[" + i + "]: " + db.walletData[i]);

            View balanceListItem = ltInflater.inflate(R.layout.item_balance, llBalanceList, false);

            ImageView ivImg = (ImageView) balanceListItem.findViewById(R.id.ivImg);
            TextView tvCategory = (TextView) balanceListItem.findViewById(R.id.tvCategory);
            tvCategory.setTextColor(Color.BLACK);

            cursor = db.getWalletData(i);
//            db.logCursor(cursor);

            if (cursor.moveToFirst()) {
                ivImg.setImageResource(cursor.getInt(cursor.getColumnIndex(DB.WALLET_COLUMN_IMAGE)));
                tvCategory.setText(cursor.getString(cursor.getColumnIndex(DB.WALLET_COLUMN_NAME)));
            }

            LinearLayout llWallet = (LinearLayout) balanceListItem.findViewById(R.id.llWallet);
            for (int j = 0; j < db.getNumberOfRecords(DB.CURRENCY_TABLE); j++) {
//                Log.d(LOG_TAG, " - currencyData[" + j + "]: " + db.currencyData[j]);

                cursor = db.getBalanceData(i, j);
//                db.logCursor(cursor);

                if (cursor.moveToFirst()) {
                    int sum = cursor.getInt(cursor.getColumnIndex(DB.RECORD_COLUMN_SUM));
                    String currency = cursor.getString(cursor.getColumnIndex(DB.CURRENCY_COLUMN_TITLE));

                    if (sum != 0) {
                        View balanceWalletListItem = ltInflater.inflate(R.layout.item_balance_wallet, llWallet, false);

                        TextView tvSum = (TextView) balanceWalletListItem.findViewById(R.id.tvSum);
                        tvSum.setText(String.valueOf(sum));

                        TextView tvCurrency = (TextView) balanceWalletListItem.findViewById(R.id.tvCurrency);
                        tvCurrency.setText(currency);

                        // Форматируем вывод, чтобы было красиво
                        DecimalFormatSymbols sumFormatSymbols = new DecimalFormatSymbols();
                        sumFormatSymbols.setGroupingSeparator(' ');
                        DecimalFormat sumFormat = new DecimalFormat("#,###", sumFormatSymbols); // отделяем тысячные разряды
                        tvSum.setText(sumFormat.format(sum));
                        if (sum < 0) {
                            tvSum.setTextColor(Color.RED);
                            tvCurrency.setTextColor(Color.RED);
                        }
                        else if (sum > 0) {
                            tvSum.setTextColor(Color.rgb(0, 127, 0));
                            tvCurrency.setTextColor(Color.rgb(0, 127, 0));
                        }

                        llWallet.addView(balanceWalletListItem);
                    }
                }
            }

            llBalanceList.addView(balanceListItem);
        }
    }
}
