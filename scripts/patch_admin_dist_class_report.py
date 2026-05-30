from pathlib import Path


path = Path("src/main/resources/admin/admin/dist/js/app.4d501d8c.js")
text = path.read_text()


def replace_once(content: str, old: str, new: str, label: str) -> str:
    if old not in content:
        raise SystemExit(f"pattern not found: {label}")
    return content.replace(old, new, 1)


start_old = (
    'created:function(){this.sessionTable=this.$storage.get("sessionTable")||"",this.getDataList()},'
    'computed:{canManageScore:function(){return"jiaoshi"===this.sessionTable||"users"===this.sessionTable}},'
    'methods:{contentStyleChange:function(){},search:function(){'
)

start_new = """created:function(){this.sessionTable=this.$storage.get("sessionTable")||"",this.getDataList()},
mounted:function(){var t=this;this._statsResizeHandler=function(){t.resizeStatsCharts()},window.addEventListener("resize",this._statsResizeHandler)},
beforeDestroy:function(){this._statsResizeHandler&&window.removeEventListener("resize",this._statsResizeHandler),this.disposeStatsCharts(),this.removeStatsVisualHost()},
computed:{canManageScore:function(){return"jiaoshi"===this.sessionTable||"users"===this.sessionTable}},
methods:{contentStyleChange:function(){},
ensureStatsVisualStyles:function(){
if(document.getElementById("class-report-visual-style"))return;
var t=document.createElement("style");
t.id="class-report-visual-style";
t.innerHTML=".class-report-visuals{margin-top:16px}.class-report-banner{margin-bottom:14px;padding:18px 20px;border-radius:10px;background:linear-gradient(135deg,#effaf7 0%,#e3f5f1 100%);border:1px solid #d3ece6}.class-report-banner__eyebrow{font-size:12px;color:#2c8d7f;margin-bottom:6px}.class-report-banner__title{font-size:18px;line-height:1.4;color:#16574d;font-weight:700}.class-report-banner__meta{margin-top:8px;display:flex;flex-wrap:wrap;gap:8px}.class-report-banner__tag{display:inline-flex;align-items:center;padding:4px 10px;border-radius:999px;background:rgba(34,165,149,.1);color:#1d7f71;font-size:12px}.class-report-banner__desc{margin-top:10px;color:#335f57;line-height:1.7;font-size:13px}.class-report-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:14px}.class-report-card{padding:12px 14px 16px;border-radius:10px;border:1px solid #e1efec;background:#fff;box-shadow:0 8px 18px rgba(32,102,92,.06)}.class-report-card__title{margin-bottom:10px;font-size:14px;font-weight:700;color:#1d5e54}.class-report-chart{width:100%;height:280px}.class-report-empty{display:flex;align-items:center;justify-content:center;height:280px;color:#8da39e;background:#f8fcfb;border-radius:8px}@media (max-width:900px){.class-report-grid{grid-template-columns:1fr}}";
document.head.appendChild(t)
},
escapeHtml:function(t){
return String(null==t?"":t).replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g,"&quot;").replace(/'/g,"&#39;")
},
findStatsDialogBody:function(){
for(var t=Array.prototype.slice.call(document.querySelectorAll(".el-dialog__wrapper")),e=0;e<t.length;e++){
var o=t[e];
if("none"!==window.getComputedStyle(o).display){
var n=o.querySelector(".el-dialog__title");
if(n&&n.textContent&&-1!==n.textContent.indexOf("成绩统计概览"))return o.querySelector(".el-dialog__body")
}}
return null
},
removeStatsVisualHost:function(){
var t=this.findStatsDialogBody(),e=t&&t.querySelector(".class-report-visuals");
e&&e.parentNode&&e.parentNode.removeChild(e)
},
disposeStatsCharts:function(){
if(!this._statsCharts)return void(this._statsCharts={});
for(var t=Object.keys(this._statsCharts),e=0;e<t.length;e++){
var o=this._statsCharts[t[e]];
o&&o.dispose&&o.dispose()
}
this._statsCharts={}
},
resizeStatsCharts:function(){
if(this._statsCharts)for(var t=Object.keys(this._statsCharts),e=0;e<t.length;e++){
var o=this._statsCharts[t[e]];
o&&o.resize&&o.resize()
}
},
getSelectedStatsTestName:function(){
for(var t=this.statsOptions.tests||[],e=0;e<t.length;e++)if(t[e].ceshibianhao===this.statsQuery.ceshibianhao)return t[e].ceshimingcheng||t[e].ceshibianhao;
return this.statsData&&this.statsData.ceshibianhao?this.statsData.ceshibianhao:this.statsQuery.ceshibianhao||"体测统计"
},
getLeadRatingEntry:function(){
for(var t=(this.statsData&&this.statsData.ratingDistribution)||{},e=[],o=Object.keys(t),n=0;n<o.length;n++){
var a=o[n],i=Number(t[a]||0);
if("未评级"!==a&&i>0){e.push({key:a,count:i})}
}
return e.sort((function(t,e){return e.count-t.count})),e[0]||null
},
buildClassReportText:function(){
if(!this.statsData)return"";
var t=this.statsData,e=this.statsQuery.banji||"当前范围",o=this.getLeadRatingEntry(),n=(t.weakItemTop||[])[0],a=null==t.avgScore?"当前还没有可用于计算的平均分":"平均分为 "+t.avgScore,i=null==t.rosterCount?"已提交报告 "+t.submittedCount+" 人，已评分 "+t.testedCount+" 人":"应测 "+t.rosterCount+" 人，已提交报告 "+t.submittedCount+" 人，已评分 "+t.testedCount+" 人",r=t.pendingScoreCount>0?"仍有 "+t.pendingScoreCount+" 人已交未评分":"报告评分进度已经清空",s=null==t.absentCount?"":"，未交/缺测 "+t.absentCount+" 人",l=t.exemptCount>0?"，免测 "+t.exemptCount+" 人":"",u=n?"当前最集中的薄弱项是 "+n.item+"，涉及 "+n.count+" 人。":"当前还没有明显聚集的薄弱项。",c=o?"班级主流评级为 "+o.key+"（"+o.count+" 人）。":"当前还没有形成明确的评级分布。";
return e+" 本次体测中，"+i+s+l+"，"+a+"。"+r+"。"+c+u
},
renderStatsVisuals:function(){
if(this.statsDialogVisible&&this.statsData){
var t=this;
this.ensureStatsVisualStyles(),this.$nextTick((function(){
var e=t.findStatsDialogBody();
if(e){
t.disposeStatsCharts(),t.removeStatsVisualHost();
var o=document.createElement("div"),n=t.statsQuery.banji||"全部班级",a=t.escapeHtml(t.getSelectedStatsTestName()),i=t.escapeHtml(t.buildClassReportText()),r=null==t.statsData.avgScore?"--":t.statsData.avgScore,s=t.statsData.abnormalCount||0,l=t.statsData.pendingScoreCount||0;
o.className="class-report-visuals";
o.innerHTML='<div class="class-report-banner"><div class="class-report-banner__eyebrow">班级体测报告</div><div class="class-report-banner__title">'+t.escapeHtml(n)+" · "+a+'</div><div class="class-report-banner__meta"><span class="class-report-banner__tag">平均分 '+t.escapeHtml(r)+'</span><span class="class-report-banner__tag">异常 '+t.escapeHtml(s)+'</span><span class="class-report-banner__tag">待评分 '+t.escapeHtml(l)+'</span></div><div class="class-report-banner__desc">'+i+'</div></div><div class="class-report-grid"><div class="class-report-card"><div class="class-report-card__title">班级进度</div><div id="class-report-progress-chart" class="class-report-chart"></div></div><div class="class-report-card"><div class="class-report-card__title">状态占比</div><div id="class-report-status-chart" class="class-report-chart"></div></div><div class="class-report-card"><div class="class-report-card__title">评级分布图</div><div id="class-report-rating-chart" class="class-report-chart"></div></div><div class="class-report-card"><div class="class-report-card__title">薄弱项分布</div><div id="class-report-weak-chart" class="class-report-chart"></div></div></div>';
e.appendChild(o),t.$nextTick((function(){
t.initStatsChart("progress","class-report-progress-chart",t.buildProgressChartOption()),
t.initStatsChart("status","class-report-status-chart",t.buildStatusChartOption()),
t.initStatsChart("rating","class-report-rating-chart",t.buildRatingChartOption()),
t.initStatsChart("weak","class-report-weak-chart",t.buildWeakChartOption()),
t.resizeStatsCharts()
}))
}
}))
}
},
initStatsChart:function(t,e,o){
var n=document.getElementById(e);
if(n)if(o){
var a=this.$echarts.init(n,"macarons");
a.setOption(o),this._statsCharts||(this._statsCharts={}),this._statsCharts[t]=a
}else n.innerHTML='<div class="class-report-empty">暂无可视化数据</div>'
},
buildProgressChartOption:function(){
if(!this.statsData)return null;
var t=this.statsData,e=[],o=[];
return null!=t.rosterCount&&(e.push("应测人数"),o.push(Number(t.rosterCount||0))),e.push("已提交报告","已评分","已交未评分","免测"),o.push(Number(t.submittedCount||0),Number(t.testedCount||0),Number(t.pendingScoreCount||0),Number(t.exemptCount||0)),null!=t.absentCount&&(e.push("未交/缺测"),o.push(Number(t.absentCount||0))),{color:["#34beac"],tooltip:{trigger:"axis",axisPointer:{type:"shadow"}},grid:{top:26,left:50,right:20,bottom:42},xAxis:{type:"category",data:e,axisLabel:{interval:0,rotate:e.length>5?20:0}},yAxis:{type:"value",minInterval:1},series:[{name:"人数",type:"bar",barMaxWidth:42,data:o,label:{show:!0,position:"top"},itemStyle:{borderRadius:[6,6,0,0]}}]}
},
buildStatusChartOption:function(){
if(!this.statsData)return null;
var t=[{name:"已评分",value:Number(this.statsData.testedCount||0)},{name:"已交未评分",value:Number(this.statsData.pendingScoreCount||0)},{name:"免测",value:Number(this.statsData.exemptCount||0)}];
null!=this.statsData.absentCount&&t.push({name:"未交/缺测",value:Number(this.statsData.absentCount||0)});
for(var e=!1,o=0;o<t.length;o++)if(t[o].value>0){e=!0;break}
return e?{color:["#36cfc9","#faad14","#7c6df2","#ff7875"],tooltip:{trigger:"item",formatter:"{b}: {c} ({d}%)"},legend:{bottom:0},series:[{name:"状态占比",type:"pie",radius:["42%","68%"],center:["50%","45%"],avoidLabelOverlap:!1,label:{formatter:"{b}\\n{c}人"},data:t}]}:null
},
buildRatingChartOption:function(){
if(!this.statsData)return null;
for(var t=this.statsData.ratingDistribution||{},e=["优秀","良好","及格","不及格","未评级"],o=[],n=0;n<e.length;n++)o.push(Number(t[e[n]]||0));
for(var a=!1,i=0;i<o.length;i++)if(o[i]>0){a=!0;break}
return a?{color:["#5b8ff9"],tooltip:{trigger:"axis",axisPointer:{type:"shadow"}},grid:{top:26,left:42,right:20,bottom:30},xAxis:{type:"category",data:e},yAxis:{type:"value",minInterval:1},series:[{type:"bar",barMaxWidth:40,data:o,label:{show:!0,position:"top"},itemStyle:{borderRadius:[6,6,0,0]}}]}:null
},
buildWeakChartOption:function(){
if(!this.statsData)return null;
var t=(this.statsData.weakItemTop||[]).slice(0,6);
if(!t.length)return null;
for(var e=[],o=[],n=t.length-1;n>=0;n--)e.push(t[n].item),o.push(Number(t[n].count||0));
return{color:["#ff9f7f"],tooltip:{trigger:"axis",axisPointer:{type:"shadow"}},grid:{top:16,left:70,right:20,bottom:20},xAxis:{type:"value",minInterval:1},yAxis:{type:"category",data:e},series:[{type:"bar",barMaxWidth:24,data:o,label:{show:!0,position:"right"},itemStyle:{borderRadius:[0,6,6,0]}}]}
},
search:function(){"""

