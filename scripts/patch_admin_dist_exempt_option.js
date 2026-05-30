var Files = Java.type("java.nio.file.Files");
var Paths = Java.type("java.nio.file.Paths");
var StandardCharsets = Java.type("java.nio.charset.StandardCharsets");

var path = Paths.get("src/main/resources/admin/admin/dist/js/app.4d501d8c.js");
var text = new java.lang.String(Files.readAllBytes(path), StandardCharsets.UTF_8);

function replaceOnce(content, oldText, newText, label) {
  var index = content.indexOf(oldText);
  if (index < 0) {
    throw new Error("pattern not found: " + label);
  }
  return content.substring(0, index) + newText + content.substring(index + oldText.length);
}

function replaceAll(content, oldText, newText, label) {
  var count = 0;
  while (content.indexOf(oldText) >= 0) {
    content = replaceOnce(content, oldText, newText, label);
    count++;
  }
  if (count === 0) {
    throw new Error("pattern not found: " + label);
  }
  print(label + "=" + count);
  return content;
}

text = replaceAll(
  text,
  'ceshipingjiOptions:["优秀","良好","及格","不及格"]',
  'ceshipingjiOptions:["优秀","良好","及格","不及格","免测"]',
  "rating-options"
);

text = replaceOnce(
  text,
  'aiPreviewText:"",ruleForm:{',
  'aiPreviewText:"",reportContext:null,ruleForm:{',
  "report-context-data"
);

text = replaceOnce(
  text,
  'resetForm:function(){this.ruleForm=this.getDefaultForm()},applyCrossDefaults:function(t){var e=this.$storage.getObj("crossObj")||{},o=t&&t.preserveId;o||(this.ruleForm.id=""),this.ruleForm.ceshibianhao=e.ceshibianhao||this.ruleForm.ceshibianhao,this.ruleForm.ceshimingcheng=e.ceshimingcheng||this.ruleForm.ceshimingcheng,this.ruleForm.jiaoshigonghao=e.jiaoshigonghao||this.ruleForm.jiaoshigonghao,this.ruleForm.jiaoshixingming=e.jiaoshixingming||this.ruleForm.jiaoshixingming,this.ruleForm.yonghuzhanghao=e.yonghuzhanghao||this.ruleForm.yonghuzhanghao,this.ruleForm.yonghuxingming=e.yonghuxingming||this.ruleForm.yonghuxingming,this.ruleForm.banji=e.banji||this.ruleForm.banji,e.beizhu&&!this.ruleForm.ceshipingjia&&(this.ruleForm.ceshipingjia=e.beizhu),this.ruleForm.yonghuzhanghao&&this.fillStudentByAccount(this.ruleForm.yonghuzhanghao)},fillStudentByAccount:function(t){',
  'resetForm:function(){this.ruleForm=this.getDefaultForm(),this.reportContext=null},applyCrossDefaults:function(t){var e=this.$storage.getObj("crossObj")||{},o=t&&t.preserveId;o||(this.ruleForm.id=""),this.reportContext=Object.assign({},e),this.ruleForm.ceshibianhao=e.ceshibianhao||this.ruleForm.ceshibianhao,this.ruleForm.ceshimingcheng=e.ceshimingcheng||this.ruleForm.ceshimingcheng,this.ruleForm.jiaoshigonghao=e.jiaoshigonghao||this.ruleForm.jiaoshigonghao,this.ruleForm.jiaoshixingming=e.jiaoshixingming||this.ruleForm.jiaoshixingming,this.ruleForm.yonghuzhanghao=e.yonghuzhanghao||this.ruleForm.yonghuzhanghao,this.ruleForm.yonghuxingming=e.yonghuxingming||this.ruleForm.yonghuxingming,this.ruleForm.banji=e.banji||this.ruleForm.banji,e.beizhu&&!this.ruleForm.ceshipingjia&&(this.ruleForm.ceshipingjia=e.beizhu),this.isReportMarkedExempt(e.beizhu)&&(this.ruleForm.ceshipingji="免测"),this.ruleForm.yonghuzhanghao&&this.fillStudentByAccount(this.ruleForm.yonghuzhanghao)},fillStudentByAccount:function(t){',
  "cross-defaults"
);

