package com.ruoyi.heritage.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexStatsVo {
    // 核心数字
    private long totalItems;      // 总展品
    private long totalViews;      // 总浏览
    private long totalInteractions; // 总互动(赞+藏+评)
    
    // 图表数据
    private List<ChartDataVo> categoryPie; // 分类占比
    private List<Map<String, Object>> trendLine;   // 趋势数据（模拟或真实）
    
    // 核心展品（用于中间展示的模型）
    private String centerModelUrl;
    private String centerModelName;

    // --- 新增的真实数据 ---

    // 1. 数字化资源构成 (有模型 vs 无模型)
    private List<ChartDataVo> resourceComposition;

    // 2. 交互行为统计 (点赞、收藏、评论总数)
    private List<ChartDataVo> interactionStats;

    // 3. 最新收录的3个展品
    private List<HeritageItem> latestItems;

    // 4. 词云列表 (只存名字和随机权重)
    private List<ChartDataVo> wordCloud;

    // 5. 热门藏品 Top 5 (新增这个字段)
    private List<ChartDataVo> top5Items;

    private Map<String, Object> categoryHierarchy;

}