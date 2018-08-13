package ru.myandroid.drebedengi_my;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static java.lang.Math.abs;

public class DB {

    private final String LOG_TAG = "myLogs";

    final int SPENDING = 1, GAIN = 2, MOVE = 3, CHANGE = 4;
    final int AUTO_SELECT = -1, NOT_SELECTED = 0, SELECTED = 1;
    final int CONFIRM_SAVE = 0, CONFIRM_EDIT = 1;

    final int div_category_gain = 1000000;
    final int div_category_group = 1000;

    static SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    static SimpleDateFormat dbTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
    static SimpleDateFormat dbBudgetDateFormat = new SimpleDateFormat("yyyy_MM", Locale.US);


    // Данные для первоначального заполнения БД
    private String[] currencyData = { "руб", "USD", "EUR" };
    private String[] categoryData = { "[Без категории]",
            "Еда", "Развлечения", "Подарки", "Проезд", "Счета", "Авто", "Учёба, курсы, тренинги", "Дом, семья", "Здоровье", "Гигиена", "Техника", "Одежда", "Спорт", "Путешествия", "Работа", "Разное", "Банковские операции",

            "Обеды, перекусы", "Продукты", "Спортивное питание",
            "Кафешки", "Встречи, сходки", "Кино", "Музеи, выставки", "Аттракционы", "Каток", "Вечеринки", "Концерты", "Аквапарк", "Разные развлечения",
            "Цветы", "Благотворительность",
            "Метро", "Электрички", "Наземный транспорт", "Такси", "Каршеринг",
            "Мобильный", "Банкинг", "Интернет",
            "Бензин", "Запчасти", "Мойка", "Обслуживание авто", "Штрафы",
            "Канцтовары",
            "Родителям", "Хозтовары", "Кварплата", "Дети", "Мебель",
            "Лечение, стоматология", "Аптека, лекарства",
            "Стрижка", "Мыло, гели, шампуни", "Бритьё",
            "Телефон", "Компьютер и комплектующие", "Планшеты, эл. книжки", "Ноутбук и комплектующие", "Принтер", "Разная техника",
            "Верхняя одежда", "Рубашки, футболки", "Джинсы, брюки, шорты", "Нижнее бельё", "Обувь", "Аксессуары", "Кофты", "Парфюм", "Услуги стилиста",
            "Велосипед", "Походный инвентарь", "Лыжный инвентарь", "Спорт. одежда", "Борьба",
            "Велопоездки", "Санатории", "По стране | жильё", "По стране | транспорт", "По стране | питание", "По стране | экскурсии", "По стране | сувениры",
            "Путешествия | жильё", "Путешествия | транспорт", "Путешествия | питание", "Путешествия | экскурсии", "Путешествия | развлечения", "Путешествия | сувениры", "Путешествия | разное",
            "Фотопечать", "Почта",
            "Комиссия за перевод", "Процент по займу" };
    private final int[] categoryGroupData = { 0,
            div_category_group, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000, 11000, 12000, 13000, 14000, 15000, 16000, 17000,

            1001, 1002, 1003,
            2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010,
            3001, 3002,
            4001, 4002, 4003, 4004, 4005,
            5001, 5002, 5003,
            6001, 6002, 6003, 6004, 6005,
            7001,
            8001, 8002, 8003, 8004, 8005,
            9001, 9002,
            10001, 10002, 10003,
            11001, 11002, 11003, 11004, 11005, 11006,
            12001, 12002, 12003, 12004, 12005, 12006, 12007, 12008, 12009,
            13001, 13002, 13003, 13004, 13005,
            14001, 14002, 14003, 14004, 14005, 14006, 14007, 14008, 14009, 14010, 14011, 14012, 14013, 14014,
            16001, 16002,
            17001, 17002 };
    private final int[] categoryParentData = { -1,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,

            1, 1, 1,
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            3, 3,
            4, 4, 4, 4, 4,
            5, 5, 5,
            6, 6, 6, 6, 6,
            7,
            8, 8, 8, 8, 8,
            9, 9,
            10, 10, 10,
            11, 11, 11, 11, 11, 11,
            12, 12, 12, 12, 12, 12, 12, 12, 12,
            13, 13, 13, 13, 13,
            14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14,
            16, 16,
            17, 17 };
    private String[] sourcesData = { "Работа (\"Агат\")", "Фриланс", "ООО \"Атлант+\"", "Альтернатива", "Родители", "Подарки", "Разное" };
    private final int[] sourcesGroupData = { 1000, 2000, 3000, 4000, 5000, 6000, 7000 };
    private String[] walletData = { "Кошелёк", "Ящик домашний", "Карта \"Сбербанк\" (Visa)", "Карта \"Тинькофф\" (Visa)", "Яндекс.Деньги", "\"Детские деньги\"", "Счёт \"Сбербанк\"", "Счёт \"Совкомбанк\"",
            "Карта \"Зенит\" (MasterCard)", "Карта \"Солид\" (MasterCard)", "Карта \"Кукуруза\" (MasterCard)", "Карта \"Альфа\" (MasterCard)",
            "PerfectMoney", "AdvCash", "ePayments", "Blockchain", "MyEtherWallet",
            "Мама", "Денис Тетерин", "Евгений", "Сбербанк", "Биржи", "Tirus", "Polybius", "GS Mining", "DEEX", "FBF" };
    private final int imageWalletData[] = { R.mipmap.wallet42, 0, 0, R.mipmap.tcs42, R.mipmap.yandex42, 0, 0, 0,
            0, 0, 0, 0,
            0, R.mipmap.advcash42, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };


