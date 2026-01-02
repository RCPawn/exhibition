package com.ruoyi.heritage.domain;

/**
 * 专门用于 ECharts 图表的通用数据对象
 */
public class ChartDataVo {
    private String name;
    private Long value;

    // 无参构造
    public ChartDataVo() {}

    // 全参构造
    public ChartDataVo(String name, Long value) {
        this.name = name;
        this.value = value;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getValue() { return value; }
    public void setValue(Long value) { this.value = value; }
}