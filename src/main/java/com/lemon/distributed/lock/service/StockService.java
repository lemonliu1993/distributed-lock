package com.lemon.distributed.lock.service;

import com.lemon.distributed.lock.pojo.Stock;
import org.springframework.stereotype.Service;

/**
 * Created by lemoon on 2023/6/20 10:08
 */
@Service
public class StockService {
    private Stock stock = new Stock();

    public void deduct(){
        stock.setStock(stock.getStock()-1);
        System.out.println("库存余量： "+ stock.getStock());
    }
}
