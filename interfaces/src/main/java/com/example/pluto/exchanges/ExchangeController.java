package com.example.pluto.exchanges;

import com.example.pluto.entities.BookTO;
import com.example.pluto.entities.SnapshotTO;
import com.example.pluto.entities.SpotTO;
import org.springframework.web.bind.annotation.*;

public interface ExchangeController {

    @GetMapping(value = "snapshot/{instrument}")
    SnapshotTO getSnapshot(@PathVariable String instrument, @RequestParam(required = false) String time);

    @GetMapping(value = "spot/{instrument}")
    SpotTO getSpot(@PathVariable String instrument, @RequestParam(required = false) String time);

    @PostMapping(value = "spot/{instrument}")
    boolean saveSpot(@PathVariable String instrument, @RequestBody(required = false) SpotTO spot);

    @GetMapping(value = "volume/{instrument}")
    int getVolume(@PathVariable String instrument, @RequestParam(required = false) String time);

    @GetMapping(value = "book/{instrument}")
    BookTO getBook(@PathVariable String instrument, @RequestParam(required = false) String time);

}
