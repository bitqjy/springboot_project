package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.CeshibaogaoEntity;
import com.entity.CeshichengjiEntity;
import com.entity.JiaoshiEntity;
import com.entity.TizhiceshiEntity;
import com.entity.YonghuEntity;
import com.entity.view.CeshichengjiView;
import com.service.AiAdviceService;
import com.service.CeshibaogaoService;
import com.service.CeshichengjiService;
import com.service.JiaoshiService;
import com.service.ScoreAnalyzeService;
import com.service.TizhiceshiService;
import com.service.YonghuService;
import com.service.dto.ScoreAnalysisResult;
import com.utils.ExcelImportUtil;
import com.utils.MPUtil;
import com.utils.PageUtils;
import com.utils.R;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 测试成绩
 * 后端接口
 */
@RestController
@RequestMapping("/ceshichengji")
public class CeshichengjiController {
    @Autowired
    private CeshichengjiService ceshichengjiService;

    @Autowired
    private YonghuService yonghuService;

    @Autowired
    private JiaoshiService jiaoshiService;

    @Autowired
    private CeshibaogaoService ceshibaogaoService;

    @Autowired
    private TizhiceshiService tizhiceshiService;

    @Autowired
    private AiAdviceService aiAdviceService;

    @Autowired
    private ScoreAnalyzeService scoreAnalyzeService;

