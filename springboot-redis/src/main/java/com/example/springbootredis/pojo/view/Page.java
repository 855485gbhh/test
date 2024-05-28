package com.example.springbootredis.pojo.view;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Page<T> {


    @Nullable
    private Integer total;     //总量
    private Integer current;   //当前分页的数据个数
    private Integer pageNum;   //当前页码
    private Integer pageSize;  //每页显示的数据条数
    private List<T> hotelDocs; //数据内容

}
