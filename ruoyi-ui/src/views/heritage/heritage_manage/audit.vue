<template>
  <div class="app-container heritage-admin-page">
    <!-- 1. 顶部标题 -->
    <div class="header-section">
      <div class="title-group">
        <h2 class="page-title">审核中心<span class="page-title-meta">· 待处理 {{ total }} 项</span></h2>
      </div>
      <div class="header-actions">
        <right-toolbar :search="false" @queryTable="getList" class="header-inline-tools" />
      </div>
    </div>

    <!-- 2. 待审核列表 -->
    <el-table
        v-loading="loading"
        :data="auditList"
        border
        stripe
        size="small"
        class="industrial-table"
    >
      <el-table-column type="index" label="序号" align="center" width="52" />
      <el-table-column label="封面" align="center" width="76">
        <template #default="scope">
          <image-preview :src="scope.row.coverImage" :width="40" :height="40" class="table-cover"/>
        </template>
      </el-table-column>
      <el-table-column label="展品名称" align="left" prop="itemName" min-width="140" :show-overflow-tooltip="true">
        <template #default="scope">
          <span class="item-name-bold">{{ scope.row.itemName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="发布人" align="center" prop="createBy" min-width="88" :show-overflow-tooltip="true" />
      <el-table-column label="提交日期" align="center" prop="createTime" width="158" />
      <el-table-column label="操作" align="center" width="120" fixed="right" class-name="heritage-op-col">
        <template #default="scope">
          <button
            type="button"
            class="hui-btn-primary hui-btn-primary--sm"
            @click="handleOpenAudit(scope.row)"
          >
            进入审核
          </button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 3. 审核对话框 -->
    <el-dialog
      v-model="open"
      class="audit-immersive-dialog"
      width="min(1080px, 94vw)"
      top="4vh"
      append-to-body
      :close-on-click-modal="false"
      destroy-on-close
      align-center
      @closed="onDialogClosed"
    >
      <template #header>
        <div class="audit-dialog-header">
          <span class="audit-dialog-header__badge">审核</span>
          <div class="audit-dialog-header__line" :title="headerLineTooltip">
            <h3 class="audit-dialog-header__title">{{ form.itemName || '—' }}</h3>
            <template v-if="form.createBy || form.createTime">
              <span class="audit-dialog-header__divider" aria-hidden="true" />
              <span class="audit-dialog-header__meta">
                <span v-if="form.createBy" class="audit-dialog-header__meta-text">{{ form.createBy }}</span>
                <span v-if="form.createBy && form.createTime" class="audit-dialog-header__dot">·</span>
                <time v-if="form.createTime" class="audit-dialog-header__meta-time">{{ form.createTime }}</time>
              </span>
            </template>
          </div>
        </div>
      </template>

      <div class="audit-split-layout">
        <!-- 左侧：仅视觉资产，纵向占满，无需为长文滚动 -->
        <div class="preview-axis">
          <div class="axis-label-wrap">
            <span class="axis-label">视觉预览</span>
            <span class="axis-hint">封面与 3D 随区域拉高</span>
          </div>

          <div class="visual-column">
            <div v-if="form.coverImage" class="cover-strip cover-strip--compact">
              <span class="cover-strip__label">封面</span>
              <image-preview :src="form.coverImage" :width="96" :height="96" class="cover-strip__img" />
            </div>

            <div class="viewer-container viewer-container--fill">
              <model-viewer
                  v-if="form.modelFile"
                  :src="getAssetUrl(form.modelFile)"
                  auto-rotate
                  camera-controls
                  shadow-intensity="1"
                  class="audit-model-viewer"
              ></model-viewer>
              <div v-else class="no-model">
                <span class="no-model__text">暂无 3D 模型</span>
                <span class="no-model__sub">图文说明在右侧浏览</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：先决策，下方图文说明占满剩余高度并单独滚动 -->
        <div class="form-axis">
          <div class="decision-section">
            <div class="axis-label-wrap axis-label-wrap--tight">
              <span class="axis-label">审核决策</span>
            </div>
            <el-form :model="auditForm" label-position="top" class="audit-decision-form">
              <el-form-item label="核准结论">
                <el-radio-group v-model="auditForm.status" class="industrial-radio">
                  <el-radio label="0" border>
                    <span class="radio-title">准予发布</span>
                    <span class="radio-desc">通过后将对访客可见</span>
                  </el-radio>
                  <el-radio label="2" border>
                    <span class="radio-title">驳回修改</span>
                    <span class="radio-desc">需填写原因，发布人将收到反馈</span>
                  </el-radio>
                </el-radio-group>
              </el-form-item>

              <transition name="el-zoom-in-top">
                <el-form-item
                  v-if="auditForm.status === '2'"
                  label="反馈意见"
                  required
                  class="reject-block"
                >
                  <el-input
                    type="textarea"
                    v-model="auditForm.rejectReason"
                    :rows="5"
                    maxlength="800"
                    show-word-limit
                    placeholder="请说明驳回原因或修改建议，便于发布人针对性调整…"
                    class="industrial-input"
                  />
                </el-form-item>
              </transition>
            </el-form>
          </div>

          <div class="doc-section">
            <div class="axis-label-wrap axis-label-wrap--tight">
              <span class="axis-label">图文说明</span>
              <span class="axis-hint">随内容滚动</span>
            </div>
            <div class="doc-scroll">
              <div class="text-content">
                <div class="info-item">
                  <div class="info-label">展品简介</div>
                  <p class="info-body">{{ form.description || '暂无简介' }}</p>
                </div>
                <el-divider class="text-divider" />
                <div class="info-item info-item--history">
                  <div class="info-label">历史渊源</div>
                  <div v-if="form.history" class="info-body audit-rich-text" v-html="form.history"></div>
                  <p v-else class="info-body history-empty">暂无</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="audit-dialog-footer">
          <el-button :disabled="submitLoading" @click="open = false">取 消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitAudit">
            确认并同步状态
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, getCurrentInstance, computed } from 'vue';
import { listHeritage_manage, auditHeritage } from "@/api/heritage/heritage_manage";

const { proxy } = getCurrentInstance();
const loading = ref(true);
const total = ref(0);
const auditList = ref([]);
const open = ref(false);
const form = ref({});
const submitLoading = ref(false);

const headerLineTooltip = computed(() => {
  const row = form.value || {};
  return [row.itemName, row.createBy, row.createTime].filter(Boolean).join(' · ');
});

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: 1, // 【核心】写死只查询待审核数据
  heritageAdminList: true
});

