package com.lemon.distributed.lock.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lemon.distributed.lock.mapper.StockMapper;
import com.lemon.distributed.lock.pojo.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lemoon on 2024/7/7 11:10
 */
@Service
public class StockService {

//    private Stock stock = new Stock();

    private ReentrantLock lock = new ReentrantLock();

    @Autowired
    private StockMapper stockMapper;

//    public synchronized void deduct() {
//        stock.setStock(stock.getStock() - 1);
//        System.out.println("库存余量: " + stock.getStock());
//    }


    public void deduct() {
//        lock.lock();
//        try {
            Stock stock = stockMapper.selectOne(new QueryWrapper<Stock>().eq("product_code", "1001"));
            if (stock != null && stock.getCount() > 0) {
                stock.setCount(stock.getCount() - 1);
                this.stockMapper.updateById(stock);
//            }
//        } finally {
//            lock.unlock();
        }
    }


}
