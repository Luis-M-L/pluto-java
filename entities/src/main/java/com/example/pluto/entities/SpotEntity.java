package com.example.pluto.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "PLUTO_SPOTS")
@NamedQuery(name = "getAllLast", query = "select s from SpotEntity s where s.timestamp > :someago")
public class SpotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // TODO: convertir a objeto InstrumentTO
    private String instrument;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private Timestamp timestamp;
    private BigDecimal bid;
    private BigDecimal mid;
    private BigDecimal offer;
    private BigDecimal volume;

    public SpotEntity() {
    }

    public SpotEntity(String instrument, BigDecimal mid) {
        this.instrument = instrument;
        this.bid = mid;
        this.offer = mid;
        setMid();
    }

    public SpotEntity(String instrument, Timestamp timestamp, BigDecimal bid, BigDecimal offer, BigDecimal volume) {
        this.instrument = instrument;
        this.timestamp = timestamp;
        this.bid = bid;
        this.offer = offer;
        this.mid = (bid != null && offer != null) ? (bid.add(offer)).divide(BigDecimal.valueOf(2.0), RoundingMode.HALF_UP) : null;
        this.volume = volume;
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

    public BigDecimal getBid() {
        return bid;
    }

    public void setBid(BigDecimal bid) {
        this.bid = bid;
        setMid();
    }

    public BigDecimal getMid() {
        return mid;
    }

    public void setMid() {
        this.mid = (this.bid != null && this.offer != null) ? (this.bid.add(this.offer)).divide(BigDecimal.valueOf(2.0), RoundingMode.HALF_UP) : null;
    }

    public BigDecimal getOffer() {
        return offer;
    }

    public void setOffer(BigDecimal offer) {
        this.offer = offer;
        setMid();
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public String toJSON() throws JsonProcessingException {
        ObjectMapper mapperObj = new ObjectMapper();
        return mapperObj.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpotEntity spotEntity = (SpotEntity) o;
        return instrument.equals(spotEntity.instrument) && Objects.equals(bid, spotEntity.bid) && mid.equals(spotEntity.mid) && Objects.equals(offer, spotEntity.offer) && Objects.equals(volume, spotEntity.volume);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrument, bid, mid, offer, volume);
    }

    @Override
    public String toString() {
        return "SpotTO{" +
                "id=" + id +
                ", instrument=" + instrument +
                ", timestamp=" + timestamp +
                ", bid=" + bid +
                ", mid=" + mid +
                ", offer=" + offer +
                ", volume=" + volume +
                '}';
    }

}
