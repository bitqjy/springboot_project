package com.entity.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * 测试免测
 * 手机端接口返回实体辅助类
 */
public class CeshimianceVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String ceshibianhao;
    private String ceshimingcheng;
    private String jiaoshigonghao;
    private String jiaoshixingming;
    private String yonghuzhanghao;
    private String yonghuxingming;
    private String banji;
    private String mianceyuanyin;
    private Date mianceriqi;

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
}

