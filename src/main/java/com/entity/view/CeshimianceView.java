package com.entity.view;

import com.baomidou.mybatisplus.annotations.TableName;
import com.entity.CeshimianceEntity;
import org.apache.commons.beanutils.BeanUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * 测试免测
 * 后端返回视图实体辅助类
 */
@TableName("ceshimiance")
public class CeshimianceView extends CeshimianceEntity<Object> implements Serializable {
    private static final long serialVersionUID = 1L;

    public CeshimianceView() {
    }

    public CeshimianceView(CeshimianceEntity<?> entity) {
        try {
            BeanUtils.copyProperties(this, entity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}

