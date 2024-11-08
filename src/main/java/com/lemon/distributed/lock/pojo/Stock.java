package com.lemon.distributed.lock.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Created by lemoon on 2024/7/7 10:53
 */

@TableName("db_stock")
@Data
public class Stock {


    private Long id;
    private String productCode;
    private String warehouse;
    private Integer count;
//    private Integer stock = 5000;
}
