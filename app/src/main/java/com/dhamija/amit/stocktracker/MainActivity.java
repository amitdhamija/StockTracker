package com.dhamija.amit.stocktracker;

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.dhamija.amit.stocktracker.db.DBHelper;
import com.dhamija.amit.stocktracker.model.StockData;
import com.dhamija.amit.stocktracker.net.ServiceHandler;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends ListActivity {

    private static final String LOGTAG = "MainActivity";

    private Activity activity = this;
    private Context context = this;
    private DBHelper dbHelper = null;

    private ViewSwitcher viewSwitcher;

    private TextView symbol;
    private TextView name;
    private TextView lastTradePrice;
    private TextView daysLowPrice;
    private TextView daysHighPrice;
    private TextView change;
    private TextView percentChange;
    private Animation slideInLeft, slideOutRight;
    private MenuItem refresh;
    private ProgressBar progress;

    private int selectedStockPosition = 0;
    private boolean addNewStock = false;
    private String newStockSymbol = "";
    private String searchQuery = "''";

    private NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private List<StockData> stockDataList = new ArrayList<StockData>();


    private String createSearchQuery(List<StockData> list) {
        String searchString = "''";

        if (!list.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (StockData stockData : list) {
                sb.append("'");
                String subString = stockData.getSymbol().replace("'", "\\'");

                sb.append(subString);
                sb.append("',");
            }

            sb.deleteCharAt(sb.length() -1 );
            searchString = sb.toString();
        }

        return searchString;
    }

    private int getColor(double value) {
        int[] colors = {Color.MAGENTA, Color.GREEN, Color.RED};
        int colorId;

        if (value > 0.00) {
            colorId = colors[1];
        } else if (value < 0.00) {
            colorId = colors[2];
        } else {
            colorId = colors[0];
        }

        return colorId;
    }

    private void refreshStockDetailView() {
        StockData stockData = (StockData) getListView().getItemAtPosition(selectedStockPosition);
        double changeValue = stockData.getChange();
        double daysLowValue = stockData.getDaysLowPrice();
        double daysHighValue = stockData.getDaysHighPrice();

        symbol.setText(stockData.getSymbol());
        name.setText(stockData.getName());
        lastTradePrice.setText(numberFormat.format(stockData.getLastTradePrice()));
        change.setTextColor(getColor(changeValue));
        change.setText(String.valueOf(changeValue));
        percentChange.setTextColor(getColor(changeValue));
        percentChange.setText(stockData.getPercentChange());

        if (daysLowValue > -1.00) {
            daysLowPrice.setText(numberFormat.format(stockData.getDaysLowPrice()));
        }
        else {
            daysLowPrice.setText("");
        }

        if (daysHighValue > -1.00) {
            daysHighPrice.setText(numberFormat.format(stockData.getDaysHighPrice()));
        }
        else {
            daysHighPrice.setText("");
        }
    }

    public void onListItemClick(ListView listView, View view, int position, long id) {
        selectedStockPosition = position;
        refreshStockDetailView();
        viewSwitcher.showNext();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            addNewStock = false;
            new FetchStockData().execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(viewSwitcher.getDisplayedChild() == 1) {
            viewSwitcher.showPrevious();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d (LOGTAG, "onCreate()");
        setContentView(R.layout.activity_main);

        slideInLeft = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        slideOutRight = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right);

        viewSwitcher = (ViewSwitcher) findViewById(R.id.view_switcher);

        symbol = (TextView) findViewById(R.id.symbol);
        name = (TextView) findViewById(R.id.name);
        lastTradePrice = (TextView) findViewById(R.id.last_trade_price);
        change = (TextView) findViewById(R.id.change);
        percentChange = (TextView) findViewById(R.id.percent_change);
        daysLowPrice = (TextView) findViewById(R.id.days_low_price);
        daysHighPrice = (TextView) findViewById(R.id.days_high_price);

        progress = (ProgressBar) findViewById(R.id.progress);

        viewSwitcher.setInAnimation(slideInLeft);
        viewSwitcher.setOutAnimation(slideOutRight);

        if (dbHelper == null)
            dbHelper = new DBHelper(context);

        String symbolToAdd = getIntent().getStringExtra("symbol");

        if (symbolToAdd != null) {
            addNewStock = true;
            newStockSymbol = getIntent().getStringExtra("symbol");
        }

        // Calling async task to get updated stock data
        new FetchStockData().execute();
    }

    @Override
    public void onRestart() {
        // Called after onPause() as Activity is brought
        // Called after onStop() but process has not been killed
        Log.d (LOGTAG, "onRestart()");
        super.onRestart();
    }

    @Override
    public void onStart() {
        // Called after onCreate() OR onRestart()
        Log.d (LOGTAG, "onStart()");
        super.onStart();
    }

    @Override
    public void onResume() {
        // Called after onStart() as Activity comes to foreground
        Log.d (LOGTAG, "onResume()");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d (LOGTAG, "onCreateOptionsMenu()");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        refresh = menu.findItem(R.id.action_refresh);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onPause() {
        // Called when Activity is placed in background
        Log.d (LOGTAG, "onPause()");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d (LOGTAG, "onStop()");
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Called before an Activity is killed
        Log.d (LOGTAG, "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        Log.d (LOGTAG, "onDestroy()");
        if (dbHelper != null)
            dbHelper.close(); //Performance feature: close db connections on destroy
        super.onDestroy();
    }


    /**
     * Async task class to get Stock Data by making HTTP call
     * */
    private class FetchStockData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            ServiceHandler serviceHandler = new ServiceHandler(context, dbHelper);
            stockDataList = dbHelper.getStockDataList();

            if (addNewStock) {
                Log.d(LOGTAG, "addNewStock: " + addNewStock);
                StockData newStockData = new StockData();
                newStockData.setSymbol(newStockSymbol);
                stockDataList.add(newStockData);
            }

            searchQuery = createSearchQuery(stockDataList);
            List<StockData> newStockDataList = serviceHandler.getStockDataList(searchQuery);

            if (!newStockDataList.isEmpty()) {
                stockDataList = newStockDataList;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progress.setVisibility(View.GONE);

            setListAdapter(new StockDataArrayAdapter(activity, stockDataList));

            if (!stockDataList.isEmpty())
                refreshStockDetailView();
        }
    }


    public class StockDataArrayAdapter extends ArrayAdapter<StockData> {
        private Activity activityContext;
        private List<StockData> list;

        class ViewHolder {
            public TextSwitcher symbol;
            public TextSwitcher name;
            public TextSwitcher price;
        }

        class TextViewFactory implements ViewSwitcher.ViewFactory {
            private int color;
            private int gravity;
            private int size;
            private Typeface typeface;

            public TextViewFactory(int gravity, int size, int color, Typeface typeface) {
                this.gravity = gravity;
                this.size = size;
                this.color = color;
                this.typeface = typeface;
            }

            @Override
            public View makeView() {
                TextView textView = new TextView(MainActivity.this);
                textView.setGravity(gravity);
                textView.setTextSize(size);
                textView.setTextColor(color);
                textView.setTypeface(typeface);
                textView.setShadowLayer(1, 1, 1, Color.BLACK);
                return textView;
            }
        }

        public StockDataArrayAdapter(Activity context, List<StockData> list) {
            super(context, R.layout.list_item, list);
            this.activityContext = context;
            this.list = list;
        }

        public void setList(List<StockData> list) {
            this.list = list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            StockData stockData = list.get(position);
            double change = stockData.getChange();

            // reuse views
            if (rowView == null) {
                LayoutInflater inflater = activityContext.getLayoutInflater();
                rowView = inflater.inflate(R.layout.list_item, null);
                // configure view holder
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.symbol = (TextSwitcher) rowView.findViewById(R.id.symbol);
                viewHolder.name = (TextSwitcher) rowView.findViewById(R.id.name);
                viewHolder.price = (TextSwitcher) rowView.findViewById(R.id.price);

                viewHolder.price.setInAnimation(slideInLeft);
                viewHolder.price.setOutAnimation(slideOutRight);

                viewHolder.symbol.setFactory(new TextViewFactory(Gravity.LEFT, 14, Color.WHITE, Typeface.DEFAULT_BOLD));
                viewHolder.name.setFactory(new TextViewFactory(Gravity.LEFT, 12, Color.GRAY, Typeface.DEFAULT));
                viewHolder.price.setFactory(new TextViewFactory(Gravity.RIGHT, 12, getColor(change), Typeface.DEFAULT_BOLD));

                rowView.setTag(viewHolder);
            }

            // fill data
            ViewHolder holder = (ViewHolder) rowView.getTag();

            holder.symbol.setText(stockData.getSymbol());
            holder.name.setText(stockData.getName());
            holder.price.setText(NumberFormat.getCurrencyInstance(Locale.US).format(stockData.getLastTradePrice()));

            return rowView;
        }
    }
}