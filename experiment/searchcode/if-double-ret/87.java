package com.grgtvs.dailyxrates.db;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;

public class RateAlert implements Serializable {
    private static final long serialVersionUID = -8692455035968501199L;
    public int _id;
    private boolean enabled = true;
    public boolean isRepeated = false;

    private JSONObject params = new JSONObject();

    public RateAlert() {
    }

    public RateAlert(Cursor c) {
        this._id = c.getInt(c.getColumnIndex("_id"));
        this.setEnabled(c.getInt(c.getColumnIndex("enabled")) == 1);
        this.isRepeated = (c.getInt(c.getColumnIndex("is_repeated")) == 1);
        try {
            this.setParams(new JSONObject(c.getString(c
                    .getColumnIndex("alert_data"))));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public double getAlertOnMax() {
        double ret = 0.0;
        try {
            ret = params.getDouble("maxRate");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    public double getAlertOnMin() {
        double ret = 0.0;
        try {
            ret = params.getDouble("minValue");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    public Currency getBaseCurrency() {
        Currency c = null;
        try {
            c = new Currency(params.getString("baseCurrency"));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return c;
    }

    public Currency getCurrency() {
        Currency c = null;
        try {
            c = new Currency(params.getString("currency"));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return c;
    }

    public int getId() {
        return _id;
    }

    public double getRateReference() {
        double ret = 0.0;
        try {
            ret = params.getDouble("baseRate");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * @return the params
     */
    public JSONObject getRawParams() {
        return params;
    }

    public String getSummary() {
        String summary = "";
        try {
            if (params.has("maxRate")) {

                summary += "Max: " + getCurrency().getSymbol() + " "
                        + params.getString("maxRate") + " ";
            }
            if (params.has("minValue")) {
                summary += "Min: " + getCurrency().getSymbol() + " "
                        + params.getString("minValue") + " ";
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return summary;
    }

    public String getTitle() {
        String title = "";
        try {
            title = params.getString("currency") + " based on "
                    + params.getString("baseCurrency");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return title;
    }

    public boolean isEnabled() {
        try {
            this.params.getBoolean("enabled");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return enabled;
    }

    public RateAlert setAlertOnMax(double rate) {
        try {
            params.put("maxRate", rate);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return this;
    }

    public RateAlert setAlertOnMin(double rate) {
        try {
            params.put("minValue", rate);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return this;
    }

    public RateAlert setBaseCurrency(Currency currency) {
        return setBaseCurrency(currency.getCode());
    }

    public RateAlert setBaseCurrency(String currencyCode) {
        try {
            params.put("baseCurrency", currencyCode);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return this;
    }

    public RateAlert setCurrency(Currency currency) {
        return setCurrency(currency.getCode());
    }

    public RateAlert setCurrency(String currencyCode) {
        try {
            params.put("currency", currencyCode);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return this;
    }

    public RateAlert setEnabled(boolean enabled) {
        try {
            this.enabled = enabled;
            params.put("enabled", enabled);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return this;
    }

    public RateAlert setParams(JSONObject params) {
        this.params = params;
        return this;
    }

    public RateAlert setRateReference(double rate) {
        try {
            params.put("baseRate", rate);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return this;
    }

    public RateAlert setRateReference(Rate rate) {
        return setRateReference(rate.getCurrentRate());
    }

}

