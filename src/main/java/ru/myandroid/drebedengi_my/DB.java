package ru.myandroid.drebedengi_my;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

import static java.lang.Math.abs;

public class DB {

    private final String LOG_TAG = "myLogs";

    static final int START = 0, SPENDING = 1, GAIN = 2, MOVE = 3, CHANGE = 4;
    static final int AUTO_SELECT = -1, NOT_SELECTED = 0, SELECTED = 1;
    final int CONFIRM_SAVE = 0, CONFIRM_EDIT = 1;

    final int div_category_gain = 1000000;
    final int div_category_group = 1000;

    static SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    static SimpleDateFormat dbTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
    static SimpleDateFormat dbBudgetDateFormat = new SimpleDateFormat("yyyy_MM", Locale.US);


    // Данные для первоначального заполнения БД
    private String[] currencyData = { "руб", "USD", "EUR" };
    private String[] categoryData = { "[Без категории]",
            "Еда", "Развлечения", "Подарки", "Благотворительность", "Проезд", "Счета", "Авто", "Учёба, курсы, тренинги", "Дом, семья", "Здоровье", "Гигиена", "Техника", "Одежда", "Спорт", "Путешествия", "Работа", "Разное", "Банковские операции",

            "Обеды, перекусы", "Продукты", "Спортивное питание",
            "Кафешки", "Встречи, сходки", "Кино", "Музеи, выставки", "Аттракционы", "Каток", "Вечеринки", "Концерты", "Аквапарк", "Разные развлечения",
            "Цветы",
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
            div_category_group, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000, 11000, 12000, 13000, 14000, 15000, 16000, 17000, 18000,

            1001, 1002, 1003,
            2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010,
            3001,
            5001, 5002, 5003, 5004, 5005,
            6001, 6002, 6003,
            7001, 7002, 7003, 7004, 7005,
            8001,
            9001, 9002, 9003, 9004, 9005,
            10001, 10002,
            11001, 11002, 11003,
            12001, 12002, 12003, 12004, 12005, 12006,
            13001, 13002, 13003, 13004, 13005, 13006, 13007, 13008, 13009,
            14001, 14002, 14003, 14004, 14005,
            15001, 15002, 15003, 15004, 15005, 15006, 15007, 15008, 15009, 15010, 15011, 15012, 15013, 15014,
            17001, 17002,
            18001, 18002 };
    private final int[] categoryParentData = { -1,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,

            1, 1, 1,
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            3,
            5, 5, 5, 5, 5,
            6, 6, 6,
            7, 7, 7, 7, 7,
            8,
            9, 9, 9, 9, 9,
            10, 10,
            11, 11, 11,
            12, 12, 12, 12, 12, 12,
            13, 13, 13, 13, 13, 13, 13, 13, 13,
            14, 14, 14, 14, 14,
            15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15,
            17, 17,
            18, 18 };
    private String[] sourcesData = { "Работа (\"Агат\")", "Фриланс", "ООО \"Атлант+\"", "Альтернатива", "Родители", "Подарки", "Разное" };
    private final int[] sourcesGroupData = { 1000, 2000, 3000, 4000, 5000, 6000, 7000 };
    private String[] walletData = { "Кошелёк", "Ящик домашний", "Карта \"Сбербанк\" (Visa)", "Карта \"Тинькофф\" (Visa)", "Яндекс.Деньги",
            "\"Детские деньги\"", "Счёт \"Сбербанк\"", "Счёт \"Совкомбанк\"",
            "Карта \"Зенит\" (MasterCard)", "Карта \"Солид\" (MasterCard)", "Карта \"Альфа\" (MasterCard)", "Карта \"Кукуруза\" (MasterCard)",
            "PerfectMoney", "AdvCash", "ePayments", "Blockchain", "MyEtherWallet",
            "Мама", "Денис Тетерин", "Евгений", "Сбербанк",
            "Биржи", "Tirus", "Polybius", "GS Mining", "DEEX", "FBF" };
    private final int imageWalletData[] = { R.mipmap.wallet42, R.mipmap.box42, R.mipmap.sber_visa42, R.mipmap.tcs42, R.mipmap.yandex42,
            R.mipmap.folder42, R.mipmap.sber42, R.mipmap.sovcom42,
            R.mipmap.zenit42, R.mipmap.solid42, R.mipmap.alfa42, R.mipmap.kyky42,
            R.mipmap.pm42, R.mipmap.advcash42, R.mipmap.epayments42, R.mipmap.blockchain42, R.mipmap.myetherwallet42,
            0, 0, 0, R.mipmap.sber42,
            0, R.mipmap.tirus42, 0, R.mipmap.gs42, R.mipmap.deex42, R.mipmap.fbf42 };


