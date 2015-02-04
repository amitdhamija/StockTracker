package com.dhamija.amit.stocktracker.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.dhamija.amit.stocktracker.model.StockData;

import java.util.ArrayList;
import java.util.List;

public final class DBHelper {
    private static final String LOGTAG = "DBHelper";

	private static final String DATABASE_NAME			        = "stockdata.db";
    private static final int 	DATABASE_VERSION 		        = 1;
    
    public static final String COLUMN_NAME_ID			        = "_id"; // primary key for tables
    
    private static final String TABLE_STOCK_DATA		        = "stock_data";

    public static final String COLUMN_NAME_SYMBOL		        = "symbol";
    public static final String COLUMN_NAME_NAME			        = "name";
    public static final String COLUMN_NAME_LAST_TRADE_PRICE	    = "last_trade_price";
    public static final String COLUMN_NAME_DAYS_LOW_PRICE	    = "days_low_price";
    public static final String COLUMN_NAME_DAYS_HIGH_PRICE      = "days_high_price";
    public static final String COLUMN_NAME_CHANGE		        = "change";
    public static final String COLUMN_NAME_PERCENT_CHANGE	    = "percent_change";

    public static final int COLUMN_INDEX_ID				        = 0;
    public static final int COLUMN_INDEX_SYMBOL                 = 1;
    public static final int COLUMN_INDEX_NAME                   = 2;
    public static final int COLUMN_INDEX_LAST_TRADE_PRICE       = 3;
    public static final int COLUMN_INDEX_DAYS_LOW_PRICE         = 4;
    public static final int COLUMN_INDEX_DAYS_HIGH_PRICE        = 5;
    public static final int COLUMN_INDEX_CHANGE                 = 6;
    public static final int COLUMN_INDEX_PERCENT_CHANGE         = 7;


    public static final int UPDATE_BIND_INDEX_NAME              = 1;
    public static final int UPDATE_BIND_INDEX_LAST_TRADE_PRICE  = 2;
    public static final int UPDATE_BIND_INDEX_DAYS_LOW_PRICE    = 3;
    public static final int UPDATE_BIND_INDEX_DAYS_HIGH_PRICE   = 4;
    public static final int UPDATE_BIND_INDEX_CHANGE            = 5;
    public static final int UPDATE_BIND_INDEX_PERCENT_CHANGE    = 6;
    public static final int UPDATE_BIND_INDEX_SYMBOL            = 7; // symbol is the second column in table; but this index is to bind with update stmt

    
    private Context context;
    private SQLiteDatabase db;
    private SQLiteStatement insertStmtStockData;
    private SQLiteStatement updateStmtStockData;
    
    private OpenHelper openHelper;

    private static final String INSERT_STOCK_DATA =
            "INSERT INTO " + TABLE_STOCK_DATA + "(" +
                    COLUMN_NAME_SYMBOL + ", " +
                    COLUMN_NAME_NAME + ", " +
                    COLUMN_NAME_LAST_TRADE_PRICE + ", " +
                    COLUMN_NAME_DAYS_LOW_PRICE + ", " +
                    COLUMN_NAME_DAYS_HIGH_PRICE + ", " +
                    COLUMN_NAME_CHANGE + ", " +
                    COLUMN_NAME_PERCENT_CHANGE + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_STOCK_DATA =
            "UPDATE " + TABLE_STOCK_DATA + " SET " +
                COLUMN_NAME_NAME + "=?, " +
                COLUMN_NAME_LAST_TRADE_PRICE + "=?, " +
                COLUMN_NAME_DAYS_LOW_PRICE + "=?, " +
                COLUMN_NAME_DAYS_HIGH_PRICE + "=?, " +
                COLUMN_NAME_CHANGE + "=?, " +
                COLUMN_NAME_PERCENT_CHANGE + "=?" +
                " WHERE " + COLUMN_NAME_SYMBOL + "=?";


    public DBHelper(Context context) {
    	this.context = context;
        openHelper = new OpenHelper(this.context);
        db = openHelper.getWritableDatabase();
        insertStmtStockData = db.compileStatement(INSERT_STOCK_DATA);
        updateStmtStockData = db.compileStatement(UPDATE_STOCK_DATA);
    }
    
