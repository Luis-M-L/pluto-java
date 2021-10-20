package com.example.pluto.bitfinex.authservices;

import com.example.pluto.bitfinex.BitfinexAPIClient;
import com.example.pluto.entities.TradeTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
public class BitfinexAuthService {

    @Autowired
    public BitfinexAPIClient client;

    public boolean trade(List<TradeTO> defTrades) {
        return false;
    }

    public String getUserInfo() {
        String res = "";
        try {
            res = client.authPost(Arrays.asList("v2", "auth", "r", "info", "user"), new HashMap<>(), "");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

}
