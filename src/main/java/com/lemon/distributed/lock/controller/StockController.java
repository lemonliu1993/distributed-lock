package com.lemon.distributed.lock.controller;

import com.lemon.distributed.lock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lemoon on 2024/7/7 11:12
 */
@RestController
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping("stock/deduct")
    public String deduct() {
        this.stockService.deduct();
        return "hello stock deduct!!";
    }
}
