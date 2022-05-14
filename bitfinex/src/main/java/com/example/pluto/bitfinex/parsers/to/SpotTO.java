package com.example.pluto.bitfinex.parsers.to;

import com.example.pluto.entities.SpotEntity;

import javax.json.JsonArray;
import java.math.BigDecimal;

public class SpotTO {

    public String SYMBOL;
    public BigDecimal BID;
    public BigDecimal BID_SIZE;
    public BigDecimal ASK;
    public BigDecimal ASK_SIZE;
    public BigDecimal DAILY_CHANGE;
    public BigDecimal DAILY_CHANGE_RELATIVE;
    public BigDecimal LAST_PRICE;
    public BigDecimal VOLUME;
    public BigDecimal HIGH;
    public BigDecimal LOW;

    public SpotTO(JsonArray j) {
        SYMBOL = j.getString(0);
        if (SYMBOL.startsWith("t")) {
            BID = j.getJsonNumber(1).bigDecimalValue();
            BID_SIZE = j.getJsonNumber(2).bigDecimalValue();
            ASK = j.getJsonNumber(3).bigDecimalValue();
            ASK_SIZE = j.getJsonNumber(4).bigDecimalValue();
            DAILY_CHANGE = j.getJsonNumber(5).bigDecimalValue();
            DAILY_CHANGE_RELATIVE = j.getJsonNumber(6).bigDecimalValue();
            LAST_PRICE = j.getJsonNumber(7).bigDecimalValue();
            VOLUME = j.getJsonNumber(8).bigDecimalValue();
            HIGH = j.getJsonNumber(9).bigDecimalValue();
            LOW = j.getJsonNumber(10).bigDecimalValue();
        }
    }

    public SpotEntity toSpot() {
        SpotEntity s = new SpotEntity();
        s.setInstrument(SYMBOL.substring(1));
        s.setBid(BID);
        s.setOffer(ASK);
        s.setVolume(VOLUME);
        return s;
    }
}