    // База данных
    private static final String DB_NAME = "database";
    private static final int DB_VERSION = 1;

    // Общие названия для полей таблиц
    static final String TABLE_COLUMN_ID = "_id";
    static final String TABLE_COLUMN_IMAGE = "image";
    static final String TABLE_COLUMN_NAME = "name";
    static final String TABLE_COLUMN_PARENT = "parent_id";


    // Таблица валют
    static final String CURRENCY_TABLE = "currency";
    static final String CURRENCY_COLUMN_ID = "_id";
    static final String CURRENCY_COLUMN_IMAGE = "image";
    static final String CURRENCY_COLUMN_NAME = "name";
    static final String CURRENCY_COLUMN_TITLE = "title";

    private static final String CURRENCY_TABLE_CREATE = "create table " + CURRENCY_TABLE + "(" +
            CURRENCY_COLUMN_ID + " integer primary key, " + CURRENCY_COLUMN_IMAGE + " integer, " +
            CURRENCY_COLUMN_NAME + " text, " + CURRENCY_COLUMN_TITLE + " text, " +
            TABLE_COLUMN_PARENT + " integer" + ");";


    // Таблица с местами хранения средств
    static final String WALLET_TABLE = "wallet";
    static final String WALLET_COLUMN_ID = "_id";
    static final String WALLET_COLUMN_IMAGE = "image";
    static final String WALLET_COLUMN_NAME = "name";

    private static final String WALLET_TABLE_CREATE = "create table " + WALLET_TABLE + "(" +
            WALLET_COLUMN_ID + " integer primary key, " + WALLET_COLUMN_IMAGE + " integer, " +
            WALLET_COLUMN_NAME + " text, " + TABLE_COLUMN_PARENT + " integer" + ");";


    // Таблица с категориями затрат и доходов
    static final String CATEGORY_TABLE = "category";
    static final String CATEGORY_COLUMN_ID = "_id";
    static final String CATEGORY_COLUMN_IMAGE = "image";
    static final String CATEGORY_COLUMN_NAME = "name";
//    static final String CATEGORY_COLUMN_GROUP = "group_id";
    static final String CATEGORY_COLUMN_PARENT = "parent_id";

    private static final String CATEGORY_TABLE_CREATE = "create table " + CATEGORY_TABLE + "(" +
            CATEGORY_COLUMN_ID + " integer primary key, " + CATEGORY_COLUMN_IMAGE + " integer, " +
            CATEGORY_COLUMN_NAME + " text, " +
//            CATEGORY_COLUMN_GROUP + " integer, " +
            CATEGORY_COLUMN_PARENT + " integer" + ");";


