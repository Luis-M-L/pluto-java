package com.pluto.entities;

import javax.persistence.*;

@Entity
@Table(name = "PLUTO_INSTRUMENTS")
public class InstrumentTO {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String ticker;

    public InstrumentTO() {
    }

    public InstrumentTO(Long id, String ticker) {
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
        return "InstrumentTO{" +
                "id=" + id +
                ", ticker='" + ticker + '\'' +
                '}';
    }
}
