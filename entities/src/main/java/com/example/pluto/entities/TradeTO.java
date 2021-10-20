package com.example.pluto.entities;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "PLUTO_TRADES")
public class TradeTO {

    @Id
    Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private Timestamp timestamp;

    private String pair;

    private Double amount;
    public TradeTO() {
    }

    public TradeTO(Timestamp timestamp, String pair, Double amount) {
        this.timestamp = timestamp;
        this.pair = pair;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "TradeTO{" +
                "timestamp=" + timestamp +
                ", pair='" + pair + '\'' +
                ", amount=" + amount +
                '}';
    }
}
