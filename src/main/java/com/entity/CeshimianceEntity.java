package com.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

/**
 * 测试免测
 * 数据库通用操作实体类（普通增删改查）
 */
@TableName("ceshimiance")
public class CeshimianceEntity<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public CeshimianceEntity() {
    }

    public CeshimianceEntity(T t) {
        try {
            BeanUtils.copyProperties(this, t);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /** 主键id */
    @TableId
    private Long id;

    /** 测试编号 */
    private String ceshibianhao;

    /** 测试名称 */
    private String ceshimingcheng;

    /** 教师工号 */
    private String jiaoshigonghao;

    /** 教师姓名 */
    private String jiaoshixingming;

    /** 用户账号 */
    private String yonghuzhanghao;

    /** 用户姓名 */
    private String yonghuxingming;

    /** 班级 */
    private String banji;

    /** 免测原因 */
    private String mianceyuanyin;

    /** 免测日期 */
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat
    private Date mianceriqi;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date addtime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCeshibianhao() {
        return ceshibianhao;
    }

    public void setCeshibianhao(String ceshibianhao) {
        this.ceshibianhao = ceshibianhao;
    }

    public String getCeshimingcheng() {
        return ceshimingcheng;
    }

    public void setCeshimingcheng(String ceshimingcheng) {
        this.ceshimingcheng = ceshimingcheng;
    }

    public String getJiaoshigonghao() {
        return jiaoshigonghao;
    }

    public void setJiaoshigonghao(String jiaoshigonghao) {
        this.jiaoshigonghao = jiaoshigonghao;
    }

    public String getJiaoshixingming() {
        return jiaoshixingming;
    }

    public void setJiaoshixingming(String jiaoshixingming) {
        this.jiaoshixingming = jiaoshixingming;
    }

    public String getYonghuzhanghao() {
        return yonghuzhanghao;
    }

    public void setYonghuzhanghao(String yonghuzhanghao) {
        this.yonghuzhanghao = yonghuzhanghao;
    }

    public String getYonghuxingming() {
        return yonghuxingming;
    }

    public void setYonghuxingming(String yonghuxingming) {
        this.yonghuxingming = yonghuxingming;
    }

    public String getBanji() {
        return banji;
    }

    public void setBanji(String banji) {
        this.banji = banji;
    }

    public String getMianceyuanyin() {
        return mianceyuanyin;
    }

    public void setMianceyuanyin(String mianceyuanyin) {
        this.mianceyuanyin = mianceyuanyin;
    }

    public Date getMianceriqi() {
        return mianceriqi;
    }

    public void setMianceriqi(Date mianceriqi) {
        this.mianceriqi = mianceriqi;
    }

    public Date getAddtime() {
        return addtime;
    }

    public void setAddtime(Date addtime) {
        this.addtime = addtime;
    }
}

