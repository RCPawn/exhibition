package com.ruoyi.heritage.service;

import java.util.List;
import com.ruoyi.heritage.domain.HeritageGallery;

/**
 * 图集管理Service接口
 * 
 * @author ruoyi
 * @date 2026-02-01
 */
public interface IHeritageGalleryService 
{
    /**
     * 查询图集管理
     * 
     * @param galleryId 图集管理主键
     * @return 图集管理
     */
    public HeritageGallery selectHeritageGalleryByGalleryId(Long galleryId);

    /**
     * 查询图集管理列表
     * 
     * @param heritageGallery 图集管理
     * @return 图集管理集合
     */
    public List<HeritageGallery> selectHeritageGalleryList(HeritageGallery heritageGallery);

    /**
     * 新增图集管理
     * 
     * @param heritageGallery 图集管理
     * @return 结果
     */
    public int insertHeritageGallery(HeritageGallery heritageGallery);

    /**
     * 修改图集管理
     * 
     * @param heritageGallery 图集管理
     * @return 结果
     */
    public int updateHeritageGallery(HeritageGallery heritageGallery);

    /**
     * 批量删除图集管理
     * 
     * @param galleryIds 需要删除的图集管理主键集合
     * @return 结果
     */
    public int deleteHeritageGalleryByGalleryIds(Long[] galleryIds);

    /**
     * 删除图集管理信息
     * 
     * @param galleryId 图集管理主键
     * @return 结果
     */
    public int deleteHeritageGalleryByGalleryId(Long galleryId);
}
