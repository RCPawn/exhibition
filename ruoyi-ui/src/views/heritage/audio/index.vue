<template>
  <div class="app-container heritage-admin-page">
    <!-- 1. 顶部操作区：工业风标题 -->
    <div class="header-section">
      <div class="title-group">
        <h2 class="page-title">音频管理</h2>
      </div>
      <div class="header-actions">
        <el-button class="industrial-add-btn" icon="Plus" @click="handleAdd">录入新音轨</el-button>
      </div>
    </div>

    <!-- 2. 检索表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" class="industrial-search-form">
      <el-form-item label="名称" prop="title">
        <el-input v-model="queryParams.title" placeholder="音频标题" clearable @keyup.enter="handleQuery" style="width: 200px" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="审核状态" clearable style="width: 120px">
          <el-option v-for="dict in heritage_audit_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 3. 数据表格 -->
    <el-table
        v-loading="loading"
        :data="audioList"
        @selection-change="handleSelectionChange"
        border
        stripe
        size="small"
        class="industrial-table"
    >
      <el-table-column type="selection" width="44" align="center"/>
      <el-table-column type="index" label="序号" align="center" width="52" />

      <el-table-column label="音频标题" align="left" prop="title" min-width="200">
        <template #default="scope">
          <span class="item-name-bold">{{ scope.row.title }}</span>
        </template>
      </el-table-column>

      <!-- 核心修复：显示关联展品名称，不再显示 ID -->
      <el-table-column label="关联资产" align="center" prop="itemId" width="180">
        <template #default="scope">
          <el-tag size="small" effect="plain" class="industrial-tag">
            {{ itemFormat(scope.row.itemId) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="heritage_audit_status" :value="scope.row.status" size="small" />
        </template>
      </el-table-column>

      <el-table-column label="发布人" align="center" prop="createBy" width="110" />

      <el-table-column label="提交日期" align="center" prop="createTime" width="120">
        <template #default="scope">
          <span class="time-text">{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" width="156" fixed="right" class-name="heritage-op-col">
        <template #default="scope">
          <div class="op-group">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 4. 对话框：补全关联展品下拉框 + 滚动容器 -->
    <el-dialog :title="title" v-model="open" width="600px" top="8vh" append-to-body :close-on-click-modal="false">
      <div class="dialog-scroll-wrapper">
        <el-form ref="audioFormRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="音频标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入音频标题" />
          </el-form-item>

          <!-- 核心新增：关联展品多选框 -->
          <el-form-item label="关联展品" prop="itemId">
            <el-select v-model="form.itemId" placeholder="请选择关联的 3D 模型资产" style="width: 100%" filterable>
              <el-option
                  v-for="item in itemOptions"
                  :key="item.itemId"
                  :label="item.itemName"
                  :value="item.itemId"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="音频文件" prop="audioUrl">
            <file-upload v-model="form.audioUrl" :file-type="['mp3', 'wav', 'ogg']" :file-size="20" />
          </el-form-item>

          <el-form-item label="内容/唱词" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入内容解析或唱词" />
          </el-form-item>

          <el-form-item label="当前状态">
            <el-radio-group v-model="form.status">
              <el-radio v-for="dict in heritage_audit_status" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Audio">
import { ref, reactive, onMounted, getCurrentInstance, toRefs } from 'vue';
import { listAudio, getAudio, delAudio, addAudio, updateAudio } from "@/api/heritage/audio";
import { listHeritage_manage } from "@/api/heritage/heritage_manage";

const { proxy } = getCurrentInstance();
const { heritage_audit_status, sys_normal_disable } = proxy.useDict('heritage_audit_status', 'sys_normal_disable');

const audioList = ref([]);
const itemOptions = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, title: null, status: null },
  rules: {
    title: [{ required: true, message: "音频标题不能为空", trigger: "blur" }],
    audioUrl: [{ required: true, message: "请上传音频文件", trigger: "change" }],
    itemId: [{ required: true, message: "请选择关联展品", trigger: "change" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询音频列表 */
function getList() {
  loading.value = true;
  listAudio(queryParams.value).then(response => {
    audioList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 查询所有展品用于下拉 */
function getItemList() {
  listHeritage_manage({ pageNum: 1, pageSize: 200 }).then(res => {
    itemOptions.value = res.rows;
  });
}

/** 翻译展品名称 */
function itemFormat(itemId) {
  if (!itemId) return "未关联";
  const item = itemOptions.value.find(i => String(i.itemId) === String(itemId));
  return item ? item.itemName : itemId;
}

function handleUpdate(row) {
  reset();
  const _audioId = row.audioId || ids.value;
  getAudio(_audioId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改音频档案";
  });
}

function submitForm() {
  proxy.$refs["audioFormRef"].validate(valid => {
    if (valid) {
      const action = form.value.audioId ? updateAudio : addAudio;
      action(form.value).then(() => {
        proxy.$modal.msgSuccess("操作成功");
        open.value = false;
        getList();
      });
    }
  });
}

function handleDelete(row) {
  const _ids = row.audioId || ids.value;
  proxy.$modal.confirm('是否确认删除数据项？').then(() => delAudio(_ids)).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  });
}

function handleAdd() { reset(); open.value = true; title.value = "录入新音轨"; }
function cancel() { open.value = false; reset(); }
function reset() {
  form.value = { audioId: null, title: null, audioUrl: null, itemId: null, status: "0" };
  if (proxy.$refs.audioFormRef) proxy.$refs.audioFormRef.resetFields();
}

function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery(); }
function handleSelectionChange(selection) {
  ids.value = selection.map(i => i.audioId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

onMounted(() => {
  getItemList(); // 必须先加载展品列表，否则表格会显示ID
  getList();
});
</script>

<style scoped lang="scss">
.item-name-bold { font-weight: 600; color: #303133; font-size: 13px; }
.industrial-tag { border-radius: 4px; border: 1px solid #ddd; color: #333; font-weight: 500; }
.pagination-wrapper { margin-top: 12px; }
.dialog-scroll-wrapper {
  max-height: 60vh; overflow-y: auto; padding: 0 20px;
  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: #eee; border-radius: 4px; }
}
:deep(.el-dialog__body) { padding: 20px 0 10px 0 !important; }
</style>