package com.example.pluto.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "PLUTO_SPOTS")
public class SpotTO {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String instrument;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private Timestamp timestamp;
    private Double bid;
    private Double mid;
    private Double offer;
    private Double volume;

    public SpotTO() {
    }

    public SpotTO(Double bid, Double offer) {
        this.bid = bid;
        this.offer = offer;
        this.mid = (bid != null && offer != null) ? bid + offer / 2 : null;
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Double getBid() {
        return bid;
    }

    public void setBid(Double bid) {
        this.bid = bid;
        setMid();
    }

    public Double getMid() {
        return mid;
    }

    public void setMid() {
        this.mid = (this.bid != null && this.offer != null) ? (this.bid + this.offer) / 2.0 : null;
    }

    public Double getOffer() {
        return offer;
    }

    public void setOffer(Double offer) {
        this.offer = offer;
        setMid();
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public String toJSON() throws JsonProcessingException {
        ObjectMapper mapperObj = new ObjectMapper();
        return mapperObj.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }

    @Override
    public String toString() {
        return "SpotTO{" +
                "bid=" + bid +
                ", mid=" + mid +
                ", offer=" + offer +
                '}';
    }
}
