package com.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.entity.CeshimianceEntity;
import com.entity.view.CeshimianceView;
import com.entity.vo.CeshimianceVO;
import com.utils.PageUtils;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 测试免测
 */
public interface CeshimianceService extends IService<CeshimianceEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CeshimianceVO> selectListVO(Wrapper<CeshimianceEntity> wrapper);

    CeshimianceVO selectVO(@Param("ew") Wrapper<CeshimianceEntity> wrapper);

    List<CeshimianceView> selectListView(Wrapper<CeshimianceEntity> wrapper);

    CeshimianceView selectView(@Param("ew") Wrapper<CeshimianceEntity> wrapper);

    PageUtils queryPage(Map<String, Object> params, Wrapper<CeshimianceEntity> wrapper);
}

