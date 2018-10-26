package ru.myandroid.drebedengi_my;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import static java.lang.Math.abs;
import static java.lang.Math.round;
import static ru.myandroid.drebedengi_my.DB.dbDateFormat;
import static ru.myandroid.drebedengi_my.DB.dbTimeFormat;
import static ru.myandroid.drebedengi_my.DB.dbBudgetDateFormat;


public class MainActivity extends AppCompatActivity {

    final String LOG_TAG = "myLogs";

    DB db;
    Cursor cursor;

    Calendar operationDate = Calendar.getInstance();
    Calendar planDate = Calendar.getInstance();

    SimpleDateFormat btnDateFormat = new SimpleDateFormat("dd MMM, EEE", Locale.US);
    SimpleDateFormat btnTimeFormat = new SimpleDateFormat("HH:mm", Locale.US);
    SimpleDateFormat planDateFormat = new SimpleDateFormat("yyyy MMM", Locale.US);

    // Формат вывода сумм - отделяем тысячные разряды
    DecimalFormatSymbols sumFormatSymbols;
    DecimalFormat sumFormat;

    int[] tabName = {R.string.operations_tab, R.string.history_tab, R.string.balance_tab, R.string.plan_tab, R.string.edit_categories_tab};
    int[] tabID = {R.id.operationsTab, R.id.historyTab, R.id.balanceTab, R.id.planTab, R.id.editCategoriesTab};
    TabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Открываем подключение к БД
        db = new DB(this);
        db.open();

        // Отделяем тысячные разряды пробелом
        sumFormatSymbols = new DecimalFormatSymbols();
        sumFormatSymbols.setGroupingSeparator(' ');
        sumFormat = new DecimalFormat("#,###", sumFormatSymbols);

        // Проверка на существование данного месяца в таблице планирования бюджета в БД
        db.addColumnToBudgetTable(dbBudgetDateFormat.format(operationDate.getTime()));

