package com.example.pluto.entities;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "PLUTO_POSITIONS")
public class PositionTO {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "BASKET_ID")
    private BasketTO basket;

    private String currency;

    private Double quantity;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private Timestamp timestamp;

    public PositionTO() {
        timestamp = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS));
    }

    public PositionTO(Long id, BasketTO basket, String currency, Double quantity) {
        this.id = id;
        this.basket = basket;
        this.currency = currency;
        this.quantity = quantity;
        timestamp = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BasketTO getBasket() {
        return basket;
    }

    public void setBasket(BasketTO basket) {
        this.basket = basket;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PositionTO)) {
            return false;
        }
        PositionTO comp = (PositionTO) obj;
        if (this.getCurrency() == null || comp.getCurrency() == null
            || this.getQuantity() == null || comp.getQuantity() == null
            || this.getBasket() == null || comp.getBasket() == null) {
            return false;
        }
        return this.getCurrency().equals(comp.getCurrency())
                && this.getQuantity().equals(comp.getQuantity())
                && this.getBasket().equals(comp.getBasket());
    }

    @Override
    public String toString() {
        return "PositionTO{" +
                "id=" + id +
                ", basket=" + basket +
                ", currency='" + currency + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
