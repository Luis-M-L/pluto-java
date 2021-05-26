package com.example.pluto.interfaces;

import com.example.pluto.entities.BookTO;
import com.example.pluto.entities.SnapshotTO;
import com.example.pluto.entities.SpotTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public interface ExchangeController {

    @GetMapping(value = "snapshot/{instrument}")
    SnapshotTO getSnapshot(@PathVariable String instrument, @RequestParam(required = false) String time);

    @GetMapping(value = "spot/{instrument}")
    SpotTO getSpot(@PathVariable String instrument, @RequestParam(required = false) String time);

    @GetMapping(value = "volume/{instrument}")
    int getVolume(@PathVariable String instrument, @RequestParam(required = false) String time);

    @GetMapping(value = "book/{instrument}")
    BookTO getBook(@PathVariable String instrument, @RequestParam(required = false) String time);

}