    public long insertStockData(StockData stockData) {
        insertStmtStockData.bindString(COLUMN_INDEX_SYMBOL,                 stockData.getSymbol());
        insertStmtStockData.bindString(COLUMN_INDEX_NAME,		            stockData.getName());
        insertStmtStockData.bindDouble(COLUMN_INDEX_LAST_TRADE_PRICE,	    stockData.getLastTradePrice());
        insertStmtStockData.bindDouble(COLUMN_INDEX_DAYS_LOW_PRICE,         stockData.getDaysLowPrice());
        insertStmtStockData.bindDouble(COLUMN_INDEX_DAYS_HIGH_PRICE,	    stockData.getDaysHighPrice());
        insertStmtStockData.bindDouble(COLUMN_INDEX_CHANGE,                 stockData.getChange());
        insertStmtStockData.bindString(COLUMN_INDEX_PERCENT_CHANGE,         stockData.getPercentChange());

        long value = insertStmtStockData.executeInsert();

        return value;
    }
    
    public int updateStockData(StockData stockData) {
        updateStmtStockData.bindString(UPDATE_BIND_INDEX_NAME,		        stockData.getName());
        updateStmtStockData.bindDouble(UPDATE_BIND_INDEX_LAST_TRADE_PRICE,	stockData.getLastTradePrice());
        updateStmtStockData.bindDouble(UPDATE_BIND_INDEX_DAYS_LOW_PRICE,	stockData.getDaysLowPrice());
        updateStmtStockData.bindDouble(UPDATE_BIND_INDEX_DAYS_HIGH_PRICE,	stockData.getDaysHighPrice());
        updateStmtStockData.bindDouble(UPDATE_BIND_INDEX_CHANGE,            stockData.getChange());
        updateStmtStockData.bindString(UPDATE_BIND_INDEX_PERCENT_CHANGE,    stockData.getPercentChange());
        updateStmtStockData.bindString(UPDATE_BIND_INDEX_SYMBOL,            stockData.getSymbol());

        int value = updateStmtStockData.executeUpdateDelete();

        return value;
    }

    public List<StockData> getStockDataList() {
        List<StockData> stockDataList = new ArrayList<>();
        Cursor cursor = db.query(TABLE_STOCK_DATA,
                new String[] { COLUMN_NAME_ID, COLUMN_NAME_SYMBOL, COLUMN_NAME_NAME, COLUMN_NAME_LAST_TRADE_PRICE, COLUMN_NAME_DAYS_LOW_PRICE, COLUMN_NAME_DAYS_HIGH_PRICE, COLUMN_NAME_CHANGE, COLUMN_NAME_PERCENT_CHANGE },
                null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                StockData stockData = new StockData();
                stockData.setSymbol(cursor.getString(COLUMN_INDEX_SYMBOL));
                stockData.setName(cursor.getString(DBHelper.COLUMN_INDEX_NAME));
                stockData.setLastTradePrice(cursor.getDouble(DBHelper.COLUMN_INDEX_LAST_TRADE_PRICE));
                stockData.setDaysLowPrice(cursor.getDouble(DBHelper.COLUMN_INDEX_DAYS_LOW_PRICE));
                stockData.setDaysHighPrice(cursor.getDouble(DBHelper.COLUMN_INDEX_DAYS_HIGH_PRICE));
                stockData.setChange(cursor.getDouble(DBHelper.COLUMN_INDEX_CHANGE));
                stockData.setPercentChange(cursor.getString(DBHelper.COLUMN_INDEX_PERCENT_CHANGE));

                stockDataList.add(stockData);
            }
            while (cursor.moveToNext());
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return stockDataList;
    }
    
    public void deleteTables() {
        db.delete(TABLE_STOCK_DATA, null, null);
    }
    
    //Releases the reference to db and closes the db object
    public void close() {
    	if ( db.isOpen() ) {
    		db.close();
    		openHelper.close();
    	}
    }

    private static class OpenHelper extends SQLiteOpenHelper {
        private static final String LOGTAG = OpenHelper.class.getSimpleName();
        
        private static final String CREATE_TABLE_STOCK_DATA =
        	"CREATE TABLE " +
        	TABLE_STOCK_DATA +
        	" (" + COLUMN_NAME_ID           + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        	COLUMN_NAME_SYMBOL              + " TEXT, " +
        	COLUMN_NAME_NAME 	            + " TEXT, " +
        	COLUMN_NAME_LAST_TRADE_PRICE 	+ " REAL, " +
        	COLUMN_NAME_DAYS_LOW_PRICE		+ " REAL, " +
        	COLUMN_NAME_DAYS_HIGH_PRICE 	+ " REAL, " +
            COLUMN_NAME_CHANGE 	            + " REAL, " +
        	COLUMN_NAME_PERCENT_CHANGE 	    + " TEXT" + ");";

        OpenHelper (Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate (SQLiteDatabase db) {
            Log.d(LOGTAG, "onCreate");
            db.execSQL(CREATE_TABLE_STOCK_DATA);
        }

        @Override
        public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("Example",
                    "Upgrading database, this will drop tables and recreate.");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCK_DATA);
            
            onCreate(db);
        }
    }
}