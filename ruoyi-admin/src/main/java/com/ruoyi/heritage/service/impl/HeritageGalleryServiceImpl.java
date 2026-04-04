package com.ruoyi.heritage.service.impl;

import java.util.List;

import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import com.ruoyi.common.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.heritage.domain.HeritageGalleryImage;
import com.ruoyi.heritage.mapper.HeritageGalleryMapper;
import com.ruoyi.heritage.domain.HeritageGallery;
import com.ruoyi.heritage.service.IHeritageGalleryService;

/**
 * 图集管理Service业务层处理
 *
 * @author ruoyi
 * @date 2026-02-01
 */
@Service
public class HeritageGalleryServiceImpl implements IHeritageGalleryService {
    @Autowired
    private HeritageGalleryMapper heritageGalleryMapper;

    /**
     * 查询图集管理
     *
     * @param galleryId 图集管理主键
     * @return 图集管理
     */
    @Override
    public HeritageGallery selectHeritageGalleryByGalleryId(Long galleryId) {
        return heritageGalleryMapper.selectHeritageGalleryByGalleryId(galleryId);
    }

    /**
     * 查询图集管理列表
     *
     * @param heritageGallery 图集管理
     * @return 图集管理
     */
    @Override
    public List<HeritageGallery> selectHeritageGalleryList(HeritageGallery heritageGallery) {
        return heritageGalleryMapper.selectHeritageGalleryList(heritageGallery);
    }

    /**
     * 新增图集管理
     *
     * @param heritageGallery 图集管理
     * @return 结果
     */
    @Transactional
    @Override
    public int insertHeritageGallery(HeritageGallery heritageGallery) {
        heritageGallery.setCreateTime(DateUtils.getNowDate());
        int rows = heritageGalleryMapper.insertHeritageGallery(heritageGallery);
        insertHeritageGalleryImage(heritageGallery);
        return rows;
    }

    /**
     * 修改图集管理
     *
     * @param heritageGallery 图集管理
     * @return 结果
     */
    @Transactional
    @Override
    public int updateHeritageGallery(HeritageGallery heritageGallery) {
        heritageGalleryMapper.deleteHeritageGalleryImageByGalleryId(heritageGallery.getGalleryId());
        insertHeritageGalleryImage(heritageGallery);
        return heritageGalleryMapper.updateHeritageGallery(heritageGallery);
    }

    /**
     * 批量删除图集管理
     *
     * @param galleryIds 需要删除的图集管理主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteHeritageGalleryByGalleryIds(Long[] galleryIds) {
        heritageGalleryMapper.deleteHeritageGalleryImageByGalleryIds(galleryIds);
        return heritageGalleryMapper.deleteHeritageGalleryByGalleryIds(galleryIds);
    }

    /**
     * 删除图集管理信息
     *
     * @param galleryId 图集管理主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteHeritageGalleryByGalleryId(Long galleryId) {
        heritageGalleryMapper.deleteHeritageGalleryImageByGalleryId(galleryId);
        return heritageGalleryMapper.deleteHeritageGalleryByGalleryId(galleryId);
    }

    /**
     * 新增图集从表信息
     *
     * @param heritageGallery 图集管理对象
     */
    public void insertHeritageGalleryImage(HeritageGallery heritageGallery) {
        List<HeritageGalleryImage> heritageGalleryImageList = heritageGallery.getHeritageGalleryImageList();
        Long galleryId = heritageGallery.getGalleryId();
        if (StringUtils.isNotNull(heritageGalleryImageList)) {
            List<HeritageGalleryImage> list = new ArrayList<HeritageGalleryImage>();
            for (HeritageGalleryImage heritageGalleryImage : heritageGalleryImageList) {
                heritageGalleryImage.setGalleryId(galleryId);
                list.add(heritageGalleryImage);
            }
            if (list.size() > 0) {
                heritageGalleryMapper.batchHeritageGalleryImage(list);
            }
        }
    }
}
