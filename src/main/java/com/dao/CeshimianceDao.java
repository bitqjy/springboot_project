package com.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.entity.CeshimianceEntity;
import com.entity.view.CeshimianceView;
import com.entity.vo.CeshimianceVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 测试免测
 */
public interface CeshimianceDao extends BaseMapper<CeshimianceEntity> {

    List<CeshimianceVO> selectListVO(@Param("ew") Wrapper<CeshimianceEntity> wrapper);

    CeshimianceVO selectVO(@Param("ew") Wrapper<CeshimianceEntity> wrapper);

    List<CeshimianceView> selectListView(@Param("ew") Wrapper<CeshimianceEntity> wrapper);

    List<CeshimianceView> selectListView(Pagination page, @Param("ew") Wrapper<CeshimianceEntity> wrapper);

    CeshimianceView selectView(@Param("ew") Wrapper<CeshimianceEntity> wrapper);
}