    // База данных
    private static final String DB_NAME = "database";
    private static final int DB_VERSION = 1;

    // Общие названия для полей таблиц
    static final String TABLE_COLUMN_ID = "_id";
    static final String TABLE_COLUMN_NAME = "name";
    static final String TABLE_COLUMN_NAME_TO = "name_to";
    static final String TABLE_COLUMN_NAME_FROM = "name_from";
    static final String TABLE_COLUMN_IMAGE = "image";
    static final String TABLE_COLUMN_IMAGE_TO = "image_to";
    static final String TABLE_COLUMN_IMAGE_FROM = "image_from";
    static final String TABLE_COLUMN_TITLE_TO = "title_to";
    static final String TABLE_COLUMN_TITLE_FROM = "title_from";
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
    static final String WALLET_COLUMN_HIDDEN = "hidden";

    private static final String WALLET_TABLE_CREATE = "create table " + WALLET_TABLE + "(" +
            WALLET_COLUMN_ID + " integer primary key, " + WALLET_COLUMN_IMAGE + " integer, " +
            WALLET_COLUMN_NAME + " text, " + TABLE_COLUMN_PARENT + " integer, " +
            WALLET_COLUMN_HIDDEN + " integer default 0" + ");";


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
    static final String RECORD_COLUMN_CURRENCY_ID_DEST = "currency_id_dest";
    static final String RECORD_COLUMN_WALLET_ID = "wallet_id";
    static final String RECORD_COLUMN_WALLET_ID_DEST = "wallet_id_dest";
    static final String RECORD_COLUMN_CATEGORY_ID = "category_id";
    static final String RECORD_COLUMN_SUM = "sum";
    static final String RECORD_COLUMN_SUM_MOVE = "sum_move";
    static final String RECORD_COLUMN_DATE = "operation_date";
    static final String RECORD_COLUMN_TIME = "operation_time";
    static final String RECORD_COLUMN_COMMENT = "comment";
    static final String RECORD_COLUMN_OPERATION_TYPE = "operation_type";
    static final String RECORD_COLUMN_SELECTED = "selected";

