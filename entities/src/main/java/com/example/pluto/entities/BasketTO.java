package com.example.pluto.entities;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "PLUTO_BASKETS")
public class BasketTO {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String label;

    @OneToMany( mappedBy = "basket", cascade = CascadeType.ALL)
    private List<WeightsTO> weights;

    public BasketTO() {
    }

    public BasketTO(Long id, String ticker) {
        this.id = id;
        this.label = ticker;
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

    @Override
    public String toString() {
        return "InstrumentTO{" +
                "id=" + id +
                ", label='" + label + '\'' +
                '}';
    }
}
