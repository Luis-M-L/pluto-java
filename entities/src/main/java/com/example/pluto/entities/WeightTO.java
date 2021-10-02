package com.example.pluto.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;

@Entity
@Table(name = "PLUTO_WEIGHTS")
public class WeightTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String currency;
    private Double weight;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "BASKET_ID")
    @JsonBackReference
    private BasketTO basket;

    public WeightTO() {
    }

    public WeightTO(Long id, String currency, Double weight, BasketTO basket) {
        this.id = id;
        this.currency = currency;
        this.weight = weight;
        this.basket = basket;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public BasketTO getBasket() {
        return basket;
    }

    public void setBasket(BasketTO basket) {
        this.basket = basket;
    }

    @Override
    public String toString() {
        return "WeightsTO{" +
                "id=" + id +
                ", instrument=" + currency +
                ", weight=" + weight +
                ", basket=" + basket.getLabel() +
                '}';
    }

    @Override
    public boolean equals(Object comparing){
        if (comparing.getClass() != WeightTO.class) {
            return false;
        }
        WeightTO wComparing = (WeightTO) comparing;
        return this.equals(wComparing);
    }

    public boolean equals(WeightTO comp) {
        if (comp.getCurrency() == null || comp.getWeight() == null || this.getCurrency() == null || this.getWeight() == null) {
            return false;
        }
        return comp.getCurrency().equals(this.getCurrency()) && comp.getWeight().equals(this.getWeight());
    }
}
