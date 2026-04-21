package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.CeshimianceEntity;
import com.entity.view.CeshimianceView;
import com.service.CeshimianceService;
import com.utils.MPUtil;
import com.utils.PageUtils;
import com.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * 测试免测
 * 后端接口
 */
@RestController
@RequestMapping("/ceshimiance")
public class CeshimianceController {
    @Autowired
    private CeshimianceService ceshimianceService;

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, CeshimianceEntity ceshimiance, HttpServletRequest request) {
        String tableName = request.getSession().getAttribute("tableName").toString();
        if (tableName.equals("jiaoshi")) {
            ceshimiance.setJiaoshigonghao((String) request.getSession().getAttribute("username"));
        }
        EntityWrapper<CeshimianceEntity> ew = new EntityWrapper<CeshimianceEntity>();
        PageUtils page = ceshimianceService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, ceshimiance), params), params));
        return R.ok().put("data", page);
    }

    /**
     * 前端列表
     */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, CeshimianceEntity ceshimiance, HttpServletRequest request) {
        EntityWrapper<CeshimianceEntity> ew = new EntityWrapper<CeshimianceEntity>();
        PageUtils page = ceshimianceService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, ceshimiance), params), params));
        return R.ok().put("data", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/lists")
    public R lists(CeshimianceEntity ceshimiance) {
        EntityWrapper<CeshimianceEntity> ew = new EntityWrapper<CeshimianceEntity>();
        ew.allEq(MPUtil.allEQMapPre(ceshimiance, "ceshimiance"));
        return R.ok().put("data", ceshimianceService.selectListView(ew));
    }

    /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(CeshimianceEntity ceshimiance) {
        EntityWrapper<CeshimianceEntity> ew = new EntityWrapper<CeshimianceEntity>();
        ew.allEq(MPUtil.allEQMapPre(ceshimiance, "ceshimiance"));
        CeshimianceView view = ceshimianceService.selectView(ew);
        return R.ok("查询测试免测成功").put("data", view);
    }

    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        CeshimianceEntity ceshimiance = ceshimianceService.selectById(id);
        return R.ok().put("data", ceshimiance);
    }

    /**
     * 前端详情
     */
    @IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id) {
        CeshimianceEntity ceshimiance = ceshimianceService.selectById(id);
        return R.ok().put("data", ceshimiance);
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CeshimianceEntity ceshimiance, HttpServletRequest request) {
        ceshimiance.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        if (ceshimiance.getMianceriqi() == null) {
            ceshimiance.setMianceriqi(new Date());
        }
        ceshimianceService.insert(ceshimiance);
        return R.ok();
    }

    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody CeshimianceEntity ceshimiance, HttpServletRequest request) {
        ceshimiance.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
        if (ceshimiance.getMianceriqi() == null) {
            ceshimiance.setMianceriqi(new Date());
        }
        ceshimianceService.insert(ceshimiance);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CeshimianceEntity ceshimiance, HttpServletRequest request) {
        ceshimianceService.updateById(ceshimiance);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        ceshimianceService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 提醒接口
     */
    @RequestMapping("/remind/{columnName}/{type}")
    public R remindCount(@PathVariable("columnName") String columnName, HttpServletRequest request,
                         @PathVariable("type") String type, @RequestParam Map<String, Object> map) {
        map.put("column", columnName);
        map.put("type", type);

        if (type.equals("2")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar c = Calendar.getInstance();
            Date remindStartDate = null;
            Date remindEndDate = null;
            if (map.get("remindstart") != null) {
                Integer remindStart = Integer.parseInt(map.get("remindstart").toString());
                c.setTime(new Date());
                c.add(Calendar.DAY_OF_MONTH, remindStart);
                remindStartDate = c.getTime();
                map.put("remindstart", sdf.format(remindStartDate));
            }
            if (map.get("remindend") != null) {
                Integer remindEnd = Integer.parseInt(map.get("remindend").toString());
                c.setTime(new Date());
                c.add(Calendar.DAY_OF_MONTH, remindEnd);
                remindEndDate = c.getTime();
                map.put("remindend", sdf.format(remindEndDate));
            }
        }

        Wrapper<CeshimianceEntity> wrapper = new EntityWrapper<CeshimianceEntity>();
        if (map.get("remindstart") != null) {
            wrapper.ge(columnName, map.get("remindstart"));
        }
        if (map.get("remindend") != null) {
            wrapper.le(columnName, map.get("remindend"));
        }

        String tableName = request.getSession().getAttribute("tableName").toString();
        if (tableName.equals("jiaoshi")) {
            wrapper.eq("jiaoshigonghao", (String) request.getSession().getAttribute("username"));
        }

        int count = ceshimianceService.selectCount(wrapper);
        return R.ok().put("count", count);
    }
}

