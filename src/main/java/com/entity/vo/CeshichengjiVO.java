package com.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 测试成绩
 * 手机端接口返回实体辅助类
 */
public class CeshichengjiVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String ceshimingcheng;
    private String jiaoshigonghao;
    private String jiaoshixingming;
    private Integer ceshipingfen;
    private String ceshipingji;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date pingfenshijian;

    private String yonghuzhanghao;
    private String yonghuxingming;
    private String banji;
    private String ceshipingjia;

    private Double run50m;
    private Double run1000m;
    private Double run800m;
    private Double longJump;
    private Integer pullUp;
    private Integer sitUp;
    private Double bmi;
    private String gender;
    private Integer grade;
    private Integer abnormalFlag;
    private String abnormalReason;
    private String weakItems;
    private String strongItems;

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

    public Integer getCeshipingfen() {
        return ceshipingfen;
    }

    public void setCeshipingfen(Integer ceshipingfen) {
        this.ceshipingfen = ceshipingfen;
    }

    public String getCeshipingji() {
        return ceshipingji;
    }

    public void setCeshipingji(String ceshipingji) {
        this.ceshipingji = ceshipingji;
    }

    public Date getPingfenshijian() {
        return pingfenshijian;
    }

    public void setPingfenshijian(Date pingfenshijian) {
        this.pingfenshijian = pingfenshijian;
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

    public String getCeshipingjia() {
        return ceshipingjia;
    }

    public void setCeshipingjia(String ceshipingjia) {
        this.ceshipingjia = ceshipingjia;
    }

    public Double getRun50m() {
        return run50m;
    }

    public void setRun50m(Double run50m) {
        this.run50m = run50m;
    }

    public Double getRun1000m() {
        return run1000m;
    }

    public void setRun1000m(Double run1000m) {
        this.run1000m = run1000m;
    }

    public Double getRun800m() {
        return run800m;
    }

    public void setRun800m(Double run800m) {
        this.run800m = run800m;
    }

    public Double getLongJump() {
        return longJump;
    }

    public void setLongJump(Double longJump) {
        this.longJump = longJump;
    }

    public Integer getPullUp() {
        return pullUp;
    }

    public void setPullUp(Integer pullUp) {
        this.pullUp = pullUp;
    }

    public Integer getSitUp() {
        return sitUp;
    }

    public void setSitUp(Integer sitUp) {
        this.sitUp = sitUp;
    }

    public Double getBmi() {
        return bmi;
    }

    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Integer getAbnormalFlag() {
        return abnormalFlag;
    }

    public void setAbnormalFlag(Integer abnormalFlag) {
        this.abnormalFlag = abnormalFlag;
    }

    public String getAbnormalReason() {
        return abnormalReason;
    }

    public void setAbnormalReason(String abnormalReason) {
        this.abnormalReason = abnormalReason;
    }

    public String getWeakItems() {
        return weakItems;
    }

    public void setWeakItems(String weakItems) {
        this.weakItems = weakItems;
    }

    public String getStrongItems() {
        return strongItems;
    }

    public void setStrongItems(String strongItems) {
        this.strongItems = strongItems;
    }
}
