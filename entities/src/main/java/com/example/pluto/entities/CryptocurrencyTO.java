package com.example.pluto.entities;

import javax.persistence.*;

@Entity
@Table(name = "PLUTO_CRYTPOCURRENCIES")
public class CryptocurrencyTO {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String ticker;

    public CryptocurrencyTO() {
    }

    public CryptocurrencyTO(Long id, String ticker) {
        this.id = id;
        this.ticker = ticker;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    @Override
    public String toString() {
        return "CryptocurrencyTO{" +
                "id=" + id +
                ", ticker='" + ticker + '\'' +
                '}';
    }
}
