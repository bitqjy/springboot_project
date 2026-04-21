<template>
  <div class="main-content">
    <div v-if="showFlag">
      <el-card v-if="canManageScore" shadow="never" class="mb16 query-panel">
        <el-form :inline="true" :model="searchForm" size="small" class="query-form">
          <el-form-item label="测试名称">
            <el-input v-model="searchForm.ceshimingcheng" clearable placeholder="支持模糊查询" />
          </el-form-item>
          <el-form-item label="测试评级">
            <el-select v-model="searchForm.ceshipingji" clearable placeholder="全部">
              <el-option v-for="item in ceshipingjiOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="异常状态">
            <el-select v-model="searchForm.abnormalFlag" clearable placeholder="全部">
              <el-option :value="1" label="异常" />
              <el-option :value="0" label="正常" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" class="query-btn" @click="search">查询</el-button>
            <el-button class="reset-btn" @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card v-if="canManageScore" shadow="never" class="mb16 toolbar-panel">
        <el-button v-if="isAuth('ceshichengji','新增')" type="primary" icon="el-icon-plus" class="toolbar-btn btn-add" @click="addOrUpdateHandler()">新增</el-button>
        <el-button v-if="isAuth('ceshichengji','删除')" type="danger" icon="el-icon-delete" class="toolbar-btn btn-delete" :disabled="dataListSelections.length<=0" @click="deleteHandler()">批量删除</el-button>
        <el-button type="warning" icon="el-icon-upload" class="toolbar-btn btn-import" @click="openImportDialog">Excel导入</el-button>
        <el-button type="info" icon="el-icon-document" class="toolbar-btn btn-template" @click="downloadTemplate">模板下载</el-button>
        <el-button type="success" icon="el-icon-user" class="toolbar-btn btn-roster" @click="openRosterDialog">缺测/免测识别</el-button>
        <el-button type="success" plain icon="el-icon-data-analysis" class="toolbar-btn btn-stats" @click="openStatsDialog">成绩统计</el-button>
        <el-button type="primary" plain icon="el-icon-magic-stick" class="toolbar-btn btn-ai" @click="openBatchAiDialog">批量AI建议</el-button>
      </el-card>

      <el-table
        class="score-table"
        v-loading="dataListLoading"
        :data="dataList"
        border
        stripe
        @selection-change="selectionChangeHandler"
      >
        <el-table-column v-if="canManageScore" type="selection" width="50" />
        <el-table-column label="测试编号" prop="ceshibianhao" min-width="120" show-overflow-tooltip />
        <el-table-column label="测试名称" prop="ceshimingcheng" min-width="120" show-overflow-tooltip />
        <el-table-column label="学生账号" prop="yonghuzhanghao" min-width="110" />
        <el-table-column label="学生姓名" prop="yonghuxingming" min-width="100" />
        <el-table-column label="班级" prop="banji" min-width="100" />
        <el-table-column label="50米(s)" prop="run50m" width="90" />
        <el-table-column label="1000米(s)" prop="run1000m" width="100" />
        <el-table-column label="800米(s)" prop="run800m" width="90" />
        <el-table-column label="立定跳远(cm)" prop="longJump" width="110" />
        <el-table-column label="引体向上" prop="pullUp" width="90" />
        <el-table-column label="仰卧起坐" prop="sitUp" width="90" />
        <el-table-column label="BMI" prop="bmi" width="80" />
        <el-table-column label="评分" prop="ceshipingfen" width="80" />
        <el-table-column label="评级" prop="ceshipingji" width="80" />
        <el-table-column label="异常" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.abnormalFlag===1?'danger':'success'" size="mini">{{ scope.row.abnormalFlag===1?'异常':'正常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="薄弱项" prop="weakItems" min-width="120" show-overflow-tooltip />
        <el-table-column label="优势项" prop="strongItems" min-width="120" show-overflow-tooltip />
        <el-table-column label="评分时间" prop="pingfenshijian" min-width="110" />
        <el-table-column
          v-if="isAuth('ceshichengji','查看') || canManageScore"
          label="操作"
          :fixed="canManageScore ? 'right' : null"
          :width="canManageScore ? 320 : 100"
        >
          <template slot-scope="scope">
            <el-button v-if="isAuth('ceshichengji','查看')" type="text" size="small" @click="addOrUpdateHandler(scope.row.id,'info')">详情</el-button>
            <el-button v-if="canManageScore && isAuth('ceshichengji','修改')" type="text" size="small" @click="addOrUpdateHandler(scope.row.id)">修改</el-button>
            <el-button v-if="canManageScore" type="text" size="small" @click="generateAi(scope.row)">AI建议</el-button>
            <el-button v-if="canManageScore && isAuth('ceshichengji','删除')" type="text" size="small" style="color:#f56c6c" @click="deleteHandler(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          @size-change="sizeChangeHandle"
          @current-change="currentChangeHandle"
          :current-page="pageIndex"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalPage"
        />
      </div>
    </div>

    <add-or-update v-if="addOrUpdateFlag" ref="addOrUpdate" :parent="this" />

    <el-dialog title="Excel/CSV成绩导入" :visible.sync="importDialogVisible" width="650px">
      <el-form :model="importForm" label-width="110px" size="small">
        <el-form-item label="测试编号" required>
          <el-input v-model="importForm.ceshibianhao" placeholder="例如：2026T1" />
        </el-form-item>
        <el-form-item label="测试名称" required>
          <el-input v-model="importForm.ceshimingcheng" placeholder="例如：2026春季体测" />
        </el-form-item>
        <el-form-item label="班级(可选)">
          <el-input v-model="importForm.banji" placeholder="为空时按Excel每行班级" />
        </el-form-item>
        <el-form-item label="导入模式">
          <el-checkbox v-model="importForm.dryRun">仅校验(dryRun)</el-checkbox>
          <el-checkbox v-model="importForm.strictAbnormal">异常拦截(strictAbnormal)</el-checkbox>
        </el-form-item>
        <el-form-item label="导入文件" required>
          <el-upload
            ref="importUpload"
            drag
            :auto-upload="false"
            multiple
            :limit="20"
            accept=".xls,.xlsx,.csv,text/csv"
            :on-change="onImportFileChange"
            :on-remove="onImportFileRemove"
            :on-exceed="onImportExceed"
            action="#"
          >
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">将Excel/CSV文件拖到此处，或点击上传（支持多选）</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="importDialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="submitImport">开始导入</el-button>
      </span>
    </el-dialog>

    <el-dialog title="导入结果" :visible.sync="importResultVisible" width="900px">
      <el-descriptions :column="4" border size="small" v-if="importResult">
        <el-descriptions-item label="文件数">{{ (importResult.fileResults || []).length }}</el-descriptions-item>
        <el-descriptions-item label="新增">{{ importResult.inserted || 0 }}</el-descriptions-item>
        <el-descriptions-item label="更新">{{ importResult.updated || 0 }}</el-descriptions-item>
        <el-descriptions-item label="跳过">{{ importResult.skipped || 0 }}</el-descriptions-item>
        <el-descriptions-item label="错误数">{{ (importResult.errors || []).length }}</el-descriptions-item>
      </el-descriptions>
      <el-tabs style="margin-top:12px;" v-if="importResult">
        <el-tab-pane :label="`文件结果(${(importResult.fileResults||[]).length})`">
          <el-table :data="importResult.fileResults || []" size="mini" max-height="260">
            <el-table-column prop="fileName" label="文件名" min-width="220" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.code===0?'success':'danger'" size="mini">
                  {{ scope.row.code===0 ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="inserted" label="新增" width="80" />
            <el-table-column prop="updated" label="更新" width="80" />
            <el-table-column prop="skipped" label="跳过" width="80" />
            <el-table-column prop="errorCount" label="错误" width="80" />
            <el-table-column prop="warningCount" label="警告" width="80" />
            <el-table-column prop="message" label="信息" min-width="200" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
        <el-tab-pane :label="`错误(${(importResult.errors||[]).length})`">
          <el-table :data="importResult.errors || []" size="mini" max-height="300">
            <el-table-column prop="row" label="行号" width="80" />
            <el-table-column prop="code" label="错误码" width="160" />
            <el-table-column prop="message" label="错误信息" min-width="220" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
        <el-tab-pane :label="`警告(${(importResult.warnings||[]).length})`">
          <el-table :data="importResult.warnings || []" size="mini" max-height="300">
            <el-table-column prop="row" label="行号" width="80" />
            <el-table-column prop="code" label="警告码" width="160" />
            <el-table-column prop="message" label="警告信息" min-width="220" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <el-dialog title="缺测/免测识别" :visible.sync="rosterDialogVisible" width="980px">
      <el-form :inline="true" :model="rosterQuery" size="small">
        <el-form-item label="测试编号">
          <el-input v-model="rosterQuery.ceshibianhao" placeholder="必填" />
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="rosterQuery.banji" placeholder="必填" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRosterCompare">开始识别</el-button>
        </el-form-item>
      </el-form>
      <el-descriptions :column="5" border size="small" v-if="rosterData">
        <el-descriptions-item label="班级">{{ rosterData.banji }}</el-descriptions-item>
        <el-descriptions-item label="总人数">{{ rosterData.rosterCount }}</el-descriptions-item>
        <el-descriptions-item label="已测">{{ rosterData.measuredCount }}</el-descriptions-item>
        <el-descriptions-item label="免测">{{ rosterData.exemptCount }}</el-descriptions-item>
        <el-descriptions-item label="缺测">{{ rosterData.missingCount }}</el-descriptions-item>
      </el-descriptions>
      <el-tabs style="margin-top:12px;" v-if="rosterData">
        <el-tab-pane :label="`已测(${(rosterData.measured||[]).length})`">
          <el-table :data="rosterData.measured || []" size="mini" max-height="260">
            <el-table-column prop="yonghuzhanghao" label="账号" width="120" />
            <el-table-column prop="yonghuxingming" label="姓名" width="100" />
            <el-table-column prop="banji" label="班级" width="120" />
            <el-table-column label="异常" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.chengji && scope.row.chengji.abnormalFlag===1 ? 'danger':'success'" size="mini">
                  {{ scope.row.chengji && scope.row.chengji.abnormalFlag===1 ? '异常':'正常' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane :label="`免测(${(rosterData.exempt||[]).length})`">
          <el-table :data="rosterData.exempt || []" size="mini" max-height="260">
            <el-table-column prop="yonghuzhanghao" label="账号" width="120" />
            <el-table-column prop="yonghuxingming" label="姓名" width="100" />
            <el-table-column prop="banji" label="班级" width="120" />
            <el-table-column prop="miance.mianceyuanyin" label="免测原因" min-width="180" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
        <el-tab-pane :label="`缺测(${(rosterData.missing||[]).length})`">
          <el-table :data="rosterData.missing || []" size="mini" max-height="260">
            <el-table-column prop="yonghuzhanghao" label="账号" width="120" />
            <el-table-column prop="yonghuxingming" label="姓名" width="100" />
            <el-table-column prop="banji" label="班级" width="120" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <el-dialog title="成绩统计概览" :visible.sync="statsDialogVisible" width="920px">
      <el-form :inline="true" :model="statsQuery" size="small">
        <el-form-item label="测试编号">
          <el-input v-model="statsQuery.ceshibianhao" placeholder="必填" />
        </el-form-item>
        <el-form-item label="班级(可选)">
          <el-input v-model="statsQuery.banji" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStats">查询统计</el-button>
        </el-form-item>
      </el-form>
      <el-descriptions :column="4" border size="small" v-if="statsData">
        <el-descriptions-item label="已测人数">{{ statsData.testedCount }}</el-descriptions-item>
        <el-descriptions-item label="免测人数">{{ statsData.exemptCount }}</el-descriptions-item>
        <el-descriptions-item label="异常人数">{{ statsData.abnormalCount }}</el-descriptions-item>
        <el-descriptions-item label="平均分">{{ statsData.avgScore==null?'--':statsData.avgScore }}</el-descriptions-item>
      </el-descriptions>
      <el-card shadow="never" v-if="statsData" style="margin-top:12px;">
        <div slot="header">评级分布</div>
        <el-tag v-for="(count, key) in statsData.ratingDistribution || {}" :key="key" style="margin:0 8px 8px 0;">{{ key }}: {{ count }}</el-tag>
      </el-card>
      <el-card shadow="never" v-if="statsData" style="margin-top:12px;">
        <div slot="header">薄弱项 Top</div>
        <el-table :data="statsData.weakItemTop || []" size="mini" max-height="260">
          <el-table-column label="项目" prop="item" min-width="150" />
          <el-table-column label="人数" prop="count" width="120" />
        </el-table>
      </el-card>
    </el-dialog>

    <el-dialog title="批量AI建议" :visible.sync="batchAiDialogVisible" width="520px">
      <el-form :model="batchAiForm" label-width="120px" size="small">
        <el-form-item label="测试编号" required>
          <el-input v-model="batchAiForm.ceshibianhao" />
        </el-form-item>
        <el-form-item label="班级(可选)">
          <el-input v-model="batchAiForm.banji" />
        </el-form-item>
        <el-form-item label="处理上限">
          <el-input-number v-model="batchAiForm.limit" :min="1" :max="500" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="batchAiDialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="batchAiLoading" @click="submitBatchAi">开始生成</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import AddOrUpdate from './add-or-update'

export default {
  components: { AddOrUpdate },
  data() {
    return {
      searchForm: {
        ceshimingcheng: '',
        ceshipingji: '',
        abnormalFlag: undefined
      },
      ceshipingjiOptions: ['优秀', '良好', '及格', '不及格'],
      dataList: [],
      pageIndex: 1,
      pageSize: 10,
      totalPage: 0,
      dataListLoading: false,
      dataListSelections: [],
      showFlag: true,
      addOrUpdateFlag: false,
      ceshichengjiCrossAddOrUpdateFlag: false,

      importDialogVisible: false,
      importResultVisible: false,
      importing: false,
      importForm: {
        ceshibianhao: '',
        ceshimingcheng: '',
        banji: '',
        dryRun: false,
        strictAbnormal: true,
        files: []
      },
      importResult: null,

      rosterDialogVisible: false,
      rosterQuery: {
        ceshibianhao: '',
        banji: ''
      },
      rosterData: null,

      statsDialogVisible: false,
      statsQuery: {
        ceshibianhao: '',
        banji: ''
      },
      statsData: null,

      batchAiDialogVisible: false,
      batchAiLoading: false,
      batchAiForm: {
        ceshibianhao: '',
        banji: '',
        limit: 50
      },
      sessionTable: ''
    }
  },
  created() {
    this.sessionTable = this.$storage.get('sessionTable') || ''
    this.getDataList()
  },
  computed: {
    canManageScore() {
      return this.sessionTable === 'jiaoshi' || this.sessionTable === 'users'
    }
  },
  methods: {
    contentStyleChange() {},
    search() {
      this.pageIndex = 1
      this.getDataList()
    },
    resetSearch() {
      this.searchForm = {
        ceshimingcheng: '',
        ceshipingji: '',
        abnormalFlag: undefined
      }
      this.search()
    },
    getDataList() {
      this.dataListLoading = true
      const params = {
        page: this.pageIndex,
        limit: this.pageSize,
        sort: 'id',
        order: 'desc'
      }
      if (this.searchForm.ceshimingcheng) {
        params.ceshimingcheng = `%${this.searchForm.ceshimingcheng}%`
      }
      if (this.searchForm.ceshipingji) {
        params.ceshipingji = this.searchForm.ceshipingji
      }
      if (this.searchForm.abnormalFlag === 0 || this.searchForm.abnormalFlag === 1) {
        params.abnormalFlag = this.searchForm.abnormalFlag
      }
      this.$http({
        url: 'ceshichengji/page',
        method: 'get',
        params
      }).then(({ data }) => {
        if (data && data.code === 0) {
          this.dataList = data.data.list || []
          this.totalPage = data.data.total || 0
        } else {
          this.dataList = []
          this.totalPage = 0
          this.$message.error(data.msg || '获取列表失败')
        }
      }).finally(() => {
        this.dataListLoading = false
      })
    },
    sizeChangeHandle(val) {
      this.pageSize = val
      this.pageIndex = 1
      this.getDataList()
    },
    currentChangeHandle(val) {
      this.pageIndex = val
      this.getDataList()
    },
    selectionChangeHandler(val) {
      this.dataListSelections = val
    },
    addOrUpdateHandler(id, type) {
      this.showFlag = false
      this.addOrUpdateFlag = true
      const actualType = type !== 'info' ? 'else' : 'info'
      this.$nextTick(() => {
        this.$refs.addOrUpdate.init(id, actualType)
      })
    },
    deleteHandler(id) {
      const ids = id ? [Number(id)] : this.dataListSelections.map(item => Number(item.id))
      if (!ids.length) {
        this.$message.warning('请先选择要删除的数据')
        return
      }
      this.$confirm(`确定进行[${id ? '删除' : '批量删除'}]操作?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: 'ceshichengji/delete',
          method: 'post',
          data: ids
        }).then(({ data }) => {
          if (data && data.code === 0) {
            this.$message.success('操作成功')
            this.search()
          } else {
            this.$message.error(data.msg || '删除失败')
          }
        })
      })
    },
    generateAi(row) {
      this.$confirm(`为学生 ${row.yonghuxingming || row.yonghuzhanghao} 生成AI建议并回写测试评价？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: `ceshichengji/aiSuggest/${row.id}`,
          method: 'get'
        }).then(({ data }) => {
          if (data && data.code === 0) {
            this.$alert(data.data || '已生成', 'AI建议', { confirmButtonText: '确定' })
            this.getDataList()
          } else {
            this.$message.error(data.msg || '生成失败')
          }
        })
      })
    },
    openImportDialog() {
      this.importDialogVisible = true
      this.importResult = null
      this.importForm.files = []
      this.$nextTick(() => {
        if (this.$refs.importUpload && this.$refs.importUpload.clearFiles) {
          this.$refs.importUpload.clearFiles()
        }
      })
    },
    onImportFileChange(file, fileList) {
      this.importForm.files = fileList || []
    },
    onImportFileRemove(file, fileList) {
      this.importForm.files = fileList || []
    },
    onImportExceed() {
      this.$message.warning('一次最多选择20个文件')
    },
    submitImport() {
      if (!this.importForm.ceshibianhao || !this.importForm.ceshimingcheng) {
        this.$message.warning('请填写测试编号和测试名称')
        return
      }
      const files = (this.importForm.files || []).filter(item => item && item.raw)
      if (!files.length) {
        this.$message.warning('请先选择导入文件')
        return
      }
      this.importing = true
      const merged = {
        inserted: 0,
        updated: 0,
        skipped: 0,
        errors: [],
        warnings: [],
        corrections: [],
        fileResults: []
      }
      let failedCount = 0

      const importOne = index => {
        if (index >= files.length) {
          return Promise.resolve()
        }
        const current = files[index]
        const formData = new FormData()
        formData.append('file', current.raw)

        return this.$http({
          url: 'ceshichengji/importExcel',
          method: 'post',
          params: {
            ceshibianhao: this.importForm.ceshibianhao,
            ceshimingcheng: this.importForm.ceshimingcheng,
            banji: this.importForm.banji || undefined,
            dryRun: this.importForm.dryRun,
            strictAbnormal: this.importForm.strictAbnormal
          },
          data: formData
        }).then(({ data }) => {
          if (data && data.code === 0) {
            const result = data.data || {}
            merged.inserted += result.inserted || 0
            merged.updated += result.updated || 0
            merged.skipped += result.skipped || 0
            merged.errors = merged.errors.concat(result.errors || [])
            merged.warnings = merged.warnings.concat(result.warnings || [])
            merged.corrections = merged.corrections.concat(result.corrections || [])
            merged.fileResults.push({
              fileName: current.name,
              code: 0,
              inserted: result.inserted || 0,
              updated: result.updated || 0,
              skipped: result.skipped || 0,
              errorCount: (result.errors || []).length,
              warningCount: (result.warnings || []).length,
              message: '导入成功'
            })
          } else {
            failedCount++
            const msg = (data && data.msg) ? data.msg : '导入失败'
            merged.errors.push({ row: '-', code: 'FILE_IMPORT_FAIL', message: `文件${current.name}导入失败：${msg}` })
            merged.fileResults.push({
              fileName: current.name,
              code: data ? data.code : -1,
              inserted: 0,
              updated: 0,
              skipped: 0,
              errorCount: 1,
              warningCount: 0,
              message: msg
            })
          }
        }).catch(err => {
          failedCount++
          const msg = (err && err.message) ? err.message : '请求异常'
          merged.errors.push({ row: '-', code: 'FILE_IMPORT_EXCEPTION', message: `文件${current.name}导入异常：${msg}` })
          merged.fileResults.push({
            fileName: current.name,
            code: -1,
            inserted: 0,
            updated: 0,
            skipped: 0,
            errorCount: 1,
            warningCount: 0,
            message: msg
          })
        }).then(() => importOne(index + 1))
      }

      importOne(0).then(() => {
        this.importResult = merged
        this.importDialogVisible = false
        this.importResultVisible = true
        if (failedCount > 0) {
          this.$message.warning(`导入完成：成功${files.length - failedCount}个，失败${failedCount}个`)
        } else {
          this.$message.success(`导入执行完成，共${files.length}个文件`)
        }
        if (!this.importForm.dryRun && (merged.inserted > 0 || merged.updated > 0)) {
          this.search()
        }
      }).finally(() => {
        this.importing = false
      })
    },
    downloadTemplate() {
      this.$http({
        url: 'ceshichengji/importTemplate',
        method: 'get'
      }).then(({ data }) => {
        if (!(data && data.code === 0)) {
          this.$message.error(data.msg || '获取模板失败')
          return
        }
        const payload = data.data || {}
        const headers = payload.headers || []
        const sampleRow = payload.sampleRow || {}
        const row = headers.map(h => sampleRow[h] == null ? '' : sampleRow[h])
        const csvContent = `${headers.join(',')}\n${row.join(',')}\n`
        const blob = new Blob(["\ufeff" + csvContent], { type: 'text/csv;charset=utf-8;' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '体测成绩导入模板.csv'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
      })
    },
    openRosterDialog() {
      this.rosterDialogVisible = true
      if (!this.rosterQuery.ceshibianhao && this.dataList.length) {
        this.rosterQuery.ceshibianhao = this.dataList[0].ceshibianhao || ''
      }
      if (!this.rosterQuery.banji && this.dataList.length) {
        this.rosterQuery.banji = this.dataList[0].banji || ''
      }
    },
    loadRosterCompare() {
      if (!this.rosterQuery.ceshibianhao || !this.rosterQuery.banji) {
        this.$message.warning('请填写测试编号和班级')
        return
      }
      this.$http({
        url: 'ceshichengji/compareRoster',
        method: 'get',
        params: {
          ceshibianhao: this.rosterQuery.ceshibianhao,
          banji: this.rosterQuery.banji
        }
      }).then(({ data }) => {
        if (data && data.code === 0) {
          this.rosterData = data.data
        } else {
          this.$message.error(data.msg || '识别失败')
        }
      })
    },
    openStatsDialog() {
      this.statsDialogVisible = true
      if (!this.statsQuery.ceshibianhao && this.dataList.length) {
        this.statsQuery.ceshibianhao = this.dataList[0].ceshibianhao || ''
      }
      if (!this.statsQuery.banji && this.dataList.length) {
        this.statsQuery.banji = this.dataList[0].banji || ''
      }
    },
    loadStats() {
      if (!this.statsQuery.ceshibianhao) {
        this.$message.warning('请填写测试编号')
        return
      }
      this.$http({
        url: 'ceshichengji/statsOverview',
        method: 'get',
        params: {
          ceshibianhao: this.statsQuery.ceshibianhao,
          banji: this.statsQuery.banji || undefined
        }
      }).then(({ data }) => {
        if (data && data.code === 0) {
          this.statsData = data.data
        } else {
          this.$message.error(data.msg || '统计失败')
        }
      })
    },
    openBatchAiDialog() {
      this.batchAiDialogVisible = true
      if (!this.batchAiForm.ceshibianhao && this.dataList.length) {
        this.batchAiForm.ceshibianhao = this.dataList[0].ceshibianhao || ''
      }
      if (!this.batchAiForm.banji && this.dataList.length) {
        this.batchAiForm.banji = this.dataList[0].banji || ''
      }
    },
    submitBatchAi() {
      if (!this.batchAiForm.ceshibianhao) {
        this.$message.warning('请填写测试编号')
        return
      }
      this.batchAiLoading = true
      this.$http({
        url: 'ceshichengji/aiSuggestBatch',
        method: 'get',
        params: {
          ceshibianhao: this.batchAiForm.ceshibianhao,
          banji: this.batchAiForm.banji || undefined,
          limit: this.batchAiForm.limit
        }
      }).then(({ data }) => {
        if (data && data.code === 0) {
          const info = data.data || {}
          this.$alert(`处理成功 ${info.processed || 0} 条，失败 ${(info.failures || []).length} 条`, '执行结果', {
            confirmButtonText: '确定'
          })
          this.batchAiDialogVisible = false
          this.search()
        } else {
          this.$message.error(data.msg || '批量生成失败')
        }
      }).finally(() => {
        this.batchAiLoading = false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.main-content {
  padding: 16px;
  min-height: 100%;
  background: linear-gradient(180deg, #f3fbf9 0%, #eef7f5 100%);
}

.mb16 {
  margin-bottom: 16px;
}

.query-panel,
.toolbar-panel {
  border: none;
  border-radius: 14px;
  box-shadow: 0 8px 24px rgba(38, 134, 118, 0.08);
}

.query-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.main-content ::v-deep .query-panel .el-card__body,
.main-content ::v-deep .toolbar-panel .el-card__body {
  padding: 14px 16px;
}

.main-content ::v-deep .query-form .el-form-item {
  margin-right: 12px;
  margin-bottom: 8px;
}

.main-content ::v-deep .query-panel .el-form-item__label {
  color: #20675c;
  font-weight: 600;
}

.main-content ::v-deep .query-panel .el-input__inner {
  height: 34px;
  line-height: 34px;
  border-radius: 18px;
  border-color: #d2e9e4;
}

.main-content ::v-deep .query-panel .el-input__inner:focus {
  border-color: #28b29f;
}

.query-btn,
.reset-btn,
.toolbar-btn {
  border-radius: 18px;
  padding: 8px 16px;
  font-weight: 600;
}

.query-btn {
  background: linear-gradient(120deg, #34beac 0%, #1fa391 100%);
  border-color: #1fa391;
}

.reset-btn {
  color: #208476;
  border-color: #b8ddd7;
  background: #f3fbf9;
}

.toolbar-panel {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.toolbar-btn {
  border: none;
  box-shadow: 0 6px 14px rgba(49, 157, 140, 0.18);
}

.btn-add {
  background: linear-gradient(120deg, #3cc5b2 0%, #22a595 100%);
}

.btn-delete {
  background: linear-gradient(120deg, #f08a8a 0%, #e66f6f 100%);
}

.btn-import {
  background: linear-gradient(120deg, #f7bf69 0%, #efab47 100%);
}

.btn-template {
  background: linear-gradient(120deg, #7eaecf 0%, #6696bc 100%);
}

.btn-roster,
.btn-stats,
.btn-ai {
  background: linear-gradient(120deg, #55c9ba 0%, #30af9e 100%);
}

.main-content ::v-deep .toolbar-panel .btn-stats.el-button--success.is-plain,
.main-content ::v-deep .toolbar-panel .btn-ai.el-button--primary.is-plain {
  color: #ffffff !important;
  border-color: transparent !important;
  background: linear-gradient(120deg, #55c9ba 0%, #30af9e 100%) !important;
}

.main-content ::v-deep .toolbar-panel .btn-stats.el-button--success.is-plain i,
.main-content ::v-deep .toolbar-panel .btn-ai.el-button--primary.is-plain i {
  color: #ffffff !important;
}

.main-content ::v-deep .toolbar-panel .btn-stats.el-button--success.is-plain:hover,
.main-content ::v-deep .toolbar-panel .btn-stats.el-button--success.is-plain:focus,
.main-content ::v-deep .toolbar-panel .btn-ai.el-button--primary.is-plain:hover,
.main-content ::v-deep .toolbar-panel .btn-ai.el-button--primary.is-plain:focus {
  color: #ffffff !important;
  border-color: transparent !important;
}

.score-table {
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(38, 134, 118, 0.1);
}

.main-content ::v-deep .score-table .el-table__header-wrapper th {
  background: #2fb4a4;
  color: #ffffff;
  border-bottom: 1px solid #27a092;
}

.main-content ::v-deep .score-table .el-table__body tr.el-table__row--striped td {
  background: #f5fbfa;
}

.main-content ::v-deep .score-table .el-table__body tr:hover > td {
  background: #e8f7f4 !important;
}

.main-content ::v-deep .score-table .el-table__body td,
.main-content ::v-deep .score-table .el-table__header th.is-leaf {
  border-bottom: 1px solid #e2f1ee;
}

.main-content ::v-deep .score-table .el-button--text {
  color: #1f9b8a;
  font-weight: 600;
}

.pagination-wrapper {
  margin-top: 16px;
  text-align: right;
  padding: 12px 14px;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 8px 24px rgba(38, 134, 118, 0.08);
}

.main-content ::v-deep .pagination-wrapper .el-pagination button:hover,
.main-content ::v-deep .pagination-wrapper .el-pager li:hover,
.main-content ::v-deep .pagination-wrapper .el-pager li.active {
  color: #22a794;
}
</style>
