package ru.myandroid.drebedengi_my;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DB {

    final String LOG_TAG = "myLogs";

    // Данные для первоначального заполнения БД
    private String[] currencyData = new String[] { "руб", "USD", "EUR" };
    private String[] walletData = new String[] { "Кошелёк", "Карта ТКС", "Яндекс", "Что-то ещё" };
    private String[] spendingData = new String[] { "Еда", "Бухло", "Тёлки", "Наркота", "Фуфло", "Понты" };
    private String[] sourcesData = new String[] { "Работа", "Дилерство", "Лафа", "Халявка" };
    private final int imageWalletData[] = {R.mipmap.wallet42, R.mipmap.tcs42, R.mipmap.yandex42, R.mipmap.advcash42};


    // База данных
    private static final String DB_NAME = "database";
    private static final int DB_VERSION = 1;

//    final String walletData[] = { "Кошелёк", "Карта", "Яндекс", "Что-то ещё" };
//    String walletData[] = { "Abc", "Def", "Ghi", "Jkl" };

    // Общие названия для полей таблиц
    public static final String TABLE_COLUMN_ID = "_id";
    public static final String TABLE_COLUMN_IMAGE = "image";
    public static final String TABLE_COLUMN_NAME = "name";


    // Таблица с местами хранения средств
    public static final String CURRENCY_TABLE = "currency";
    public static final String CURRENCY_COLUMN_ID = "_id";
    public static final String CURRENCY_COLUMN_IMAGE = "image";
    public static final String CURRENCY_COLUMN_NAME = "name";
    public static final String CURRENCY_COLUMN_TITLE = "title";

    private static final String CURRENCY_TABLE_CREATE = "create table " + CURRENCY_TABLE + "(" +
            CURRENCY_COLUMN_ID + " integer primary key, " + CURRENCY_COLUMN_IMAGE + " integer, " +
            CURRENCY_COLUMN_NAME + " text, " + CURRENCY_COLUMN_TITLE + " text" + ");";


    // Таблица с местами хранения средств
    public static final String WALLET_TABLE = "wallet";
    public static final String WALLET_COLUMN_ID = "_id";
    public static final String WALLET_COLUMN_IMAGE = "image";
    public static final String WALLET_COLUMN_NAME = "name";

    private static final String WALLET_TABLE_CREATE = "create table " + WALLET_TABLE + "(" +
            WALLET_COLUMN_ID + " integer primary key, " + WALLET_COLUMN_IMAGE + " integer, " +
            WALLET_COLUMN_NAME + " text" + ");";


    // Таблица с категориями затрат
    public static final String SPENDING_TABLE = "spending";
    public static final String SPENDING_COLUMN_ID = "_id";
    public static final String SPENDING_COLUMN_IMAGE = "image";
    public static final String SPENDING_COLUMN_NAME = "name";

    private static final String SPENDING_TABLE_CREATE = "create table " + SPENDING_TABLE + "(" +
            SPENDING_COLUMN_ID + " integer primary key, " + SPENDING_COLUMN_IMAGE + " integer, " +
            SPENDING_COLUMN_NAME + " text" + ");";


    // Таблица с категориями доходов
    public static final String SOURCES_TABLE = "sources";
    public static final String SOURCES_COLUMN_ID = "_id";
    public static final String SOURCES_COLUMN_IMAGE = "image";
    public static final String SOURCES_COLUMN_NAME = "name";

    private static final String SOURCES_TABLE_CREATE = "create table " + SOURCES_TABLE + "(" +
            SOURCES_COLUMN_ID + " integer primary key, " + SOURCES_COLUMN_IMAGE + " integer, " +
            SOURCES_COLUMN_NAME + " text" + ");";


    // Таблица операций (транзакций) - расходы, доходы, перемещения и обмены
    // currency_id, wallet_id, category_id, sum, currentDate, comment
    public static final String RECORD_TABLE = "records";
    public static final String RECORD_COLUMN_ID = "_id";
    public static final String RECORD_COLUMN_CURRENCY_ID = "currency_id";
//    public static final String RECORD_COLUMN_CURRENCY_ID_DEST = "currency_id_dest";
    public static final String RECORD_COLUMN_WALLET_ID = "wallet_id";
    public static final String RECORD_COLUMN_CATEGORY_ID = "category_id";
    public static final String RECORD_COLUMN_SUM = "sum";
    public static final String RECORD_COLUMN_DATE = "operation_date";
    public static final String RECORD_COLUMN_COMMENT = "comment";
    public static final String RECORD_COLUMN_OPERATION_TYPE = "operation_type";

    private static final String RECORD_TABLE_CREATE = "create table " + RECORD_TABLE + "(" +
            RECORD_COLUMN_ID + " integer primary key, " +
            RECORD_COLUMN_CURRENCY_ID + " integer, " +
            RECORD_COLUMN_WALLET_ID + " integer, " +
            RECORD_COLUMN_CATEGORY_ID + " integer, " +
            RECORD_COLUMN_SUM + " real, " +
            RECORD_COLUMN_DATE + " text, " +
            RECORD_COLUMN_COMMENT + " text, " +
            RECORD_COLUMN_OPERATION_TYPE + " integer" +
            ");";


    private final Context mCtx;

    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DB(Context ctx) {
        mCtx = ctx;
    }

    // открыть подключение
    public void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    // закрыть подключение
    public void close() {
        if (mDBHelper != null) mDBHelper.close();
    }

    // получить все данные из таблицы
    public Cursor getAllData(final String TABLE_NAME) {
//        return mDB.query(WALLET_TABLE, null, null, null, null, null, null);
        return mDB.query(TABLE_NAME, null, null, null, null, null, null);
    }

    // получить данные из таблицы для вывода истории операций
    public Cursor getAllHistoryData() {
//        return mDB.query(TABLE_NAME, null, null, null, null, null, null);
        String sqlQuery = "select "
                + WALLET_TABLE + "." + WALLET_COLUMN_IMAGE + ", "
                + SPENDING_TABLE + "." + SPENDING_COLUMN_NAME + ", "
                + CURRENCY_TABLE + "." + CURRENCY_COLUMN_TITLE + ", "
                + RECORD_TABLE + "." + RECORD_COLUMN_DATE + ", "
                + RECORD_TABLE + "." + RECORD_COLUMN_SUM
                + " from " + RECORD_TABLE
                + " inner join " + SPENDING_TABLE
                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_CATEGORY_ID
                + " = " + SPENDING_TABLE + "." + SPENDING_COLUMN_ID
                + " inner join " + WALLET_TABLE
                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_WALLET_ID
                + " = " + WALLET_TABLE + "." + WALLET_COLUMN_ID
                + " inner join " + CURRENCY_TABLE
                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_CURRENCY_ID
                + " = " + CURRENCY_TABLE + "." + CURRENCY_COLUMN_ID;
        return mDB.rawQuery(sqlQuery, null);
    }


    // получить данные из таблицы для вывода баланса
//    public Cursor getBalanceData(int wallet, int currency) {
    public String getBalanceData(int wallet, int currency) {
        String sqlQuery = "select "
                + WALLET_TABLE + "." + WALLET_COLUMN_IMAGE + ", "
                + WALLET_TABLE + "." + WALLET_COLUMN_NAME + ", "
                + CURRENCY_TABLE + "." + CURRENCY_COLUMN_TITLE + ", "
//                + RECORD_TABLE + "." + RECORD_COLUMN_CURRENCY_ID + ", "
//                + RECORD_TABLE + "." + RECORD_COLUMN_WALLET_ID + ", "
                + "sum(" + RECORD_TABLE + "." + RECORD_COLUMN_SUM + ") as " + RECORD_COLUMN_SUM
                + " from " + RECORD_TABLE
//                + " where " + RECORD_TABLE + "." + RECORD_COLUMN_WALLET_ID + " = 0"
                + " inner join " + WALLET_TABLE
                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_WALLET_ID
                + " = " + WALLET_TABLE + "." + WALLET_COLUMN_ID
                + " inner join " + CURRENCY_TABLE
                + " on " + RECORD_TABLE + "." + RECORD_COLUMN_CURRENCY_ID
                + " = " + CURRENCY_TABLE + "." + CURRENCY_COLUMN_ID
                + " where " + RECORD_TABLE + "." + RECORD_COLUMN_WALLET_ID + " = " + wallet
                + " and " + RECORD_TABLE + "." + RECORD_COLUMN_CURRENCY_ID + " = " + currency;
        Log.d(LOG_TAG, sqlQuery);
//        return mDB.rawQuery(sqlQuery, null);
        return sqlQuery;
    }


    // получить все данные из таблиц для вывода баланса
    public Cursor getAllBalanceData() {
        String sqlQuery = getBalanceData(0, 0) + " union all " + getBalanceData(0, 1) + " union all "
                + getBalanceData(1, 0) + " union all " + getBalanceData(1, 1) + " union all "
                + getBalanceData(2, 0) + " union all " + getBalanceData(2, 1) + " union all "
                + getBalanceData(3, 0) + " union all " + getBalanceData(3, 1);
        Log.d(LOG_TAG, sqlQuery);
        return mDB.rawQuery(sqlQuery, null);
    }


    //изменить запись в WALLET_TABLE
//    public void changeRec(int pos, boolean isChecked) {
//        ContentValues cv = new ContentValues();
////        cv.put(COLUMN_CHK, (isChecked) ? 1 : 0);
//        mDB.update(WALLET_TABLE, cv, WALLET_COLUMN_ID + " = " + (pos + 1), null);
//    }


    // Добавление записи об операции
    // operation_type,
    // currency_id, currency_id_dest, wallet_id, wallet_id_dest, category_id,
    // sum, sumDest, currentDate, comment
    public void addTransaction(int operation_type, int currency_id, int wallet_id, int category_id,
                               double sum, String currentDate, String comment) {
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
        cv.put(RECORD_COLUMN_COMMENT, comment);

        mDB.insert(RECORD_TABLE, null, cv);
    }


    // вывод в лог данных из курсора
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
    }


    // класс по созданию и управлению БД
    private class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
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
//                cv.put(WALLET_COLUMN_NAME, "Some text " + i);
                cv.put(WALLET_COLUMN_NAME, walletData[i]);
                cv.put(WALLET_COLUMN_IMAGE, imageWalletData[i]);
                db.insert(WALLET_TABLE, null, cv);
            }


            db.execSQL(SPENDING_TABLE_CREATE);
            cv.clear();
            for (int i = 0; i < spendingData.length; i++) {
                cv.put(SPENDING_COLUMN_ID, i);
                cv.put(SPENDING_COLUMN_NAME, spendingData[i]);
                db.insert(SPENDING_TABLE, null, cv);
            }


            db.execSQL(SOURCES_TABLE_CREATE);
            cv.clear();
            for (int i = 0; i < sourcesData.length; i++) {
                cv.put(SOURCES_COLUMN_ID, i);
                cv.put(SOURCES_COLUMN_NAME, sourcesData[i]);
                db.insert(SOURCES_TABLE, null, cv);
            }


            db.execSQL(RECORD_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}