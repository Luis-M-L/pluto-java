package com.pluto.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "PLUTO_BASKETS")
public class BasketTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BASKET_ID")
    private Long id;

    @Column(unique = true)
    private String label;

    @OneToMany( mappedBy = "basket", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<WeightTO> weights;

    public BasketTO() {
    }

    public BasketTO(Long id, String label, List<WeightTO> weights) {
        this.id = id;
        this.label = label;
        weights.forEach(w -> w.setBasket(this));
        this.weights = weights;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<WeightTO> getWeights() {
        return weights;
    }

    public void setWeights(List<WeightTO> weights) {
        weights.forEach(w -> w.setBasket(this));
        this.weights = weights;
    }

    @Override
    public String toString() {
        return "InstrumentTO{" +
                "id=" + id +
                ", label='" + label + '\'' +
                " weights='" + weights + "'}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, weights);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasketTO basket = (BasketTO) o;
        return label.equals(basket.label) && weights.equals(basket.weights);
    }
}
