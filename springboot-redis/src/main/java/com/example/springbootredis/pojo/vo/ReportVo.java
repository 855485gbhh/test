package com.example.springbootredis.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportVo {
    private Integer total; //总共签到次数
    private Integer recentTimes;  //最近签到次数
    private Integer maxTimes;  //最大连续签到次数
}
