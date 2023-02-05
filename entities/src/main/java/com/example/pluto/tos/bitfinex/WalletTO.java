package com.example.pluto.tos.bitfinex;

import com.example.pluto.entities.PositionTO;

import javax.json.JsonArray;
import java.math.BigDecimal;

public class WalletTO {

    public String WALLET_TYPE;
    public String CURRENCY;
    public BigDecimal BALANCE;
    public BigDecimal UNSETTLED_INTEREST;
    public BigDecimal AVAILABLE_BALANCE;
    public String LAST_CHANGE;
    public Object TRADE_DETAILS;

    public WalletTO(JsonArray j) {
        WALLET_TYPE = j.isNull(0) ? null : j.getString(0);
        CURRENCY = j.isNull(1) ? null : j.getString(1);
        BALANCE = j.isNull(2) ? null : j.getJsonNumber(2).bigDecimalValue();
        UNSETTLED_INTEREST = j.isNull(3) ? null : j.getJsonNumber(3).bigDecimalValue();
        AVAILABLE_BALANCE = j.isNull(4) ? null : j.getJsonNumber(4).bigDecimalValue();
        LAST_CHANGE = j.isNull(5) ? null : j.getString(5);
        TRADE_DETAILS = j.isNull(6) ? null : j.get(6);
    }

    public PositionTO toPositionTO() {
        PositionTO p = new PositionTO();
        p.setCurrency(CURRENCY);
        p.setQuantity(BALANCE);
        return p;
    }
}
