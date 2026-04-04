package com.ruoyi.heritage.mapper;

import java.util.List;
import com.ruoyi.heritage.domain.HeritageGallery;
import com.ruoyi.heritage.domain.HeritageGalleryImage;

/**
 * 图集管理Mapper接口
 * 
 * @author ruoyi
 * @date 2026-02-01
 */
public interface HeritageGalleryMapper 
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
     * 删除图集管理
     * 
     * @param galleryId 图集管理主键
     * @return 结果
     */
    public int deleteHeritageGalleryByGalleryId(Long galleryId);

    /**
     * 批量删除图集管理
     * 
     * @param galleryIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteHeritageGalleryByGalleryIds(Long[] galleryIds);

    /**
     * 批量删除图集从表
     * 
     * @param galleryIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteHeritageGalleryImageByGalleryIds(Long[] galleryIds);
    
    /**
     * 批量新增图集从表
     * 
     * @param heritageGalleryImageList 图集从表列表
     * @return 结果
     */
    public int batchHeritageGalleryImage(List<HeritageGalleryImage> heritageGalleryImageList);
    

    /**
     * 通过图集管理主键删除图集从表信息
     * 
     * @param galleryId 图集管理ID
     * @return 结果
     */
    public int deleteHeritageGalleryImageByGalleryId(Long galleryId);
}
