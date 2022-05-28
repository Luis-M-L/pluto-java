package com.example.pluto.bitfinex.parsers.to;

import com.example.pluto.bitfinex.parsers.BitfinexParser;
import com.example.pluto.entities.SpotEntity;

import javax.json.JsonArray;
import java.math.BigDecimal;
import java.sql.Timestamp;

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
    public Timestamp TIMESTAMP;

    public SpotTO(JsonArray j) {
        SYMBOL = j.getString(0);
        if (SYMBOL.startsWith("t")) {
            BID = BitfinexParser.getBigDecimalFromJsonArray(j, 1);
            BID_SIZE = BitfinexParser.getBigDecimalFromJsonArray(j, 2);
            ASK = BitfinexParser.getBigDecimalFromJsonArray(j, 3);
            ASK_SIZE = BitfinexParser.getBigDecimalFromJsonArray(j, 4);
            DAILY_CHANGE = BitfinexParser.getBigDecimalFromJsonArray(j, 5);
            DAILY_CHANGE_RELATIVE = BitfinexParser.getBigDecimalFromJsonArray(j, 6);
            LAST_PRICE = BitfinexParser.getBigDecimalFromJsonArray(j, 7);
            VOLUME = BitfinexParser.getBigDecimalFromJsonArray(j, 8);
            HIGH = BitfinexParser.getBigDecimalFromJsonArray(j, 9);
            LOW = BitfinexParser.getBigDecimalFromJsonArray(j, 10);
            if (j.size() >= 12 && !j.isNull(12)) {
                TIMESTAMP = new Timestamp(j.getJsonNumber(12).longValue());
            }
        }
    }

    public SpotEntity toSpot() {
        SpotEntity s = new SpotEntity();
        s.setInstrument(SYMBOL.substring(1));
        s.setBid(BID);
        s.setOffer(ASK);
        s.setVolume(VOLUME);
        s.setTimestamp(TIMESTAMP);
        return s;
    }
}
