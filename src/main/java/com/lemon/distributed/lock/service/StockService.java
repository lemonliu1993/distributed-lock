package com.lemon.distributed.lock.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lemon.distributed.lock.mapper.StockMapper;
import com.lemon.distributed.lock.pojo.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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


    @Transactional
    public void deduct(){
        //1.查询库存信息并锁定库存信息
        List<Stock> stocks = this.stockMapper.queryStock("1001");
        //这里取第一个库存
        Stock stock = stocks.get(0);

        //2.判断库存是否充足
        if(stock != null && stock.getCount() >0){
            //3.扣减库存
            stock.setCount(stock.getCount()-1);
            stockMapper.updateById(stock);
        }
    }


    @Transactional
    public  void deduct2() {
        stockMapper.updateStock("1001",1);
//        Stock stock = stockMapper.selectOne(new QueryWrapper<Stock>().eq("product_code", "1001"));
//        if (stock != null && stock.getCount() > 0) {
//            stock.setCount(stock.getCount() - 1);
//            this.stockMapper.updateById(stock);
//        }
    }

}