text = replace_once(text, start_old, start_new, "start-block")

stats_old = """openStatsDialog:function(){var t=this;this.statsDialogVisible=!0,this.statsData=null,this.loadStatsOptions().then((function(){if(!t.statsQuery.ceshibianhao){var e=t.statsOptions.tests[0];t.statsQuery.ceshibianhao=e?e.ceshibianhao:t.dataList[0]&&t.dataList[0].ceshibianhao||""}t.statsQuery.ceshibianhao&&t.loadStatsOptions(t.statsQuery.ceshibianhao)}))},loadStatsOptions:function(){var t=this,e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:this.statsQuery.ceshibianhao;return this.statsOptionsLoading=!0,this.$http({url:"ceshichengji/statsOptions",method:"get",params:{ceshibianhao:e||void 0}}).then((function(e){var o=e.data;if(o&&0===o.code){var n=o.data||{};t.statsOptions={tests:n.tests||[],banjis:n.banjis||[]},t.statsQuery.banji&&-1===t.statsOptions.banjis.indexOf(t.statsQuery.banji)&&(t.statsQuery.banji="")}else t.statsOptions={tests:[],banjis:[]},t.$message.error(o&&o.msg||"获取统计筛选项失败")})).finally((function(){t.statsOptionsLoading=!1}))},formatStatsTestLabel:function(t){return t?t.ceshimingcheng?"".concat(t.ceshibianhao," - ").concat(t.ceshimingcheng):t.ceshibianhao:""},onStatsTestChange:function(t){this.statsQuery.banji="",this.statsData=null,this.loadStatsOptions(t)},loadStats:function(){var t=this;this.statsQuery.ceshibianhao?this.$http({url:"ceshichengji/statsOverview",method:"get",params:{ceshibianhao:this.statsQuery.ceshibianhao,banji:this.statsQuery.banji||void 0}}).then((function(e){var o=e.data;o&&0===o.code?t.statsData=o.data:t.$message.error(o.msg||"统计失败")})):this.$message.warning("请选择测试编号")},openBatchAiDialog:function(){"""