    private static final String RECORD_TABLE_CREATE = "create table " + RECORD_TABLE + "(" +
            RECORD_COLUMN_ID + " integer primary key, " +
            RECORD_COLUMN_CURRENCY_ID + " integer, " +
            RECORD_COLUMN_CURRENCY_ID_DEST + " integer, " +
            RECORD_COLUMN_WALLET_ID + " integer, " +
            RECORD_COLUMN_WALLET_ID_DEST + " integer, " +
            RECORD_COLUMN_CATEGORY_ID + " integer, " +
            RECORD_COLUMN_SUM + " real, " +
            RECORD_COLUMN_SUM_MOVE + " real, " +
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
        if ((localCursor != null) && (localCursor.moveToFirst())) {
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
// 1. Операции
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    // Добавление записи об операции
    void addTransaction(int mode, long id, int operation_type, long category_id,
                        double sum, double sum_dest, String currentDate, String currentTime, String comment,
                        long currency_id, long currency_id_dest, long wallet_id, long wallet_id_dest) {
        ContentValues cv = new ContentValues();
        cv.put(RECORD_COLUMN_OPERATION_TYPE, operation_type);
        cv.put(RECORD_COLUMN_CURRENCY_ID, currency_id);
        cv.put(RECORD_COLUMN_CURRENCY_ID_DEST, currency_id_dest);
        cv.put(RECORD_COLUMN_WALLET_ID, wallet_id);
        cv.put(RECORD_COLUMN_WALLET_ID_DEST, wallet_id_dest);
        cv.put(RECORD_COLUMN_CATEGORY_ID, category_id);
        if ((operation_type == SPENDING) || (operation_type == GAIN)) {
            cv.put(RECORD_COLUMN_SUM, sum);
        }
        else if (operation_type == MOVE) {
            cv.put(RECORD_COLUMN_SUM_MOVE, sum);
        }
        else if (operation_type == CHANGE) {
            cv.put(RECORD_COLUMN_SUM, sum_dest);
            cv.put(RECORD_COLUMN_SUM_MOVE, sum);
        }
        cv.put(RECORD_COLUMN_DATE, currentDate);
        cv.put(RECORD_COLUMN_TIME, currentTime);
        if (comment.equals("")) {
            cv.putNull(RECORD_COLUMN_COMMENT);
        }
        else {
            cv.put(RECORD_COLUMN_COMMENT, comment);
        }
        cv.put(RECORD_COLUMN_SELECTED, 0);

        try {
            if (mode == CONFIRM_SAVE) {
                mDB.insert(RECORD_TABLE, null, cv);
//                Log.d(LOG_TAG, "ins id = " + ins_id);
            }
            else if (mode == CONFIRM_EDIT) {
                mDB.update(RECORD_TABLE, cv, RECORD_COLUMN_ID + " = " + id, null);
//                Log.d(LOG_TAG, "upd id = " + id);
            }
        }
        catch (Exception ex) {
            Log.d(LOG_TAG, ex.getClass() + " error: " + ex.getMessage());
        }
    }


    // Удалить запись о транзакции
    int[] deleteTransaction(long id) {
        Cursor cursor = mDB.rawQuery("select "
                + RECORD_COLUMN_OPERATION_TYPE + ","
                + RECORD_COLUMN_WALLET_ID + ","
                + RECORD_COLUMN_WALLET_ID_DEST + ","
                + RECORD_COLUMN_CURRENCY_ID + ","
                + RECORD_COLUMN_CURRENCY_ID_DEST
                + " from " + RECORD_TABLE
                + " where " + RECORD_COLUMN_ID + " = " + id, null);
        int[] ids = new int[4]; for (int i = 0; i < 4; ++i) { ids[i] = 0; }
        if ((cursor != null) && (cursor.moveToFirst())) {
            Log.d(LOG_TAG, "Operation has type " + cursor.getInt(cursor.getColumnIndex(RECORD_COLUMN_OPERATION_TYPE)) + " | id " + id);
            int type = cursor.getInt(cursor.getColumnIndex(RECORD_COLUMN_OPERATION_TYPE));
            ids[0] = cursor.getInt(cursor.getColumnIndex(RECORD_COLUMN_WALLET_ID));
            ids[1] = cursor.getInt(cursor.getColumnIndex(RECORD_COLUMN_WALLET_ID_DEST));
            ids[2] = cursor.getInt(cursor.getColumnIndex(RECORD_COLUMN_CURRENCY_ID_DEST));
            ids[3] = cursor.getInt(cursor.getColumnIndex(RECORD_COLUMN_CURRENCY_ID));
            if (type == MOVE) ids[3] = cursor.getInt(cursor.getColumnIndex(RECORD_COLUMN_CURRENCY_ID_DEST));

            mDB.delete(RECORD_TABLE, RECORD_COLUMN_ID + " = " + id, null);
            if ((type == MOVE) || (type == CHANGE)) { // Также удаляем фейковые операции для корректного баланса
                Log.d(LOG_TAG, "Also delete id's " + (id + 1) + " and " + (id + 2));
                mDB.delete(RECORD_TABLE, RECORD_COLUMN_ID + " = " + (id + 1), null);
                mDB.delete(RECORD_TABLE, RECORD_COLUMN_ID + " = " + (id + 2), null);
            }
            cursor.close();
        }
        return ids;
    }


    // Загрузка данных для редактирования транзакции
    Cursor loadTransactionDataById(long id) {
        return mDB.query(RECORD_TABLE, null, RECORD_COLUMN_ID + " = " + id, null, null, null, null);
    }

// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 2. Журнал операций
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    // получить данные из таблицы для вывода истории операций
    Cursor getAllHistoryData() {
//        String sqlQueryBase = "select "
//                + RECORD_TABLE + "." + RECORD_COLUMN_ID + ", "
//                + RECORD_TABLE + "." + RECORD_COLUMN_DATE + ", "
//                + RECORD_TABLE + "." + RECORD_COLUMN_TIME + ", "
//                + RECORD_TABLE + "." + RECORD_COLUMN_SUM + ", "
//                + RECORD_TABLE + "." + RECORD_COLUMN_COMMENT + ", "
//                + RECORD_TABLE + "." + RECORD_COLUMN_SELECTED + ", "
//                + RECORD_TABLE + "." + RECORD_COLUMN_OPERATION_TYPE + ", "
//                + CATEGORY_TABLE + "." + CATEGORY_COLUMN_NAME + ", "
//                + CURRENCY_TABLE + "." + CURRENCY_COLUMN_TITLE + ", "
//                + WALLET_TABLE + "_to" + "." + WALLET_COLUMN_NAME + " as " + TABLE_COLUMN_NAME_TO + ", "
//                + WALLET_TABLE + "_to" + "." + WALLET_COLUMN_IMAGE + " as " + TABLE_COLUMN_IMAGE_TO + ", "
//                + WALLET_TABLE + "_from" + "." + WALLET_COLUMN_NAME + " as " + TABLE_COLUMN_NAME_FROM + ", "
//                + WALLET_TABLE + "_from" + "." + WALLET_COLUMN_IMAGE + " as " + TABLE_COLUMN_IMAGE_FROM
//                + " from " + RECORD_TABLE
//
//                + " inner join " + CATEGORY_TABLE
//                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_CATEGORY_ID
//                + " = " + CATEGORY_TABLE + "." + CATEGORY_COLUMN_ID
//
//                + " inner join " + WALLET_TABLE + " as " + WALLET_TABLE + "_from"
//                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_WALLET_ID
//                + " = " + WALLET_TABLE + "_from" + "." + WALLET_COLUMN_ID
//
//                + " left join " + WALLET_TABLE + " as " + WALLET_TABLE + "_to"
//                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_WALLET_ID_DEST
//                + " = " + WALLET_TABLE + "_to" + "." + WALLET_COLUMN_ID
//
//                + " inner join " + CURRENCY_TABLE
//                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_CURRENCY_ID
//                + " = " + CURRENCY_TABLE + "." + CURRENCY_COLUMN_ID;

        String sqlQueryConditionSpendGain = " where "
                + CATEGORY_TABLE + "." + CATEGORY_COLUMN_NAME + " != ''";

        String sqlQueryConditionMoveChange = " where ("
                + RECORD_COLUMN_OPERATION_TYPE + " = " + MOVE
                + " or " + RECORD_COLUMN_OPERATION_TYPE + " = " + CHANGE + ")";

//                + " where ("
//                + CATEGORY_TABLE + "." + CATEGORY_COLUMN_NAME + " != ''"
//                + " or " + RECORD_COLUMN_OPERATION_TYPE + " = " + MOVE
//                + " or " + RECORD_COLUMN_OPERATION_TYPE + " = " + CHANGE + ")"

        String sqlQueryOrder = " order by "
//                + RECORD_TABLE + "."
                + RECORD_COLUMN_DATE + " desc, "
//                + RECORD_TABLE + "."
                + RECORD_COLUMN_TIME + " desc";

        String sqlQuery = "select * from ("
                + sqlQueryHistoryBase("_to") + sqlQueryConditionSpendGain // выборка расходов и доходов
                + " union "
                + sqlQueryHistoryBase("_from") + sqlQueryConditionMoveChange // выборка перемещений и обменов
                + ")" + sqlQueryOrder;

//        Log.d(LOG_TAG, sqlQuery);
        Cursor cursor = null;
        try {
            cursor = mDB.rawQuery(sqlQuery, null);
        }
        catch (Exception ex) {
            Log.d(LOG_TAG, ex.getClass() + " error: " + ex.getMessage());
        }
        return cursor;
    }


    // Вспомогательное формирование запроса для вывода истории
    private String sqlQueryHistoryBase(String direction) {
        return "select "
                + RECORD_TABLE + "." + RECORD_COLUMN_ID + ", "
                + RECORD_TABLE + "." + RECORD_COLUMN_DATE + ", "
                + RECORD_TABLE + "." + RECORD_COLUMN_TIME + ", "
                + RECORD_TABLE + "." + RECORD_COLUMN_SUM + ", "
                + RECORD_TABLE + "." + RECORD_COLUMN_SUM_MOVE + ", "
                + RECORD_TABLE + "." + RECORD_COLUMN_COMMENT + ", "
                + RECORD_TABLE + "." + RECORD_COLUMN_SELECTED + ", "
                + RECORD_TABLE + "." + RECORD_COLUMN_OPERATION_TYPE + ", "
                + CATEGORY_TABLE + "." + CATEGORY_COLUMN_NAME + ", "
                + WALLET_TABLE + "_to" + "." + WALLET_COLUMN_NAME + " as " + TABLE_COLUMN_NAME_TO + ", "
                + WALLET_TABLE + "_to" + "." + WALLET_COLUMN_IMAGE + " as " + TABLE_COLUMN_IMAGE_TO + ", "
//                + WALLET_TABLE + "_from" + "." + WALLET_COLUMN_NAME + " as " + TABLE_COLUMN_NAME_FROM + ", "
                + WALLET_TABLE + direction + "." + WALLET_COLUMN_NAME + " as " + TABLE_COLUMN_NAME_FROM + ", "
                + WALLET_TABLE + "_from" + "." + WALLET_COLUMN_IMAGE + " as " + TABLE_COLUMN_IMAGE_FROM + ", "
                + CURRENCY_TABLE + "_to" + "." + CURRENCY_COLUMN_TITLE + " as " + TABLE_COLUMN_TITLE_TO + ", "
                + CURRENCY_TABLE + "_from" + "." + CURRENCY_COLUMN_TITLE + " as " + TABLE_COLUMN_TITLE_FROM
                + " from " + RECORD_TABLE

                + " inner join " + CATEGORY_TABLE
                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_CATEGORY_ID
                + " = " + CATEGORY_TABLE + "." + CATEGORY_COLUMN_ID

                + " inner join " + WALLET_TABLE + " as " + WALLET_TABLE + "_from"
                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_WALLET_ID
                + " = " + WALLET_TABLE + "_from" + "." + WALLET_COLUMN_ID

                + " left join " + WALLET_TABLE + " as " + WALLET_TABLE + "_to"
                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_WALLET_ID_DEST
                + " = " + WALLET_TABLE + "_to" + "." + WALLET_COLUMN_ID

                + " left join " + CURRENCY_TABLE + " as " + CURRENCY_TABLE + "_from"
                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_CURRENCY_ID
                + " = " + CURRENCY_TABLE + "_from" + "." + CURRENCY_COLUMN_ID

                + " left join " + CURRENCY_TABLE + " as " + CURRENCY_TABLE + "_to"
                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_CURRENCY_ID_DEST
                + " = " + CURRENCY_TABLE + "_to" + "." + CURRENCY_COLUMN_ID;
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

// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 3. Баланс
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    // Получить список доступных мест хранения
    Cursor getWalletData(int wallet) {
        String sqlQuery = "select "
                + WALLET_TABLE + "." + WALLET_COLUMN_ID + ", "
                + WALLET_TABLE + "." + WALLET_COLUMN_IMAGE + ", "
                + WALLET_TABLE + "." + WALLET_COLUMN_NAME + ", "
                + WALLET_TABLE + "." + WALLET_COLUMN_HIDDEN
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
                + WALLET_TABLE + "." + WALLET_COLUMN_HIDDEN + ", "
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
                + " and " + RECORD_TABLE + "." + RECORD_COLUMN_CURRENCY_ID + " = " + currency
                + " and (" + RECORD_TABLE + "." + RECORD_COLUMN_OPERATION_TYPE + " = " + START
                + " or " + RECORD_TABLE + "." + RECORD_COLUMN_OPERATION_TYPE + " = " + SPENDING
                + " or " + RECORD_TABLE + "." + RECORD_COLUMN_OPERATION_TYPE + " = " + GAIN + ")";
//        Log.d(LOG_TAG, sqlQuery);
        return mDB.rawQuery(sqlQuery, null);
    }


    void setWalletHidden(int id) {
        int setParameter = 0;

        Cursor cursor = mDB.rawQuery("select " + WALLET_COLUMN_HIDDEN + " from " + WALLET_TABLE
                    + " where " + WALLET_COLUMN_ID + " = " + id, null);
//        logCursor(cursor);
        if ((cursor != null) && (cursor.moveToFirst())) {
            int dbParameter = cursor.getInt(cursor.getColumnIndex(WALLET_COLUMN_HIDDEN));
            setParameter = (dbParameter == NOT_SELECTED) ? SELECTED : NOT_SELECTED;
            cursor.close();
        }

        ContentValues cv = new ContentValues();
        cv.put(WALLET_COLUMN_HIDDEN, setParameter);
        mDB.update(WALLET_TABLE, cv, WALLET_COLUMN_ID + " = " + id, null);
    }


    BalanceStruct getBalanceData() {
        // Получаем размеры нашего массива сумм
        int walletsNumber = getNumberOfRecords(DB.WALLET_TABLE);
        int currenciesNumber = getNumberOfRecords(DB.CURRENCY_TABLE);

        int[] walletHiddenData = new int[walletsNumber];
        int[][] balanceData = new int[walletsNumber][currenciesNumber];
        String[] currencyData = new String[currenciesNumber];
        for (int i = 0; i < walletsNumber; i++) {
            for (int j = 0; j < currenciesNumber; j++) {
                Cursor cursor = getBalanceData(i, j);
//                db.logCursor(cursor);

                if ((cursor != null) && (cursor.moveToFirst())) {
                    walletHiddenData[i] = cursor.getInt(cursor.getColumnIndex(WALLET_COLUMN_HIDDEN));
                    balanceData[i][j] = cursor.getInt(cursor.getColumnIndex(RECORD_COLUMN_SUM));
                    currencyData[j] = cursor.getString(cursor.getColumnIndex(CURRENCY_COLUMN_TITLE));
//                    Log.d(LOG_TAG, "balanceData[][]: " + balanceData[i][j]);
//                    Log.d(LOG_TAG, "currencyData[]: " + currencyData[j]);
//                    Log.d(LOG_TAG, "hiddenData[]: " + walletHiddenData[i]);
                }
                else {
                    walletHiddenData[i] = 0;
                    balanceData[i][j] = 0;
                    currencyData[j] = "";
                }
            }
        }

        return new BalanceStruct(walletsNumber, currenciesNumber, walletHiddenData, balanceData, currencyData);
    }

// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 4. Категории
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    final int EDIT_SPEND = 0, EDIT_SOURCE = 1, EDIT_WALLET = 2;

    void createNewCategory(int mode, String category, Vector<Integer> currency) {
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
                cv.put(WALLET_COLUMN_NAME, category);
                long wallet_id = mDB.insert(WALLET_TABLE, null, cv);

                for (int i = 0; i < currency.size(); ++i) {
                    cv.clear();
                    cv.put(RECORD_COLUMN_SUM, 0);
                    cv.put(RECORD_COLUMN_WALLET_ID, wallet_id);
                    cv.put(RECORD_COLUMN_CURRENCY_ID, currency.get(i));
                    cv.put(RECORD_COLUMN_OPERATION_TYPE, START);
                    mDB.insert(RECORD_TABLE, null, cv);
                }
                break;
        }
    }


    void updateCategory(int mode, long wallet_id, String category, double remain, long currency_id) {
        if (mode == EDIT_WALLET) {
            Log.d(LOG_TAG, "update WALLET id " + wallet_id + ": category " + category + "; remain = " + remain);

            ContentValues cv = new ContentValues();
            if (!category.equals("")) {
                cv.put(WALLET_COLUMN_NAME, category);
                mDB.update(WALLET_TABLE, cv, WALLET_COLUMN_ID + " = " + wallet_id, null);
            }

            cv.clear();
            cv.put(RECORD_COLUMN_SUM, remain);
            String conditional = RECORD_COLUMN_WALLET_ID + " = " + wallet_id
                    + " and " + RECORD_COLUMN_CURRENCY_ID + " = " + currency_id
                    + " and " + RECORD_COLUMN_OPERATION_TYPE + " = " + START;
            mDB.update(RECORD_TABLE, cv, conditional, null);
        }
        else {
            ContentValues cv = new ContentValues();
            if (!category.equals("")) cv.put(CATEGORY_COLUMN_NAME, category);
            Log.d(LOG_TAG, "update category id: " + wallet_id);
            mDB.update(CATEGORY_TABLE, cv, CATEGORY_COLUMN_ID + " = " + wallet_id, null);
        }
    }


    double getWalletStartSum(long wallet_id, long currency_id) {
        String[] col = { RECORD_COLUMN_SUM };
        String conditional = RECORD_COLUMN_WALLET_ID + " = " + wallet_id
                + " and " + RECORD_COLUMN_CURRENCY_ID + " = " + currency_id
                + " and " + RECORD_COLUMN_OPERATION_TYPE + " = " + START;
        Cursor cursor = mDB.query(RECORD_TABLE, col, conditional, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                double remain = cursor.getDouble(cursor.getColumnIndex(DB.RECORD_COLUMN_SUM));
                cursor.close();
                return remain;
            }
        }
        return 0.0;
    }

// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// 5. Планирование
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    static final int PLAN = 0, FACT = 1, REMAIN = 2;

