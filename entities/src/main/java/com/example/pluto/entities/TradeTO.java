package com.example.pluto.entities;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "PLUTO_TRADES")
public class TradeTO {

    @Id
    Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private Timestamp issuedTimestamp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private Timestamp effectiveTimestamp;

    private String pair;
    private Double amount;
    private String status;

    public TradeTO() {
    }

    public TradeTO(String pair, Double amount) {
        this.issuedTimestamp = Timestamp.valueOf(Instant.now().toString());
        this.pair = pair;
        this.amount = amount;
    }

    public TradeTO(Timestamp issuedTimestamp, Timestamp effectiveTimestamp, String pair, Double amount) {
        this.issuedTimestamp = issuedTimestamp;
        this.effectiveTimestamp = effectiveTimestamp;
        this.pair = pair;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timestamp getIssuedTimestamp() {
        return issuedTimestamp;
    }

    public void setIssuedTimestamp(Timestamp timestamp) {
        this.issuedTimestamp = timestamp;
    }

    public Timestamp getEffectiveTimestamp() {
        return effectiveTimestamp;
    }

    public void setEffectiveTimestamp(Timestamp effectiveTimestamp) {
        this.effectiveTimestamp = effectiveTimestamp;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeTO tradeTO = (TradeTO) o;
        return pair.equals(tradeTO.pair) && amount.equals(tradeTO.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pair, amount);
    }

    @Override
    public String toString() {
        return "TradeTO{" +
                "timestamp=" + issuedTimestamp +
                ", pair='" + pair + '\'' +
                ", amount=" + amount +
                '}';
    }
}