text = replaceOnce(
  text,
  'doValidate:function(){var t=this,e=this.buildPayload();this.$http({url:"ceshichengji/validate",method:"post",data:e}).then((function(e){var o=e.data;if(o&&0===o.code){var n=o.data||{};t.analysis=n,null!=n.compositeScore&&(t.ruleForm.ceshipingfen=n.compositeScore),n.rating&&(t.ruleForm.ceshipingji=n.rating),n.abnormalMessages&&(t.ruleForm.abnormalFlag=n.abnormal?1:0,t.ruleForm.abnormalReason=(n.abnormalMessages||[]).join("、"),t.ruleForm.weakItems=(n.weakItems||[]).join("、"),t.ruleForm.strongItems=(n.strongItems||[]).join("、")),t.$message.success("校验完成")}else t.$message.error(o.msg||"校验失败")}))},previewAiAdvice:function(){var t=this,e=this.buildPayload();this.$http({url:"ceshichengji/aiSuggestPreview",method:"post",data:e}).then((function(e){var o=e.data;if(o&&0===o.code){var n=o.data||{};t.aiPreviewText=n.advice||"",n.analysis&&(t.analysis=n.analysis),t.aiPreviewDialogVisible=!0}else t.$message.error(o.msg||"AI预览失败")}))},applyAiPreview:function(){this.ruleForm.ceshipingjia=this.aiPreviewText,this.aiPreviewDialogVisible=!1,this.$message.success("已写入测试评价")},onSubmit:function(){var t=this;this.$refs.ruleForm.validate((function(e){if(e){var o=t.buildPayload();t.$http({url:"ceshichengji/".concat(o.id?"update":"save"),method:"post",data:o}).then((function(e){var o=e.data;o&&0===o.code?(o.analysis&&(t.analysis=o.analysis),t.$message.success("操作成功"),t.parent.showFlag=!0,t.parent.addOrUpdateFlag=!1,t.parent.ceshichengjiCrossAddOrUpdateFlag=!1,t.parent.search()):t.$message.error(o.msg||"提交失败")}))}}))},back:function(){this.parent.showFlag=!0,this.parent.addOrUpdateFlag=!1,this.parent.ceshichengjiCrossAddOrUpdateFlag=!1},buildPayload:function(){var t=Object.assign({},this.ruleForm),e=["ceshipingfen","pullUp","sitUp"],o=["run50m","run1000m","run800m","longJump","bmi"];return e.forEach((function(e){var o=t[e];t[e]=""===o||void 0===o||null===o?null:parseInt(o,10)})),o.forEach((function(e){var o=t[e];t[e]=""===o||void 0===o||null===o?null:parseFloat(o)})),t}}',
  'doValidate:function(){if("免测"===this.ruleForm.ceshipingji)return void this.$message.warning("已选择免测，无需进行成绩校验");var t=this,e=this.buildPayload();this.$http({url:"ceshichengji/validate",method:"post",data:e}).then((function(e){var o=e.data;if(o&&0===o.code){var n=o.data||{};t.analysis=n,null!=n.compositeScore&&(t.ruleForm.ceshipingfen=n.compositeScore),n.rating&&(t.ruleForm.ceshipingji=n.rating),n.abnormalMessages&&(t.ruleForm.abnormalFlag=n.abnormal?1:0,t.ruleForm.abnormalReason=(n.abnormalMessages||[]).join("、"),t.ruleForm.weakItems=(n.weakItems||[]).join("、"),t.ruleForm.strongItems=(n.strongItems||[]).join("、")),t.$message.success("校验完成")}else t.$message.error(o.msg||"校验失败")}))},previewAiAdvice:function(){if("免测"===this.ruleForm.ceshipingji)return void this.$message.warning("已选择免测，无需生成成绩建议");var t=this,e=this.buildPayload();this.$http({url:"ceshichengji/aiSuggestPreview",method:"post",data:e}).then((function(e){var o=e.data;if(o&&0===o.code){var n=o.data||{};t.aiPreviewText=n.advice||"",n.analysis&&(t.analysis=n.analysis),t.aiPreviewDialogVisible=!0}else t.$message.error(o.msg||"AI预览失败")}))},applyAiPreview:function(){this.ruleForm.ceshipingjia=this.aiPreviewText,this.aiPreviewDialogVisible=!1,this.$message.success("已写入测试评价")},onSubmit:function(){var t=this;if("免测"===this.ruleForm.ceshipingji)return void this.submitExempt();this.$refs.ruleForm.validate((function(e){if(e){var o=t.buildPayload(),n=function(){t.$http({url:"ceshichengji/".concat(o.id?"update":"save"),method:"post",data:o}).then((function(e){var o=e.data;o&&0===o.code?(o.analysis&&(t.analysis=o.analysis),t.finishSubmit("操作成功")):t.$message.error(o.msg||"提交失败")}))};t.syncReportRemark(!1).then(n).catch((function(e){t.$message.error(e&&e.message||"同步报告状态失败")}))}}))},finishSubmit:function(t){this.$message.success(t||"操作成功"),this.parent.showFlag=!0,this.parent.addOrUpdateFlag=!1,this.parent.ceshichengjiCrossAddOrUpdateFlag=!1,this.parent.search()},submitExempt:function(){var t=this,e=this.reportContext||this.$storage.getObj("crossObj")||{};if(!e.id)return void this.$message.error("缺少报告记录，无法标记免测");this.syncReportRemark(!0).then((function(){return t.removeExistingScore()})).then((function(){t.finishSubmit("已标记为免测")})).catch((function(e){t.$message.error(e&&e.message||"免测提交失败")}))},removeExistingScore:function(){var t=this,e=this.ruleForm.id||this.reportContext&&this.reportContext.scoreId||(this.$storage.getObj("crossObj")||{}).scoreId;return e?this.$http({url:"ceshichengji/delete",method:"post",data:[Number(e)]}).then((function(o){var n=o.data;if(!(n&&0===n.code))return Promise.reject(new Error(n&&n.msg||"免测已标记，但原成绩删除失败"));return t.ruleForm.id="",t.reportContext&&(t.reportContext.scoreId="",t.reportContext.hasScore=!1),n})):Promise.resolve()},syncReportRemark:function(t){if(!this.isCrossMode)return Promise.resolve();var e=Object.assign({},this.reportContext||this.$storage.getObj("crossObj")||{});if(!e.id)return Promise.resolve();var o=t?this.buildExemptRemark(e.beizhu):this.stripExemptRemark(e.beizhu),n={id:e.id,ceshibianhao:e.ceshibianhao,ceshimingcheng:e.ceshimingcheng,jiaoshigonghao:e.jiaoshigonghao,jiaoshixingming:e.jiaoshixingming,baogaowenjian:e.baogaowenjian,tijiaoriqi:e.tijiaoriqi,yonghuzhanghao:e.yonghuzhanghao,yonghuxingming:e.yonghuxingming,banji:e.banji,beizhu:o,addtime:e.addtime},a=this;return this.$http({url:"ceshibaogao/update",method:"post",data:n}).then((function(t){var n=t.data;if(!(n&&0===n.code))return Promise.reject(new Error(n&&n.msg||"报告备注同步失败"));return a.reportContext=Object.assign({},e,{beizhu:o}),n}))},normalizeRemark:function(t){return String(t||"").replace(/<[^>]*>/g," ").replace(/&nbsp;/gi," ").replace(/&amp;/gi,"&").replace(/　/g," ").replace(/\\s+/g," ").trim()},isReportMarkedExempt:function(t){return-1!==this.normalizeRemark(t).indexOf("免测")},extractExemptReason:function(t){var e=this.normalizeRemark(t).match(/免测[:：]?\\s*(.*)/);return e&&e[1]?e[1].trim():""},stripExemptRemark:function(t){var e=String(t||"");return e=e.replace(/<p[^>]*>\\s*免测(?:[:：][\\s\\S]*?)?<\\/p>/gi,""),e=e.replace(/<div[^>]*>\\s*免测(?:[:：][\\s\\S]*?)?<\\/div>/gi,""),e=e.replace(/(?:^|<br\\s*\\/?>|\\r?\\n)\\s*免测[:：]?[^\\n\\r<]*/gi,""),e=e.replace(/(<br\\s*\\/?>\\s*){2,}/gi,"<br>"),e.trim()},buildExemptRemark:function(t){var e=this.stripExemptRemark(t),o=this.extractExemptReason(this.ruleForm.ceshipingjia)||this.normalizeRemark(this.ruleForm.ceshipingjia),n=o?"免测："+o:"免测";return e?""+e+"<p>"+n+"</p>":"<p>"+n+"</p>"},back:function(){this.parent.showFlag=!0,this.parent.addOrUpdateFlag=!1,this.parent.ceshichengjiCrossAddOrUpdateFlag=!1},buildPayload:function(){var t=Object.assign({},this.ruleForm),e=["ceshipingfen","pullUp","sitUp"],o=["run50m","run1000m","run800m","longJump","bmi"];return e.forEach((function(e){var o=t[e];t[e]=""===o||void 0===o||null===o?null:parseInt(o,10)})),o.forEach((function(e){var o=t[e];t[e]=""===o||void 0===o||null===o?null:parseFloat(o)})),t}}',
  "score-editor-methods"
);

