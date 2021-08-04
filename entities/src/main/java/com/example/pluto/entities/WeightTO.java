package com.example.pluto.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;

@Entity
@Table(name = "PLUTO_WEIGHTS")
public class WeightTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String instrument;
    private Double weight;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "BASKET_ID")
    @JsonBackReference
    private BasketTO basket;

    public WeightTO() {
    }

    public WeightTO(Long id, String instrument, Double weight, BasketTO basket) {
        this.id = id;
        this.instrument = instrument;
        this.weight = weight;
        this.basket = basket;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
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
                ", instrument=" + instrument +
                ", weight=" + weight +
                ", basket=" + basket +
                '}';
    }
}
