<template>
  <div class="addEdit-block">
    <el-form ref="ruleForm" :model="ruleForm" :rules="rules" label-width="100px" size="small" class="detail-form-content">
      <el-card shadow="never" class="mb12">
        <div slot="header">基础信息</div>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="测试编号" prop="ceshibianhao">
              <el-input v-model="ruleForm.ceshibianhao" :readonly="baseInfoReadonly" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="测试名称" prop="ceshimingcheng">
              <el-input v-model="ruleForm.ceshimingcheng" :readonly="baseInfoReadonly" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="评分时间" prop="pingfenshijian">
              <el-date-picker
                v-model="ruleForm.pingfenshijian"
                type="date"
                format="yyyy-MM-dd"
                value-format="yyyy-MM-dd"
                :disabled="isInfo"
                placeholder="评分时间"
                style="width:100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="教师工号" prop="jiaoshigonghao">
              <el-input v-model="ruleForm.jiaoshigonghao" :readonly="baseInfoReadonly" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="教师姓名" prop="jiaoshixingming">
              <el-input v-model="ruleForm.jiaoshixingming" :readonly="baseInfoReadonly" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="班级" prop="banji">
              <el-input v-model="ruleForm.banji" :readonly="baseInfoReadonly" />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="学生账号" prop="yonghuzhanghao">
              <el-input v-model="ruleForm.yonghuzhanghao" :readonly="baseInfoReadonly" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="学生姓名" prop="yonghuxingming">
              <el-input v-model="ruleForm.yonghuxingming" :readonly="baseInfoReadonly" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="ruleForm.gender" :disabled="baseInfoReadonly" clearable placeholder="请选择" style="width:100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="测试评分" prop="ceshipingfen">
              <el-input v-model="ruleForm.ceshipingfen" :readonly="isInfo" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="测试评级" prop="ceshipingji">
              <el-select v-model="ruleForm.ceshipingji" :disabled="isInfo" clearable style="width:100%">
                <el-option v-for="item in ceshipingjiOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <el-card shadow="never" class="mb12">
        <div slot="header">细分成绩</div>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="50米(秒)" prop="run50m">
              <el-input v-model="ruleForm.run50m" :readonly="isInfo" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="1000米(秒)" prop="run1000m">
              <el-input v-model="ruleForm.run1000m" :readonly="isInfo" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="800米(秒)" prop="run800m">
              <el-input v-model="ruleForm.run800m" :readonly="isInfo" />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="立定跳远(cm)" prop="longJump">
              <el-input v-model="ruleForm.longJump" :readonly="isInfo" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="引体向上" prop="pullUp">
              <el-input v-model="ruleForm.pullUp" :readonly="isInfo" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="仰卧起坐" prop="sitUp">
              <el-input v-model="ruleForm.sitUp" :readonly="isInfo" />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="BMI" prop="bmi">
              <el-input v-model="ruleForm.bmi" :readonly="isInfo" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="异常状态">
              <el-tag :type="ruleForm.abnormalFlag===1?'danger':'success'">{{ ruleForm.abnormalFlag===1?'异常':'正常' }}</el-tag>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="异常原因">
              <el-input v-model="ruleForm.abnormalReason" readonly />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="薄弱项">
              <el-input v-model="ruleForm.weakItems" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优势项">
              <el-input v-model="ruleForm.strongItems" readonly />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <el-card shadow="never" class="mb12">
        <div slot="header">训练建议</div>
        <el-form-item label="测试评价" prop="ceshipingjia">
          <editor v-if="!isInfo" v-model="ruleForm.ceshipingjia" class="editor" action="file/upload" style="min-width: 240px" />
          <div v-else v-html="ruleForm.ceshipingjia"></div>
        </el-form-item>
      </el-card>

      <el-card shadow="never" class="mb12" v-if="analysis">
        <div slot="header">智能分析结果</div>
        <el-descriptions :column="4" border size="small">
          <el-descriptions-item label="是否异常">{{ analysis.abnormal ? '是':'否' }}</el-descriptions-item>
          <el-descriptions-item label="综合分">{{ analysis.compositeScore == null ? '--' : analysis.compositeScore }}</el-descriptions-item>
          <el-descriptions-item label="评级">{{ analysis.rating || '--' }}</el-descriptions-item>
          <el-descriptions-item label="异常条数">{{ (analysis.abnormalMessages || []).length }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-form-item>
        <el-button v-if="!isInfo" type="warning" icon="el-icon-data-analysis" @click="doValidate">智能校验</el-button>
        <el-button v-if="!isInfo" type="primary" plain icon="el-icon-magic-stick" @click="previewAiAdvice">AI建议预览</el-button>
        <el-button v-if="!isInfo" type="primary" @click="onSubmit">提交</el-button>
        <el-button @click="back">{{ isInfo ? '返回':'取消' }}</el-button>
      </el-form-item>
    </el-form>

    <el-dialog title="AI建议预览" :visible.sync="aiPreviewDialogVisible" width="760px">
      <el-alert type="info" show-icon :closable="false" title="下方为预览内容，点击“应用到测试评价”可自动写入编辑框" />
      <div class="ai-preview">{{ aiPreviewText }}</div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="aiPreviewDialogVisible=false">关闭</el-button>
        <el-button type="primary" @click="applyAiPreview">应用到测试评价</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { isNumber, isIntNumer } from '@/utils/validate'

export default {
  props: ['parent'],
  data() {
    const validateNumber = (rule, value, callback) => {
      if (!value) {
        callback()
      } else if (!isNumber(value)) {
        callback(new Error('请输入数字'))
      } else {
        callback()
      }
    }
    const validateInt = (rule, value, callback) => {
      if (!value) {
        callback()
      } else if (!isIntNumer(value)) {
        callback(new Error('请输入整数'))
      } else {
        callback()
      }
    }

    return {
      id: '',
      type: 'else',
      ceshipingjiOptions: ['优秀', '良好', '及格', '不及格', '免测'],
      analysis: null,
      aiPreviewDialogVisible: false,
      aiPreviewText: '',
      reportContext: null,
      ruleForm: {
        id: '',
        ceshibianhao: '',
        ceshimingcheng: '',
        jiaoshigonghao: '',
        jiaoshixingming: '',
        ceshipingfen: '',
        ceshipingji: '',
        pingfenshijian: '',
        yonghuzhanghao: '',
        yonghuxingming: '',
        banji: '',
        ceshipingjia: '',
        run50m: '',
        run1000m: '',
        run800m: '',
        longJump: '',
        pullUp: '',
        sitUp: '',
        bmi: '',
        gender: '',
        abnormalFlag: 0,
        abnormalReason: '',
        weakItems: '',
        strongItems: ''
      },
      rules: {
        ceshibianhao: [{ required: true, message: '请输入测试编号', trigger: 'blur' }],
        ceshimingcheng: [{ required: true, message: '请输入测试名称', trigger: 'blur' }],
        yonghuzhanghao: [{ required: true, message: '请输入学生账号', trigger: 'blur' }],
        ceshipingfen: [{ validator: validateInt, trigger: 'blur' }],
        run50m: [{ validator: validateNumber, trigger: 'blur' }],
        run1000m: [{ validator: validateNumber, trigger: 'blur' }],
        run800m: [{ validator: validateNumber, trigger: 'blur' }],
        longJump: [{ validator: validateNumber, trigger: 'blur' }],
        pullUp: [{ validator: validateInt, trigger: 'blur' }],
        sitUp: [{ validator: validateInt, trigger: 'blur' }],
        bmi: [{ validator: validateNumber, trigger: 'blur' }]
      }
    }
  },
  computed: {
    isInfo() {
      return this.type === 'info'
    },
    isCrossMode() {
      return this.type === 'cross' || this.type === 'crossEdit'
    },
    baseInfoReadonly() {
      return this.isInfo || this.isCrossMode
    }
  },
  created() {
    this.ruleForm.pingfenshijian = this.getCurDate()
  },
  methods: {
    init(id, type) {
      this.id = id || ''
      this.type = type || 'else'
      this.analysis = null
      this.aiPreviewText = ''
      this.aiPreviewDialogVisible = false
      this.reportContext = null
      this.resetForm()
      if (this.type === 'cross') {
        // 跨表（测试报告 -> 测试成绩）：始终按新增处理，避免把报告ID当成成绩ID
        this.id = ''
        this.applyCrossDefaults()
        this.fillSessionDefaults()
        return
      }
      if (this.type === 'crossEdit') {
        if (id) {
          this.info(id, () => {
            this.applyCrossDefaults({ preserveId: true })
            this.fillSessionDefaults()
          })
        } else {
          this.applyCrossDefaults()
          this.fillSessionDefaults()
        }
        return
      }
      if (id) {
        this.info(id)
      } else {
        this.fillSessionDefaults()
      }
    },
    getDefaultForm() {
      return {
        id: '',
        ceshibianhao: '',
        ceshimingcheng: '',
        jiaoshigonghao: '',
        jiaoshixingming: '',
        ceshipingfen: '',
        ceshipingji: '',
        pingfenshijian: this.getCurDate(),
        yonghuzhanghao: '',
        yonghuxingming: '',
        banji: '',
        ceshipingjia: '',
        run50m: '',
        run1000m: '',
        run800m: '',
        longJump: '',
        pullUp: '',
        sitUp: '',
        bmi: '',
        gender: '',
        abnormalFlag: 0,
        abnormalReason: '',
        weakItems: '',
        strongItems: ''
      }
    },
    resetForm() {
      this.ruleForm = this.getDefaultForm()
      this.reportContext = null
    },
    applyCrossDefaults(options) {
      const crossObj = this.$storage.getObj('crossObj') || {}
      const preserveId = options && options.preserveId
      this.reportContext = Object.assign({}, crossObj)
      if (!preserveId) {
        this.ruleForm.id = ''
      }
      this.ruleForm.ceshibianhao = crossObj.ceshibianhao || this.ruleForm.ceshibianhao
      this.ruleForm.ceshimingcheng = crossObj.ceshimingcheng || this.ruleForm.ceshimingcheng
      this.ruleForm.jiaoshigonghao = crossObj.jiaoshigonghao || this.ruleForm.jiaoshigonghao
      this.ruleForm.jiaoshixingming = crossObj.jiaoshixingming || this.ruleForm.jiaoshixingming
      this.ruleForm.yonghuzhanghao = crossObj.yonghuzhanghao || this.ruleForm.yonghuzhanghao
      this.ruleForm.yonghuxingming = crossObj.yonghuxingming || this.ruleForm.yonghuxingming
      this.ruleForm.banji = crossObj.banji || this.ruleForm.banji
      if (crossObj.beizhu && !this.ruleForm.ceshipingjia) {
        this.ruleForm.ceshipingjia = crossObj.beizhu
      }
      if (this.isReportMarkedExempt(crossObj.beizhu)) {
        this.ruleForm.ceshipingji = '免测'
      }
      if (this.ruleForm.yonghuzhanghao) {
        this.fillStudentByAccount(this.ruleForm.yonghuzhanghao)
      }
    },
    fillStudentByAccount(account) {
      this.$http({
        url: 'yonghu/page',
        method: 'get',
        params: {
          page: 1,
          limit: 1,
          yonghuzhanghao: account
        }
      }).then(({ data }) => {
        if (!(data && data.code === 0)) {
          return
        }
        const student = ((data.data || {}).list || [])[0]
        if (!student) {
          return
        }
        this.ruleForm.yonghuxingming = student.yonghuxingming || this.ruleForm.yonghuxingming
        this.ruleForm.banji = student.banji || this.ruleForm.banji
        this.ruleForm.gender = student.xingbie || this.ruleForm.gender
      })
    },
    fillSessionDefaults() {
      this.$http({
        url: `${this.$storage.get('sessionTable')}/session`,
        method: 'get'
      }).then(({ data }) => {
        if (!(data && data.code === 0)) {
          return
        }
        const json = data.data || {}
        if (this.$storage.get('sessionTable') === 'jiaoshi') {
          this.ruleForm.jiaoshigonghao = json.jiaoshigonghao || this.ruleForm.jiaoshigonghao
          this.ruleForm.jiaoshixingming = json.jiaoshixingming || this.ruleForm.jiaoshixingming
        }
        if (this.$storage.get('sessionTable') === 'yonghu') {
          this.ruleForm.yonghuzhanghao = json.yonghuzhanghao || this.ruleForm.yonghuzhanghao
          this.ruleForm.yonghuxingming = json.yonghuxingming || this.ruleForm.yonghuxingming
          this.ruleForm.banji = json.banji || this.ruleForm.banji
          this.ruleForm.gender = json.xingbie || this.ruleForm.gender
        }
      })
    },
    info(id, done) {
      this.$http({
        url: `ceshichengji/info/${id}`,
        method: 'get'
      }).then(({ data }) => {
        if (data && data.code === 0) {
          const info = data.data || {}
          if (info.ceshipingjia) {
            const reg = new RegExp('../../../upload', 'g')
            info.ceshipingjia = info.ceshipingjia.replace(reg, '../../../springboot415ef/upload')
          }
          this.ruleForm = Object.assign(this.getDefaultForm(), info)
          if (typeof done === 'function') {
            done()
          }
        } else {
          this.$message.error(data.msg || '加载详情失败')
        }
      })
    },
    doValidate() {
      if (this.ruleForm.ceshipingji === '免测') {
        this.$message.warning('已选择免测，无需进行成绩校验')
        return
      }
      const payload = this.buildPayload()
      this.$http({
        url: 'ceshichengji/validate',
        method: 'post',
        data: payload
      }).then(({ data }) => {
        if (data && data.code === 0) {
          const analysis = data.data || {}
          this.analysis = analysis
          if (analysis.compositeScore != null) {
            this.ruleForm.ceshipingfen = analysis.compositeScore
          }
          if (analysis.rating) {
            this.ruleForm.ceshipingji = analysis.rating
          }
          if (analysis.abnormalMessages) {
            this.ruleForm.abnormalFlag = analysis.abnormal ? 1 : 0
            this.ruleForm.abnormalReason = (analysis.abnormalMessages || []).join('、')
            this.ruleForm.weakItems = (analysis.weakItems || []).join('、')
            this.ruleForm.strongItems = (analysis.strongItems || []).join('、')
          }
          this.$message.success('校验完成')
        } else {
          this.$message.error(data.msg || '校验失败')
        }
      })
    },
    previewAiAdvice() {
      if (this.ruleForm.ceshipingji === '免测') {
        this.$message.warning('已选择免测，无需生成成绩建议')
        return
      }
      const payload = this.buildPayload()
      this.$http({
        url: 'ceshichengji/aiSuggestPreview',
        method: 'post',
        data: payload
      }).then(({ data }) => {
        if (data && data.code === 0) {
          const payload = data.data || {}
          this.aiPreviewText = payload.advice || ''
          if (payload.analysis) {
            this.analysis = payload.analysis
          }
          this.aiPreviewDialogVisible = true
        } else {
          this.$message.error(data.msg || 'AI预览失败')
        }
      })
    },
    applyAiPreview() {
      this.ruleForm.ceshipingjia = this.aiPreviewText
      this.aiPreviewDialogVisible = false
      this.$message.success('已写入测试评价')
    },
    onSubmit() {
      if (this.ruleForm.ceshipingji === '免测') {
        this.submitExempt()
        return
      }
      this.$refs.ruleForm.validate(valid => {
        if (!valid) {
          return
        }
        const payload = this.buildPayload()
        const saveScore = () => {
          this.$http({
            url: `ceshichengji/${!payload.id ? 'save' : 'update'}`,
            method: 'post',
            data: payload
          }).then(({ data }) => {
            if (data && data.code === 0) {
              if (data.analysis) {
                this.analysis = data.analysis
              }
              this.finishSubmit('操作成功')
            } else {
              this.$message.error(data.msg || '提交失败')
            }
          })
        }
        this.syncReportRemark(false).then(saveScore).catch(error => {
          this.$message.error((error && error.message) || '同步报告状态失败')
        })
      })
    },
    finishSubmit(message) {
      this.$message.success(message || '操作成功')
      this.parent.showFlag = true
      this.parent.addOrUpdateFlag = false
      this.parent.ceshichengjiCrossAddOrUpdateFlag = false
      this.parent.search()
    },
    submitExempt() {
      if (!this.reportContext || !this.reportContext.id) {
        this.$message.error('缺少报告记录，无法标记免测')
        return
      }
      this.syncReportRemark(true)
        .then(() => this.removeExistingScore())
        .then(() => {
          this.finishSubmit('已标记为免测')
        })
        .catch(error => {
          this.$message.error((error && error.message) || '免测提交失败')
        })
    },
    removeExistingScore() {
      const scoreId = this.ruleForm.id || (this.reportContext && this.reportContext.scoreId)
      if (!scoreId) {
        return Promise.resolve()
      }
      return this.$http({
        url: 'ceshichengji/delete',
        method: 'post',
        data: [Number(scoreId)]
      }).then(({ data }) => {
        if (!(data && data.code === 0)) {
          return Promise.reject(new Error((data && data.msg) || '免测已标记，但原成绩删除失败'))
        }
        this.ruleForm.id = ''
        if (this.reportContext) {
          this.reportContext.scoreId = ''
          this.reportContext.hasScore = false
        }
        return data
      })
    },
    syncReportRemark(markExempt) {
      if (!this.isCrossMode) {
        return Promise.resolve()
      }
      const report = Object.assign({}, this.reportContext || this.$storage.getObj('crossObj') || {})
      if (!report.id) {
        return Promise.resolve()
      }
      const nextRemark = markExempt ? this.buildExemptRemark(report.beizhu) : this.stripExemptRemark(report.beizhu)
      const payload = {
        id: report.id,
        ceshibianhao: report.ceshibianhao,
        ceshimingcheng: report.ceshimingcheng,
        jiaoshigonghao: report.jiaoshigonghao,
        jiaoshixingming: report.jiaoshixingming,
        baogaowenjian: report.baogaowenjian,
        tijiaoriqi: report.tijiaoriqi,
        yonghuzhanghao: report.yonghuzhanghao,
        yonghuxingming: report.yonghuxingming,
        banji: report.banji,
        beizhu: nextRemark,
        addtime: report.addtime
      }
      return this.$http({
        url: 'ceshibaogao/update',
        method: 'post',
        data: payload
      }).then(({ data }) => {
        if (!(data && data.code === 0)) {
          return Promise.reject(new Error((data && data.msg) || '报告备注同步失败'))
        }
        this.reportContext = Object.assign({}, report, { beizhu: nextRemark })
        return data
      })
    },
    normalizeRemark(remark) {
      return String(remark || '')
        .replace(/<[^>]*>/g, ' ')
        .replace(/&nbsp;/gi, ' ')
        .replace(/&amp;/gi, '&')
        .replace(/　/g, ' ')
        .replace(/\s+/g, ' ')
        .trim()
    },
    isReportMarkedExempt(remark) {
      return this.normalizeRemark(remark).indexOf('免测') !== -1
    },
    extractExemptReason(remark) {
      const normalized = this.normalizeRemark(remark)
      const match = normalized.match(/免测[:：]?\s*(.*)/)
      return match && match[1] ? match[1].trim() : ''
    },
    stripExemptRemark(remark) {
      let cleaned = String(remark || '')
      cleaned = cleaned.replace(/<p[^>]*>\s*免测(?:[:：][\s\S]*?)?<\/p>/gi, '')
      cleaned = cleaned.replace(/<div[^>]*>\s*免测(?:[:：][\s\S]*?)?<\/div>/gi, '')
      cleaned = cleaned.replace(/(?:^|<br\s*\/?>|\r?\n)\s*免测[:：]?[^\n\r<]*/gi, '')
      cleaned = cleaned.replace(/(<br\s*\/?>\s*){2,}/gi, '<br>')
      return cleaned.trim()
    },
    buildExemptRemark(remark) {
      const cleaned = this.stripExemptRemark(remark)
      const reason = this.extractExemptReason(this.ruleForm.ceshipingjia) || this.normalizeRemark(this.ruleForm.ceshipingjia)
      const exemptText = reason ? `免测：${reason}` : '免测'
      if (!cleaned) {
        return `<p>${exemptText}</p>`
      }
      return `${cleaned}<p>${exemptText}</p>`
    },
    back() {
      this.parent.showFlag = true
      this.parent.addOrUpdateFlag = false
      this.parent.ceshichengjiCrossAddOrUpdateFlag = false
    },
    buildPayload() {
      const payload = Object.assign({}, this.ruleForm)
      const toInt = ['ceshipingfen', 'pullUp', 'sitUp']
      const toDouble = ['run50m', 'run1000m', 'run800m', 'longJump', 'bmi']
      toInt.forEach(k => {
        const v = payload[k]
        if (v === '' || v === undefined || v === null) {
          payload[k] = null
        } else {
          payload[k] = parseInt(v, 10)
        }
      })
      toDouble.forEach(k => {
        const v = payload[k]
        if (v === '' || v === undefined || v === null) {
          payload[k] = null
        } else {
          payload[k] = parseFloat(v)
        }
      })
      return payload
    }
  }
}
</script>

<style lang="scss" scoped>
.addEdit-block {
  padding: 10px;
}
.mb12 {
  margin-bottom: 12px;
}
.ai-preview {
  white-space: pre-wrap;
  line-height: 1.7;
  max-height: 420px;
  overflow-y: auto;
  margin-top: 10px;
  padding: 10px;
  background: #f8f8f8;
  border-radius: 4px;
}
.editor {
  min-height: 300px;
}
</style>
