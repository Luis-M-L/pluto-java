package com.example.pluto.controllers;

import com.example.pluto.entities.BookTO;
import com.example.pluto.entities.SpotEntity;
import com.example.pluto.entities.TradeTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

public interface IExchangeController {

    @GetMapping(value = "spotsHist/{instruments}")
    List<SpotEntity> getSpotsHist(@PathVariable String instruments);

    @GetMapping(value = "spots/{instruments}/{start}/{end}")
    List<SpotEntity> getSpots(@PathVariable("instruments") String instruments, @PathVariable("start") Long start, @PathVariable("end") Long end);

    @GetMapping(value = "spots/{instruments}")
    Map<String, SpotEntity> getSpots(@PathVariable String instruments);

    @GetMapping(value = "spot/{instrument}")
    SpotEntity getSpot(@PathVariable String instrument, @RequestParam(required = false) String time);

    @PostMapping(value = "spot/{instrument}")
    boolean saveSpot(@PathVariable String instrument, @RequestBody(required = false) SpotEntity spot);

    @GetMapping(value = "volume/{instrument}")
    int getVolume(@PathVariable String instrument, @RequestParam(required = false) String time);

    @GetMapping(value = "book/{instrument}")
    BookTO getBook(@PathVariable String instrument, @RequestParam(required = false) String time);

    @PostMapping(value = "trade")
    List<TradeTO> trade(@RequestBody List<TradeTO> defTrades);

    @PostMapping(value = "posttrade")
    List<TradeTO> updateChangedTrades(@RequestBody(required = false) List<TradeTO> placed);

}
