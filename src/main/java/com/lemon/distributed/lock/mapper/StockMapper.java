package com.lemon.distributed.lock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemon.distributed.lock.pojo.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Created by lemoon on 2023/6/26 22:55
 */
public interface StockMapper extends BaseMapper<Stock> {

    @Update("update db_stock set count = count - #{count} where product_code= #{productCode} and count >= #{count}")
    int updateStock(@Param("productCode") String productCode, @Param("count") Integer count);

    @Select("select * from db_stock where product_code = #{productCode} for update")
    List<Stock> queryStock(@Param("productCode") String productCode);
}