const auditForm = reactive({
  itemId: null,
  status: '0',
  rejectReason: ''
});

function getList() {
  loading.value = true;
  listHeritage_manage(queryParams).then(res => {
    auditList.value = res.rows;
    total.value = res.total;
    loading.value = false;
  });
}

function handleOpenAudit(row) {
  form.value = { ...row };
  auditForm.itemId = row.itemId;
  auditForm.status = '0';
  auditForm.rejectReason = '';
  open.value = true;
}

function onDialogClosed() {
  submitLoading.value = false;
}

function submitAudit() {
  const reason = (auditForm.rejectReason || '').trim();
  if (auditForm.status === '2' && !reason) {
    proxy.$modal.msgError("请填写驳回原因");
    return;
  }

  submitLoading.value = true;
  const payload = {
    ...auditForm,
    rejectReason: auditForm.status === '2' ? reason : auditForm.rejectReason
  };

  auditHeritage(payload)
    .then(() => {
      proxy.$modal.msgSuccess("审核处理成功");
      open.value = false;
      getList();
    })
    .finally(() => {
      submitLoading.value = false;
    });
}

const getAssetUrl = (url) => import.meta.env.VITE_APP_BASE_API + url;

onMounted(() => getList());
</script>

<style scoped lang="scss">
.item-name-bold { font-weight: 600; color: #303133; font-size: 13px; }

/* 弹窗整体（append-to-body 下子节点仍带 scoped） */
:deep(.audit-immersive-dialog.el-dialog) {
  display: flex;
  flex-direction: column;
  max-width: 1080px;
  margin: 0 auto;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.12);
}

