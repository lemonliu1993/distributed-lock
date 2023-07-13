package com.lemon.distributed.lock.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lemon.distributed.lock.mapper.StockMapper;
import com.lemon.distributed.lock.pojo.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lemoon on 2023/6/20 10:08
 */
@Service
public class StockService{
//    private Stock stock = new Stock();

    @Autowired
    private StockMapper stockMapper;

    private ReentrantLock lock = new ReentrantLock();

    //    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void deduct() {
        lock.lock();
        try {
            stockMapper.updateStock("1001",1);
            System.out.println(stockMapper.selectById(1));

            /**
            //  update insert delete 写操作本身就会加锁
            //  update db_stock set count = count -1 where product_code= '10001' and count >= 1
            //  1.查询库存
            Stock stock = stockMapper.selectOne(new QueryWrapper<Stock>().eq("product_code", "1001"));
            //  2.判断库存是否充足
            if (stock != null && stock.getCount() > 0) {
                stock.setCount(stock.getCount() - 1);
                //  3.更新到数据库
                stockMapper.updateById(stock);
            }
             */
//        stock.setStock(stock.getStock()-1);
//        System.out.println("库存余量： "+ stock.getStock());

        }finally {
            lock.unlock();
        }
    }
}
