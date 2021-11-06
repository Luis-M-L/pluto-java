package com.example.pluto.bitfinex.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/position")
public class PositionsController {

    private static final Logger LOG = LoggerFactory.getLogger(PositionsController.class);

}