    void addColumnToBudgetTable(String newColumn) {
        String sqlQuery = "alter table " + BUDGET_TABLE
                + " add column " + BUDGET_COLUMN_PLAN + newColumn + " text default 0";
//        Log.d(LOG_TAG, sqlQuery);
        try {
            mDB.execSQL(sqlQuery);
        }
        catch (Exception ex) {
            Log.d(LOG_TAG, ex.getClass() + " error: " + ex.getMessage());
        }

        sqlQuery = "alter table " + BUDGET_TABLE
                + " add column " + BUDGET_COLUMN_FACT + newColumn + " text default 0";
//        Log.d(LOG_TAG, sqlQuery);
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
        if (columnType == REMAIN) {
            String sqlQuery = "select "
                    + "sum(" + BUDGET_TABLE + "." + BUDGET_COLUMN_PLAN + column + ")"
                    + " - sum(" + BUDGET_TABLE + "." + BUDGET_COLUMN_FACT + column + ")"
                    + " as " + RECORD_COLUMN_SUM
                    + " from " + BUDGET_TABLE;
//        Log.d(LOG_TAG, sqlQuery);
            return mDB.rawQuery(sqlQuery, null);
        }
        else {
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
    }


    void updateLimitForCategory(String column, long id, int limit) {
        ContentValues cv = new ContentValues();
        cv.put(BUDGET_COLUMN_PLAN + column, limit);
//        Log.d(LOG_TAG, "update limit for category " + id + " (" + limit + "), month: " + column);
        mDB.update(BUDGET_TABLE, cv, BUDGET_COLUMN_ID + " = " + id, null);
    }


    private void updateFactSumForCategory(String column, long id) {
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
//            Log.d(LOG_TAG, sqlQuery);
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


    void refreshFactSumForAllCategories(String month) {
        Cursor c = getPlanData(month);
        if ((c != null) && (c.moveToFirst())) {
//            Log.d(LOG_TAG, "plan categories count: " + c.getCount());
            do {
                int category_id = c.getInt(c.getColumnIndex(DB.BUDGET_COLUMN_ID));
//                Log.d(LOG_TAG, "category_id: " + category_id);
                updateFactSumForCategory(month, category_id);
            } while (c.moveToNext());
        }
    }


    Cursor getRemainData(String column) {
        String sqlQuery = "select "
                + BUDGET_TABLE + "." + BUDGET_COLUMN_ID + ", "
                + CATEGORY_TABLE + "." + CATEGORY_COLUMN_NAME + ", "
                + "(" + BUDGET_TABLE + "." + BUDGET_COLUMN_PLAN + column + ")"
                + " - (" + BUDGET_TABLE + "." + BUDGET_COLUMN_FACT + column + ")"
                + " as " + RECORD_COLUMN_SUM
                + " from " + BUDGET_TABLE
                + " inner join " + CATEGORY_TABLE
                + " on " + BUDGET_TABLE + "." + BUDGET_COLUMN_ID
                + " = " + CATEGORY_TABLE + "." + CATEGORY_COLUMN_ID
                + " where " + CATEGORY_TABLE + "." + CATEGORY_COLUMN_PARENT + " = 0"
                + " and " + BUDGET_TABLE + "." + BUDGET_COLUMN_PLAN + column
                + " - " + BUDGET_TABLE + "." + BUDGET_COLUMN_FACT + column + " != 0";
//        Log.d(LOG_TAG, sqlQuery);
        return mDB.rawQuery(sqlQuery, null);
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
            db.insert(CATEGORY_TABLE, null, cv);
            for (int i = 0; i < categoryData.length; i++) {
                cv.clear();
                cv.put(CATEGORY_COLUMN_ID, categoryGroupData[i]);
                cv.put(CATEGORY_COLUMN_NAME, categoryData[i]);
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


            // Начальные суммы
            db.execSQL(RECORD_TABLE_CREATE);
            cv.clear();
            for (int i = 0; i < walletData.length; i++) {
                for (int j = 0; j < currencyData.length; j++) {
                    cv.put(RECORD_COLUMN_SUM, 0);
                    cv.put(RECORD_COLUMN_WALLET_ID, i);
                    cv.put(RECORD_COLUMN_CURRENCY_ID, j);
                    cv.put(RECORD_COLUMN_OPERATION_TYPE, START);
                    db.insert(RECORD_TABLE, null, cv);
                }
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }


// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====
// Выгрузка БД
// == ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    // Попробуем выписать данные таблиц
    public String backupTables() {
        Log.d(LOG_TAG, "unloadTables");

        String summary = "";

        String SQL_GET_ALL_TABLES = "SELECT name"
                + " FROM sqlite_master WHERE type = 'table'"
                + " AND name !='android_metadata' AND name !='sqlite_sequence'";
//        Log.d(LOG_TAG, SQL_GET_ALL_TABLES);

        Cursor cursor = mDB.rawQuery(SQL_GET_ALL_TABLES, null);

//        try {
//            cursor = mDB.rawQuery(SQL_GET_ALL_TABLES, null);
//        }
//        catch (Exception ex) {
//            Log.d(LOG_TAG, ex.getClass() + " error: " + ex.getMessage());
//        }
//        logCursor(cursor);


        // Этап 1 - получаем имена таблиц
//        String[] tableName;
        Vector<String> tableName = new Vector<>();
        if (cursor != null) {
            // пока сделаю так - не знаю, как лучше

            // первый проход - тупо получаем количество таблиц
            int count = 0;
            if (cursor.moveToFirst()) {
                do {
                    ++count;
                } while (cursor.moveToNext());
            }
            Log.d(LOG_TAG, "Кол-во таблиц: " + count);

            // второй проход - записываем названия таблиц
//            tableName = new String[count];
            if (cursor.moveToFirst()) {
                String str;
                int i = 0;
                do {
                    str = "";
                    for (String cn : cursor.getColumnNames()) {
//                        str = str.concat(cn + " = " + cursor.getString(cursor.getColumnIndex(cn)) + "; ");
                        str = cursor.getString(cursor.getColumnIndex(cn));
                    }
//                    Log.d(LOG_TAG, str);
//                    tableName[i] = str;
                    tableName.add(str);
                    ++i;
                } while (cursor.moveToNext());
            }
//            for (int i = 0; i < count; ++i) {
//                Log.d(LOG_TAG, tableName[i]);
//            }

//            Log.d(LOG_TAG, "tableNames:");
//            for (int i = 0; i < tableName.size(); ++i) {
//                Log.d(LOG_TAG, i + ": " + tableName.get(i));
//            }
            cursor.close();
        }


        // Этап 2 - для каждой таблицы получаем имена столбцов и данные полей
        for (int i = 0; i < tableName.size(); ++i) {
//            Log.d(LOG_TAG, i + ": " + tableName.get(i));
//            Log.d(LOG_TAG, "TABLE " + tableName.get(i));
            summary += "TABLE " + tableName.get(i) + "\n";

            String sqlQuery = "SELECT * FROM " + tableName.get(i);
            cursor = mDB.rawQuery(sqlQuery, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String str;
                    do {
                        str = "";
                        for (String cn : cursor.getColumnNames()) {
                            str = str.concat(cn + "=" + cursor.getString(cursor.getColumnIndex(cn)) + ";");
                        }
//                        Log.d(LOG_TAG, str);
                        summary += str + "\n";
                    } while (cursor.moveToNext());
                }
            }
        }

//        Log.d(LOG_TAG, summary);
        return summary;
    }


    // Пробуем заполнить таблицы из прочитанного файла
    void reloadTable(String currentTable, String buffer) {
        Log.d(LOG_TAG, currentTable + " -> " + buffer);

        if (buffer.equals("TABLE " + currentTable)) {
            try {
                mDB.delete(currentTable, null, null);
            }
            catch (Exception ex) {
                Log.d(LOG_TAG, ex.getClass() + " error: " + ex.getMessage());
            }
            return;
        }


        ContentValues cv = new ContentValues();
        String[] array = buffer.split(";");

        for (String field : array) {
//            Log.d(LOG_TAG, "array: " + field);
            String[] pair = field.split("=");
            Log.d(LOG_TAG, pair[0] + "   " + pair[1]);

            if (!pair[1].equals("null")) {
                cv.put(pair[0], pair[1]);
            }
            else {
                cv.putNull(pair[0]);
            }
        }

        mDB.insert(currentTable, null, cv);
    }
}