:deep(.audit-immersive-dialog .el-dialog__header) {
  padding: 0;
  margin: 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
  display: flex;
  align-items: center;
  min-height: 0;
}

:deep(.audit-immersive-dialog .el-dialog__headerbtn) {
  top: 0;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 12px 0 8px;
}

:deep(.audit-immersive-dialog .el-dialog__body) {
  padding: 12px 20px 14px;
  flex: 1;
  min-height: 0;
}

:deep(.audit-immersive-dialog .el-dialog__footer) {
  padding: 12px 20px 16px;
  border-top: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color-blank);
}

.audit-dialog-header {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 44px 9px 18px;
}

.audit-dialog-header__badge {
  flex-shrink: 0;
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
  padding: 4px 7px;
  border-radius: 6px;
  color: var(--el-color-primary);
  background: linear-gradient(180deg, var(--el-color-primary-light-9) 0%, var(--el-fill-color-light) 100%);
  border: 1px solid var(--el-color-primary-light-5);
  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.65) inset;
}

.audit-dialog-header__line {
  display: flex;
  align-items: center;
  min-width: 0;
  flex: 1;
  gap: 0;
}

.audit-dialog-header__title {
  margin: 0;
  flex: 1 1 auto;
  min-width: 0;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: -0.02em;
  line-height: 1.35;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.audit-dialog-header__divider {
  flex-shrink: 0;
  width: 1px;
  height: 16px;
  margin: 0 12px 0 4px;
  background: var(--el-border-color);
  opacity: 0.9;
}

.audit-dialog-header__meta {
  flex: 0 1 auto;
  display: inline-flex;
  align-items: baseline;
  gap: 0;
  max-width: min(380px, 42%);
  min-width: 0;
  font-size: 13px;
  font-weight: 400;
  line-height: 1.35;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.audit-dialog-header__meta-text {
  overflow: hidden;
  text-overflow: ellipsis;
}

.audit-dialog-header__meta-time {
  font-variant-numeric: tabular-nums;
  color: var(--el-text-color-secondary);
}

.audit-dialog-header__dot {
  margin: 0 5px;
  opacity: 0.65;
  flex-shrink: 0;
}

.audit-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  width: 100%;
}

.audit-split-layout {
  --audit-body-max-h: min(72vh, calc(100vh - 152px));
  display: flex;
  align-items: stretch;
  gap: clamp(16px, 2vw, 28px);
  height: var(--audit-body-max-h);
  max-height: var(--audit-body-max-h);
}

@supports (height: 1dvh) {
  .audit-split-layout {
    --audit-body-max-h: min(72dvh, calc(100dvh - 152px));
  }
}

.axis-label-wrap {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  flex-shrink: 0;
  margin-bottom: 12px;
}

.axis-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--el-text-color-secondary);
  padding-left: 10px;
  border-left: 3px solid var(--el-color-primary);
}

.axis-hint {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  white-space: nowrap;
}

.preview-axis {
  flex: 1.12;
  min-width: 0;
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding-right: clamp(8px, 1.5vw, 20px);
  border-right: 1px solid var(--el-border-color-lighter);
}

