package com.lemon.distributed.lock.controller;

import com.lemon.distributed.lock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lemoon on 2023/6/20 15:11
 */
@RestController
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping("stock/deduct")
    public String deduct() {
        this.stockService.deduct();
        return "hello stock deduct";
    }


    @GetMapping("test/fair/lock/{id}")
    public String testFailLock(@PathVariable("id") Long id) {
        this.stockService.testFairLock(id);
        return "hello test fair lock";
    }

    @GetMapping("test/read/lock")
    public String testReadLock() {
        this.stockService.testReadLock();
        return "hello test read lock";
    }

    @GetMapping("test/write/lock")
    public String testWriteLock() {
        this.stockService.testWriteLock();
        return "hello test write lock";
    }

    @GetMapping("test/semaphore")
    public String testSemaphore() {
        this.stockService.testSemaphore();
        return "hello semaphore";
    }
}