    // Таблица операций (транзакций) - расходы, доходы, перемещения и обмены
    // currency_id, wallet_id, category_id, sum, currentDate, comment
    static final String RECORD_TABLE = "records";
    static final String RECORD_COLUMN_ID = "_id";
    static final String RECORD_COLUMN_CURRENCY_ID = "currency_id";
//    static final String RECORD_COLUMN_CURRENCY_ID_DEST = "currency_id_dest";
    static final String RECORD_COLUMN_WALLET_ID = "wallet_id";
    static final String RECORD_COLUMN_CATEGORY_ID = "category_id";
    static final String RECORD_COLUMN_SUM = "sum";
    static final String RECORD_COLUMN_DATE = "operation_date";
    static final String RECORD_COLUMN_TIME = "operation_time";
    static final String RECORD_COLUMN_COMMENT = "comment";
    static final String RECORD_COLUMN_OPERATION_TYPE = "operation_type";
    static final String RECORD_COLUMN_SELECTED = "selected";

    private static final String RECORD_TABLE_CREATE = "create table " + RECORD_TABLE + "(" +
            RECORD_COLUMN_ID + " integer primary key, " +
            RECORD_COLUMN_CURRENCY_ID + " integer, " +
            RECORD_COLUMN_WALLET_ID + " integer, " +
            RECORD_COLUMN_CATEGORY_ID + " integer, " +
            RECORD_COLUMN_SUM + " real, " +
            RECORD_COLUMN_DATE + " text, " +
            RECORD_COLUMN_TIME + " text, " +
            RECORD_COLUMN_COMMENT + " text, " +
            RECORD_COLUMN_OPERATION_TYPE + " integer, " +
            RECORD_COLUMN_SELECTED + " integer" +
            ");";


    // Таблица для планирования бюджета
    static final String BUDGET_TABLE = "budget_plan";
    static final String BUDGET_COLUMN_ID = "_id";
    static final String BUDGET_COLUMN_PLAN = "plan_";
    static final String BUDGET_COLUMN_FACT = "fact_";

    private static final String BUDGET_TABLE_CREATE = "create table " + BUDGET_TABLE + "(" +
            BUDGET_COLUMN_ID + " integer primary key" +
            ");";


// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 0. БД
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    private int maxSpendingCategory = -1;
    private int maxSourceCategory = -1;

    private final Context mCtx;

    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    DB(Context ctx) {
        mCtx = ctx;
    }

    // открыть подключение
    void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    // закрыть подключение
    void close() {
        if (mDBHelper != null) mDBHelper.close();
    }

    // получить все данные из таблицы
    Cursor getAllData(final String TABLE_NAME, int string_id) {
        if (!TABLE_NAME.equals(CATEGORY_TABLE)) {
            return mDB.query(TABLE_NAME, null, null, null, null, null, null);
        }
//        Log.d(LOG_TAG, "--- string_id " + string_id + " ---");
        String selection = "";
        String[] selectionArgs = new String[] { String.valueOf(div_category_gain) };
        if (string_id == R.string.category) {
//            Log.d(LOG_TAG, "--- R.string.category ---");
            selection = "_id < ? and _id > -1";
        }
        else if (string_id == R.string.source) {
//            Log.d(LOG_TAG, "--- R.string.source ---");
            selection = "_id >= ?";
        }
//        Cursor localCursor = mDB.query(TABLE_NAME, null, selection, selectionArgs, CATEGORY_COLUMN_GROUP, null, null);
        Cursor localCursor = mDB.query(TABLE_NAME, null, selection, selectionArgs, CATEGORY_COLUMN_ID, null, null);

        // Заодно считаем id, с которым надо записать категорию в таблицу
        if (localCursor.moveToFirst()) {
            int maxCategory = -1;
            do {
                int id = localCursor.getInt(localCursor.getColumnIndex(CATEGORY_COLUMN_ID));
                if (id > maxCategory) {
                    maxCategory = id;
                }
//                Log.d(LOG_TAG, "id: " + id);
            } while (localCursor.moveToNext());

            if (string_id == R.string.category) {
                maxSpendingCategory = maxCategory;
            }
            else if (string_id == R.string.source) {
                maxSourceCategory = maxCategory;
            }
//            Log.d(LOG_TAG, "max category id: " + maxCategory);
        }
        return localCursor;
    }

// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 2. Журнал операций
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    // получить данные из таблицы для вывода истории операций
    Cursor getAllHistoryData() {
        String sqlQuery = "select "
                + RECORD_TABLE + "." + RECORD_COLUMN_ID + ", "
                + CURRENCY_TABLE + "." + CURRENCY_COLUMN_TITLE + ", "
                + WALLET_TABLE + "." + WALLET_COLUMN_IMAGE + ", "
                + CATEGORY_TABLE + "." + CATEGORY_COLUMN_NAME + ", "
                + RECORD_TABLE + "." + RECORD_COLUMN_DATE + ", "
                + RECORD_TABLE + "." + RECORD_COLUMN_TIME + ", "
                + RECORD_TABLE + "." + RECORD_COLUMN_SUM + ", "
                + RECORD_TABLE + "." + RECORD_COLUMN_COMMENT + ", "
                + RECORD_TABLE + "." + RECORD_COLUMN_SELECTED + ", "
                + RECORD_TABLE + "." + RECORD_COLUMN_OPERATION_TYPE
                + " from " + RECORD_TABLE
                + " inner join " + CATEGORY_TABLE
                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_CATEGORY_ID
                + " = " + CATEGORY_TABLE + "." + CATEGORY_COLUMN_ID
                + " inner join " + WALLET_TABLE
                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_WALLET_ID
                + " = " + WALLET_TABLE + "." + WALLET_COLUMN_ID
                + " inner join " + CURRENCY_TABLE
                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_CURRENCY_ID
                + " = " + CURRENCY_TABLE + "." + CURRENCY_COLUMN_ID
                + " order by "
                + RECORD_TABLE + "." + RECORD_COLUMN_DATE + " desc, "
                + RECORD_TABLE + "." + RECORD_COLUMN_TIME + " desc";
//        Log.d(LOG_TAG, sqlQuery);
        return mDB.rawQuery(sqlQuery, null);
    }

// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 3. Баланс
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    // Получить список доступных мест хранения
    Cursor getWalletData(int wallet) {
        String sqlQuery = "select "
                + WALLET_TABLE + "." + WALLET_COLUMN_ID + ", "
                + WALLET_TABLE + "." + WALLET_COLUMN_IMAGE + ", "
                + WALLET_TABLE + "." + WALLET_COLUMN_NAME
                + " from " + WALLET_TABLE
                + " where " + WALLET_COLUMN_ID + " = " + wallet;
//        Log.d(LOG_TAG, sqlQuery);
        return mDB.rawQuery(sqlQuery, null);
    }


