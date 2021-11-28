package com.example.pluto.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;

@Entity
@Table(name = "PLUTO_POSITIONS")
public class PositionTO {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "BASKET_ID")
    @JsonBackReference
    private BasketTO basket;

    private String currency;

    private Double quantity;

    public PositionTO() {
    }

    public PositionTO(Long id, BasketTO basket, String currency, Double quantity) {
        this.id = id;
        this.basket = basket;
        this.currency = currency;
        this.quantity = quantity;
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
}
