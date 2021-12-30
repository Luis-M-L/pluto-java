package com.example.pluto;

public final class PlutoConstants {

    public final static String HTTP_PREFIX = "http://";
    public final static String HEADER_NAME_CONTENT_TYPE = "Content-Type";
    public final static String HEADER_VALUE_APPLICATION_JSON = "application/json";

    public enum Socket {
        ORDENANZA("ordenanza:48557"),
        BITFINEX("bitfinex:48558");

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
        BITFINEX_SPOTS("/bitfinex/spots/"),
        BITFINEX_SPOT("/bitfinex/spot/"),
        BITFINEX_UPDATE("/bitfinex/update/"),
        POSITION_UPDATE("/position/update/"),
        POSITION_ALL("/position/all/"),
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