    // Получить список доступных мест хранения
    int getNumberOfRecords(String TABLE_NAME) {
        return mDB.query(TABLE_NAME, null, null, null, null, null, null).getCount();
    }


    // получить данные из таблицы для вывода баланса
    Cursor getBalanceData(int wallet, int currency) {
        String sqlQuery = "select "
                + WALLET_TABLE + "." + WALLET_COLUMN_ID + ", "
                + WALLET_TABLE + "." + WALLET_COLUMN_IMAGE + ", "
                + WALLET_TABLE + "." + WALLET_COLUMN_NAME + ", "
                + CURRENCY_TABLE + "." + CURRENCY_COLUMN_TITLE + ", "
                + "sum(" + RECORD_TABLE + "." + RECORD_COLUMN_SUM + ") as " + RECORD_COLUMN_SUM
                + " from " + RECORD_TABLE
                + " inner join " + WALLET_TABLE
                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_WALLET_ID
                + " = " + WALLET_TABLE + "." + WALLET_COLUMN_ID
                + " inner join " + CURRENCY_TABLE
                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_CURRENCY_ID
                + " = " + CURRENCY_TABLE + "." + CURRENCY_COLUMN_ID
                + " where " + RECORD_TABLE + "." + RECORD_COLUMN_WALLET_ID + " = " + wallet
                + " and " + RECORD_TABLE + "." + RECORD_COLUMN_CURRENCY_ID + " = " + currency;
//        Log.d(LOG_TAG, sqlQuery);
        return mDB.rawQuery(sqlQuery, null);
    }