    private static final List<String> IMPORT_TEMPLATE_HEADERS = Arrays.asList(
            "学号", "姓名", "班级", "性别", "年级", "50米", "1000米", "800米", "立定跳远", "引体向上", "仰卧起坐", "BMI"
    );

    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, CeshichengjiEntity ceshichengji,
                  HttpServletRequest request) {
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        if ("jiaoshi".equals(tableName)) {
            ceshichengji.setJiaoshigonghao((String) request.getSession().getAttribute("username"));
        }
        if ("yonghu".equals(tableName)) {
            ceshichengji.setYonghuzhanghao((String) request.getSession().getAttribute("username"));
        }
        EntityWrapper<CeshichengjiEntity> ew = new EntityWrapper<CeshichengjiEntity>();
        PageUtils page = ceshichengjiService.queryPage(
                params,
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, ceshichengji), params), params)
        );

        return R.ok().put("data", page);
    }

    /**
     * 前端列表
     */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, CeshichengjiEntity ceshichengji,
                  HttpServletRequest request) {
        EntityWrapper<CeshichengjiEntity> ew = new EntityWrapper<CeshichengjiEntity>();
        PageUtils page = ceshichengjiService.queryPage(
                params,
                MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, ceshichengji), params), params)
        );
        return R.ok().put("data", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/lists")
    public R lists(CeshichengjiEntity ceshichengji) {
        EntityWrapper<CeshichengjiEntity> ew = new EntityWrapper<CeshichengjiEntity>();
        ew.allEq(MPUtil.allEQMapPre(ceshichengji, "ceshichengji"));
        return R.ok().put("data", ceshichengjiService.selectListView(ew));
    }

    /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(CeshichengjiEntity ceshichengji) {
        EntityWrapper<CeshichengjiEntity> ew = new EntityWrapper<CeshichengjiEntity>();
        ew.allEq(MPUtil.allEQMapPre(ceshichengji, "ceshichengji"));
        CeshichengjiView ceshichengjiView = ceshichengjiService.selectView(ew);
        return R.ok("查询测试成绩成功").put("data", ceshichengjiView);
    }

    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        CeshichengjiEntity ceshichengji = ceshichengjiService.selectById(id);
        return R.ok().put("data", ceshichengji);
    }

    /**
     * 前端详情
     */
    @IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id) {
        CeshichengjiEntity ceshichengji = ceshichengjiService.selectById(id);
        return R.ok().put("data", ceshichengji);
    }

    /**
     * 手动录入前校验（不落库）
     */
    @RequestMapping("/validate")
    public R validate(@RequestBody CeshichengjiEntity ceshichengji, HttpServletRequest request) {
        if (!isTeacherOrAdminSession(request)) {
            return R.error("仅教师或管理员可操作");
        }
        fillContextFromSession(ceshichengji, request);
        enrichStudentFields(ceshichengji);
        ScoreAnalysisResult analysis = scoreAnalyzeService.analyze(ceshichengji);
        scoreAnalyzeService.fillComputedFields(ceshichengji, analysis);

        return R.ok().put("data", analysis.toMap());
    }

    /**
     * 导入模板说明
     */
    @RequestMapping("/importTemplate")
    public R importTemplate() {
        Map<String, Object> sample = new LinkedHashMap<String, Object>();
        sample.put("学号", "20230001");
        sample.put("姓名", "张三");
        sample.put("班级", "大一1班");
        sample.put("性别", "男");
        sample.put("年级", "1");
        sample.put("50米", "7.8");
        sample.put("1000米", "3:45");
        sample.put("800米", "");
        sample.put("立定跳远", "2.15m");
        sample.put("引体向上", "10");
        sample.put("仰卧起坐", "");
        sample.put("BMI", "22.1");

        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("headers", IMPORT_TEMPLATE_HEADERS);
        data.put("requiredHeaders", Arrays.asList("学号", "姓名"));
        data.put("sampleRow", sample);
        data.put("notes", Arrays.asList(
                "1000米/800米可填“秒值(225)”或“分秒(3:45/3分45秒)”",
                "立定跳远可填cm或m，例如“210”或“2.10m”",
                "strictAbnormal=true时，超合理范围数据将进入错误报告，不写入数据库"
        ));
        return R.ok().put("data", data);
    }

    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CeshichengjiEntity ceshichengji, HttpServletRequest request) {
        if (!isTeacherOrAdminSession(request)) {
            return R.error("仅教师或管理员可新增成绩");
        }
        fillContextFromSession(ceshichengji, request);
        enrichStudentFields(ceshichengji);
        CeshichengjiEntity existed = findExistingScoreForUpsert(ceshichengji, request);
        boolean updated = existed != null && existed.getId() != null;
        if (updated) {
            ceshichengji.setId(existed.getId());
        } else {
            ceshichengji.setId(randomId());
        }

        ScoreAnalysisResult analysis = scoreAnalyzeService.analyze(ceshichengji);
        scoreAnalyzeService.fillComputedFields(ceshichengji, analysis);
        if (ceshichengji.getPingfenshijian() == null) {
            ceshichengji.setPingfenshijian(new Date());
        }
        if (updated) {
            ceshichengjiService.updateById(ceshichengji);
        } else {
            ceshichengjiService.insert(ceshichengji);
        }

        R resp = R.ok().put("analysis", analysis.toMap()).put("updated", updated);
        if (analysis.isAbnormal()) {
            resp.put("warning", (updated ? "已更新" : "已保存") + "，但成绩存在异常项，请复核后确认");
        }
        return resp;
    }

    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody CeshichengjiEntity ceshichengji, HttpServletRequest request) {
        if (!isTeacherOrAdminSession(request)) {
            return R.error("仅教师或管理员可新增成绩");
        }
        fillContextFromSession(ceshichengji, request);
        enrichStudentFields(ceshichengji);
        CeshichengjiEntity existed = findExistingScoreForUpsert(ceshichengji, request);
        boolean updated = existed != null && existed.getId() != null;
        if (updated) {
            ceshichengji.setId(existed.getId());
        } else {
            ceshichengji.setId(randomId());
        }

        ScoreAnalysisResult analysis = scoreAnalyzeService.analyze(ceshichengji);
        scoreAnalyzeService.fillComputedFields(ceshichengji, analysis);
        if (ceshichengji.getPingfenshijian() == null) {
            ceshichengji.setPingfenshijian(new Date());
        }
        if (updated) {
            ceshichengjiService.updateById(ceshichengji);
        } else {
            ceshichengjiService.insert(ceshichengji);
        }

        R resp = R.ok().put("analysis", analysis.toMap()).put("updated", updated);
        if (analysis.isAbnormal()) {
            resp.put("warning", (updated ? "已更新" : "已保存") + "，但成绩存在异常项，请复核后确认");
        }
        return resp;
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CeshichengjiEntity ceshichengji, HttpServletRequest request) {
        if (!isTeacherOrAdminSession(request)) {
            return R.error("仅教师或管理员可修改成绩");
        }
        fillContextFromSession(ceshichengji, request);
        enrichStudentFields(ceshichengji);

        ScoreAnalysisResult analysis = scoreAnalyzeService.analyze(ceshichengji);
        scoreAnalyzeService.fillComputedFields(ceshichengji, analysis);
        ceshichengjiService.updateById(ceshichengji);

        R resp = R.ok().put("analysis", analysis.toMap());
        if (analysis.isAbnormal()) {
            resp.put("warning", "已更新，但成绩存在异常项，请复核后确认");
        }
        return resp;
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids, HttpServletRequest request) {
        if (!isTeacherOrAdminSession(request)) {
            return R.error("仅教师或管理员可删除成绩");
        }
        ceshichengjiService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 成绩Excel导入（体育老师上传）
     *
     * 支持模板：
     * - 学号/用户账号、姓名、班级、性别、年级
     * - 50米、1000米、800米、立定跳远、引体向上、仰卧起坐、BMI
     * - 兼容旧模板：测试评分/分数/成绩
     */
    @RequestMapping("/importExcel")
    public R importExcel(@RequestParam("file") MultipartFile file,
                         @RequestParam(value = "tizhiceshiId", required = false) Long tizhiceshiId,
                         @RequestParam(value = "ceshibianhao", required = false) String ceshibianhao,
                         @RequestParam(value = "ceshimingcheng", required = false) String ceshimingcheng,
                         @RequestParam(value = "banji", required = false) String banji,
                         @RequestParam(value = "dryRun", required = false, defaultValue = "false") Boolean dryRun,
                         @RequestParam(value = "strictAbnormal", required = false, defaultValue = "true") Boolean strictAbnormal,
                         HttpServletRequest request) throws Exception {
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        if (!isTeacherOrAdmin(tableName)) {
            return R.error("仅教师或管理员可导入成绩");
        }

        String sessionUsername = (String) request.getSession().getAttribute("username");
        String jiaoshigonghao = null;
        String jiaoshixingming = null;
        if ("jiaoshi".equals(tableName)) {
            jiaoshigonghao = sessionUsername;
            JiaoshiEntity<?> j = jiaoshiService.selectOne(new EntityWrapper<JiaoshiEntity>().eq("jiaoshigonghao", jiaoshigonghao));
            if (j != null) {
                jiaoshixingming = j.getJiaoshixingming();
            }
        } else if ("users".equals(tableName)) {
            jiaoshigonghao = sessionUsername;
            jiaoshixingming = "管理员";
        }

        TizhiceshiEntity<?> importTask = resolveImportTaskForImport(tizhiceshiId, ceshibianhao, jiaoshigonghao, tableName);
        if (importTask == null) {
            return R.error("请先选择已发布测试任务，再导入成绩");
        }
        String resolvedTestNo = StringUtils.trimToEmpty(importTask.getCeshibianhao());
        String resolvedTestName = StringUtils.trimToEmpty(importTask.getCeshimingcheng());
        if (StringUtils.isBlank(resolvedTestNo) || StringUtils.isBlank(resolvedTestName)) {
            return R.error("所选测试任务缺少测试编号或测试名称，暂不能导入");
        }
        if (StringUtils.isNotBlank(importTask.getJiaoshigonghao())) {
            jiaoshigonghao = importTask.getJiaoshigonghao().trim();
        }
        if (StringUtils.isNotBlank(importTask.getJiaoshixingming())) {
            jiaoshixingming = importTask.getJiaoshixingming().trim();
        }

        ExcelImportUtil.SheetData sheet = ExcelImportUtil.readFirstSheet(file);

        List<Map<String, Object>> errors = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> warnings = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> corrections = new ArrayList<Map<String, Object>>();
        int inserted = 0;
        int updated = 0;
        int skipped = 0;

        int rowIndex = 1;
        for (Map<String, String> row : sheet.rows) {
            rowIndex++;
            List<String> rowErrors = new ArrayList<String>();

            String rowBanji = pick(row, "班级", "banji");
            if (StringUtils.isBlank(rowBanji)) {
                rowBanji = banji;
            }
            String zhanghao = pick(row, "学号", "用户账号", "账号", "yonghuzhanghao", "studentNo");
            String xingming = pick(row, "姓名", "用户姓名", "yonghuxingming");

            if (StringUtils.isBlank(zhanghao) && StringUtils.isNotBlank(xingming) && StringUtils.isNotBlank(rowBanji)) {
                YonghuEntity<?> u = yonghuService.selectOne(
                        new EntityWrapper<YonghuEntity>().eq("banji", rowBanji).eq("yonghuxingming", xingming)
                );
                if (u != null) {
                    zhanghao = u.getYonghuzhanghao();
                }
            }
            if (StringUtils.isBlank(zhanghao)) {
                errors.add(err(rowIndex, "STUDENT_NO_MISSING", "缺少学号/用户账号，且无法通过姓名+班级反查", row));
                continue;
            }

            YonghuEntity<?> student = yonghuService.selectOne(new EntityWrapper<YonghuEntity>().eq("yonghuzhanghao", zhanghao));
            if (student == null) {
                errors.add(err(rowIndex, "STUDENT_NOT_FOUND", "学号不存在：" + zhanghao, row));
                continue;
            }

            if (StringUtils.isBlank(xingming)) {
                xingming = student.getYonghuxingming();
            } else if (StringUtils.isNotBlank(student.getYonghuxingming())
                    && !StringUtils.equals(student.getYonghuxingming(), xingming)) {
                warnings.add(err(rowIndex, "NAME_MISMATCH", "姓名与系统不一致，已按系统姓名写入", row));
                xingming = student.getYonghuxingming();
            }
            if (StringUtils.isBlank(rowBanji)) {
                rowBanji = student.getBanji();
            }

            CeshichengjiEntity entity = ceshichengjiService.selectOne(
                    new EntityWrapper<CeshichengjiEntity>().eq("ceshibianhao", resolvedTestNo).eq("yonghuzhanghao", zhanghao)
            );
            boolean exists = entity != null;
            if (!exists) {
                entity = new CeshichengjiEntity<Object>();
                entity.setId(randomId());
            }

            entity.setCeshibianhao(resolvedTestNo);
            entity.setCeshimingcheng(resolvedTestName);
            entity.setJiaoshigonghao(jiaoshigonghao);
            entity.setJiaoshixingming(jiaoshixingming);
            entity.setYonghuzhanghao(zhanghao);
            entity.setYonghuxingming(xingming);
            entity.setBanji(rowBanji);
            entity.setPingfenshijian(new Date());

            String gender = pick(row, "性别", "gender");
            if (StringUtils.isBlank(gender)) {
                gender = student.getXingbie();
            }
            entity.setGender(gender);
            entity.setGrade(parseGradeField(row, rowErrors, "年级", "grade"));

            entity.setRun50m(parseTimeSecondsField(row, rowErrors, "50米", "50米(s)", "50m", "run50m"));
            entity.setRun1000m(parseTimeSecondsField(row, rowErrors, "1000米", "1000米(s)", "run1000m"));
            entity.setRun800m(parseTimeSecondsField(row, rowErrors, "800米", "800米(s)", "run800m"));
            entity.setLongJump(parseDistanceCmField(row, rowErrors, "立定跳远", "立定跳远(cm)", "longJump", "long_jump"));
            entity.setPullUp(parseIntegerField(row, rowErrors, "引体向上", "pullUp", "pull_up"));
            entity.setSitUp(parseIntegerField(row, rowErrors, "仰卧起坐", "sitUp", "sit_up"));
            entity.setBmi(parseDoubleField(row, rowErrors, "BMI", "bmi"));

            String oldComment = pick(row, "测试评价", "评价", "ceshipingjia");
            if (StringUtils.isNotBlank(oldComment)) {
                entity.setCeshipingjia(oldComment);
            }

            // 兼容旧模板：只有综合分
            Integer oldScore = parseAndFixScore(pick(row, "测试评分", "分数", "成绩", "ceshipingfen"), corrections, rowIndex);
            if (oldScore != null) {
                entity.setCeshipingfen(oldScore);
                if (StringUtils.isBlank(entity.getCeshipingji())) {
                    entity.setCeshipingji(scoreToRating(oldScore));
                }
            }

            if (!rowErrors.isEmpty()) {
                errors.add(err(rowIndex, "FORMAT_ERROR", StringUtils.join(rowErrors, "；"), row));
                continue;
            }

            ScoreAnalysisResult analysis = scoreAnalyzeService.analyze(entity);
            scoreAnalyzeService.fillComputedFields(entity, analysis);

            if (analysis.isAbnormal() && Boolean.TRUE.equals(strictAbnormal)) {
                errors.add(err(rowIndex, "OUT_OF_RANGE", analysis.abnormalText(), row));
                continue;
            }

            if (!hasAnyDetailedMetrics(entity) && entity.getCeshipingfen() == null) {
                errors.add(err(rowIndex, "SCORE_EMPTY", "缺少可用成绩（既没有细分项目，也没有综合分）", row));
                continue;
            }

            if (Boolean.TRUE.equals(dryRun)) {
                skipped++;
                continue;
            }

            if (!exists) {
                ceshichengjiService.insert(entity);
                inserted++;
            } else {
                ceshichengjiService.updateById(entity);
                updated++;
            }
        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("inserted", inserted);
        data.put("updated", updated);
        data.put("skipped", skipped);
        data.put("errors", errors);
        data.put("warnings", warnings);
        data.put("corrections", corrections);
        data.put("template", IMPORT_TEMPLATE_HEADERS);
        data.put("tizhiceshiId", importTask.getId());
        data.put("ceshibianhao", resolvedTestNo);
        data.put("ceshimingcheng", resolvedTestName);
        return R.ok().put("data", data);
    }

    /**
     * 成绩导入筛选项（已发布测试任务 + 班级）
     */
    @RequestMapping("/importOptions")
    public R importOptions(@RequestParam(value = "tizhiceshiId", required = false) Long tizhiceshiId,
                           HttpServletRequest request) {
        if (!isTeacherOrAdminSession(request)) {
            return R.error("仅教师或管理员可查看导入筛选项");
        }

        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        String teacherNo = "jiaoshi".equals(tableName) ? (String) request.getSession().getAttribute("username") : null;

        EntityWrapper<TizhiceshiEntity> taskWrapper = new EntityWrapper<TizhiceshiEntity>();
        if (StringUtils.isNotBlank(teacherNo)) {
            taskWrapper.eq("jiaoshigonghao", teacherNo);
        }
        taskWrapper.orderBy("id", false);
        List<TizhiceshiEntity> tasks = tizhiceshiService.selectList(taskWrapper);

        List<Map<String, Object>> testOptions = new ArrayList<Map<String, Object>>();
        if (tasks != null) {
            for (TizhiceshiEntity task : tasks) {
                if (task == null || StringUtils.isBlank(task.getCeshibianhao())) {
                    continue;
                }
                Map<String, Object> item = new LinkedHashMap<String, Object>();
                item.put("id", task.getId());
                item.put("ceshibianhao", StringUtils.trimToEmpty(task.getCeshibianhao()));
                item.put("ceshimingcheng", StringUtils.trimToEmpty(task.getCeshimingcheng()));
                testOptions.add(item);
            }
        }

        Set<String> banjiSet = new LinkedHashSet<String>();
        List<YonghuEntity> students = yonghuService.selectList(new EntityWrapper<YonghuEntity>());
        if (students != null) {
            for (YonghuEntity student : students) {
                addTextOption(banjiSet, student.getBanji());
            }
        }

        TizhiceshiEntity<?> selectedTask = null;
        if (tizhiceshiId != null) {
            selectedTask = resolveImportTaskForImport(tizhiceshiId, null, teacherNo, tableName);
            if (selectedTask == null) {
                return R.error("所选测试任务不存在或无导入权限");
            }
            addTaskClassesForImport(banjiSet, selectedTask.getCeshibianhao(), teacherNo);
        }

        List<String> banjiOptions = new ArrayList<String>(banjiSet);
        banjiOptions.sort(String::compareToIgnoreCase);

        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("tests", testOptions);
        data.put("banjis", banjiOptions);
        if (selectedTask != null) {
            Map<String, Object> selected = new LinkedHashMap<String, Object>();
            selected.put("id", selectedTask.getId());
            selected.put("ceshibianhao", StringUtils.trimToEmpty(selectedTask.getCeshibianhao()));
            selected.put("ceshimingcheng", StringUtils.trimToEmpty(selectedTask.getCeshimingcheng()));
            data.put("selectedTest", selected);
        }
        return R.ok().put("data", data);
    }

    /**
     * 报告提交/缺测/免测名单对比
     */
    @RequestMapping("/compareRoster")
    public R compareRoster(@RequestParam("ceshibianhao") String ceshibianhao,
                           @RequestParam("banji") String banji,
                           HttpServletRequest request) {
        if (!isTeacherOrAdminSession(request)) {
            return R.error("仅教师或管理员可使用报告提交识别");
        }
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        String teacherNo = "jiaoshi".equals(tableName) ? (String) request.getSession().getAttribute("username") : null;

        List<YonghuEntity> roster = yonghuService.selectList(new EntityWrapper<YonghuEntity>().eq("banji", banji));
        if (roster == null) {
            roster = new ArrayList<YonghuEntity>();
        }
        EntityWrapper<CeshichengjiEntity> scoreWrapper = new EntityWrapper<CeshichengjiEntity>();
        scoreWrapper.eq("ceshibianhao", ceshibianhao).eq("banji", banji);
        if (StringUtils.isNotBlank(teacherNo)) {
            scoreWrapper.eq("jiaoshigonghao", teacherNo);
        }
        scoreWrapper.orderBy("id", false);
        List<CeshichengjiEntity> scored = ceshichengjiService.selectList(scoreWrapper);
        if (scored == null) {
            scored = new ArrayList<CeshichengjiEntity>();
        }

        EntityWrapper<CeshibaogaoEntity> reportWrapper = new EntityWrapper<CeshibaogaoEntity>();
        reportWrapper.eq("ceshibianhao", ceshibianhao).eq("banji", banji);
        if (StringUtils.isNotBlank(teacherNo)) {
            reportWrapper.eq("jiaoshigonghao", teacherNo);
        }
        reportWrapper.orderBy("id", false);
        List<CeshibaogaoEntity> reports = ceshibaogaoService.selectList(reportWrapper);
        if (reports == null) {
            reports = new ArrayList<CeshibaogaoEntity>();
        }

        Map<String, CeshichengjiEntity> scoredByAccount = new HashMap<String, CeshichengjiEntity>();
        for (CeshichengjiEntity e : scored) {
            if (StringUtils.isNotBlank(e.getYonghuzhanghao()) && !scoredByAccount.containsKey(e.getYonghuzhanghao())) {
                scoredByAccount.put(e.getYonghuzhanghao(), e);
            }
        }
        Map<String, CeshibaogaoEntity> reportByAccount = new HashMap<String, CeshibaogaoEntity>();
        for (CeshibaogaoEntity report : reports) {
            if (StringUtils.isNotBlank(report.getYonghuzhanghao()) && !reportByAccount.containsKey(report.getYonghuzhanghao())) {
                reportByAccount.put(report.getYonghuzhanghao(), report);
            }
        }

        List<Map<String, Object>> missing = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> measured = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> submitted = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> exempted = new ArrayList<Map<String, Object>>();
        int abnormalCount = 0;

        for (YonghuEntity u : roster) {
            String acc = u.getYonghuzhanghao();
            if (StringUtils.isBlank(acc)) {
                continue;
            }
            CeshibaogaoEntity report = reportByAccount.get(acc);
            CeshichengjiEntity score = scoredByAccount.get(acc);
            if (report == null) {
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("status", "MISSING_REPORT");
                m.put("yonghuzhanghao", acc);
                m.put("yonghuxingming", u.getYonghuxingming());
                m.put("banji", u.getBanji());
                if (score != null) {
                    m.put("chengji", score);
                }
                missing.add(m);
                continue;
            }
            if (isExemptReport(report)) {
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("status", "EXEMPT");
                m.put("yonghuzhanghao", acc);
                m.put("yonghuxingming", u.getYonghuxingming());
                m.put("banji", u.getBanji());
                m.put("baogao", report);
                m.put("mianceyuanyin", reportExemptReason(report));
                exempted.add(m);
                continue;
            }
            if (score != null) {
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("status", "SCORED");
                m.put("yonghuzhanghao", acc);
                m.put("yonghuxingming", u.getYonghuxingming());
                m.put("banji", u.getBanji());
                m.put("baogao", report);
                m.put("chengji", score);
                measured.add(m);
                if (score.getAbnormalFlag() != null && score.getAbnormalFlag() == 1) {
                    abnormalCount++;
                }
                continue;
            }
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("status", "SUBMITTED");
            m.put("yonghuzhanghao", acc);
            m.put("yonghuxingming", u.getYonghuxingming());
            m.put("banji", u.getBanji());
            m.put("baogao", report);
            submitted.add(m);
        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("banji", banji);
        data.put("ceshibianhao", ceshibianhao);
        data.put("rosterCount", roster.size());
        data.put("submittedCount", measured.size() + submitted.size() + exempted.size());
        data.put("scoredCount", measured.size());
        data.put("measuredCount", measured.size());
        data.put("pendingCount", submitted.size());
        data.put("exemptCount", exempted.size());
        data.put("missingCount", missing.size());
        data.put("abnormalCount", abnormalCount);
        data.put("measured", measured);
        data.put("submitted", submitted);
        data.put("exempt", exempted);
        data.put("missing", missing);
        return R.ok().put("data", data);
    }

    /**
     * 成绩统计筛选项：测试编号、班级
     */
    @RequestMapping("/statsOptions")
    public R statsOptions(@RequestParam(value = "ceshibianhao", required = false) String ceshibianhao,
                          HttpServletRequest request) {
        if (!isTeacherOrAdminSession(request)) {
            return R.error("仅教师或管理员可查看统计筛选项");
        }

        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        String teacherNo = "jiaoshi".equals(tableName) ? (String) request.getSession().getAttribute("username") : null;

        Map<String, Map<String, Object>> testMap = new LinkedHashMap<String, Map<String, Object>>();
        EntityWrapper<TizhiceshiEntity> testWrapper = new EntityWrapper<TizhiceshiEntity>();
        if (StringUtils.isNotBlank(teacherNo)) {
            testWrapper.eq("jiaoshigonghao", teacherNo);
        }
        List<TizhiceshiEntity> tests = tizhiceshiService.selectList(testWrapper);
        if (tests != null) {
            for (TizhiceshiEntity test : tests) {
                addTestOption(testMap, test.getCeshibianhao(), test.getCeshimingcheng());
            }
        }

        EntityWrapper<CeshichengjiEntity> scoreTestWrapper = new EntityWrapper<CeshichengjiEntity>();
        if (StringUtils.isNotBlank(teacherNo)) {
            scoreTestWrapper.eq("jiaoshigonghao", teacherNo);
        }
        List<CeshichengjiEntity> scoresForTests = ceshichengjiService.selectList(scoreTestWrapper);
        if (scoresForTests != null) {
            for (CeshichengjiEntity score : scoresForTests) {
                addTestOption(testMap, score.getCeshibianhao(), score.getCeshimingcheng());
            }
        }

        EntityWrapper<CeshibaogaoEntity> reportTestWrapper = new EntityWrapper<CeshibaogaoEntity>();
        if (StringUtils.isNotBlank(teacherNo)) {
            reportTestWrapper.eq("jiaoshigonghao", teacherNo);
        }
        List<CeshibaogaoEntity> reportsForTests = ceshibaogaoService.selectList(reportTestWrapper);
        if (reportsForTests != null) {
            for (CeshibaogaoEntity report : reportsForTests) {
                addTestOption(testMap, report.getCeshibianhao(), report.getCeshimingcheng());
            }
        }

        Set<String> banjiSet = new LinkedHashSet<String>();
        List<YonghuEntity> students = yonghuService.selectList(new EntityWrapper<YonghuEntity>());
        if (students != null) {
            for (YonghuEntity student : students) {
                addTextOption(banjiSet, student.getBanji());
            }
        }

        EntityWrapper<CeshichengjiEntity> scoreClassWrapper = new EntityWrapper<CeshichengjiEntity>();
        if (StringUtils.isNotBlank(teacherNo)) {
            scoreClassWrapper.eq("jiaoshigonghao", teacherNo);
        }
        if (StringUtils.isNotBlank(ceshibianhao)) {
            scoreClassWrapper.eq("ceshibianhao", ceshibianhao);
        }
        List<CeshichengjiEntity> scoresForClasses = ceshichengjiService.selectList(scoreClassWrapper);
        if (scoresForClasses != null) {
            for (CeshichengjiEntity score : scoresForClasses) {
                addTextOption(banjiSet, score.getBanji());
            }
        }

        EntityWrapper<CeshibaogaoEntity> reportClassWrapper = new EntityWrapper<CeshibaogaoEntity>();
        if (StringUtils.isNotBlank(teacherNo)) {
            reportClassWrapper.eq("jiaoshigonghao", teacherNo);
        }
        if (StringUtils.isNotBlank(ceshibianhao)) {
            reportClassWrapper.eq("ceshibianhao", ceshibianhao);
        }
        List<CeshibaogaoEntity> reportsForClasses = ceshibaogaoService.selectList(reportClassWrapper);
        if (reportsForClasses != null) {
            for (CeshibaogaoEntity report : reportsForClasses) {
                addTextOption(banjiSet, report.getBanji());
            }
        }
        if (banjiSet.isEmpty() && students != null) {
            for (YonghuEntity student : students) {
                addTextOption(banjiSet, student.getBanji());
            }
        }

        List<Map<String, Object>> testOptions = new ArrayList<Map<String, Object>>(testMap.values());
        testOptions.sort((a, b) -> String.valueOf(a.get("ceshibianhao")).compareToIgnoreCase(String.valueOf(b.get("ceshibianhao"))));
        List<String> banjiOptions = new ArrayList<String>(banjiSet);
        banjiOptions.sort(String::compareToIgnoreCase);

        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("tests", testOptions);
        data.put("banjis", banjiOptions);
        return R.ok().put("data", data);
    }

    /**
     * 成绩统计总览
     */
    @RequestMapping("/statsOverview")
    public R statsOverview(@RequestParam("ceshibianhao") String ceshibianhao,
                           @RequestParam(value = "banji", required = false) String banji,
                           HttpServletRequest request) {
        if (!isTeacherOrAdminSession(request)) {
            return R.error("仅教师或管理员可查看统计");
        }
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        String teacherNo = "jiaoshi".equals(tableName) ? (String) request.getSession().getAttribute("username") : null;

        EntityWrapper<CeshichengjiEntity> scoreWrapper = new EntityWrapper<CeshichengjiEntity>();
        scoreWrapper.eq("ceshibianhao", ceshibianhao);
        if (StringUtils.isNotBlank(teacherNo)) {
            scoreWrapper.eq("jiaoshigonghao", teacherNo);
        }
        if (StringUtils.isNotBlank(banji)) {
            scoreWrapper.eq("banji", banji);
        }
        List<CeshichengjiEntity> scoreList = ceshichengjiService.selectList(scoreWrapper);
        if (scoreList == null) {
            scoreList = new ArrayList<CeshichengjiEntity>();
        }

        EntityWrapper<CeshibaogaoEntity> reportWrapper = new EntityWrapper<CeshibaogaoEntity>();
        reportWrapper.eq("ceshibianhao", ceshibianhao);
        if (StringUtils.isNotBlank(teacherNo)) {
            reportWrapper.eq("jiaoshigonghao", teacherNo);
        }
        if (StringUtils.isNotBlank(banji)) {
            reportWrapper.eq("banji", banji);
        }
        List<CeshibaogaoEntity> reportList = ceshibaogaoService.selectList(reportWrapper);
        if (reportList == null) {
            reportList = new ArrayList<CeshibaogaoEntity>();
        }
        Set<String> submittedAccounts = new HashSet<String>();
        Set<String> exemptAccounts = new HashSet<String>();
        for (CeshibaogaoEntity report : reportList) {
            if (StringUtils.isNotBlank(report.getYonghuzhanghao())) {
                submittedAccounts.add(report.getYonghuzhanghao());
                if (isExemptReport(report)) {
                    exemptAccounts.add(report.getYonghuzhanghao());
                }
            }
        }

        int abnormalCount = 0;
        int totalScore = 0;
        int scoreCount = 0;

        Map<String, Integer> ratingDist = new LinkedHashMap<String, Integer>();
        ratingDist.put("优秀", 0);
        ratingDist.put("良好", 0);
        ratingDist.put("及格", 0);
        ratingDist.put("不及格", 0);
        ratingDist.put("未评级", 0);

        Map<String, Integer> weakItemCount = new HashMap<String, Integer>();

        for (CeshichengjiEntity record : scoreList) {
            if (record.getAbnormalFlag() != null && record.getAbnormalFlag() == 1) {
                abnormalCount++;
            }
            if (record.getCeshipingfen() != null) {
                totalScore += record.getCeshipingfen();
                scoreCount++;
            }

            String rating = StringUtils.trimToEmpty(record.getCeshipingji());
            if ("优秀".equals(rating) || "良好".equals(rating) || "及格".equals(rating) || "不及格".equals(rating)) {
                ratingDist.put(rating, ratingDist.get(rating) + 1);
            } else {
                ratingDist.put("未评级", ratingDist.get("未评级") + 1);
            }

            if (StringUtils.isNotBlank(record.getWeakItems())) {
                String[] items = record.getWeakItems().split("[、,，;；/\\s]+");
                for (String item : items) {
                    if (StringUtils.isBlank(item)) {
                        continue;
                    }
                    Integer c = weakItemCount.get(item);
                    weakItemCount.put(item, c == null ? 1 : c + 1);
                }
            }
        }

        List<Map<String, Object>> weakItemTop = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, Integer> e : weakItemCount.entrySet()) {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("item", e.getKey());
            m.put("count", e.getValue());
            weakItemTop.add(m);
        }
        weakItemTop.sort((a, b) -> Integer.compare((Integer) b.get("count"), (Integer) a.get("count")));
        if (weakItemTop.size() > 10) {
            weakItemTop = weakItemTop.subList(0, 10);
        }

        Integer rosterCount = null;
        Integer absentCount = null;
        int pendingScoreCount = 0;
        Set<String> testedAccounts = new HashSet<String>();
        for (CeshichengjiEntity record : scoreList) {
            if (StringUtils.isNotBlank(record.getYonghuzhanghao())) {
                testedAccounts.add(record.getYonghuzhanghao());
            }
        }
        for (String account : submittedAccounts) {
            if (!testedAccounts.contains(account) && !exemptAccounts.contains(account)) {
                pendingScoreCount++;
            }
        }
        if (StringUtils.isNotBlank(banji)) {
            List<YonghuEntity> roster = yonghuService.selectList(new EntityWrapper<YonghuEntity>().eq("banji", banji));
            rosterCount = roster == null ? 0 : roster.size();
            int missing = 0;
            if (roster != null) {
                for (YonghuEntity u : roster) {
                    String acc = u.getYonghuzhanghao();
                    if (StringUtils.isBlank(acc)) {
                        continue;
                    }
                    if (!submittedAccounts.contains(acc)) {
                        missing++;
                    }
                }
            }
            absentCount = missing;
        }

        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("ceshibianhao", ceshibianhao);
        data.put("banji", banji);
        data.put("testedCount", scoreList.size());
        data.put("submittedCount", submittedAccounts.size());
        data.put("pendingScoreCount", pendingScoreCount);
        data.put("exemptCount", exemptAccounts.size());
        data.put("abnormalCount", abnormalCount);
        data.put("avgScore", scoreCount == 0 ? null : (double) Math.round((totalScore * 10D / scoreCount)) / 10D);
        data.put("ratingDistribution", ratingDist);
        data.put("weakItemTop", weakItemTop);
        data.put("rosterCount", rosterCount);
        data.put("absentCount", absentCount);
        return R.ok().put("data", data);
    }

    /**
     * 生成并回写（单条）个性化建议到“测试评价”
     */
    @RequestMapping("/aiSuggest/{id}")
    public R aiSuggest(@PathVariable("id") Long id, HttpServletRequest request) {
        if (!isTeacherOrAdminSession(request)) {
            return R.error("仅教师或管理员可生成AI建议");
        }
        CeshichengjiEntity record = ceshichengjiService.selectById(id);
        if (record == null) {
            return R.error("记录不存在");
        }
        String advice = aiAdviceService.generateAdvice(record);
        record.setCeshipingjia(advice);
        ceshichengjiService.updateById(record);
        return R.ok().put("data", advice);
    }

    /**
     * 仅预览 AI 建议（不写库）
     */
    @RequestMapping("/aiSuggestPreview")
    public R aiSuggestPreview(@RequestBody CeshichengjiEntity record, HttpServletRequest request) {
        if (!isTeacherOrAdminSession(request)) {
            return R.error("仅教师或管理员可生成AI建议");
        }
        enrichStudentFields(record);
        ScoreAnalysisResult analysis = scoreAnalyzeService.analyze(record);
        scoreAnalyzeService.fillComputedFields(record, analysis);
        String advice = aiAdviceService.generateAdvice(record);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("advice", advice);
        data.put("analysis", analysis.toMap());
        return R.ok().put("data", data);
    }

    /**
     * 批量生成并回写个性化建议（按测试编号/班级筛选）
     */
    @RequestMapping("/aiSuggestBatch")
    public R aiSuggestBatch(@RequestParam("ceshibianhao") String ceshibianhao,
                            @RequestParam(value = "banji", required = false) String banji,
                            @RequestParam(value = "limit", required = false, defaultValue = "50") Integer limit,
                            HttpServletRequest request) {
        if (!isTeacherOrAdminSession(request)) {
            return R.error("仅教师或管理员可批量生成AI建议");
        }
        Wrapper<CeshichengjiEntity> ew = new EntityWrapper<CeshichengjiEntity>().eq("ceshibianhao", ceshibianhao);
        if (StringUtils.isNotBlank(banji)) {
            ew.eq("banji", banji);
        }
        ew.orderBy("id", true);
        List<CeshichengjiEntity> list = ceshichengjiService.selectList(ew);
        int processed = 0;
        List<Map<String, Object>> failures = new ArrayList<Map<String, Object>>();
        for (CeshichengjiEntity r : list) {
            if (processed >= limit) {
                break;
            }
            try {
                String advice = aiAdviceService.generateAdvice(r);
                r.setCeshipingjia(advice);
                ceshichengjiService.updateById(r);
                processed++;
            } catch (Exception ex) {
                Map<String, Object> f = new HashMap<String, Object>();
                f.put("id", r.getId());
                f.put("yonghuzhanghao", r.getYonghuzhanghao());
                f.put("message", ex.getMessage());
                failures.add(f);
            }
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("processed", processed);
        data.put("failures", failures);
        return R.ok().put("data", data);
    }

    /**
     * 按“测试编号+学生账号(+教师工号)”定位成绩（用于报告页判断是新增还是修改）
     */
    @RequestMapping("/matchByReport")
    public R matchByReport(@RequestParam(value = "ceshibianhao", required = false) String ceshibianhao,
                           @RequestParam(value = "ceshimingcheng", required = false) String ceshimingcheng,
                           @RequestParam("yonghuzhanghao") String yonghuzhanghao,
                           @RequestParam(value = "jiaoshigonghao", required = false) String jiaoshigonghao,
                           HttpServletRequest request) {
        if (request == null || request.getSession() == null) {
            return R.error("会话已失效，请重新登录");
        }
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        String username = String.valueOf(request.getSession().getAttribute("username"));
        boolean isTeacherOrAdmin = isTeacherOrAdmin(tableName);
        boolean isStudent = "yonghu".equals(tableName);
        if (!isTeacherOrAdmin && !isStudent) {
            return R.error("仅教师、管理员或学生可操作");
        }

        if (isStudent) {
            if (StringUtils.isBlank(username)) {
                return R.error("登录信息缺失，请重新登录");
            }
            if (StringUtils.isNotBlank(yonghuzhanghao) && !StringUtils.equals(yonghuzhanghao.trim(), username)) {
                return R.error("仅可查询本人成绩状态");
            }
            yonghuzhanghao = username;
        }

        if (StringUtils.isBlank(yonghuzhanghao)) {
            return R.error("学生账号不能为空");
        }
        if (StringUtils.isBlank(ceshibianhao) && StringUtils.isBlank(ceshimingcheng)) {
            return R.error("测试编号和测试名称至少填写一个");
        }

        EntityWrapper<CeshichengjiEntity> ew = new EntityWrapper<CeshichengjiEntity>();
        ew.eq("yonghuzhanghao", yonghuzhanghao.trim());
        if (StringUtils.isNotBlank(ceshibianhao)) {
            ew.eq("ceshibianhao", ceshibianhao.trim());
        } else {
            ew.eq("ceshimingcheng", ceshimingcheng.trim());
        }

        boolean hasTeacherFilter = false;
        if ("jiaoshi".equals(tableName) && StringUtils.isNotBlank(username)) {
            ew.eq("jiaoshigonghao", username);
            hasTeacherFilter = true;
        } else if (StringUtils.isNotBlank(jiaoshigonghao)) {
            ew.eq("jiaoshigonghao", jiaoshigonghao.trim());
            hasTeacherFilter = true;
        }

        ew.orderBy("id", false);
        ew.last("limit 1");
        List<CeshichengjiEntity> list = ceshichengjiService.selectList(ew);
        if ((list == null || list.isEmpty()) && hasTeacherFilter) {
            // 兼容历史数据：教师工号字段可能存在历史不一致，回退为“同测试+同学生”匹配
            EntityWrapper<CeshichengjiEntity> fallback = new EntityWrapper<CeshichengjiEntity>();
            fallback.eq("yonghuzhanghao", yonghuzhanghao.trim());
            if (StringUtils.isNotBlank(ceshibianhao)) {
                fallback.eq("ceshibianhao", ceshibianhao.trim());
            } else {
                fallback.eq("ceshimingcheng", ceshimingcheng.trim());
            }
            fallback.orderBy("id", false);
            fallback.last("limit 1");
            list = ceshichengjiService.selectList(fallback);
        }
        CeshichengjiEntity hit = (list == null || list.isEmpty()) ? null : list.get(0);

        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("hasScore", hit != null);
        data.put("scoreId", hit == null ? null : hit.getId());
        data.put("status", hit == null ? "未给成绩" : "已给成绩");
        return R.ok().put("data", data);
    }

    /**
     * 提醒接口
     */
    @RequestMapping("/remind/{columnName}/{type}")
    public R remindCount(@PathVariable("columnName") String columnName, HttpServletRequest request,
                         @PathVariable("type") String type, @RequestParam Map<String, Object> map) {
        map.put("column", columnName);
        map.put("type", type);

        if ("2".equals(type)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar c = Calendar.getInstance();
            if (map.get("remindstart") != null) {
                Integer remindStart = Integer.parseInt(map.get("remindstart").toString());
                c.setTime(new Date());
                c.add(Calendar.DAY_OF_MONTH, remindStart);
                map.put("remindstart", sdf.format(c.getTime()));
            }
            if (map.get("remindend") != null) {
                Integer remindEnd = Integer.parseInt(map.get("remindend").toString());
                c.setTime(new Date());
                c.add(Calendar.DAY_OF_MONTH, remindEnd);
                map.put("remindend", sdf.format(c.getTime()));
            }
        }

        Wrapper<CeshichengjiEntity> wrapper = new EntityWrapper<CeshichengjiEntity>();
        if (map.get("remindstart") != null) {
            wrapper.ge(columnName, map.get("remindstart"));
        }
        if (map.get("remindend") != null) {
            wrapper.le(columnName, map.get("remindend"));
        }

        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        if ("jiaoshi".equals(tableName)) {
            wrapper.eq("jiaoshigonghao", String.valueOf(request.getSession().getAttribute("username")));
        }
        if ("yonghu".equals(tableName)) {
            wrapper.eq("yonghuzhanghao", String.valueOf(request.getSession().getAttribute("username")));
        }

        int count = ceshichengjiService.selectCount(wrapper);
        return R.ok().put("count", count);
    }

    private void fillContextFromSession(CeshichengjiEntity<?> record, HttpServletRequest request) {
        if (record == null || request == null || request.getSession() == null) {
            return;
        }
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        String username = String.valueOf(request.getSession().getAttribute("username"));

        if ("jiaoshi".equals(tableName) && StringUtils.isNotBlank(username)) {
            // 教师提交成绩时，教师身份以会话为准，避免跨表带入旧值导致归属错乱
            record.setJiaoshigonghao(username);
            JiaoshiEntity<?> j = jiaoshiService.selectOne(new EntityWrapper<JiaoshiEntity>().eq("jiaoshigonghao", username));
            if (j != null && StringUtils.isNotBlank(j.getJiaoshixingming())) {
                record.setJiaoshixingming(j.getJiaoshixingming());
            }
        }

        if ("yonghu".equals(tableName) && StringUtils.isNotBlank(username)
                && StringUtils.isBlank(record.getYonghuzhanghao())) {
            record.setYonghuzhanghao(username);
        }

        if ("users".equals(tableName) && StringUtils.isNotBlank(username)) {
            if (StringUtils.isBlank(record.getJiaoshigonghao())) {
                record.setJiaoshigonghao(username);
            }
            if (StringUtils.isBlank(record.getJiaoshixingming())) {
                record.setJiaoshixingming("管理员");
            }
        }
    }

    private CeshichengjiEntity findExistingScoreForUpsert(CeshichengjiEntity<?> record, HttpServletRequest request) {
        if (record == null || StringUtils.isBlank(record.getYonghuzhanghao()) || request == null || request.getSession() == null) {
            return null;
        }
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        String username = String.valueOf(request.getSession().getAttribute("username"));

        EntityWrapper<CeshichengjiEntity> ew = new EntityWrapper<CeshichengjiEntity>();
        ew.eq("yonghuzhanghao", record.getYonghuzhanghao().trim());
        if (StringUtils.isNotBlank(record.getCeshibianhao())) {
            ew.eq("ceshibianhao", record.getCeshibianhao().trim());
        } else if (StringUtils.isNotBlank(record.getCeshimingcheng())) {
            ew.eq("ceshimingcheng", record.getCeshimingcheng().trim());
        } else {
            return null;
        }

        if ("jiaoshi".equals(tableName) && StringUtils.isNotBlank(username)) {
            ew.eq("jiaoshigonghao", username);
        } else if (StringUtils.isNotBlank(record.getJiaoshigonghao())) {
            ew.eq("jiaoshigonghao", record.getJiaoshigonghao().trim());
        }
        ew.orderBy("id", false);
        ew.last("limit 1");
        List<CeshichengjiEntity> list = ceshichengjiService.selectList(ew);
        if ((list == null || list.isEmpty()) && "jiaoshi".equals(tableName)) {
            // 兼容历史数据：教师工号写错时，按同测试同学生回退一次
            EntityWrapper<CeshichengjiEntity> fallback = new EntityWrapper<CeshichengjiEntity>();
            fallback.eq("yonghuzhanghao", record.getYonghuzhanghao().trim());
            if (StringUtils.isNotBlank(record.getCeshibianhao())) {
                fallback.eq("ceshibianhao", record.getCeshibianhao().trim());
            } else {
                fallback.eq("ceshimingcheng", record.getCeshimingcheng().trim());
            }
            fallback.orderBy("id", false);
            fallback.last("limit 1");
            list = ceshichengjiService.selectList(fallback);
        }
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    private static boolean isTeacherOrAdmin(String tableName) {
        return "jiaoshi".equals(tableName) || "users".equals(tableName);
    }

    private static boolean isTeacherOrAdminSession(HttpServletRequest request) {
        if (request == null || request.getSession() == null) {
            return false;
        }
        String tableName = String.valueOf(request.getSession().getAttribute("tableName"));
        return isTeacherOrAdmin(tableName);
    }

    private TizhiceshiEntity<?> resolveImportTaskForImport(Long tizhiceshiId,
                                                           String ceshibianhao,
                                                           String teacherNo,
                                                           String tableName) {
        EntityWrapper<TizhiceshiEntity> wrapper = new EntityWrapper<TizhiceshiEntity>();
        if ("jiaoshi".equals(tableName) && StringUtils.isNotBlank(teacherNo)) {
            wrapper.eq("jiaoshigonghao", teacherNo);
        }
        if (tizhiceshiId != null) {
            wrapper.eq("id", tizhiceshiId);
            return tizhiceshiService.selectOne(wrapper);
        }
        if (StringUtils.isBlank(ceshibianhao)) {
            return null;
        }
        wrapper.eq("ceshibianhao", ceshibianhao.trim());
        return tizhiceshiService.selectOne(wrapper);
    }

    private void addTaskClassesForImport(Set<String> banjiSet, String ceshibianhao, String teacherNo) {
        if (banjiSet == null || StringUtils.isBlank(ceshibianhao)) {
            return;
        }

        EntityWrapper<CeshichengjiEntity> scoreWrapper = new EntityWrapper<CeshichengjiEntity>();
        scoreWrapper.eq("ceshibianhao", ceshibianhao.trim());
        if (StringUtils.isNotBlank(teacherNo)) {
            scoreWrapper.eq("jiaoshigonghao", teacherNo);
        }
        List<CeshichengjiEntity> scores = ceshichengjiService.selectList(scoreWrapper);
        if (scores != null) {
            for (CeshichengjiEntity score : scores) {
                addTextOption(banjiSet, score.getBanji());
            }
        }

        EntityWrapper<CeshibaogaoEntity> reportWrapper = new EntityWrapper<CeshibaogaoEntity>();
        reportWrapper.eq("ceshibianhao", ceshibianhao.trim());
        if (StringUtils.isNotBlank(teacherNo)) {
            reportWrapper.eq("jiaoshigonghao", teacherNo);
        }
        List<CeshibaogaoEntity> reports = ceshibaogaoService.selectList(reportWrapper);
        if (reports != null) {
            for (CeshibaogaoEntity report : reports) {
                addTextOption(banjiSet, report.getBanji());
            }
        }
    }

    private void enrichStudentFields(CeshichengjiEntity<?> record) {
        if (record == null) {
            return;
        }

        YonghuEntity<?> student = null;
        if (StringUtils.isNotBlank(record.getYonghuzhanghao())) {
            student = yonghuService.selectOne(new EntityWrapper<YonghuEntity>().eq("yonghuzhanghao", record.getYonghuzhanghao()));
        }
        if (student == null && StringUtils.isNotBlank(record.getYonghuxingming()) && StringUtils.isNotBlank(record.getBanji())) {
            student = yonghuService.selectOne(new EntityWrapper<YonghuEntity>()
                    .eq("yonghuxingming", record.getYonghuxingming())
                    .eq("banji", record.getBanji()));
            if (student != null && StringUtils.isBlank(record.getYonghuzhanghao())) {
                record.setYonghuzhanghao(student.getYonghuzhanghao());
            }
        }

        if (student != null) {
            if (StringUtils.isBlank(record.getYonghuxingming())) {
                record.setYonghuxingming(student.getYonghuxingming());
            }
            if (StringUtils.isBlank(record.getBanji())) {
                record.setBanji(student.getBanji());
            }
            if (StringUtils.isBlank(record.getGender())) {
                record.setGender(student.getXingbie());
            }
        }
        if (record.getGrade() == null) {
            Integer inferredGrade = inferGradeFromClassName(record.getBanji());
            if (inferredGrade != null) {
                record.setGrade(inferredGrade);
            }
        }
    }

    private static Integer inferGradeFromClassName(String className) {
        if (StringUtils.isBlank(className)) {
            return null;
        }
        String s = className.trim();
        if (s.contains("大一") || s.contains("高一") || s.contains("一年级") || s.contains("一年")) {
            return 1;
        }
        if (s.contains("大二") || s.contains("高二") || s.contains("二年级") || s.contains("二年")) {
            return 2;
        }
        if (s.contains("大三") || s.contains("高三") || s.contains("三年级") || s.contains("三年")) {
            return 3;
        }
        if (s.contains("大四") || s.contains("四年级") || s.contains("四年")) {
            return 4;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '1' && c <= '9') {
                return c - '0';
            }
        }
        return null;
    }

    private static boolean hasAnyDetailedMetrics(CeshichengjiEntity<?> entity) {
        return entity.getRun50m() != null
                || entity.getRun1000m() != null
                || entity.getRun800m() != null
                || entity.getLongJump() != null
                || entity.getPullUp() != null
                || entity.getSitUp() != null
                || entity.getBmi() != null;
    }

    private static Long randomId() {
        return new Date().getTime() + (long) Math.floor(Math.random() * 1000D);
    }

    private static String pick(Map<String, String> row, String... keys) {
        if (row == null || keys == null) {
            return null;
        }
        for (String k : keys) {
            if (StringUtils.isBlank(k)) {
                continue;
            }
            String v = row.get(k.replaceAll("\\s+", ""));
            if (StringUtils.isNotBlank(v)) {
                return v.trim();
            }
        }
        return null;
    }

    private static Map<String, Object> err(int rowIndex, String code, String message, Map<String, String> row) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("row", rowIndex);
        m.put("code", code);
        m.put("message", message);
        m.put("data", row);
        return m;
    }

    private static Double parseDoubleField(Map<String, String> row, List<String> rowErrors, String... keys) {
        String raw = pick(row, keys);
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        Double value = parseFlexibleNumber(raw);
        if (value != null) {
            return value;
        }
        rowErrors.add((keys != null && keys.length > 0 ? keys[0] : "字段") + "格式错误：" + raw);
        return null;
    }

    private static Integer parseIntegerField(Map<String, String> row, List<String> rowErrors, String... keys) {
        String raw = pick(row, keys);
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        Double value = parseFlexibleNumber(raw);
        if (value == null) {
            rowErrors.add((keys != null && keys.length > 0 ? keys[0] : "字段") + "格式错误：" + raw);
            return null;
        }
        long rounded = Math.round(value);
        if (Math.abs(value - rounded) <= 0.000001D) {
            return (int) rounded;
        }
        rowErrors.add((keys != null && keys.length > 0 ? keys[0] : "字段") + "格式错误：" + raw);
        return null;
    }

    private static Integer parseGradeField(Map<String, String> row, List<String> rowErrors, String... keys) {
        String raw = pick(row, keys);
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        String s = normalizeRaw(raw);
        if (StringUtils.isBlank(s)) {
            return null;
        }
        if (s.matches("^[-+]?\\d+$")) {
            return Integer.parseInt(s);
        }
        if ("大一".equals(s) || "高一".equals(s) || "一年级".equals(s) || "一年".equals(s)) {
            return 1;
        }
        if ("大二".equals(s) || "高二".equals(s) || "二年级".equals(s) || "二年".equals(s)) {
            return 2;
        }
        if ("大三".equals(s) || "高三".equals(s) || "三年级".equals(s) || "三年".equals(s)) {
            return 3;
        }
        if ("大四".equals(s) || "四年级".equals(s) || "四年".equals(s)) {
            return 4;
        }
        Double numeric = parseFlexibleNumber(raw);
        if (numeric != null) {
            return (int) Math.round(numeric);
        }
        rowErrors.add((keys != null && keys.length > 0 ? keys[0] : "字段") + "格式错误：" + raw);
        return null;
    }

    private static Double parseTimeSecondsField(Map<String, String> row, List<String> rowErrors, String... keys) {
        String raw = pick(row, keys);
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        Double value = parseTimeToSeconds(raw);
        if (value != null) {
            return value;
        }
        rowErrors.add((keys != null && keys.length > 0 ? keys[0] : "字段") + "格式错误：" + raw);
        return null;
    }

    private static Double parseDistanceCmField(Map<String, String> row, List<String> rowErrors, String... keys) {
        String raw = pick(row, keys);
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        Double value = parseDistanceToCm(raw);
        if (value != null) {
            return value;
        }
        rowErrors.add((keys != null && keys.length > 0 ? keys[0] : "字段") + "格式错误：" + raw);
        return null;
    }

    private static Integer parseAndFixScore(String raw, List<Map<String, Object>> corrections, int rowIndex) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        Double d = parseFlexibleNumber(raw);
        if (d == null) {
            return null;
        }
        long rounded = Math.round(d);
        long fixed = rounded;
        boolean corrected = false;
        while (fixed > 100 && fixed % 10 == 0) {
            fixed = fixed / 10;
            corrected = true;
        }
        if (corrected && corrections != null) {
            Map<String, Object> c = new HashMap<String, Object>();
            c.put("row", rowIndex);
            c.put("field", "ceshipingfen");
            c.put("from", rounded);
            c.put("to", fixed);
            corrections.add(c);
        }
        return (int) fixed;
    }

    private static Double parseFlexibleNumber(String raw) {
        String s = normalizeRaw(raw);
        if (StringUtils.isBlank(s) || s.contains(":")) {
            return null;
        }
        if (s.matches("^[-+]?\\d+(\\.\\d+)?$")) {
            return Double.parseDouble(s);
        }
        if (s.matches("^([-+]?\\d+(\\.\\d+)?)[a-zA-Z\\u4e00-\\u9fa5/%^0-9]+$")) {
            String n = s.replaceAll("([a-zA-Z\\u4e00-\\u9fa5/%^0-9]+)$", "");
            if (n.matches("^[-+]?\\d+(\\.\\d+)?$")) {
                return Double.parseDouble(n);
            }
        }
        return null;
    }

    private static Double parseTimeToSeconds(String raw) {
        String s = normalizeRaw(raw).toLowerCase();
        if (StringUtils.isBlank(s)) {
            return null;
        }
        if (s.matches("^[-+]?\\d+(\\.\\d+)?$")) {
            return Double.parseDouble(s);
        }

        // 支持 mm:ss / m:ss.ss
        if (s.matches("^\\d{1,2}:\\d{1,2}(\\.\\d+)?$")) {
            String[] parts = s.split(":");
            double minute = Double.parseDouble(parts[0]);
            double second = Double.parseDouble(parts[1]);
            if (second >= 60D) {
                return null;
            }
            return minute * 60D + second;
        }

        // 支持 3分45秒
        if (s.matches("^\\d+(\\.\\d+)?分\\d+(\\.\\d+)?秒?$")) {
            String[] parts = s.split("分");
            double minute = Double.parseDouble(parts[0]);
            String secondText = parts[1].replace("秒", "");
            double second = Double.parseDouble(secondText);
            if (second >= 60D) {
                return null;
            }
            return minute * 60D + second;
        }

        // 支持 3m45s / 3min45s / 3分钟45秒
        String compact = s.replace("分钟", "min");
        if (compact.matches("^\\d+(\\.\\d+)?(min|m)\\d+(\\.\\d+)?(s|sec|秒)?$")) {
            String[] parts = compact.split("(min|m)");
            if (parts.length == 2) {
                double minute = Double.parseDouble(parts[0]);
                String secondText = parts[1].replaceAll("(s|sec|秒)$", "");
                if (secondText.matches("^\\d+(\\.\\d+)?$")) {
                    double second = Double.parseDouble(secondText);
                    if (second >= 60D) {
                        return null;
                    }
                    return minute * 60D + second;
                }
            }
        }

        // 支持 225s / 225秒
        if (s.matches("^[-+]?\\d+(\\.\\d+)?(s|sec|secs|second|seconds|秒)$")) {
            String secondText = s.replaceAll("(s|sec|secs|second|seconds|秒)$", "");
            if (secondText.matches("^[-+]?\\d+(\\.\\d+)?$")) {
                return Double.parseDouble(secondText);
            }
        }
        return null;
    }

    private static Double parseDistanceToCm(String raw) {
        String s = normalizeRaw(raw).toLowerCase();
        if (StringUtils.isBlank(s)) {
            return null;
        }
        if (s.matches("^[-+]?\\d+(\\.\\d+)?$")) {
            return Double.parseDouble(s);
        }
        if (s.matches("^[-+]?\\d+(\\.\\d+)?(cm|厘米)$")) {
            String value = s.replaceAll("(cm|厘米)$", "");
            return Double.parseDouble(value);
        }
        if (s.matches("^[-+]?\\d+(\\.\\d+)?(m|米)$")) {
            String value = s.replaceAll("(m|米)$", "");
            return Double.parseDouble(value) * 100D;
        }
        return null;
    }

    private static String normalizeRaw(String raw) {
        if (raw == null) {
            return null;
        }
        return raw.trim()
                .replace("：", ":")
                .replace("，", ",")
                .replace("。", ".")
                .replace("．", ".")
                .replace("＋", "+")
                .replace("－", "-")
                .replace("（", "(")
                .replace("）", ")")
                .replaceAll("\\s+", "");
    }

    private static void addTestOption(Map<String, Map<String, Object>> testMap,
                                      String ceshibianhao,
                                      String ceshimingcheng) {
        String testNo = StringUtils.trimToEmpty(ceshibianhao);
        if (StringUtils.isBlank(testNo)) {
            return;
        }
        Map<String, Object> item = testMap.get(testNo);
        if (item == null) {
            item = new LinkedHashMap<String, Object>();
            item.put("ceshibianhao", testNo);
            item.put("ceshimingcheng", StringUtils.trimToEmpty(ceshimingcheng));
            testMap.put(testNo, item);
            return;
        }

        Object existingName = item.get("ceshimingcheng");
        if (StringUtils.isBlank(existingName == null ? null : String.valueOf(existingName))
                && StringUtils.isNotBlank(ceshimingcheng)) {
            item.put("ceshimingcheng", ceshimingcheng.trim());
        }
    }

    private static void addTextOption(Set<String> options, String value) {
        if (StringUtils.isNotBlank(value)) {
            options.add(value.trim());
        }
    }

    private static boolean isExemptReport(CeshibaogaoEntity report) {
        return report != null && normalizeReportRemark(report.getBeizhu()).contains("免测");
    }

    private static String reportExemptReason(CeshibaogaoEntity report) {
        String remark = report == null ? "" : normalizeReportRemark(report.getBeizhu());
        return StringUtils.isBlank(remark) ? "报告备注标记免测" : remark;
    }

    private static String normalizeReportRemark(String remark) {
        if (StringUtils.isBlank(remark)) {
            return "";
        }
        return remark
                .replaceAll("<[^>]*>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("　", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String scoreToRating(int score) {
        if (score >= 85) {
            return "优秀";
        }
        if (score >= 75) {
            return "良好";
        }
        if (score >= 50) {
            return "及格";
        }
        return "不及格";
    }
}