        // Инициализация вкладок
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();
        for (int i = 0; i < tabID.length; i++) {
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

                // Обновляем дату, если последняя операция была сегодня
                actualizeDate();
            }
        });

        getSupportActionBar().hide();

        initOperationsTabContent();
        initHistoryTabContent();
        initBalanceTabContent();
        initEditCategoriesTabContent();
        initPlanTabContent();
    }


    private void setupTab(String title, int id) {
        TabHost.TabSpec spec = tabHost.newTabSpec(title);
        spec.setContent(id);
        spec.setIndicator(title);
        tabHost.addTab(spec);
    }


    private void updateSpinners() {
        spinCurrencyBind.clear();
        spinCurrencyDestBind.clear();
        spinWalletBind.clear();
        spinWalletDestBind.clear();
        spinCategoryBind.clear();
        spinSourceBind.clear();

        spinEditCategoryBind.clear();
        spinEditSourceBind.clear();
        spinEditWalletBind.clear();
//        spinCurrencyRemainBind.clear();

        loadDataForSpinner(DB.CURRENCY_TABLE, spinCurrency, spinCurrencyBind, R.string.currency, spin_currency);
        loadDataForSpinner(DB.CURRENCY_TABLE, spinCurrencyDest, spinCurrencyDestBind, R.string.currency, spin_currency_dest);
        loadDataForSpinner(DB.WALLET_TABLE, spinWallet, spinWalletBind, R.string.wallet, spin_wallet);
        loadDataForSpinner(DB.WALLET_TABLE, spinWalletDest, spinWalletDestBind, R.string.wallet, spin_wallet_dest);
        loadDataForSpinner(DB.CATEGORY_TABLE, spinCategory, spinCategoryBind, R.string.category, spin_spending);
        loadDataForSpinner(DB.CATEGORY_TABLE, spinSource, spinSourceBind, R.string.source, spin_source);

        loadDataForSpinner(DB.CATEGORY_TABLE, spinEditCategory, spinEditCategoryBind, R.string.category, spin_edit_category);
        loadDataForSpinner(DB.CATEGORY_TABLE, spinEditSource, spinEditSourceBind, R.string.source, spin_edit_source);
        loadDataForSpinner(DB.WALLET_TABLE, spinEditWallet, spinEditWalletBind, R.string.wallet, spin_edit_wallet);
//        loadDataForSpinner(DB.CURRENCY_TABLE, spinCurrencyRemain, spinCurrencyRemainBind, R.string.currency, spin_currency_remain);
    }


    private void updateTime() {
        Date localDate = operationDate.getTime();
        localDate.setHours(Integer.parseInt(new SimpleDateFormat("HH", Locale.US).format(Calendar.getInstance().getTime())));
        localDate.setMinutes(Integer.parseInt(new SimpleDateFormat("mm", Locale.US).format(Calendar.getInstance().getTime())));
        operationDate.setTime(localDate);
        btnTime.setText(btnTimeFormat.format(localDate)); // Устанавливаем текущее время при открытии окна
    }


    @Override
    protected void onStart() {
        super.onStart();
        updateTime();
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
    final int DIALOG_EDIT_PLAN = 4;

    public static final int spin_currency = 100,
            spin_currency_dest = 101,
            spin_wallet = 102,
            spin_wallet_dest = 103,
            spin_spending = 104,
            spin_source = 105;

    int currentRadioButton = 0;
    int[] radioButtons = {R.id.rbSpend, R.id.rbGain, R.id.rbMove, R.id.rbChange};

    // виджеты 1-го блока
    LinearLayout operationsTab;
    TableRow etSumRow, etSumDestRow;
    EditText etSum, etSumDest, etComment;
    Button btnDateLeft, btnDateRight, btnDate, btnTime, btnConfirm, btnEdit, btnCancel;
    Spinner spinCurrency, spinCurrencyDest, spinWallet, spinWalletDest, spinCategory, spinSource;
    RadioGroup rgOperationChoice;

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

        loadDataForSpinner(DB.CURRENCY_TABLE, spinCurrency, spinCurrencyBind, R.string.currency, spin_currency);
        loadDataForSpinner(DB.CURRENCY_TABLE, spinCurrencyDest, spinCurrencyDestBind, R.string.currency, spin_currency_dest);
        loadDataForSpinner(DB.WALLET_TABLE, spinWallet, spinWalletBind, R.string.wallet, spin_wallet);
        loadDataForSpinner(DB.WALLET_TABLE, spinWalletDest, spinWalletDestBind, R.string.wallet, spin_wallet_dest);
        loadDataForSpinner(DB.CATEGORY_TABLE, spinCategory, spinCategoryBind, R.string.category, spin_spending);
        loadDataForSpinner(DB.CATEGORY_TABLE, spinSource, spinSourceBind, R.string.source, spin_source);

        etComment = (EditText) findViewById(R.id.etComment);

        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        btnEdit = (Button) findViewById(R.id.btnEdit);
        btnEdit.setVisibility(View.GONE);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setVisibility(View.GONE);

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


    void actualizeDate() {
        if (dbDateFormat.format(operationDate.getTime()).equals(dbDateFormat.format(Calendar.getInstance().getTime()))) {
            operationDate.setTime(Calendar.getInstance().getTime());
            btnTime.setText(btnTimeFormat.format(operationDate.getTime()));
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

            case R.id.tvDelete:
                showDialog(DIALOG_DELETE);
                break;

            case R.id.tvEdit:
                tabHost.setCurrentTabByTag(getString(R.string.operations_tab));
                loadOperationDataToOperationTab();
                break;

            case R.id.btnDate:
                showDialog(DIALOG_DATE);
                break;

            case R.id.btnTime:
                showDialog(DIALOG_TIME);
                break;

            case R.id.btnTag:
                showDialog(DIALOG_TAGS);
                break;

            case R.id.btnConfirm:
                onConfirmOperation(db.CONFIRM_SAVE);
                break;

            case R.id.btnEdit:
                onConfirmOperation(db.CONFIRM_EDIT);
                break;

            case R.id.btnCancel:
                onCancelEdit();
                break;

            case R.id.rbSpend:
                updateTime();
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

            default:
                break;
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
        final AlertDialog.Builder adb = new AlertDialog.Builder(this);
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

            case DIALOG_EDIT_PLAN:
                adb.setMessage(R.string.edit_plan_message);

                final EditText textInput = new EditText(this);
                textInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                adb.setView(textInput);
                adb.setPositiveButton(R.string.confirmButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
//                        Log.d(LOG_TAG, "Редактируем лимит для категории " + recordPlanTableID + ": " + textInput.getText().toString());
                        if (!textInput.getText().toString().equals("")) {
                            String currMonth = dbBudgetDateFormat.format(planDate.getTime());
                            db.updateLimitForCategory(currMonth, recordPlanTableID, Integer.parseInt(textInput.getText().toString()));
                            // А ещё обновим план:
                            loadDataForControlList();
                            loadDataForPlanList();
                            textInput.setText("");
                            Toast.makeText(MainActivity.this, R.string.edit_limit_message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                adb.setNeutralButton(R.string.cancelButton, null);

                Dialog dialog = adb.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                return dialog;

            default:
                return adb.create();
        }
    }


    long m_spending_category_id = -1;
    long m_gain_category_id = -1;
    long m_currency_id = -1;
    long m_wallet_id = -1;
    long m_currency_id_dest = -1;
    long m_wallet_id_dest = -1;


    public void onConfirmOperation(int mode) {
        if (!etSum.getText().toString().equals(getResources().getString(R.string.sum_hint))
                && !etSum.getText().toString().equals("")
                && Double.parseDouble(etSum.getText().toString()) > 0) {
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
                    operation_type = DB.SPENDING;
                    confirmText = "Трата сохранена";
                    category_id = m_spending_category_id;
                    sum = -Double.parseDouble(etSum.getText().toString());
                    break;

                case R.id.rbGain:
                    operation_type = DB.GAIN;
                    confirmText = "Доход сохранён";
                    category_id = m_gain_category_id;
                    sum = Double.parseDouble(etSum.getText().toString());
                    break;

                case R.id.rbMove:
                    operation_type = DB.MOVE;
                    confirmText = "Перемещение сохранено";
                    sum = Double.parseDouble(etSum.getText().toString());
                    break;

                case R.id.rbChange:
                    operation_type = DB.CHANGE;
                    confirmText = "Обмен валют сохранён";
                    sum = Double.parseDouble(etSum.getText().toString());
                    sumDest = Double.parseDouble(etSumDest.getText().toString());
                    break;

                default:
                    break;
            }

            // Очищаем поля
            etSum.setText("");
            etSumDest.setText("");
            etComment.setText("");

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
                if (operation_type == DB.SPENDING || operation_type == DB.GAIN) {
                    db.addTransaction(mode, history_item_selected_id, operation_type, category_id,
                            sum, -1, currentDate, currentTime, comment,
                            m_currency_id, -1, m_wallet_id, -1);
//                    Log.d(LOG_TAG, "Добавление транзакции - обновляем баланс для " + m_wallet_id + " " + m_currency_id);
                    updateBalanceDataArray((int) m_wallet_id, (int) m_currency_id); // Здесь надо обновить только одно поле в массиве балансов
                }
                else if (operation_type == DB.MOVE) {
                    db.addTransaction(mode, history_item_selected_id, DB.MOVE, category_id,
                            sum, -1, currentDate, currentTime, comment,
                            -1, m_currency_id, m_wallet_id, m_wallet_id_dest);

                    // Для корректного подсчёта баланса дублируем операцию
                    db.addTransaction(mode, history_item_selected_id + 1, DB.SPENDING, category_id,
                            -sum, -1, currentDate, currentTime, comment,
                            m_currency_id, -1, m_wallet_id, -1);
                    db.addTransaction(mode, history_item_selected_id + 2, DB.GAIN, category_id,
                            sum, -1, currentDate, currentTime, comment,
                            m_currency_id, -1, m_wallet_id_dest, -1);

//                    Log.d(LOG_TAG, "Добавление транзакции - обновляем баланс для " + m_wallet_id + " " + m_currency_id);
//                    Log.d(LOG_TAG, "Добавление транзакции - обновляем баланс для " + m_wallet_id_dest + " " + m_currency_id);
                    updateBalanceDataArray((int) m_wallet_id, (int) m_currency_id);
                    updateBalanceDataArray((int) m_wallet_id_dest, (int) m_currency_id);
                }
                else if (operation_type == DB.CHANGE) {
                    db.addTransaction(mode, history_item_selected_id, DB.CHANGE, category_id,
                            -sum, sumDest, currentDate, currentTime, comment,
                            m_currency_id_dest, m_currency_id, m_wallet_id, m_wallet_id_dest);

                    // Для корректного подсчёта баланса дублируем операцию
                    db.addTransaction(mode, history_item_selected_id + 1, DB.SPENDING, category_id,
                            -sum, -1, currentDate, currentTime, comment,
                            m_currency_id, -1, m_wallet_id, -1);
                    db.addTransaction(mode, history_item_selected_id + 2, DB.GAIN, category_id,
                            sumDest, -1, currentDate, currentTime, comment,
                            m_currency_id_dest, -1, m_wallet_id_dest, -1);

//                    Log.d(LOG_TAG, "Добавление транзакции - обновляем баланс для " + m_wallet_id + " " + m_currency_id);
//                    Log.d(LOG_TAG, "Добавление транзакции - обновляем баланс для " + m_wallet_id_dest + " " + m_currency_id_dest);
                    updateBalanceDataArray((int) m_wallet_id, (int) m_currency_id);
                    updateBalanceDataArray((int) m_wallet_id_dest, (int) m_currency_id_dest);
                }
            }
            catch (Exception ex) {
                Log.d(LOG_TAG, ex.getClass() + " db write error: " + ex.getMessage());
            }
            finally {
                Toast.makeText(this, confirmText, Toast.LENGTH_SHORT).show();

                // Если операция была за сегодняшний день, ставим актуальное время для новой
                actualizeDate();

                // А ещё обновим историю операций, баланс и бюджет
                loadDataForOperationHistory();
                loadDataForBalance();
                refreshPlanTab();

                if (mode == db.CONFIRM_EDIT) {
                    tabHost.setCurrentTabByTag(getString(R.string.history_tab));

                    btnConfirm.setVisibility(View.VISIBLE);
                    btnEdit.setVisibility(View.GONE);
                    btnCancel.setVisibility(View.GONE);
                }
            }
        }
        else {
            Toast.makeText(this, R.string.error_confirm_zero, Toast.LENGTH_SHORT).show();
        }
    }


    DialogInterface.OnClickListener deleteButtonClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_POSITIVE: // Положительная кнопка
                    int[] ids = db.deleteTransaction(history_item_selected_id);
                    Toast.makeText(MainActivity.this, R.string.record_deleted, Toast.LENGTH_SHORT).show();

                    // А ещё обновим историю операций, баланс и бюджет
                    loadDataForOperationHistory();
//                    Log.d(LOG_TAG, "Удаление транзакции - обновляем баланс для " + ids[0] + " " + ids[2]);
//                    Log.d(LOG_TAG, "Удаление транзакции - обновляем баланс для " + ids[1] + " " + ids[3]);
                    updateBalanceDataArray(ids[0], ids[2]); // Здесь надо обновить только одно поле в массиве балансов
                    if (ids[3] != -1) updateBalanceDataArray(ids[1], ids[3]); // И ещё одно, если было перемещение или обмен
                    loadDataForBalance();
                    refreshPlanTab();
                    break;

                case Dialog.BUTTON_NEUTRAL: // Нейтральная кнопка
                    break;
            }
        }
    };


    // Загрузка данных в спиннеры из БД
    public void loadDataForSpinner(final String TABLE_NAME, Spinner currentSpinner, Vector<Integer> spinnerBind, int string_id, final int type) {
//        Log.d(LOG_TAG, "load data for spinner: " + type);
        cursor = db.getAllData(TABLE_NAME, string_id);
//        db.logCursor(cursor);

        // формируем столбцы сопоставления
        String[] from = { DB.TABLE_COLUMN_IMAGE, DB.TABLE_COLUMN_NAME };
        int[] to = { R.id.ivImg, R.id.tvText };

        // создааем адаптер и настраиваем список
        currentSpinner.setAdapter(new SimpleCursorAdapter(this, R.layout.item_spinner, cursor, from, to));
        currentSpinner.setPrompt(getResources().getString(string_id));

        // связка индекса спиннера с id записи из базы
        if ((cursor != null) && (cursor.moveToFirst())) {
            int column = cursor.getColumnIndex(DB.CATEGORY_COLUMN_ID);
//            Log.d(LOG_TAG, "spinner: " + getResources().getString(string_id));
            do {
//                Log.d(LOG_TAG, "spin id: " + cursor.getInt(column));
                spinnerBind.add(cursor.getInt(column));
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

                    case spin_edit_category: m_edit_category_id = id;     break;
                    case spin_edit_source:   m_edit_source_id = id;       break;
                    case spin_edit_wallet:   m_edit_wallet_id = id;       break;
                    case spin_currency_remain: m_currency_remain_id = id; break;

                    default: break;
                }

                if ((type == spin_edit_wallet) || (type == spin_currency_remain)) { // Заполняем начальные суммы
                    setWalletStartSum();
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

        if ((cursor != null) && (cursor.moveToFirst())) {
            int operationType = cursor.getInt(cursor.getColumnIndex(DB.RECORD_COLUMN_OPERATION_TYPE));
            int currencyID = cursor.getInt(cursor.getColumnIndex(DB.RECORD_COLUMN_CURRENCY_ID));
            int currencyIDDest = cursor.getInt(cursor.getColumnIndex(DB.RECORD_COLUMN_CURRENCY_ID_DEST));
            int walletID = cursor.getInt(cursor.getColumnIndex(DB.RECORD_COLUMN_WALLET_ID));
            int walletIDDest = cursor.getInt(cursor.getColumnIndex(DB.RECORD_COLUMN_WALLET_ID_DEST));
            int categoryID = cursor.getInt(cursor.getColumnIndex(DB.RECORD_COLUMN_CATEGORY_ID));
            int sum = cursor.getInt(cursor.getColumnIndex(DB.RECORD_COLUMN_SUM));
            int sumMove = cursor.getInt(cursor.getColumnIndex(DB.RECORD_COLUMN_SUM_MOVE));
            String operationDateText = cursor.getString(cursor.getColumnIndex(DB.RECORD_COLUMN_DATE));
            String operationTimeText = cursor.getString(cursor.getColumnIndex(DB.RECORD_COLUMN_TIME));
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

            if ((operationType == DB.SPENDING) || (operationType == DB.GAIN)) {
                etSum.setText(String.valueOf(abs(sum)));
                spinCurrency.setSelection(getSpinnerIndexByID(currencyID, spinCurrencyBind));
                spinCurrencyDest.setSelection(getSpinnerIndexByID(currencyIDDest, spinCurrencyDestBind));
            }
            else if ((operationType == DB.MOVE) || (operationType == DB.CHANGE)) {
                etSum.setText(String.valueOf(abs(sumMove)));
                etSumDest.setText(String.valueOf(abs(sum)));
                spinCurrency.setSelection(getSpinnerIndexByID(currencyIDDest, spinCurrencyBind));
                spinCurrencyDest.setSelection(getSpinnerIndexByID(currencyID, spinCurrencyDestBind));
            }

            spinWallet.setSelection(getSpinnerIndexByID(walletID, spinWalletBind));
            spinWalletDest.setSelection(getSpinnerIndexByID(walletIDDest, spinWalletDestBind));
            if (categoryID < db.div_category_gain) {
                spinCategory.setSelection(getSpinnerIndexByID(categoryID, spinCategoryBind));
            }
            else spinSource.setSelection(getSpinnerIndexByID(categoryID, spinSourceBind));

            etComment.setText(comment);
            try {
                operationDate.setTime(new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.US).parse(operationDateText + "_" + operationTimeText));
                btnDate.setText(checkDateForToday(operationDate.getTime()));
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
                db.setSelectedParameter(id, DB.AUTO_SELECT);
                if (history_item_selected_id != id) { // Убираем меню со старого элемента
                    db.setSelectedParameter(history_item_selected_id, DB.NOT_SELECTED);
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

        String[] from = { DB.TABLE_COLUMN_IMAGE_FROM, DB.TABLE_COLUMN_IMAGE_TO, DB.RECORD_COLUMN_OPERATION_TYPE,
                DB.CATEGORY_COLUMN_NAME, DB.TABLE_COLUMN_NAME_FROM, DB.TABLE_COLUMN_NAME_TO, DB.RECORD_COLUMN_COMMENT,
                DB.RECORD_COLUMN_SUM, DB.RECORD_COLUMN_SUM_MOVE, DB.TABLE_COLUMN_TITLE_FROM, DB.TABLE_COLUMN_TITLE_TO,
                DB.RECORD_COLUMN_DATE, DB.RECORD_COLUMN_TIME, DB.RECORD_COLUMN_SELECTED, DB.RECORD_COLUMN_SELECTED };

        int[] to = { R.id.ivImgFrom, R.id.ivImgTo, R.id.ivImgMove,
                R.id.tvCategory, R.id.tvWalletFrom, R.id.tvWalletTo, R.id.tvComment,
                R.id.tvSum, R.id.tvSumMove, R.id.tvCurrency, R.id.tvCurrencyMove,
                R.id.tvDate, R.id.tvTime, R.id.tvDelete, R.id.tvEdit };

        lvHistory.setAdapter(new HistoryCursorAdapter(this, R.layout.item_history, cursor, from, to));
    }


// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 3. Вкладка баланса
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    LinearLayout llBalanceList;
    LayoutInflater ltInflater;

    BalanceStruct balanceStruct;

    private void initBalanceTabContent() {
        Log.d(LOG_TAG, "initBalanceTabContent");
        llBalanceList = (LinearLayout) findViewById(R.id.llBalanceList);
        updateBalanceDataArray();
        loadDataForBalance();
    }


    void loadDataForBalance() {
//        Log.d(LOG_TAG, "loadDataForBalance()");
        llBalanceList.removeAllViews();
        ltInflater = getLayoutInflater();

        for (int i = 0; i < balanceStruct.walletsNumber; i++) {
//            Log.d(LOG_TAG, "walletData[" + i + "]: " + db.walletData[i]);

            View balanceListItem = ltInflater.inflate(R.layout.item_balance, llBalanceList, false);
            balanceListItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Здесь обновляем только графику, массив балансов не трогаем
                    TextView tvID = (TextView) view.findViewById(R.id.tvID);
                    int id = Integer.valueOf(tvID.getText().toString());
//                    Log.d(LOG_TAG, "### on item click! id " + id);
                    setWalletHidden(id);
                    loadDataForBalance();
                }
            });

            TextView tvID = (TextView) balanceListItem.findViewById(R.id.tvID);
            ImageView ivImg = (ImageView) balanceListItem.findViewById(R.id.ivImg);
            TextView tvCategory = (TextView) balanceListItem.findViewById(R.id.tvCategory);
            tvCategory.setTextColor(Color.BLACK);

            cursor = db.getWalletData(i);
//            db.logCursor(cursor);

            if ((cursor != null) && (cursor.moveToFirst())) {
                tvID.setText(cursor.getString(cursor.getColumnIndex(DB.WALLET_COLUMN_ID)));
                ivImg.setImageResource(cursor.getInt(cursor.getColumnIndex(DB.WALLET_COLUMN_IMAGE)));
                tvCategory.setText(cursor.getString(cursor.getColumnIndex(DB.WALLET_COLUMN_NAME)));
            }

            boolean isWalletNotEmpty = false;
            LinearLayout llWallet = (LinearLayout) balanceListItem.findViewById(R.id.llWallet);
            for (int j = 0; j < balanceStruct.currenciesNumber; j++) {
//                Log.d(LOG_TAG, " - currencyData[" + j + "]: "); // + db.currencyData[j]);

                if (balanceStruct.walletHiddenData[i] == DB.SELECTED) { // Если элемент скрытый - не отображаем баланс
                    isWalletNotEmpty = true;
                    break;
                }

                int sum = balanceStruct.balanceData[i][j];
                String currency = balanceStruct.currencyData[j];

                if (sum != 0) {
                    View balanceWalletListItem = ltInflater.inflate(R.layout.item_balance_wallet, llWallet, false);

                    TextView tvSum = (TextView) balanceWalletListItem.findViewById(R.id.tvSum);
                    TextView tvCurrency = (TextView) balanceWalletListItem.findViewById(R.id.tvCurrency);

                    // Форматируем вывод, чтобы было красиво
                    formatBalanceSum(sum, tvSum, currency, tvCurrency);

                    isWalletNotEmpty = true;
                    llWallet.addView(balanceWalletListItem);
                }
            }

            if (isWalletNotEmpty) llBalanceList.addView(balanceListItem);
        }

        // И ещё добавляем view для итоговой суммы
        loadDataForSummaryBalance();
    }


    void loadDataForSummaryBalance() {
//        Log.d(LOG_TAG, "loadDataForSummaryBalance()");
        View balanceListItem = ltInflater.inflate(R.layout.item_balance, llBalanceList, false);

        TextView tvCategory = (TextView) balanceListItem.findViewById(R.id.tvCategory);
        tvCategory.setText(getResources().getString(R.string.summary));
        tvCategory.setTextColor(Color.BLACK);
        tvCategory.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size));
        tvCategory.setTypeface(null, Typeface.BOLD);

        for (int j = 0; j < balanceStruct.currenciesNumber; j++) {
            String currency = balanceStruct.currencyData[j];
            int sum = 0;
            for (int i = 0; i < balanceStruct.walletsNumber; i++) {
                if (balanceStruct.walletHiddenData[i] != DB.SELECTED) {
                    sum += balanceStruct.balanceData[i][j];
                }
            }

            // Выводим суммарный баланс и валюту
            LinearLayout llWallet = (LinearLayout) balanceListItem.findViewById(R.id.llWallet);
            View balanceWalletListItem = ltInflater.inflate(R.layout.item_balance_wallet, llWallet, false);

            TextView tvSum = (TextView) balanceWalletListItem.findViewById(R.id.tvSum);
            tvSum.setTypeface(null, Typeface.BOLD);

            TextView tvCurrency = (TextView) balanceWalletListItem.findViewById(R.id.tvCurrency);
            tvCurrency.setTypeface(null, Typeface.BOLD);

            // Форматируем вывод, чтобы было красиво
            formatBalanceSum(sum, tvSum, currency, tvCurrency);

            llWallet.addView(balanceWalletListItem);
        }

        llBalanceList.addView(balanceListItem);
    }


    void formatBalanceSum(int sum, TextView tvSum, String currency, TextView tvCurrency) {
        tvSum.setText(sumFormat.format(sum));
        tvSum.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_balance_sum_size));

        tvCurrency.setText(currency);
        tvCurrency.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_balance_size));

        if (sum < 0) {
            tvSum.setTextColor(Color.RED);
            tvCurrency.setTextColor(Color.RED);
        }
        else if (sum > 0) {
            tvSum.setTextColor(getResources().getColor(R.color.positive));
            tvCurrency.setTextColor(getResources().getColor(R.color.positive));
        }
    }


    void updateBalanceDataArray() {
        Log.d(LOG_TAG, "fillBalanceDataArray()");
//        balanceStruct.balanceData = db.getBalanceDataArray();
        balanceStruct = db.getBalanceData();

//        int walletsNumber = db.getNumberOfRecords(DB.WALLET_TABLE);
//        int currenciesNumber = db.getNumberOfRecords(DB.CURRENCY_TABLE);
//        for (int i = 0; i < balanceStruct.walletsNumber; i++) {
//            Log.d(LOG_TAG, "balanceData[" + i + "] hidden: " + balanceStruct.walletHiddenData[i]);
//            for (int j = 0; j < balanceStruct.currenciesNumber; j++) {
//                Log.d(LOG_TAG, "balanceData[" + i + "][" + j + "]: " + balanceStruct.balanceData[i][j]
//                        + " " + balanceStruct.currencyData[j]);
//            }
//        }
    }


    void updateBalanceDataArray(int wallet) {
        for (int j = 0; j < balanceStruct.currenciesNumber; ++j) {
            updateBalanceDataArray(wallet, j);
        }
    }


    void updateBalanceDataArray(int wallet, int currency) {
        Cursor cursor = db.getBalanceData(wallet, currency);
//        db.logCursor(cursor);

        if ((cursor != null) && (cursor.moveToFirst())) {
            int sum = cursor.getInt(cursor.getColumnIndex(DB.RECORD_COLUMN_SUM));
            Log.d(LOG_TAG, "previous balance: " + balanceStruct.balanceData[wallet][currency]);
            balanceStruct.balanceData[wallet][currency] = sum;
            Log.d(LOG_TAG, "new balance: " + balanceStruct.balanceData[wallet][currency]);
        }
    }


    void setWalletHidden(int id) {
        balanceStruct.walletHiddenData[id] =
                (balanceStruct.walletHiddenData[id] == DB.NOT_SELECTED) ? DB.SELECTED : DB.NOT_SELECTED;
        db.setWalletHidden(id);
    }


// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 4. Вкладка редактирования категорий
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    public static final int spin_edit_category = 400,
            spin_edit_source = 401,
            spin_edit_wallet = 402,
            spin_currency_remain = 403;

    long m_edit_category_id = -1;
    long m_edit_source_id = -1;
    long m_edit_wallet_id = -1;
    long m_currency_remain_id = -1;

    TextView textEditCategory;
    RadioGroup rgEditChoice;
    Spinner spinEditCategory, spinEditSource, spinEditWallet, spinCurrencyRemain;
    CheckBox chkEditCategory;
    EditText etCategory, etEditRemain;
    LinearLayout llEditRemain;

    Vector<Integer> spinEditCategoryBind = new Vector<>();
    Vector<Integer> spinEditSourceBind = new Vector<>();
    Vector<Integer> spinEditWalletBind = new Vector<>();
    Vector<Integer> spinCurrencyRemainBind = new Vector<>();

    private void initEditCategoriesTabContent() {
        Log.d(LOG_TAG, "initEditCategoriesTabContent");

        textEditCategory = (TextView) findViewById(R.id.textEditCategory);

        rgEditChoice = (RadioGroup) findViewById(R.id.rgEditChoice);

        spinEditCategory = (Spinner) findViewById(R.id.spinEditCategory);
        loadDataForSpinner(DB.CATEGORY_TABLE, spinEditCategory, spinEditCategoryBind, R.string.category, spin_edit_category);

        spinEditSource = (Spinner) findViewById(R.id.spinEditSource);
        loadDataForSpinner(DB.CATEGORY_TABLE, spinEditSource, spinEditSourceBind, R.string.source, spin_edit_source);

        spinEditWallet = (Spinner) findViewById(R.id.spinEditWallet);
        loadDataForSpinner(DB.WALLET_TABLE, spinEditWallet, spinEditWalletBind, R.string.wallet, spin_edit_wallet);

        spinCurrencyRemain = (Spinner) findViewById(R.id.spinCurrencyRemain);
        loadDataForSpinner(DB.CURRENCY_TABLE, spinCurrencyRemain, spinCurrencyRemainBind, R.string.currency, spin_currency_remain);

        chkEditCategory = (CheckBox) findViewById(R.id.chkEditCategory);

        etCategory = (EditText) findViewById(R.id.etCategory);

        llEditRemain = (LinearLayout) findViewById(R.id.llEditRemain);

        etEditRemain = (EditText) findViewById(R.id.etEditRemain);

        // Выбираем конкретную категорию
        rgEditChoice.check(R.id.rbEditSpend);
        onEditLayoutButtonClick(findViewById(R.id.rbEditSpend));
    }


    private void resetEditCategoriesTab() {
        textEditCategory.setText(getResources().getString(R.string.edit_cat_edit));
        spinEditCategory.setEnabled(true);
        spinEditSource.setEnabled(true);
        spinEditWallet.setEnabled(true);
        chkEditCategory.setChecked(false);
        etEditRemain.setText("");
        etCategory.setText("");
    }


    public void onEditLayoutButtonClick(View v) {
        switch (v.getId()) {
            case R.id.rbEditSpend:
                spinEditCategory.setVisibility(View.VISIBLE);
                spinEditSource.setVisibility(View.GONE);
                spinEditWallet.setVisibility(View.GONE);
                llEditRemain.setVisibility(View.GONE);
                chkEditCategory.setChecked(false);
                break;

            case R.id.rbEditGain:
                spinEditCategory.setVisibility(View.GONE);
                spinEditSource.setVisibility(View.VISIBLE);
                spinEditWallet.setVisibility(View.GONE);
                llEditRemain.setVisibility(View.GONE);
                chkEditCategory.setChecked(false);
                break;

            case R.id.rbEditWallet:
                spinEditCategory.setVisibility(View.GONE);
                spinEditSource.setVisibility(View.GONE);
                spinEditWallet.setVisibility(View.VISIBLE);
                llEditRemain.setVisibility(View.VISIBLE);
                chkEditCategory.setChecked(false);
                break;

            case R.id.btnConfirm:
//                Log.d(LOG_TAG, "on save click");
                // Выбираем то, что редактируем
                int mode = -1;
                long edit_id = -1;
                double remain = -1.0;
                switch (rgEditChoice.getCheckedRadioButtonId()) {
                    case R.id.rbEditSpend:
                        mode = db.EDIT_SPEND;
                        edit_id = m_edit_category_id;
                        break;

                    case R.id.rbEditGain:
                        mode = db.EDIT_SOURCE;
                        edit_id = m_edit_source_id;
                        break;

                    case R.id.rbEditWallet:
                        mode = db.EDIT_WALLET;
                        edit_id = m_edit_wallet_id;
                        String remainTxt = etEditRemain.getText().toString();
                        if (remainTxt.equals("")) remain = Double.parseDouble(etEditRemain.getHint().toString());
                        else remain = Double.parseDouble(remainTxt);
                        break;

                    default: break;
                }

                String newCategoryName = etCategory.getText().toString();
                if ((!newCategoryName.equals("")) || (mode == db.EDIT_WALLET)) {
                    if (chkEditCategory.isChecked()) { // Добавляем новую категорию
                        if (!newCategoryName.equals("")) {
//                            Log.d(LOG_TAG, "check: create new category");
                            db.createNewCategory(mode, newCategoryName, spinCurrencyRemainBind);
                            Toast.makeText(MainActivity.this, R.string.message_edit_cat_new, Toast.LENGTH_SHORT).show();
                        }
                    }
                    else { // Редактируем данную категорию
//                    Log.d(LOG_TAG, "not check: edit this category");
                        db.updateCategory(mode, edit_id, newCategoryName, remain, m_currency_remain_id);
                        Toast.makeText(MainActivity.this, R.string.message_edit_cat_edit, Toast.LENGTH_SHORT).show();
                    }
                    updateSpinners();
                    resetEditCategoriesTab();
                    updateBalanceDataArray((int) edit_id); // Здесь надо обновить только одно МЕСТО ХРАНЕНИЯ (а не поле) в массиве балансов
                    loadDataForBalance();
                }
                else Toast.makeText(MainActivity.this, R.string.message_edit_cat_edit_error, Toast.LENGTH_SHORT).show();

                break;

            case R.id.chkEditCategory:
                if (chkEditCategory.isChecked()) {
//                    Log.d(LOG_TAG, "check!");
                    spinEditCategory.setEnabled(false);
                    spinEditSource.setEnabled(false);
                    spinEditWallet.setEnabled(false);
                    textEditCategory.setText(getResources().getString(R.string.edit_cat_new));
                    llEditRemain.setVisibility(View.GONE);
                }
                else {
//                    Log.d(LOG_TAG, "not check!");
                    spinEditCategory.setEnabled(true);
                    spinEditSource.setEnabled(true);
                    spinEditWallet.setEnabled(true);
                    textEditCategory.setText(getResources().getString(R.string.edit_cat_edit));
                    if (rgEditChoice.getCheckedRadioButtonId() == R.id.rbEditWallet) {
                        llEditRemain.setVisibility(View.VISIBLE);
                    }
                }
                break;

            default:
                break;
        }
    }


    void setWalletStartSum() {
        if ((m_edit_wallet_id == -1) || (m_currency_remain_id == -1)) return;

        int remain = (int) db.getWalletStartSum(m_edit_wallet_id, m_currency_remain_id);
        etEditRemain.setHint(sumFormat.format(remain));
//        Log.d(LOG_TAG, "Set remain " + remain + ": wallet id " + m_edit_wallet_id + ", currency id " + m_currency_remain_id);
    }

// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 5. Вкладка планирования
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    RadioGroup rgPlanChoice;
    TextView textMonth, tvSumPlan, tvSumFact;
    TextView tvLimitPlanLabel, tvLimitPlan, tvLRemainSum, tvLAlreadySpendSum;
    ListView lvPlan, lvRemains;
    LinearLayout layControl, layPlan;
    LinearLayout lRemain, lAlreadySpend, lSpend, layPlanTitle;
    ProgressBar pbLimit;
    TextView tvPbLimit;

    long recordPlanTableID = -1;

    private void initPlanTabContent() {
        Log.d(LOG_TAG, "initPlanTabContent");

        rgPlanChoice = (RadioGroup) findViewById(R.id.rgPlanChoice);
        rgPlanChoice.check(R.id.rbControl);

        // === Контроль расходов    === ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

        layControl = (LinearLayout) findViewById(R.id.layControl);

        tvLimitPlan = (TextView) findViewById(R.id.tvLimitPlan);
        tvLimitPlanLabel = (TextView) findViewById(R.id.tvLimitPlanLabel);
        String limitPlanLabelText = getString(R.string.plan_limit) + " (" + planDateFormat.format(planDate.getTime()) + ")";
        tvLimitPlanLabel.setText(limitPlanLabelText);

        pbLimit = (ProgressBar) findViewById(R.id.pbLimit);
        tvPbLimit = (TextView) findViewById(R.id.tvPbLimit);

        lRemain = (LinearLayout) findViewById(R.id.lRemain);
        TextView tvRemain = (TextView) lRemain.findViewById(R.id.tvName);
        tvRemain.setText(R.string.plan_remain);
        tvLRemainSum = (TextView) lRemain.findViewById(R.id.tvSumRemain);
        lRemain.setBackgroundColor(getResources().getColor(R.color.colorTextViewBack));

        lvRemains = (ListView) findViewById(R.id.lvRemains);

        lAlreadySpend = (LinearLayout) findViewById(R.id.lAlreadySpend);
        TextView tvAlreadySpend = (TextView) lAlreadySpend.findViewById(R.id.tvName);
        tvAlreadySpend.setText(R.string.plan_already_spend);
        tvLAlreadySpendSum = (TextView) lAlreadySpend.findViewById(R.id.tvSumRemain);
        lAlreadySpend.setBackgroundColor(getResources().getColor(R.color.colorTextViewBack));

        loadDataForControlList();

        // === Планирование бюджета     ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

        layPlan = (LinearLayout) findViewById(R.id.layPlan);
        layPlan.setVisibility(View.GONE);

        layPlanTitle = (LinearLayout) findViewById(R.id.layPlanTitle);
        layPlanTitle.setOnTouchListener(new OnSwipeTouchListener(this) {
            public void onSwipeLeft() {
                planDate.add(Calendar.MONTH, 1);
                db.addColumnToBudgetTable(dbBudgetDateFormat.format(planDate.getTime()));
                db.refreshFactSumForAllCategories(dbBudgetDateFormat.format(planDate.getTime()));
                loadDataForPlanList();
            }

            public void onSwipeRight() {
                planDate.add(Calendar.MONTH, -1);
                db.addColumnToBudgetTable(dbBudgetDateFormat.format(planDate.getTime()));
                db.refreshFactSumForAllCategories(dbBudgetDateFormat.format(planDate.getTime()));
                loadDataForPlanList();
            }
        });

        textMonth = (TextView) findViewById(R.id.textMonth);

        lSpend = (LinearLayout) findViewById(R.id.lSpend);
        lSpend.setBackgroundColor(getResources().getColor(R.color.colorSpend));

        TextView tvCat = (TextView) lSpend.findViewById(R.id.tvCategory);
        tvCat.setText(R.string.radio_spend);

        tvSumPlan = (TextView) lSpend.findViewById(R.id.tvSumPlan);

        tvSumFact = (TextView) lSpend.findViewById(R.id.tvSumFact);

        lvPlan = (ListView) findViewById(R.id.lvPlan);
        lvPlan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Log.d(LOG_TAG, "on list plan item click: " + i + " " + l);
                recordPlanTableID = l;
                showDialog(DIALOG_EDIT_PLAN);
            }
        });

        loadDataForPlanList();
    }


    private void refreshPlanTab() {
        db.refreshFactSumForAllCategories(dbBudgetDateFormat.format(operationDate.getTime()));
        loadDataForControlList();
        loadDataForPlanList();
    }


    int updatePlanSum(int columnType, TextView tvSum, Calendar date) {
        cursor = db.getPlanAllSpendSum(columnType, dbBudgetDateFormat.format(date.getTime()));
//        db.logCursor(cursor);
        if ((cursor != null) && (cursor.moveToFirst())) {
            int sum = cursor.getInt(cursor.getColumnIndex(DB.RECORD_COLUMN_SUM));
            tvSum.setText(sumFormat.format(sum));

            if (columnType == DB.REMAIN) {
                if (sum < 0) tvSum.setTextColor(Color.RED);
                else if (sum > 0) tvSum.setTextColor(getResources().getColor(R.color.positive));
            }

            return sum;
        }
        else return 0;
    }


    void loadDataForControlList() {
        double totalLimit   = updatePlanSum(DB.PLAN, tvLimitPlan, Calendar.getInstance());
        double alreadySpend = updatePlanSum(DB.FACT, tvLAlreadySpendSum, Calendar.getInstance());
        updatePlanSum(DB.REMAIN, tvLRemainSum, Calendar.getInstance());

        int percentLimit = (int) round(alreadySpend / totalLimit * 100);
        String percentLimitTxt = String.valueOf(percentLimit) + "%";
        pbLimit.setProgress(percentLimit);
        tvPbLimit.setText(percentLimitTxt);
        if ((percentLimit < 0) || (percentLimit > 100)) {
            pbLimit.getProgressDrawable().setColorFilter(getResources().getColor(R.color.negativeProgress), PorterDuff.Mode.SRC_IN);
        }
        else {
            pbLimit.setProgressDrawable(getResources().getDrawable(R.drawable.progress_drawable));
        }

        String currMonth = dbBudgetDateFormat.format(Calendar.getInstance().getTime());
        cursor = db.getRemainData(currMonth);
//        db.logCursor(cursor);

        String[] from = { DB.CATEGORY_COLUMN_NAME, DB.RECORD_COLUMN_SUM };
        int[] to = { R.id.tvName, R.id.tvSumRemain };

        lvRemains.setAdapter(new PlanCursorAdapter(this, R.layout.item_plan_control, cursor, from, to));
    }


    void loadDataForPlanList() {
//        Log.d(LOG_TAG, "load data for plan list");

        textMonth.setText(planDateFormat.format(planDate.getTime()));

        updatePlanSum(DB.PLAN, tvSumPlan, planDate);
        updatePlanSum(DB.FACT, tvSumFact, planDate);

        String currMonth = dbBudgetDateFormat.format(planDate.getTime());
        cursor = db.getPlanData(currMonth);
//        db.logCursor(cursor);

        // формируем столбцы сопоставления
        String[] from = { DB.CATEGORY_COLUMN_NAME, DB.BUDGET_COLUMN_PLAN + currMonth, DB.BUDGET_COLUMN_FACT + currMonth };
        int[] to = { R.id.tvCategory, R.id.tvSumPlan, R.id.tvSumFact };

        // создааем адаптер и настраиваем список
        lvPlan.setAdapter(new PlanCursorAdapter(this, R.layout.item_plan, cursor, from, to));
    }


    public void onPlanLayoutButtonClick(View v) {
        switch (v.getId()) {
            case R.id.rbControl:
                layControl.setVisibility(View.VISIBLE);
                layPlan.setVisibility(View.GONE);
                break;

            case R.id.rbPlan:
                layControl.setVisibility(View.GONE);
                layPlan.setVisibility(View.VISIBLE);
                break;

            default:
                break;
        }
    }
}