    // Добавление записи об операции
    // operation_type, currency_id, currency_id_dest, wallet_id, wallet_id_dest, category_id,
    // sum, sumDest, currentDate, comment
    void addTransaction(long id, int operation_type, long currency_id, long wallet_id, long category_id,
                               double sum, String currentDate, String currentTime, String comment, int mode) {
        ContentValues cv = new ContentValues();
        cv.put(RECORD_COLUMN_OPERATION_TYPE, operation_type);
        cv.put(RECORD_COLUMN_CURRENCY_ID, currency_id);
//        cv.put(RECORD_COLUMN_CURRENCY_ID_DEST, currency_id_dest);
        cv.put(RECORD_COLUMN_WALLET_ID, + wallet_id);
//        cv.put(RECORD_COLUMN_WALLET_ID_DEST, + wallet_id_dest);
        cv.put(RECORD_COLUMN_CATEGORY_ID, + category_id);
        cv.put(RECORD_COLUMN_SUM, sum);
//        cv.put(RECORD_COLUMN_SUM_DEST, sumDest);
        cv.put(RECORD_COLUMN_DATE, currentDate);
        cv.put(RECORD_COLUMN_TIME, currentTime);
        cv.put(RECORD_COLUMN_COMMENT, comment);
        cv.put(RECORD_COLUMN_SELECTED, 0);

        if (mode == CONFIRM_SAVE) mDB.insert(RECORD_TABLE, null, cv);
        else if (mode == CONFIRM_EDIT) mDB.update(RECORD_TABLE, cv, RECORD_COLUMN_ID + " = " + id, null);
    }


    // Добавление параметра отображения "контекстного" меню в журнале операций
    void setSelectedParameter(long id, int setParameter) {
        ContentValues cv = new ContentValues();
        if (setParameter == AUTO_SELECT) {
            Cursor cursor = mDB.rawQuery("select " + RECORD_COLUMN_SELECTED + " from " + RECORD_TABLE
                    + " where " + RECORD_COLUMN_ID + " = " + id, null);
            if (cursor.moveToFirst()) {
//            Log.d(LOG_TAG, "id " + id + ": now selected = " + cursor.getInt(cursor.getColumnIndex(RECORD_COLUMN_SELECTED)));
                int dbParameter = cursor.getInt(cursor.getColumnIndex(RECORD_COLUMN_SELECTED));
                setParameter = (dbParameter == NOT_SELECTED) ? SELECTED : NOT_SELECTED;
            }
            cursor.close();
        }

//        Log.d(LOG_TAG, "id " + id + ": set " + setParameter);
        cv.put(RECORD_COLUMN_SELECTED, setParameter);
        mDB.update(RECORD_TABLE, cv, RECORD_COLUMN_ID + " = " + id, null);
    }


    // Удалить запись из DB_TABLE
    void deleteTransaction(long id) {
        mDB.delete(RECORD_TABLE, RECORD_COLUMN_ID + " = " + id, null);
    }


    // Загрузка данных для редактирования транзакции
    Cursor loadTransactionDataById(long id) {
        return mDB.query(RECORD_TABLE, null, RECORD_COLUMN_ID + " = " + id, null, null, null, null);
    }

// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 4. Категории
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    final int EDIT_SPEND = 0, EDIT_SOURCE = 1, EDIT_WALLET = 2;

    void createNewCategory(int mode, String category) {
        ContentValues cv = new ContentValues();

        switch (mode) {
            case EDIT_SPEND:
                cv.put(CATEGORY_COLUMN_ID, maxSpendingCategory + 1);
                cv.put(CATEGORY_COLUMN_NAME, category);
                mDB.insert(CATEGORY_TABLE, null, cv);

                // Также, добавляем эту категорию в таблицу планирования бюджета
                cv.clear();
                cv.put(BUDGET_COLUMN_ID, maxSpendingCategory + 1);
                mDB.insert(BUDGET_TABLE, null, cv);
                break;

            case EDIT_SOURCE:
                cv.put(CATEGORY_COLUMN_ID, maxSourceCategory + 1);
                cv.put(CATEGORY_COLUMN_NAME, category);
                mDB.insert(CATEGORY_TABLE, null, cv);

                // Также, добавляем эту категорию в таблицу планирования бюджета
                cv.clear();
                cv.put(BUDGET_COLUMN_ID, maxSourceCategory + 1);
                mDB.insert(BUDGET_TABLE, null, cv);
                break;

            case EDIT_WALLET:
                cv.put(CATEGORY_COLUMN_NAME, category);
                mDB.insert(WALLET_TABLE, null, cv);
                break;
        }
    }


    void updateCategory(int mode, long id, String category) {
        if (mode == EDIT_WALLET) {
            ContentValues cv = new ContentValues();
            cv.put(WALLET_COLUMN_NAME, category);
            Log.d(LOG_TAG, "update WALLET id: " + id);
            mDB.update(WALLET_TABLE, cv, WALLET_COLUMN_ID + " = " + id, null);
        }
        else {
            ContentValues cv = new ContentValues();
            cv.put(CATEGORY_COLUMN_NAME, category);
            Log.d(LOG_TAG, "update category id: " + id);
            mDB.update(CATEGORY_TABLE, cv, CATEGORY_COLUMN_ID + " = " + id, null);
        }
    }

// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 5. Планирование
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    static final int PLAN = 0, FACT = 1;

