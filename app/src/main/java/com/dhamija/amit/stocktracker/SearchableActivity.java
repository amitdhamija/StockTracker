package com.dhamija.amit.stocktracker;

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dhamija.amit.stocktracker.db.DBHelper;
import com.dhamija.amit.stocktracker.model.StockData;
import com.dhamija.amit.stocktracker.net.ServiceHandler;

import java.util.ArrayList;
import java.util.List;


public class SearchableActivity extends ListActivity {

    private static final String LOGTAG = "SearchableActivity";

    private Activity activity = this;
    private Context context = this;
    private DBHelper dbHelper = null;

    private ProgressBar progress;

    private String searchQuery = "''";
    private List<StockData> symbolsList = new ArrayList<StockData>();


    private String createSearchQuery(String query) {
        String searchString = "''";

        if (!query.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("'");
            sb.append(query.replace(",", "','").replace(" ", ""));
            sb.append("'");

            searchString = sb.toString();
        }

        return searchString;
    }

    private void handleIntent(Intent intent) {
        Log.d (LOGTAG, "handleIntent()");
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            searchQuery = createSearchQuery(query);
            Log.d(LOGTAG, searchQuery);

            new SearchStock().execute();
        }
    }

    public void onListItemClick(ListView listView, View view, int position, long id) {
        StockData stockData = (StockData) listView.getItemAtPosition(position);
        String symbol = stockData.getSymbol();
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("symbol", symbol);

        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d (LOGTAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        progress = (ProgressBar) findViewById(R.id.progress);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        handleIntent(getIntent());
    }

    @Override
    public void onRestart() {
        // Called after onPause() as Activity is brought
        // Called after onStop() but process has not been killed
        Log.d(LOGTAG, "onRestart()");
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
    public void onPause() {
        // Called when Activity is placed in background
        Log.d (LOGTAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d (LOGTAG, "onNewIntent()");
        setIntent(intent);
        handleIntent(intent);
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
        super.onDestroy();
    }

    /**
     * Async task class to get Stock Symbol by making HTTP call
     * */
    private class SearchStock extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d (LOGTAG, "onPreExecute()");
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            ServiceHandler serviceHandler = new ServiceHandler(context, dbHelper);
            symbolsList = serviceHandler.getSymbolsList(searchQuery);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.d(LOGTAG, "onPostExecute()");
            progress.setVisibility(View.GONE);

            setListAdapter(new SymbolsArrayAdapter(activity, symbolsList));
        }
    }


    public class SymbolsArrayAdapter extends ArrayAdapter<StockData> {
        private Activity activityContext;
        private List<StockData> list;

        class ViewHolder {
            public TextView symbol;
            public TextView name;
        }

        public SymbolsArrayAdapter(Activity context, List<StockData> list) {
            super(context, R.layout.list_item_search, list);
            this.activityContext = context;
            this.list = list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            // reuse views
            if (rowView == null) {
                LayoutInflater inflater = activityContext.getLayoutInflater();
                rowView = inflater.inflate(R.layout.list_item_search, null);
                // configure view holder
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.symbol = (TextView) rowView.findViewById(R.id.symbol);
                viewHolder.name = (TextView) rowView.findViewById(R.id.name);
                rowView.setTag(viewHolder);
            }

            // fill data
            ViewHolder holder = (ViewHolder) rowView.getTag();
            StockData stockData = list.get(position);
            holder.symbol.setText(stockData.getSymbol());
            holder.name.setText(stockData.getName());

            return rowView;
        }
    }
}