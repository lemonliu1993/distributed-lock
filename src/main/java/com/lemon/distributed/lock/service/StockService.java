package com.lemon.distributed.lock.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lemon.distributed.lock.mapper.StockMapper;
import com.lemon.distributed.lock.pojo.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lemoon on 2024/7/7 11:10
 */
@Service
//@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class StockService {


    private ReentrantLock lock = new ReentrantLock();

    @Autowired
    private StockMapper stockMapper;


    public  void deduct() {
        stockMapper.updateStock("1001",1);
//        Stock stock = stockMapper.selectOne(new QueryWrapper<Stock>().eq("product_code", "1001"));
//        if (stock != null && stock.getCount() > 0) {
//            stock.setCount(stock.getCount() - 1);
//            this.stockMapper.updateById(stock);
//        }
    }

}