.visual-column {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cover-strip--compact {
  margin-bottom: 0;
  flex-shrink: 0;
}

.cover-strip {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  padding: 10px 12px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
}

.cover-strip__label {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
}

.cover-strip__img {
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.viewer-container {
  background: linear-gradient(180deg, #f5f7fa 0%, #eef1f6 100%);
  border-radius: 10px;
  position: relative;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
}

.viewer-container--fill {
  flex: 1;
  min-height: 200px;
  margin-bottom: 0;
}

.audit-model-viewer {
  width: 100%;
  height: 100%;
  outline: none;
}

.no-model {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: var(--el-text-color-placeholder);
}

.no-model__text {
  font-size: 14px;
  font-weight: 600;
}

.no-model__sub {
  font-size: 12px;
  opacity: 0.85;
}

.text-content {
  padding-bottom: 8px;
}

.text-divider {
  margin: 14px 0;
}

.info-item {
  & + .info-item {
    margin-top: 4px;
  }
}

.info-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}

.info-body {
  margin: 0;
  font-size: 14px;
  line-height: 1.75;
  color: var(--el-text-color-regular);
  word-break: break-word;
}

.axis-label-wrap--tight {
  margin-bottom: 8px;
}

.info-item--history .info-body {
  padding: 0;
  background: transparent;
  border-radius: 0;
  border: none;
}

:deep(.audit-rich-text) {
  font-size: 14px;
  line-height: 1.75;
  color: var(--el-text-color-regular);

  p {
    margin: 0 0 0.65em;
    &:last-child {
      margin-bottom: 0;
    }
  }

  img {
    max-width: 100%;
    height: auto;
    border-radius: 6px;
  }

}

.history-empty {
  margin: 0;
  padding: 0;
  color: var(--el-text-color-placeholder);
  font-style: normal;
}

.form-axis {
  flex: 1;
  min-width: 0;
  max-width: none;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

@media (min-width: 901px) {
  .form-axis {
    min-width: 300px;
  }
}

.decision-section {
  flex-shrink: 0;
}

.doc-section {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  margin-top: 4px;
  padding-top: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.doc-scroll {
  flex: 1;
  min-height: 140px;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 10px 12px;
  margin: 0 -4px 0 0;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
  scroll-behavior: smooth;

  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: var(--el-border-color);
    border-radius: 3px;
  }
  &::-webkit-scrollbar-thumb:hover {
    background: var(--el-text-color-placeholder);
  }
}

.audit-decision-form {
  padding-bottom: 0;
}

.industrial-radio {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;

  :deep(.el-radio) {
    margin-right: 0;
    width: 100%;
    height: auto;
    min-height: 52px;
    padding: 10px 14px;
    margin: 0;
    border-radius: 8px;
    border: 1px solid var(--el-border-color);
    align-items: flex-start;
    transition: border-color 0.2s, box-shadow 0.2s;

    &.is-checked {
      border-color: var(--el-color-primary);
      background: var(--el-color-primary-light-9);
      box-shadow: 0 0 0 1px var(--el-color-primary-light-7);
    }
  }

  :deep(.el-radio__label) {
    display: flex;
    flex-direction: column;
    gap: 2px;
    padding-left: 10px;
    white-space: normal;
    line-height: 1.35;
  }

  :deep(.el-radio__input.is-checked .el-radio__inner) {
    background-color: var(--el-color-primary);
    border-color: var(--el-color-primary);
  }
}

.radio-title {
  font-weight: 600;
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.radio-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.reject-block {
  margin-bottom: 0;
}

:deep(.reject-block .el-textarea__inner) {
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
}

@media (max-width: 900px) {
  .audit-split-layout {
    flex-direction: column;
    height: auto;
    max-height: min(78vh, calc(100vh - 180px));
    overflow-y: auto;
  }

  @supports (height: 1dvh) {
    .audit-split-layout {
      max-height: min(78dvh, calc(100dvh - 180px));
    }
  }

  .preview-axis {
    border-right: none;
    border-bottom: 1px solid var(--el-border-color-lighter);
    padding-right: 0;
    padding-bottom: 16px;
    flex-shrink: 0;
    max-height: min(52vh, 440px);
    min-height: 200px;
  }

  .viewer-container--fill {
    min-height: 200px;
    max-height: min(42vh, 380px);
    flex: 0 1 auto;
  }

  .form-axis {
    max-width: none;
    padding-top: 4px;
  }

  .axis-hint {
    display: none;
  }
}

.pagination-wrapper { margin-top: 30px; display: flex; justify-content: center; }
</style>
