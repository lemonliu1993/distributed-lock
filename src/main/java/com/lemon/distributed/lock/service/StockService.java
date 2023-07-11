package com.lemon.distributed.lock.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lemon.distributed.lock.mapper.StockMapper;
import com.lemon.distributed.lock.pojo.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lemoon on 2023/6/20 10:08
 */
@Service
public class StockService {
//    private Stock stock = new Stock();

    @Autowired
    private StockMapper stockMapper;

    public void deduct(){

        Stock stock = stockMapper.selectOne(new QueryWrapper<Stock>().eq("product_code", "1001"));
        if(stock != null && stock.getCount()>0){
            stock.setCount(stock.getCount()-1);
            stockMapper.updateById(stock);
        }
//        stock.setStock(stock.getStock()-1);
//        System.out.println("库存余量： "+ stock.getStock());
    }
}
