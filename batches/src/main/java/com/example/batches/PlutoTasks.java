package com.example.batches;

import static com.example.pluto.PlutoConstants.HTTP_PREFIX;

public class PlutoTasks {

    protected static String buildUrl(String... split) {
        StringBuilder sb = new StringBuilder(HTTP_PREFIX);
        for (int i = 0; i <= split.length - 1; i++) {
            sb.append(split[i]);
        }
        return sb.toString();
    }

}
