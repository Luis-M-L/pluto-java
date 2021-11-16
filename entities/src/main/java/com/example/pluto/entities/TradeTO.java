package com.example.pluto.entities;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "PLUTO_TRADES")
public class TradeTO {

    @Id
    public Long id;

    private Long exchangeId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private Timestamp issuedTimestamp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private Timestamp effectiveTimestamp;

    private String pair;
    private BigDecimal price;
    private Double amount;
    private String status;

    public TradeTO() {
    }

    public TradeTO(String pair, BigDecimal price, Double amount) {
        this.issuedTimestamp = Timestamp.valueOf(Instant.now().toString());
        this.pair = pair;
        this.price = price;
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

    public Long getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(Long exchangeId) {
        this.exchangeId = exchangeId;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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
        return pair.equals(tradeTO.pair) && price.equals(tradeTO.price) && amount.equals(tradeTO.amount) && Objects.equals(status, tradeTO.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pair, price, amount, status);
    }

    @Override
    public String toString() {
        return "TradeTO{" +
                "id=" + id +
                ", issuedTimestamp=" + issuedTimestamp +
                ", effectiveTimestamp=" + effectiveTimestamp +
                ", pair='" + pair + '\'' +
                ", price=" + price +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                '}';
    }
}
