package com.example.pluto.exchanges;

import com.example.pluto.entities.BookTO;
import com.example.pluto.entities.SpotEntity;
import com.example.pluto.entities.TradeTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ExchangeController {

    @GetMapping(value = "spots/{instruments}")
    List<SpotEntity> getSpots(@PathVariable String instruments);

    @GetMapping(value = "spot/{instrument}")
    SpotEntity getSpot(@PathVariable String instrument, @RequestParam(required = false) String time);

    @PostMapping(value = "spot/{instrument}")
    boolean saveSpot(@PathVariable String instrument, @RequestBody(required = false) SpotEntity spot);

    @GetMapping(value = "volume/{instrument}")
    int getVolume(@PathVariable String instrument, @RequestParam(required = false) String time);

    @GetMapping(value = "book/{instrument}")
    BookTO getBook(@PathVariable String instrument, @RequestParam(required = false) String time);

    @GetMapping(value = "trade/unactive/{pair}")
    List<TradeTO> getUnactiveOrders(@PathVariable String pair);

    @PostMapping(value = "trade")
    List<TradeTO> trade(@RequestBody List<TradeTO> defTrades);

    @PostMapping(value = "posttrade")
    List<TradeTO> updateChangedTrades(@RequestBody(required = false) List<TradeTO> placed);

}
