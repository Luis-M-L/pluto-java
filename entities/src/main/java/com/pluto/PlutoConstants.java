package com.pluto;

import java.util.HashMap;
import java.util.Map;

public final class PlutoConstants {

    public static final String HTTP_PREFIX = "http://";
    public static final String HEADER_NAME_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_VALUE_APPLICATION_JSON = "application/json";

    public static final Map<String, Double> minAmount = initializeMinAmount();

    private static Map<String, Double> initializeMinAmount() {
        Map<String, Double> ma = new HashMap<>(1);
        ma.put("IOT", 4.0);
        ma.put("XMR", 0.02);
        ma.put("BTC", 0.0);
        return ma;
    }

    public enum OrderType {
        EXCHANGE_LIMIT("EXCHANGE LIMIT"),
        EXCHANGE_MARKET("EXCHANGE MARKET");

        private String e;

        OrderType(String e) {
            this.e = e;
        }

        public String value() {
            return e;
        }
    }

    public enum Socket {
        ORDENANZA("localhost:48557"),
        BITFINEX("localhost:48558");

        private String s;

        Socket(String s) {
            this.s = s;
        }

        public String value() {
            return s;
        }
    }

    public enum Path {
        BASKET_ALL("/basket/all/"),
        BITFINEX_TRADE("/bitfinex/trade/"),
        BITFINEX_POSTTRADE("/bitfinex/posttrade/"),
        BITFINEX_SPOTS_HIST("/bitfinex/spotsHist/"),
        BITFINEX_SPOTS("/bitfinex/spots/"),
        BITFINEX_SPOT("/bitfinex/spot/"),
        BITFINEX_UNACTIVE("/bitfinex/trade/unactive/"),
        POSITION_UPDATE("/position/update/"),
        POSITION_ALL("/position/all/"),
        POSITION_LAST("/position/last/"),
        INSTRUMENTS("/instruments/");

        private String s;

        Path(String s) {
            this.s = s;
        }

        public String value() {
            return s;
        }
    }


}
