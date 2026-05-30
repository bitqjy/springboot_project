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
        <el-button type="success" icon="el-icon-user" class="toolbar-btn btn-roster" @click="openRosterDialog">报告提交识别</el-button>
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

    <el-dialog title="Excel/CSV成绩导入" :visible.sync="importDialogVisible" width="700px">
      <el-form :model="importForm" label-width="110px" size="small">
        <el-alert
          title="成绩导入会自动绑定到已发布测试任务，测试编号和名称以后端任务为准。"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 12px;"
        />
        <el-form-item label="已发布任务" required>
          <el-select
            v-model="importForm.tizhiceshiId"
            filterable
            clearable
            placeholder="请选择已发布测试任务"
            :loading="importOptionsLoading"
            style="width: 100%;"
            @change="onImportTaskChange"
          >
            <el-option
              v-for="item in importOptions.tests"
              :key="item.id"
              :label="formatStatsTestLabel(item)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="测试编号">
          <el-input v-model="importForm.ceshibianhao" readonly placeholder="选择任务后自动带出" />
        </el-form-item>
        <el-form-item label="测试名称">
          <el-input v-model="importForm.ceshimingcheng" readonly placeholder="选择任务后自动带出" />
        </el-form-item>
        <el-form-item label="班级(可选)">
          <el-select
            v-model="importForm.banji"
            filterable
            clearable
            placeholder="不选则按Excel每行班级"
            :loading="importOptionsLoading"
            style="width: 100%;"
          >
            <el-option
              v-for="item in importOptions.banjis"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
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

    <el-dialog title="报告提交识别" :visible.sync="rosterDialogVisible" width="980px">
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
      <el-descriptions :column="6" border size="small" v-if="rosterData">
        <el-descriptions-item label="班级">{{ rosterData.banji }}</el-descriptions-item>
        <el-descriptions-item label="应提交">{{ rosterData.rosterCount }}</el-descriptions-item>
        <el-descriptions-item label="已提交">{{ rosterData.submittedCount }}</el-descriptions-item>
        <el-descriptions-item label="已评分">{{ rosterData.scoredCount || rosterData.measuredCount }}</el-descriptions-item>
        <el-descriptions-item label="免测">{{ rosterData.exemptCount }}</el-descriptions-item>
        <el-descriptions-item label="未交/缺测">{{ rosterData.missingCount }}</el-descriptions-item>
      </el-descriptions>
      <el-tabs style="margin-top:12px;" v-if="rosterData">
        <el-tab-pane :label="`已评分(${(rosterData.measured||[]).length})`">
          <el-table :data="rosterData.measured || []" size="mini" max-height="260">
            <el-table-column prop="yonghuzhanghao" label="账号" width="120" />
            <el-table-column prop="yonghuxingming" label="姓名" width="100" />
            <el-table-column prop="banji" label="班级" width="120" />
            <el-table-column label="报告文件" min-width="150">
              <template slot-scope="scope">
                <el-button v-if="scope.row.baogao && scope.row.baogao.baogaowenjian" type="text" size="small" @click="downloadReport(scope.row.baogao.baogaowenjian)">下载报告</el-button>
                <span v-else>--</span>
              </template>
            </el-table-column>
            <el-table-column label="异常" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.chengji && scope.row.chengji.abnormalFlag===1 ? 'danger':'success'" size="mini">
                  {{ scope.row.chengji && scope.row.chengji.abnormalFlag===1 ? '异常':'正常' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane :label="`已交未评分(${(rosterData.submitted||[]).length})`">
          <el-table :data="rosterData.submitted || []" size="mini" max-height="260">
            <el-table-column prop="yonghuzhanghao" label="账号" width="120" />
            <el-table-column prop="yonghuxingming" label="姓名" width="100" />
            <el-table-column prop="banji" label="班级" width="120" />
            <el-table-column label="报告文件" min-width="150">
              <template slot-scope="scope">
                <el-button v-if="scope.row.baogao && scope.row.baogao.baogaowenjian" type="text" size="small" @click="downloadReport(scope.row.baogao.baogaowenjian)">下载报告</el-button>
                <span v-else>--</span>
              </template>
            </el-table-column>
            <el-table-column prop="baogao.tijiaoriqi" label="提交日期" width="120" />
            <el-table-column prop="baogao.beizhu" label="备注" min-width="180" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
        <el-tab-pane :label="`免测(${(rosterData.exempt||[]).length})`">
          <el-table :data="rosterData.exempt || []" size="mini" max-height="260">
            <el-table-column prop="yonghuzhanghao" label="账号" width="120" />
            <el-table-column prop="yonghuxingming" label="姓名" width="100" />
            <el-table-column prop="banji" label="班级" width="120" />
            <el-table-column label="报告文件" min-width="150">
              <template slot-scope="scope">
                <el-button v-if="scope.row.baogao && scope.row.baogao.baogaowenjian" type="text" size="small" @click="downloadReport(scope.row.baogao.baogaowenjian)">下载报告</el-button>
                <span v-else>--</span>
              </template>
            </el-table-column>
            <el-table-column prop="mianceyuanyin" label="免测依据" min-width="180" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
        <el-tab-pane :label="`未交/缺测(${(rosterData.missing||[]).length})`">
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
          <el-select
            v-model="statsQuery.ceshibianhao"
            filterable
            clearable
            placeholder="请选择测试编号"
            :loading="statsOptionsLoading"
            style="width: 240px"
            @change="onStatsTestChange">
            <el-option
              v-for="item in statsOptions.tests"
              :key="item.ceshibianhao"
              :label="formatStatsTestLabel(item)"
              :value="item.ceshibianhao" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级(可选)">
          <el-select
            v-model="statsQuery.banji"
            filterable
            clearable
            placeholder="全部班级"
            :loading="statsOptionsLoading"
            style="width: 180px">
            <el-option
              v-for="item in statsOptions.banjis"
              :key="item"
              :label="item"
              :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStats">查询统计</el-button>
        </el-form-item>
      </el-form>
      <el-descriptions :column="4" border size="small" v-if="statsData">
        <el-descriptions-item label="已评分人数">{{ statsData.testedCount }}</el-descriptions-item>
        <el-descriptions-item label="已提交报告">{{ statsData.submittedCount }}</el-descriptions-item>
        <el-descriptions-item label="已交未评分">{{ statsData.pendingScoreCount }}</el-descriptions-item>
        <el-descriptions-item label="免测人数">{{ statsData.exemptCount }}</el-descriptions-item>
        <el-descriptions-item label="未交/缺测">{{ statsData.absentCount==null?'--':statsData.absentCount }}</el-descriptions-item>
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
      ceshipingjiOptions: ['优秀', '良好', '及格', '不及格', '免测'],
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
      importOptionsLoading: false,
      importOptions: {
        tests: [],
        banjis: []
      },
      importForm: {
        tizhiceshiId: '',
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
      statsOptionsLoading: false,
      statsOptions: {
        tests: [],
        banjis: []
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
  mounted() {
    this._statsResizeHandler = () => {
      this.resizeStatsCharts()
    }
    window.addEventListener('resize', this._statsResizeHandler)
  },
  beforeDestroy() {
    if (this._statsResizeHandler) {
      window.removeEventListener('resize', this._statsResizeHandler)
    }
    this.disposeStatsCharts()
    this.removeStatsVisualHost()
  },
  computed: {
    canManageScore() {
      return this.sessionTable === 'jiaoshi' || this.sessionTable === 'users'
    }
  },
  methods: {
    contentStyleChange() {},
    ensureStatsVisualStyles() {
      if (document.getElementById('class-report-visual-style')) {
        return
      }
      const style = document.createElement('style')
      style.id = 'class-report-visual-style'
      style.innerHTML = `
        .class-report-visuals {
          margin-top: 16px;
        }
        .class-report-banner {
          margin-bottom: 14px;
          padding: 18px 20px;
          border-radius: 10px;
          background: linear-gradient(135deg, #effaf7 0%, #e3f5f1 100%);
          border: 1px solid #d3ece6;
        }
        .class-report-banner__eyebrow {
          font-size: 12px;
          color: #2c8d7f;
          margin-bottom: 6px;
        }
        .class-report-banner__title {
          font-size: 18px;
          line-height: 1.4;
          color: #16574d;
          font-weight: 700;
        }
        .class-report-banner__meta {
          margin-top: 8px;
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
        }
        .class-report-banner__tag {
          display: inline-flex;
          align-items: center;
          padding: 4px 10px;
          border-radius: 999px;
          background: rgba(34, 165, 149, 0.1);
          color: #1d7f71;
          font-size: 12px;
        }
        .class-report-banner__desc {
          margin-top: 10px;
          color: #335f57;
          line-height: 1.7;
          font-size: 13px;
        }
        .class-report-grid {
          display: grid;
          grid-template-columns: repeat(2, minmax(0, 1fr));
          gap: 14px;
        }
        .class-report-card {
          padding: 12px 14px 16px;
          border-radius: 10px;
          border: 1px solid #e1efec;
          background: #fff;
          box-shadow: 0 8px 18px rgba(32, 102, 92, 0.06);
        }
        .class-report-card__title {
          margin-bottom: 10px;
          font-size: 14px;
          font-weight: 700;
          color: #1d5e54;
        }
        .class-report-chart {
          width: 100%;
          height: 280px;
        }
        .class-report-empty {
          display: flex;
          align-items: center;
          justify-content: center;
          height: 280px;
          color: #8da39e;
          background: #f8fcfb;
          border-radius: 8px;
        }
        @media (max-width: 900px) {
          .class-report-grid {
            grid-template-columns: 1fr;
          }
        }
      `
      document.head.appendChild(style)
    },
    escapeHtml(text) {
      return String(text == null ? '' : text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
    },
    findStatsDialogBody() {
      const wrappers = Array.from(document.querySelectorAll('.el-dialog__wrapper'))
      for (const wrapper of wrappers) {
        if (window.getComputedStyle(wrapper).display === 'none') {
          continue
        }
        const titleEl = wrapper.querySelector('.el-dialog__title')
        if (titleEl && titleEl.textContent && titleEl.textContent.indexOf('成绩统计概览') !== -1) {
          return wrapper.querySelector('.el-dialog__body')
        }
      }
      return null
    },
    removeStatsVisualHost() {
      const body = this.findStatsDialogBody()
      const host = body && body.querySelector('.class-report-visuals')
      if (host && host.parentNode) {
        host.parentNode.removeChild(host)
      }
    },
    disposeStatsCharts() {
      if (!this._statsCharts) {
        this._statsCharts = {}
        return
      }
      Object.keys(this._statsCharts).forEach(key => {
        const chart = this._statsCharts[key]
        if (chart && chart.dispose) {
          chart.dispose()
        }
      })
      this._statsCharts = {}
    },
    resizeStatsCharts() {
      if (!this._statsCharts) {
        return
      }
      Object.keys(this._statsCharts).forEach(key => {
        const chart = this._statsCharts[key]
        if (chart && chart.resize) {
          chart.resize()
        }
      })
    },
    getSelectedStatsTestName() {
      const hit = (this.statsOptions.tests || []).find(item => item.ceshibianhao === this.statsQuery.ceshibianhao)
      if (hit && hit.ceshimingcheng) {
        return hit.ceshimingcheng
      }
      if (this.statsData && this.statsData.ceshibianhao) {
        return this.statsData.ceshibianhao
      }
      return this.statsQuery.ceshibianhao || '体测统计'
    },
    getLeadRatingEntry() {
      const dist = (this.statsData && this.statsData.ratingDistribution) || {}
      const entries = Object.keys(dist)
        .filter(key => key !== '未评级' && Number(dist[key] || 0) > 0)
        .map(key => ({ key, count: Number(dist[key] || 0) }))
        .sort((a, b) => b.count - a.count)
      return entries[0] || null
    },
    buildClassReportText() {
      if (!this.statsData) {
        return ''
      }
      const data = this.statsData
      const className = this.statsQuery.banji || '当前范围'
      const leadRating = this.getLeadRatingEntry()
      const weakTop = (data.weakItemTop || [])[0]
      const avgText = data.avgScore == null ? '当前还没有可用于计算的平均分' : `平均分为 ${data.avgScore}`
      const rosterText = data.rosterCount == null ? `已提交报告 ${data.submittedCount} 人，已评分 ${data.testedCount} 人` : `应测 ${data.rosterCount} 人，已提交报告 ${data.submittedCount} 人，已评分 ${data.testedCount} 人`
      const pendingText = data.pendingScoreCount > 0 ? `仍有 ${data.pendingScoreCount} 人已交未评分` : '报告评分进度已经清空'
      const absentText = data.absentCount == null ? '' : `，未交/缺测 ${data.absentCount} 人`
      const exemptText = data.exemptCount > 0 ? `，免测 ${data.exemptCount} 人` : ''
      const weakText = weakTop ? `当前最集中的薄弱项是 ${weakTop.item}，涉及 ${weakTop.count} 人。` : '当前还没有明显聚集的薄弱项。'
      const ratingText = leadRating ? `班级主流评级为 ${leadRating.key}（${leadRating.count} 人）。` : '当前还没有形成明确的评级分布。'
      return `${className} 本次体测中，${rosterText}${absentText}${exemptText}，${avgText}。${pendingText}。${ratingText}${weakText}`
    },
    renderStatsVisuals() {
      if (!this.statsDialogVisible || !this.statsData) {
        return
      }
      this.ensureStatsVisualStyles()
      this.$nextTick(() => {
        const body = this.findStatsDialogBody()
        if (!body) {
          return
        }
        this.disposeStatsCharts()
        this.removeStatsVisualHost()

        const host = document.createElement('div')
        host.className = 'class-report-visuals'
        const classLabel = this.statsQuery.banji || '全部班级'
        const testName = this.escapeHtml(this.getSelectedStatsTestName())
        const summary = this.escapeHtml(this.buildClassReportText())
        const avgScore = this.statsData.avgScore == null ? '--' : this.statsData.avgScore
        const abnormalCount = this.statsData.abnormalCount || 0
        const pendingCount = this.statsData.pendingScoreCount || 0
        host.innerHTML = `
          <div class="class-report-banner">
            <div class="class-report-banner__eyebrow">班级体测报告</div>
            <div class="class-report-banner__title">${this.escapeHtml(classLabel)} · ${testName}</div>
            <div class="class-report-banner__meta">
              <span class="class-report-banner__tag">平均分 ${this.escapeHtml(avgScore)}</span>
              <span class="class-report-banner__tag">异常 ${this.escapeHtml(abnormalCount)}</span>
              <span class="class-report-banner__tag">待评分 ${this.escapeHtml(pendingCount)}</span>
            </div>
            <div class="class-report-banner__desc">${summary}</div>
          </div>
          <div class="class-report-grid">
            <div class="class-report-card">
              <div class="class-report-card__title">班级进度</div>
              <div id="class-report-progress-chart" class="class-report-chart"></div>
            </div>
            <div class="class-report-card">
              <div class="class-report-card__title">状态占比</div>
              <div id="class-report-status-chart" class="class-report-chart"></div>
            </div>
            <div class="class-report-card">
              <div class="class-report-card__title">评级分布图</div>
              <div id="class-report-rating-chart" class="class-report-chart"></div>
            </div>
            <div class="class-report-card">
              <div class="class-report-card__title">薄弱项分布</div>
              <div id="class-report-weak-chart" class="class-report-chart"></div>
            </div>
          </div>
        `
        body.appendChild(host)
        this.$nextTick(() => {
          this.initStatsChart('progress', 'class-report-progress-chart', this.buildProgressChartOption())
          this.initStatsChart('status', 'class-report-status-chart', this.buildStatusChartOption())
          this.initStatsChart('rating', 'class-report-rating-chart', this.buildRatingChartOption())
          this.initStatsChart('weak', 'class-report-weak-chart', this.buildWeakChartOption())
          this.resizeStatsCharts()
        })
      })
    },
    initStatsChart(key, elementId, option) {
      const el = document.getElementById(elementId)
      if (!el) {
        return
      }
      if (!option) {
        el.innerHTML = '<div class="class-report-empty">暂无可视化数据</div>'
        return
      }
      const chart = this.$echarts.init(el, 'macarons')
      chart.setOption(option)
      if (!this._statsCharts) {
        this._statsCharts = {}
      }
      this._statsCharts[key] = chart
    },
    buildProgressChartOption() {
      if (!this.statsData) {
        return null
      }
      const data = this.statsData
      const categories = []
      const values = []
      if (data.rosterCount != null) {
        categories.push('应测人数')
        values.push(Number(data.rosterCount || 0))
      }
      categories.push('已提交报告', '已评分', '已交未评分', '免测')
      values.push(
        Number(data.submittedCount || 0),
        Number(data.testedCount || 0),
        Number(data.pendingScoreCount || 0),
        Number(data.exemptCount || 0)
      )
      if (data.absentCount != null) {
        categories.push('未交/缺测')
        values.push(Number(data.absentCount || 0))
      }
      return {
        color: ['#34beac'],
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { top: 26, left: 50, right: 20, bottom: 42 },
        xAxis: {
          type: 'category',
          data: categories,
          axisLabel: {
            interval: 0,
            rotate: categories.length > 5 ? 20 : 0
          }
        },
        yAxis: {
          type: 'value',
          minInterval: 1
        },
        series: [{
          name: '人数',
          type: 'bar',
          barMaxWidth: 42,
          data: values,
          label: {
            show: true,
            position: 'top'
          },
          itemStyle: {
            borderRadius: [6, 6, 0, 0]
          }
        }]
      }
    },
    buildStatusChartOption() {
      if (!this.statsData) {
        return null
      }
      const items = [
        { name: '已评分', value: Number(this.statsData.testedCount || 0) },
        { name: '已交未评分', value: Number(this.statsData.pendingScoreCount || 0) },
        { name: '免测', value: Number(this.statsData.exemptCount || 0) }
      ]
      if (this.statsData.absentCount != null) {
        items.push({ name: '未交/缺测', value: Number(this.statsData.absentCount || 0) })
      }
      const hasValue = items.some(item => item.value > 0)
      if (!hasValue) {
        return null
      }
      return {
        color: ['#36cfc9', '#faad14', '#7c6df2', '#ff7875'],
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          bottom: 0
        },
        series: [{
          name: '状态占比',
          type: 'pie',
          radius: ['42%', '68%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: false,
          label: {
            formatter: '{b}\n{c}人'
          },
          data: items
        }]
      }
    },
    buildRatingChartOption() {
      if (!this.statsData) {
        return null
      }
      const dist = this.statsData.ratingDistribution || {}
      const categories = ['优秀', '良好', '及格', '不及格', '未评级']
      const values = categories.map(key => Number(dist[key] || 0))
      if (!values.some(item => item > 0)) {
        return null
      }
      return {
        color: ['#5b8ff9'],
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { top: 26, left: 42, right: 20, bottom: 30 },
        xAxis: {
          type: 'category',
          data: categories
        },
        yAxis: {
          type: 'value',
          minInterval: 1
        },
        series: [{
          type: 'bar',
          barMaxWidth: 40,
          data: values,
          label: {
            show: true,
            position: 'top'
          },
          itemStyle: {
            borderRadius: [6, 6, 0, 0]
          }
        }]
      }
    },
    buildWeakChartOption() {
      if (!this.statsData) {
        return null
      }
      const source = (this.statsData.weakItemTop || []).slice(0, 6)
      if (!source.length) {
        return null
      }
      return {
        color: ['#ff9f7f'],
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { top: 16, left: 70, right: 20, bottom: 20 },
        xAxis: {
          type: 'value',
          minInterval: 1
        },
        yAxis: {
          type: 'category',
          data: source.map(item => item.item).reverse()
        },
        series: [{
          type: 'bar',
          barMaxWidth: 24,
          data: source.map(item => Number(item.count || 0)).reverse(),
          label: {
            show: true,
            position: 'right'
          },
          itemStyle: {
            borderRadius: [0, 6, 6, 0]
          }
        }]
      }
    },
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
      this.loadImportOptions().then(() => {
        this.ensureImportTaskSelection()
      })
      this.$nextTick(() => {
        if (this.$refs.importUpload && this.$refs.importUpload.clearFiles) {
          this.$refs.importUpload.clearFiles()
        }
      })
    },
    onImportFileChange(file, fileList) {
      this.importForm.files = (fileList && fileList.length ? fileList.slice() : (file ? [file] : []))
    },
    onImportFileRemove(file, fileList) {
      this.importForm.files = fileList && fileList.length ? fileList.slice() : []
    },
    onImportExceed() {
      this.$message.warning('一次最多选择20个文件')
    },
    extractImportRawFile(item) {
      if (!item) {
        return null
      }
      return item.raw || item.originFileObj || item.file || (typeof item.size === 'number' && item.name ? item : null)
    },
    getSelectedImportFiles() {
      const uploadFiles = this.$refs.importUpload && Array.isArray(this.$refs.importUpload.uploadFiles)
        ? this.$refs.importUpload.uploadFiles
        : []
      const source = uploadFiles.length ? uploadFiles : (this.importForm.files || [])
      return (source || []).map(item => {
        const raw = this.extractImportRawFile(item)
        if (!raw) {
          return null
        }
        return {
          raw,
          name: item.name || raw.name || '导入文件'
        }
      }).filter(Boolean)
    },
    loadImportOptions(tizhiceshiId = this.importForm.tizhiceshiId) {
      this.importOptionsLoading = true
      return this.$http({
        url: 'ceshichengji/importOptions',
        method: 'get',
        params: {
          tizhiceshiId: tizhiceshiId || undefined
        }
      }).then(({ data }) => {
        if (data && data.code === 0) {
          const payload = data.data || {}
          this.importOptions = {
            tests: payload.tests || [],
            banjis: payload.banjis || []
          }
          this.syncImportTaskMeta()
          if (this.importForm.banji && this.importOptions.banjis.indexOf(this.importForm.banji) === -1) {
            this.importForm.banji = ''
          }
        } else {
          this.importOptions = {
            tests: [],
            banjis: []
          }
          this.syncImportTaskMeta()
          this.$message.error((data && data.msg) || '获取导入筛选项失败')
        }
      }).finally(() => {
        this.importOptionsLoading = false
      })
    },
    ensureImportTaskSelection() {
      const tests = this.importOptions.tests || []
      if (!tests.length) {
        this.importForm.tizhiceshiId = ''
        this.syncImportTaskMeta()
        return
      }
      const currentTask = tests.find(item => String(item.id) === String(this.importForm.tizhiceshiId))
      if (currentTask) {
        this.syncImportTaskMeta()
        return
      }
      const listFirst = this.dataList[0] || {}
      const matched = tests.find(item => item.ceshibianhao === listFirst.ceshibianhao) || tests[0]
      this.importForm.tizhiceshiId = matched ? matched.id : ''
      this.syncImportTaskMeta()
      if (this.importForm.tizhiceshiId) {
        this.loadImportOptions(this.importForm.tizhiceshiId)
      }
    },
    syncImportTaskMeta() {
      const tests = this.importOptions.tests || []
      const hit = tests.find(item => String(item.id) === String(this.importForm.tizhiceshiId))
      this.importForm.ceshibianhao = hit ? (hit.ceshibianhao || '') : ''
      this.importForm.ceshimingcheng = hit ? (hit.ceshimingcheng || '') : ''
    },
    onImportTaskChange(value) {
      this.importForm.banji = ''
      this.syncImportTaskMeta()
      this.loadImportOptions(value)
    },
    submitImport() {
      if (!this.importForm.tizhiceshiId) {
        this.$message.warning('请选择已发布测试任务')
        return
      }
      const files = this.getSelectedImportFiles()
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
        formData.append('file', current.raw, current.name || (current.raw && current.raw.name) || 'import-file')

        return this.$http({
          url: 'ceshichengji/importExcel',
          method: 'post',
          params: {
            tizhiceshiId: this.importForm.tizhiceshiId,
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
        if (!this.importForm.dryRun && merged.inserted === 0 && merged.updated === 0 && (merged.errors || []).length > 0) {
          this.$alert(
            '本次没有成功导入任何成绩。若Excel里已有学号，姓名不一致只会警告，不会拦截；若没有学号，则必须按“班级 + 姓名”匹配学生。请打开“错误”页签查看明细。',
            '导入未成功',
            { confirmButtonText: '知道了' }
          )
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
    downloadReport(file) {
      if (!file) {
        this.$message.warning('暂无报告文件')
        return
      }
      const fileName = String(file).split('/').pop()
      window.open(`${this.$base.url}file/download?fileName=${encodeURIComponent(fileName)}`)
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
      this.statsData = null
      this.disposeStatsCharts()
      this.removeStatsVisualHost()
      this.loadStatsOptions().then(() => {
        if (!this.statsQuery.ceshibianhao) {
          const first = this.statsOptions.tests[0]
          this.statsQuery.ceshibianhao = first ? first.ceshibianhao : (this.dataList[0] && this.dataList[0].ceshibianhao) || ''
        }
        if (this.statsQuery.ceshibianhao) {
          this.loadStatsOptions(this.statsQuery.ceshibianhao)
        }
      })
    },
    loadStatsOptions(ceshibianhao = this.statsQuery.ceshibianhao) {
      this.statsOptionsLoading = true
      return this.$http({
        url: 'ceshichengji/statsOptions',
        method: 'get',
        params: {
          ceshibianhao: ceshibianhao || undefined
        }
      }).then(({ data }) => {
        if (data && data.code === 0) {
          const payload = data.data || {}
          this.statsOptions = {
            tests: payload.tests || [],
            banjis: payload.banjis || []
          }
          if (this.statsQuery.banji && this.statsOptions.banjis.indexOf(this.statsQuery.banji) === -1) {
            this.statsQuery.banji = ''
          }
        } else {
          this.statsOptions = {
            tests: [],
            banjis: []
          }
          this.$message.error((data && data.msg) || '获取统计筛选项失败')
        }
      }).finally(() => {
        this.statsOptionsLoading = false
      })
    },
    formatStatsTestLabel(item) {
      if (!item) {
        return ''
      }
      return item.ceshimingcheng ? `${item.ceshibianhao} - ${item.ceshimingcheng}` : item.ceshibianhao
    },
    onStatsTestChange(value) {
      this.statsQuery.banji = ''
      this.statsData = null
      this.disposeStatsCharts()
      this.removeStatsVisualHost()
      this.loadStatsOptions(value)
    },
    loadStats() {
      if (!this.statsQuery.ceshibianhao) {
        this.$message.warning('请选择测试编号')
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
          this.renderStatsVisuals()
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