stats_new = """openStatsDialog:function(){var t=this;this.statsDialogVisible=!0,this.statsData=null,this.disposeStatsCharts(),this.removeStatsVisualHost(),this.loadStatsOptions().then((function(){if(!t.statsQuery.ceshibianhao){var e=t.statsOptions.tests[0];t.statsQuery.ceshibianhao=e?e.ceshibianhao:t.dataList[0]&&t.dataList[0].ceshibianhao||""}t.statsQuery.ceshibianhao&&t.loadStatsOptions(t.statsQuery.ceshibianhao)}))},loadStatsOptions:function(){var t=this,e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:this.statsQuery.ceshibianhao;return this.statsOptionsLoading=!0,this.$http({url:"ceshichengji/statsOptions",method:"get",params:{ceshibianhao:e||void 0}}).then((function(e){var o=e.data;if(o&&0===o.code){var n=o.data||{};t.statsOptions={tests:n.tests||[],banjis:n.banjis||[]},t.statsQuery.banji&&-1===t.statsOptions.banjis.indexOf(t.statsQuery.banji)&&(t.statsQuery.banji="")}else t.statsOptions={tests:[],banjis:[]},t.$message.error(o&&o.msg||"获取统计筛选项失败")})).finally((function(){t.statsOptionsLoading=!1}))},formatStatsTestLabel:function(t){return t?t.ceshimingcheng?"".concat(t.ceshibianhao," - ").concat(t.ceshimingcheng):t.ceshibianhao:""},onStatsTestChange:function(t){this.statsQuery.banji="",this.statsData=null,this.disposeStatsCharts(),this.removeStatsVisualHost(),this.loadStatsOptions(t)},loadStats:function(){var t=this;this.statsQuery.ceshibianhao?this.$http({url:"ceshichengji/statsOverview",method:"get",params:{ceshibianhao:this.statsQuery.ceshibianhao,banji:this.statsQuery.banji||void 0}}).then((function(e){var o=e.data;o&&0===o.code?(t.statsData=o.data,t.renderStatsVisuals()):t.$message.error(o.msg||"统计失败")})):this.$message.warning("请选择测试编号")},openBatchAiDialog:function(){"""

text = replace_once(text, stats_old, stats_new, "stats-block")

path.write_text(text)
print(f"patched={path}")
