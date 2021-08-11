package com.example.pluto.positions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/basket")
public class PositionsController {

    private static final Logger LOG = LoggerFactory.getLogger(PositionsController.class);

    @Autowired
    PositionsService positionsService;

}
