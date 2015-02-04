package com.dhamija.amit.stocktracker.model;

public class StockData
{
    protected String symbol;
    protected String name;
    protected Double lastTradePrice;
    protected Double daysLowPrice;
    protected Double daysHighPrice;
    protected Double change;
    protected String percentChange;

    public String getSymbol() {
        return symbol;
    }
    public void setSymbol (String symbol) {
        this.symbol = symbol;
    }
    public String getName() {
        return name;
    }
    public void setName (String name) {
        this.name = name;
    }
    public Double getLastTradePrice() {
        return lastTradePrice;
    }
    public void setLastTradePrice (Double lastTradePrice) {
        this.lastTradePrice = lastTradePrice;
    }
    public Double getDaysHighPrice() { return daysHighPrice; }
    public void setDaysHighPrice(Double daysHighPrice) { this.daysHighPrice = daysHighPrice; }
    public Double getDaysLowPrice() { return daysLowPrice; }
    public void setDaysLowPrice(Double daysLowPrice) { this.daysLowPrice = daysLowPrice; }
    public Double getChange() {
        return change;
    }
    public void setChange (Double change) {
        this.change = change;
    }
    public String getPercentChange() {
        return percentChange;
    }
    public void setPercentChange (String percentChange) {
        this.percentChange = percentChange;
    }
}