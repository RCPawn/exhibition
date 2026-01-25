<template>
  <div class="app-container">
    <!-- 1. 顶部标题 -->
    <div class="header-section">
      <div class="title-group">
        <h2 class="page-title">审核中心</h2>
        <p class="page-subtitle">待处理资产：{{ total }} 项</p>
      </div>
    </div>

    <!-- 2. 待审核列表 -->
    <el-table v-loading="loading" :data="auditList" border class="industrial-table">
      <el-table-column type="index" label="序号" align="center" width="70" />
      <el-table-column label="封面" align="center" width="120">
        <template #default="scope">
          <image-preview :src="scope.row.coverImage" :width="60" :height="60" class="table-cover"/>
        </template>
      </el-table-column>
      <el-table-column label="展品名称" align="left" prop="itemName" min-width="200" :show-overflow-tooltip="true">
        <template #default="scope">
          <span class="item-name-bold">{{ scope.row.itemName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="发布人" align="center" prop="createBy" width="150" />
      <el-table-column label="提交日期" align="center" prop="createTime" width="180" />
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template #default="scope">
          <button class="industrial-audit-btn" @click="handleOpenAudit(scope.row)">
            进入审核
          </button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 3. 沉浸式审核对话框 -->
    <el-dialog :title="'审核请求：' + form.itemName" v-model="open" width="1000px" top="5vh" append-to-body :close-on-click-modal="false">
      <div class="audit-split-layout">
        <!-- 左侧：内容深度预览 -->
        <div class="preview-axis">
          <h4 class="axis-label">提交内容预览</h4>
          <div class="scroll-area">
            <!-- 3D模型预览区 -->
            <div class="viewer-container">
              <model-viewer
                  v-if="form.modelFile"
                  :src="getAssetUrl(form.modelFile)"
                  auto-rotate camera-controls
                  shadow-intensity="1"
                  class="audit-model-viewer"
              ></model-viewer>
              <div v-else class="no-model">无 3D 模型资产</div>
            </div>

            <div class="text-content">
              <div class="info-item">
                <label>展品简介</label>
                <p>{{ form.description }}</p>
              </div>
              <el-divider />
              <div class="info-item">
                <label>历史渊源</label>
                <div v-html="form.history"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：审核决策区 -->
        <div class="form-axis">
          <h4 class="axis-label">审核决策</h4>
          <el-form :model="auditForm" label-position="top">
            <el-form-item label="核准结论">
              <el-radio-group v-model="auditForm.status" class="industrial-radio">
                <el-radio label="0" border>准予发布</el-radio>
                <el-radio label="2" border>驳回修改</el-radio>
              </el-radio-group>
            </el-form-item>

            <transition name="el-zoom-in-top">
              <el-form-item label="反馈意见" v-if="auditForm.status === '2'" required>
                <el-input
                    type="textarea"
                    v-model="auditForm.rejectReason"
                    :rows="6"
                    placeholder="请说明驳回的具体原因或修改建议..."
                    class="industrial-input"
                />
              </el-form-item>
            </transition>
          </el-form>

          <div class="submit-wrap">
            <el-button class="confirm-btn" @click="submitAudit">确认并同步状态</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, getCurrentInstance } from 'vue';
import { listHeritage_manage, auditHeritage } from "@/api/heritage/heritage_manage";

const { proxy } = getCurrentInstance();
const loading = ref(true);
const total = ref(0);
const auditList = ref([]);
const open = ref(false);
const form = ref({});

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: 1 // 【核心】写死只查询待审核数据
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
  form.value = row;
  auditForm.itemId = row.itemId;
  auditForm.status = '0';
  auditForm.rejectReason = '';
  open.value = true;
}

function submitAudit() {
  if (auditForm.status === '2' && !auditForm.rejectReason) {
    proxy.$modal.msgError("请填写驳回原因");
    return;
  }

  auditHeritage(auditForm).then(() => {
    proxy.$modal.msgSuccess("审核处理成功");
    open.value = false;
    getList();
  });
}

const getAssetUrl = (url) => import.meta.env.VITE_APP_BASE_API + url;

onMounted(() => getList());
</script>

<style scoped lang="scss">
.header-section {
  display: flex; justify-content: space-between; align-items: flex-end;
  border-bottom: 2px solid #000; padding-bottom: 18px; margin-bottom: 25px;
  .page-title { font-size: 26px; font-weight: 900; letter-spacing: -1px; }
  .page-subtitle { color: #999; margin-top: 5px; }
}

.industrial-table {
  border-radius: 0;
  :deep(.el-table__header) th { background-color: #fcfcfc !important; color: #000; font-weight: 900; }
  :deep(.el-table__row) { height: 80px; }
}

.industrial-audit-btn {
  background: #000; color: #fff; border: none; padding: 8px 15px;
  font-size: 11px; font-weight: 900; cursor: pointer;
  &:hover { background: #333; }
}

/* 审核分屏布局 */
.audit-split-layout {
  display: flex; height: 65vh; gap: 30px;

  .axis-label { font-size: 12px; font-weight: 900; color: #ccc; margin-bottom: 20px; border-left: 3px solid #000; padding-left: 10px; }

  .preview-axis {
    flex: 1.6; display: flex; flex-direction: column; border-right: 1px solid #eee; padding-right: 20px;
    .scroll-area { flex: 1; overflow-y: auto; padding-right: 10px; }
    .viewer-container { height: 350px; background: #f8f8f8; margin-bottom: 20px; position: relative;
      .audit-model-viewer { width: 100%; height: 100%; outline: none; }
      .no-model { display: flex; align-items: center; justify-content: center; height: 100%; color: #ccc; font-weight: 900; }
    }
    .text-content {
      .info-item { label { font-size: 12px; font-weight: 900; color: #999; } p { font-size: 14px; line-height: 1.8; } }
    }
  }

  .form-axis {
    flex: 1; display: flex; flex-direction: column;
    .industrial-radio {
      display: flex; flex-direction: column; gap: 10px; width: 100%;
      :deep(.el-radio) { margin-right: 0; width: 100%; height: 45px; border-radius: 0; border: 1px solid #eee; &.is-checked { border-color: #000; .el-radio__label { color: #000; } } }
      :deep(.el-radio__input.is-checked .el-checkbox__inner) { background-color: #000; border-color: #000; }
    }
    .submit-wrap { margin-top: auto; .confirm-btn { width: 100%; height: 50px; background: #000; color: #fff; border: none; border-radius: 0; font-weight: 900; letter-spacing: 2px; &:hover { background: #333; } } }
  }
}

.pagination-wrapper { margin-top: 30px; display: flex; justify-content: center; }
</style>