text = replaceOnce(
  text,
  'getScoreActionLabel:function(t){return t&&t.hasScore?"修改成绩":"成绩"},hydrateScoreStatus:function(t){var e=this;Array.isArray(t)&&0!==t.length&&t.forEach((function(t){e.$set(t,"hasScore",!1),e.$set(t,"scoreId",""),e.$set(t,"scoreStatus","未给成绩"),t&&t.yonghuzhanghao&&(t.ceshibianhao||t.ceshimingcheng)?e.$http({url:"ceshichengji/matchByReport",method:"get",params:{ceshibianhao:t.ceshibianhao,ceshimingcheng:t.ceshimingcheng,yonghuzhanghao:t.yonghuzhanghao,jiaoshigonghao:t.jiaoshigonghao}}).then((function(o){var n=o.data;if(n&&0===n.code){var a=n.data||{};e.$set(t,"hasScore",!!a.hasScore),e.$set(t,"scoreId",a.scoreId||""),e.$set(t,"scoreStatus",a.status||(a.hasScore?"已给成绩":"未给成绩"))}})):e.$set(t,"scoreStatus","缺少关键信息")}))},init:function(){},search:function(){this.pageIndex=1,this.getDataList()}',
  'getScoreActionLabel:function(t){return this.isReportMarkedExempt(t&&t.beizhu)?"修改免测":t&&t.hasScore?"修改成绩":"成绩/免测"},normalizeRemark:function(t){return String(t||"").replace(/<[^>]*>/g," ").replace(/&nbsp;/gi," ").replace(/&amp;/gi,"&").replace(/　/g," ").replace(/\\s+/g," ").trim()},isReportMarkedExempt:function(t){return-1!==this.normalizeRemark(t).indexOf("免测")},applyScoreStatus:function(t,e){var o=this.isReportMarkedExempt(t&&t.beizhu),n=!!(e&&e.hasScore);this.$set(t,"exemptFlag",o),this.$set(t,"hasScore",n),this.$set(t,"scoreId",e&&e.scoreId||""),this.$set(t,"scoreStatus",o?"已标记免测":e&&e.status|| (n?"已给成绩":"未给成绩"))},hydrateScoreStatus:function(t){var e=this;Array.isArray(t)&&0!==t.length&&t.forEach((function(t){e.applyScoreStatus(t,{}),t&&t.yonghuzhanghao&&(t.ceshibianhao||t.ceshimingcheng)?e.$http({url:"ceshichengji/matchByReport",method:"get",params:{ceshibianhao:t.ceshibianhao,ceshimingcheng:t.ceshimingcheng,yonghuzhanghao:t.yonghuzhanghao,jiaoshigonghao:t.jiaoshigonghao}}).then((function(o){var n=o.data;n&&0===n.code&&e.applyScoreStatus(t,n.data||{})})):e.$set(t,"scoreStatus","缺少关键信息")}))},init:function(){},search:function(){this.pageIndex=1,this.getDataList()}',
  "report-list-status"
);

Files.write(path, String(text).getBytes(StandardCharsets.UTF_8));
print("patched=" + path.toString());
