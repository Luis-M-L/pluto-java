package com.example.pluto.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
@Table(name = "PLUTO_TRADES")
public class TradeTO {

    public static final String ACTIVE_STATUS = "ACTIVE";
    public static final String CLOSED_STATUS = "EXECUTED";
    public static final String CANCELLED_STATUS = "CANCELLED";

    @Id
    @GeneratedValue
    public Long id;

    private Long exchangeId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private Timestamp issuedTimestamp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private Timestamp effectiveTimestamp;

    private String pair;
    private BigDecimal price;
    private BigDecimal amount;
    private String status;

    public TradeTO() {
    }

    public TradeTO(String pair, BigDecimal amount) {
        this.issuedTimestamp = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        this.pair = pair;
        this.amount = amount;
    }

    public TradeTO(String pair, BigDecimal price, BigDecimal amount) {
        this.issuedTimestamp = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        this.pair = pair;
        this.price = price;
        this.amount = amount;
    }

    public TradeTO(Timestamp issuedTimestamp, Timestamp effectiveTimestamp, String pair, BigDecimal amount) {
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonIgnore
    public String getBase() {
        return pair.substring(0, 3);
    }

    @JsonIgnore
    public String getQuoted() {
        return pair.substring(3, 6);
    }

    public boolean looksAlike(TradeTO comp, double threshold) {
        return this.pair.equals(comp.getPair())
                && Math.abs(this.price.doubleValue() - comp.getPrice().doubleValue()) < threshold
                && Math.abs(this.amount.doubleValue() - comp.getAmount().doubleValue()) < threshold;
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
