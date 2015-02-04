package com.dhamija.amit.stocktracker.net;

import android.content.Context;
import android.util.Log;

import com.dhamija.amit.stocktracker.db.DBHelper;
import com.dhamija.amit.stocktracker.model.StockData;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ServiceHandler {
    private static final String LOGTAG = "ServiceHandler";

    public static final int GET             = 1;
    public static final int POST            = 2;

    public static final String YQL_URL = "https://query.yahooapis.com/v1/public/yql?q=";
    public static final String YQL_STMT_STOCK_DATA = "select%20Symbol%2C%20Name%2C%20LastTradePriceOnly%2C%20DaysLow%2C%20DaysHigh%2C%20Change%2C%20PercentChange%2C%20ErrorIndicationreturnedforsymbolchangedinvalid%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(";
    public static final String YQL_STMT_STOCK_SYMBOL = "select%20Symbol%2C%20Name%2C%20ErrorIndicationreturnedforsymbolchangedinvalid%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(";
    public static final String YQL_NAME_VALUE_PAIRS = ")&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";

    public static final String YIMG_URL = "http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=";
    public static final String YIMG_NAME_VALUE_PAIRS = "&callback=YAHOO.Finance.SymbolSuggest.ssCallback";

    private static final String ERROR = "error";
    private static final String ERROR_NO_SUCH_SYMBOL = "ErrorIndicationreturnedforsymbolchangedinvalid";
    private static final String QUERY = "query";
    private static final String COUNT = "count";
    private static final String RESULTS = "results";
    private static final String QUOTE = "quote";
    private static final String SYMBOL = "Symbol";
    private static final String NAME = "Name";
    private static final String LAST_TRADE_PRICE = "LastTradePriceOnly";
    private static final String DAYS_LOW_PRICE = "DaysLow";
    private static final String DAYS_HIGH_PRICE = "DaysHigh";
    private static final String CHANGE = "Change";
    private static final String PERCENT_CHANGE = "PercentChange";

    private static final String RESULT_SET = "ResultSet";
    private static final String RESULT = "Result";

    private Context context;
    private DBHelper dbHelper;

    private List<StockData> stockDataList = new ArrayList<StockData>();
    private List<StockData> symbolsList = new ArrayList<StockData>();


    public ServiceHandler(Context context, DBHelper dbHelper) {
        this.context = context;
        this.dbHelper = dbHelper;
    }

    private void parseStockSymbol(JSONObject stockSymbolObject) {
        Log.d(LOGTAG, "ERROR node is null (symbol found?): " + stockSymbolObject.isNull(ERROR_NO_SUCH_SYMBOL));

        try {
            if (stockSymbolObject.isNull(ERROR_NO_SUCH_SYMBOL)) { // ERROR node is null (i.e. stock symbol was found)
                StockData stockData = new StockData();
                stockData.setSymbol(stockSymbolObject.getString(SYMBOL));
                stockData.setName(stockSymbolObject.getString(NAME));

                symbolsList.add(stockData);
            }
            else {
                // ERROR node is not null
                // stock symbol or company name was not found using yql service request
                // search using yimg service request
                String yimgResponse = makeRequest(ServiceHandler.YIMG_URL + stockSymbolObject.getString(SYMBOL) + ServiceHandler.YIMG_NAME_VALUE_PAIRS, ServiceHandler.GET, null);
                Log.d(LOGTAG, "yimgResponse: " + yimgResponse);

                if (yimgResponse != null) {
                    String yimgJson = yimgResponse.substring(yimgResponse.indexOf("(") + 1, yimgResponse.lastIndexOf(")"));

                    try {
                        JSONObject yimgJsonObject = new JSONObject(yimgJson);
                        JSONObject resultSet = yimgJsonObject.getJSONObject(RESULT_SET);
                        JSONArray resultArray = resultSet.getJSONArray(RESULT);

                        if (resultArray.length() > 0) { // result contains stock symbol(s)

                            String searchString = "''";
                            StringBuilder sb = new StringBuilder();

                            // create a search string from the list of symbols
                            for (int i = 0; i < resultArray.length(); i++) {
                                JSONObject stockNameObject = resultArray.getJSONObject(i);

                                sb.append("'");
                                String subString = stockNameObject.getString("symbol").replace("'", "\\'");
                                sb.append(subString);
                                sb.append("',");
                            }

                            sb.deleteCharAt(sb.length() -1 );
                            searchString = sb.toString();
                            Log.d(LOGTAG, "search string: "  + searchString);

                            // now send request to yql service using the new search string to get symbol data
                            String yqlResponse = makeRequest(YQL_URL + YQL_STMT_STOCK_SYMBOL + searchString + YQL_NAME_VALUE_PAIRS, GET, null);

                            if (yqlResponse != null) {
                                Log.d(LOGTAG, "yqlResponse: " + yqlResponse);

                                try {
                                    JSONObject yqlJsonObject = new JSONObject(yqlResponse);
                                    JSONObject query = yqlJsonObject.getJSONObject(QUERY);

                                    if (!query.isNull(RESULTS)) {

                                        if (query.getInt(COUNT) == 1) {
                                            JSONObject newStockSymbolObject = query.getJSONObject(RESULTS).getJSONObject(QUOTE);
                                            if (newStockSymbolObject.isNull(ERROR_NO_SUCH_SYMBOL)) { // ERROR node is null (i.e. stock symbol was found)
                                                StockData stockData = new StockData();
                                                stockData.setSymbol(newStockSymbolObject.getString(SYMBOL));
                                                stockData.setName(newStockSymbolObject.getString(NAME));

                                                symbolsList.add(stockData);
                                            }

                                        } else {
                                            JSONArray newStockSymbolArray = query.getJSONObject(RESULTS).getJSONArray(QUOTE);

                                            for (int i = 0; i < newStockSymbolArray.length(); i++) {
                                                JSONObject newStockSymbolObject = newStockSymbolArray.getJSONObject(i);
                                                if (newStockSymbolObject.isNull(ERROR_NO_SUCH_SYMBOL)) { // ERROR node is null (i.e. stock symbol was found)
                                                    StockData stockData = new StockData();
                                                    stockData.setSymbol(newStockSymbolObject.getString(SYMBOL));
                                                    stockData.setName(newStockSymbolObject.getString(NAME));

                                                    symbolsList.add(stockData);
                                                }
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseStockData(JSONObject stockDataObject) {
        try {
            if (stockDataObject.isNull(ERROR_NO_SUCH_SYMBOL)) { // ERROR node is null (i.e. stock symbol was found)

                StockData stockData = new StockData();
                stockData.setSymbol(stockDataObject.getString(SYMBOL));
                stockData.setName(stockDataObject.getString(NAME));
                stockData.setLastTradePrice(stockDataObject.getDouble(LAST_TRADE_PRICE));
                stockData.setChange(stockDataObject.getDouble(CHANGE));
                stockData.setPercentChange(stockDataObject.getString(PERCENT_CHANGE));

                if(!stockDataObject.isNull(DAYS_LOW_PRICE)) {
                    stockData.setDaysLowPrice(stockDataObject.getDouble(DAYS_LOW_PRICE));
                }
                else {
                    stockData.setDaysLowPrice(-1.00);
                }

                if(!stockDataObject.isNull(DAYS_HIGH_PRICE)) {
                    stockData.setDaysHighPrice(stockDataObject.getDouble(DAYS_HIGH_PRICE));
                }
                else {
                    stockData.setDaysHighPrice(-1.00);
                }

                // store data in db and add to the list
                storeStockData(stockData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void storeStockData(StockData stockData) {
        // if row gets updated (stock data already exists)
        if(dbHelper.updateStockData(stockData) != 1) {
            dbHelper.insertStockData(stockData);
        }

        stockDataList.add(stockData);
    }

    /**
     * Make service request
     * @url - url to make request
     * @method - http request method
     * */
    public String makeRequest(String url, int method) {
        return this.makeRequest(url, method, null);
    }

    /**
     * Make service request
     * @url - url to make request
     * @method - http request method
     * @params - http request params
     * */
    public String makeRequest(String url, int method, List<NameValuePair> params) {
        String response = null;
        try {
            // http client
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;

            // checking http request method type
            if (method == POST) {
                HttpPost httpPost = new HttpPost(url);
                // adding post params
                if (params != null) {
                    httpPost.setEntity(new UrlEncodedFormEntity(params));
                }

                httpResponse = httpClient.execute(httpPost);

            } else if (method == GET) {
                // appending params to url
                if (params != null) {
                    String paramString = URLEncodedUtils
                            .format(params, "utf-8");
                    url += "?" + paramString;
                }
                HttpGet httpGet = new HttpGet(url);

                httpResponse = httpClient.execute(httpGet);

            }
            httpEntity = httpResponse.getEntity();
            response = EntityUtils.toString(httpEntity);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }


    public List<StockData> getSymbolsList(String searchQuery) {
        String response = makeRequest(YQL_URL + YQL_STMT_STOCK_DATA +
                searchQuery +
                YQL_NAME_VALUE_PAIRS, GET);
        Log.d(LOGTAG, "getSymbolsList Response: " + response);

        if (response != null) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                // TODO: check for jsonObject.isNull(ERROR)
                JSONObject query = jsonObject.getJSONObject(QUERY);

                if (!query.isNull(RESULTS)) {
                    // clear existing list now that new data is available
                    symbolsList.clear();

                    if (query.getInt(COUNT) == 1) {
                        JSONObject stockSymbolObject = query.getJSONObject(RESULTS).getJSONObject(QUOTE);
                        parseStockSymbol(stockSymbolObject);

                    } else {
                        JSONArray stockDataArray = query.getJSONObject(RESULTS).getJSONArray(QUOTE);

                        for (int i = 0; i < stockDataArray.length(); i++) {
                            JSONObject stockSymbolObject = stockDataArray.getJSONObject(i);
                            parseStockSymbol(stockSymbolObject);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return symbolsList;
    }

    public List<StockData> getStockDataList(String searchQuery) {
        String response = makeRequest(YQL_URL + YQL_STMT_STOCK_DATA +
                searchQuery +
                YQL_NAME_VALUE_PAIRS, GET);
        Log.d(LOGTAG, "getStockDataList Response: " + response);

        if (response != null) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                // TODO: check for jsonObject.isNull(ERROR)
                JSONObject query = jsonObject.getJSONObject(QUERY);

                if (!query.isNull(RESULTS)) {
                    // clear existing list now that new data is available
                    stockDataList.clear();

                    if (query.getInt(COUNT) == 1) {
                        JSONObject stockDataObject = query.getJSONObject(RESULTS).getJSONObject(QUOTE);
                        parseStockData(stockDataObject);

                    } else {
                        JSONArray stockDataArray = query.getJSONObject(RESULTS).getJSONArray(QUOTE);

                        for (int i = 0; i < stockDataArray.length(); i++) {
                            JSONObject stockDataObject = stockDataArray.getJSONObject(i);
                            parseStockData(stockDataObject);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return stockDataList;
    }
}