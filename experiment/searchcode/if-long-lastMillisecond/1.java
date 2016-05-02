package org.wigor.trendbar;

import org.joda.time.DateTime;
import org.wigor.trendbar.objects.Quote;
import org.wigor.trendbar.objects.TrendBar;
import org.wigor.trendbar.storage.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Quotes generator.
 */
public class QuotesProvider {

    private TrendBar.Type type;
    private String symbol;
    private Double initialPrice;
    private Double price;
    private Double std;
    private long lastMillisecond;
    private Random random = new Random();

    private TrendBar currentTrendBar;

    private List<TrendBar> localStorage = new ArrayList<>();

    /**
     * Create generator
     *
     * @param type - trend bar type
     * @param symbol - trend bar symbol
     * @param initPrice - init quote price
     * @param initDate - init quote date
     * @param std - standard deviation for price change step
     */
    public QuotesProvider(TrendBar.Type type, String symbol, Double initPrice, DateTime initDate, Double std) {
        this.type = type;
        this.symbol = symbol;
        this.price = initPrice;
        this.initialPrice = initPrice;
        this.std = std;
        this.lastMillisecond = initDate.getMillis();
    }

    public Quote nextQuote() {
        price += (random.nextGaussian() + 0.5 - (1/(1 + Math.exp(initialPrice - price)))) * std;
        lastMillisecond += 1 + random.nextInt(3);
        Quote quote = new Quote(symbol, price, new DateTime(lastMillisecond));
        if (currentTrendBar == null) {
            currentTrendBar = Util.initializeTrendBar(quote, type);
        } else {
            if (quote.getDate().isBefore(currentTrendBar.getEndDate())) {
                currentTrendBar.updateWithQuote(quote);
            } else {
                localStorage.add(currentTrendBar);
                currentTrendBar = Util.initializeTrendBar(quote, type);
            }
        }
        return quote;
    }

    public List<TrendBar> getLocalStorage() {
        return localStorage;
    }
}

