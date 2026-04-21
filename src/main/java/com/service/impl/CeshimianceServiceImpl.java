package com.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dao.CeshimianceDao;
import com.entity.CeshimianceEntity;
import com.entity.view.CeshimianceView;
import com.entity.vo.CeshimianceVO;
import com.service.CeshimianceService;
import com.utils.PageUtils;
import com.utils.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("ceshimianceService")
public class CeshimianceServiceImpl extends ServiceImpl<CeshimianceDao, CeshimianceEntity> implements CeshimianceService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Page<CeshimianceEntity> page = this.selectPage(
                new Query<CeshimianceEntity>(params).getPage(),
                new EntityWrapper<CeshimianceEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Wrapper<CeshimianceEntity> wrapper) {
        Page<CeshimianceView> page = new Query<CeshimianceView>(params).getPage();
        page.setRecords(baseMapper.selectListView(page, wrapper));
        return new PageUtils(page);
    }

    @Override
    public List<CeshimianceVO> selectListVO(Wrapper<CeshimianceEntity> wrapper) {
        return baseMapper.selectListVO(wrapper);
    }

    @Override
    public CeshimianceVO selectVO(Wrapper<CeshimianceEntity> wrapper) {
        return baseMapper.selectVO(wrapper);
    }

    @Override
    public List<CeshimianceView> selectListView(Wrapper<CeshimianceEntity> wrapper) {
        return baseMapper.selectListView(wrapper);
    }

    @Override
    public CeshimianceView selectView(Wrapper<CeshimianceEntity> wrapper) {
        return baseMapper.selectView(wrapper);
    }
}