    void addColumnToBudgetTable(String newColumn) {
        String sqlQuery = "alter table " + BUDGET_TABLE + " add column " + BUDGET_COLUMN_PLAN + newColumn + " text";
        Log.d(LOG_TAG, sqlQuery);
        try {
            mDB.execSQL(sqlQuery);
        }
        catch (Exception ex) {
            Log.d(LOG_TAG, ex.getClass() + " error: " + ex.getMessage());
        }

        sqlQuery = "alter table " + BUDGET_TABLE + " add column " + BUDGET_COLUMN_FACT + newColumn + " text";
        Log.d(LOG_TAG, sqlQuery);
        try {
            mDB.execSQL(sqlQuery);
        }
        catch (Exception ex) {
            Log.d(LOG_TAG, ex.getClass() + " error: " + ex.getMessage());
        }

//        logCursor(mDB.rawQuery("select * from " + BUDGET_TABLE, null));
    }


    Cursor getPlanData(String column) {
        String sqlQuery = "select "
                + BUDGET_TABLE + "." + BUDGET_COLUMN_ID + ", "
                + BUDGET_TABLE + "." + BUDGET_COLUMN_PLAN + column + ", "
                + BUDGET_TABLE + "." + BUDGET_COLUMN_FACT + column + ", "
                + CATEGORY_TABLE + "." + CATEGORY_COLUMN_NAME
                + " from " + BUDGET_TABLE
                + " inner join " + CATEGORY_TABLE
                + " on " + BUDGET_TABLE + "." + BUDGET_COLUMN_ID
                + " = " + CATEGORY_TABLE + "." + CATEGORY_COLUMN_ID
                + " where " + CATEGORY_TABLE + "." + CATEGORY_COLUMN_PARENT + " = 0";
//        Log.d(LOG_TAG, sqlQuery);
        return mDB.rawQuery(sqlQuery, null);
    }


    Cursor getPlanAllSpendSum(int columnType, String column) {
        if (columnType == PLAN) {
            column = BUDGET_COLUMN_PLAN + column;
        }
        else if (columnType == FACT) {
            column = BUDGET_COLUMN_FACT + column;
        }

        String sqlQuery = "select "
                + "sum(" + BUDGET_TABLE + "." + column + ") as " + RECORD_COLUMN_SUM
                + " from " + BUDGET_TABLE;
//        Log.d(LOG_TAG, sqlQuery);
        return mDB.rawQuery(sqlQuery, null);
    }


    void updateLimitForCategory(String column, long id, int limit) {
        ContentValues cv = new ContentValues();
        cv.put(BUDGET_COLUMN_PLAN + column, limit);
//        Log.d(LOG_TAG, "update limit for category " + id + " (" + limit + "), month: " + column);
        mDB.update(BUDGET_TABLE, cv, BUDGET_COLUMN_ID + " = " + id, null);
    }


    void updateFactSumForCategory(String column, long id) {
        int factSum = calculateFactSumForCategory(column, id);

        ContentValues cv = new ContentValues();
        cv.put(BUDGET_COLUMN_FACT + column, factSum);
//        Log.d(LOG_TAG, "update fact sum for category " + id + " (" + factSum + "), month: " + column);
        mDB.update(BUDGET_TABLE, cv, BUDGET_COLUMN_ID + " = " + id, null);
    }


    private int calculateFactSumForCategory(String month, long category_id) {
        try {
            String seekMonth = new SimpleDateFormat("yyyy-MM", Locale.US).format(dbBudgetDateFormat.parse(month));
//            Log.d(LOG_TAG, seekMonth);

            String sqlQuery = "select "
                    + "sum(" + RECORD_TABLE + "." + RECORD_COLUMN_SUM + ") as " + RECORD_COLUMN_SUM
                    + " from " + RECORD_TABLE
                    + " where " + RECORD_TABLE + "." + RECORD_COLUMN_DATE + " like '" + seekMonth + "%'"
                    + " and " + RECORD_TABLE + "." + RECORD_COLUMN_CATEGORY_ID + " >= " + category_id
                    + " and " + RECORD_TABLE + "." + RECORD_COLUMN_CATEGORY_ID + " < " + (category_id + div_category_group);
            Log.d(LOG_TAG, sqlQuery);
            Cursor cursor = mDB.rawQuery(sqlQuery, null);
//            logCursor(cursor);
            if ((cursor != null) && (cursor.moveToFirst())) {
                int factSum = cursor.getInt(cursor.getColumnIndex(DB.RECORD_COLUMN_SUM));
                cursor.close();
                return abs(factSum);
            }
        }
        catch (Exception exception) {
            Log.d(LOG_TAG, exception.toString());
        }
        return -1;
    }

// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// Остальное
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    // Вывод в лог данных из курсора
    public void logCursor(Cursor c) {
        if (c != null) {
            if (c.moveToFirst()) {
                String str;
                do {
                    str = "";
                    for (String cn : c.getColumnNames()) {
                        str = str.concat(cn + " = " + c.getString(c.getColumnIndex(cn)) + "; ");
                    }
                    Log.d(LOG_TAG, str);
                } while (c.moveToNext());
            }
        } else
            Log.d(LOG_TAG, "Cursor is null");
        Log.d(LOG_TAG, "\n");
    }


    // класс по созданию и управлению БД
    private class DBHelper extends SQLiteOpenHelper {
        DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // создаем и заполняем БД
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CURRENCY_TABLE_CREATE);
            ContentValues cv = new ContentValues();
            for (int i = 0; i < currencyData.length; i++) {
                cv.put(CURRENCY_COLUMN_ID, i);
                cv.put(CURRENCY_COLUMN_NAME, currencyData[i]);
                cv.put(CURRENCY_COLUMN_TITLE, currencyData[i]);
                db.insert(CURRENCY_TABLE, null, cv);
            }


            db.execSQL(WALLET_TABLE_CREATE);
            cv.clear();
            for (int i = 0; i < walletData.length; i++) {
                cv.put(WALLET_COLUMN_ID, i);
                cv.put(WALLET_COLUMN_NAME, walletData[i]);
                cv.put(WALLET_COLUMN_IMAGE, imageWalletData[i]);
                db.insert(WALLET_TABLE, null, cv);
            }


            db.execSQL(CATEGORY_TABLE_CREATE);
            cv.clear();
            cv.put(CATEGORY_COLUMN_ID, -1);
            cv.put(CATEGORY_COLUMN_NAME, "<->");
            db.insert(CATEGORY_TABLE, null, cv);
            for (int i = 0; i < categoryData.length; i++) {
                cv.clear();
                cv.put(CATEGORY_COLUMN_ID, categoryGroupData[i]);
                cv.put(CATEGORY_COLUMN_NAME, categoryData[i]);
//                cv.put(CATEGORY_COLUMN_GROUP, categoryGroupData[i]);
                cv.put(CATEGORY_COLUMN_PARENT, categoryParentData[i]);
                if (categoryGroupData[i] % div_category_group != 0) {
                    cv.put(CATEGORY_COLUMN_IMAGE, R.mipmap.empty42);
                }
                db.insert(CATEGORY_TABLE, null, cv);
            }
            for (int i = 0; i < sourcesData.length; i++) {
                cv.clear();
                cv.put(CATEGORY_COLUMN_ID, sourcesGroupData[i] + div_category_gain);
                cv.put(CATEGORY_COLUMN_NAME, sourcesData[i]);
//                cv.put(CATEGORY_COLUMN_GROUP, sourcesGroupData[i]);
                if (sourcesGroupData[i] % div_category_group != 0) {
                    cv.put(CATEGORY_COLUMN_IMAGE, R.mipmap.empty42);
                }
                db.insert(CATEGORY_TABLE, null, cv);
            }


            db.execSQL(BUDGET_TABLE_CREATE);
            cv.clear();
            for (int i = 0; i < categoryData.length; i++) {
                cv.put(BUDGET_COLUMN_ID, categoryGroupData[i]);
                db.insert(BUDGET_TABLE, null, cv);
            }
            for (int i = 0; i < sourcesData.length; i++) {
                cv.put(BUDGET_COLUMN_ID, sourcesGroupData[i] + div_category_gain);
                db.insert(BUDGET_TABLE, null, cv);
            }


            db.execSQL(RECORD_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}