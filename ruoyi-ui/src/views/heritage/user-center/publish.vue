<template>
  <div class="app-container">
    <!-- 1. 顶部标题与操作区 -->
    <div class="header-section">
      <div class="title-group">
        <h2 class="page-title">我的发布</h2>
        <p class="page-subtitle">管理您上传的数字展品及其全生命周期状态</p>
      </div>
      <el-button class="industrial-add-btn" icon="Plus" @click="handleAdd">发布展品</el-button>
    </div>

    <!-- 2. 数据表格 -->
    <el-table
        v-loading="loading"
        :data="myPublishList"
        border
        class="industrial-table"
        style="width: 100%"
    >
      <el-table-column type="index" label="序号" align="center" width="65" />
      <el-table-column label="封面" align="center" width="110">
        <template #default="scope">
          <image-preview :src="scope.row.coverImage" :width="50" :height="50" class="table-cover"/>
        </template>
      </el-table-column>
      <el-table-column label="展品名称" align="center" prop="itemName" min-width="200" :show-overflow-tooltip="true">
        <template #default="scope">
          <span class="item-name-bold">{{ scope.row.itemName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="所属类别" align="center" prop="categoryId" min-width="120">
        <template #default="scope">
          <el-tag size="small" effect="plain" class="industrial-tag">{{ categoryFormat(scope.row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="当前状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status" size="small" />
        </template>
      </el-table-column>
      <el-table-column label="数据统计 (览/赞/藏)" align="center" min-width="220">
        <template #default="scope">
          <div class="stat-group">
            <div class="stat-item"><el-icon><View /></el-icon> {{ scope.row.viewCount || 0 }}</div>
            <div class="stat-item text-danger"><el-icon><Pointer /></el-icon> {{ scope.row.likeCount || 0 }}</div>
            <div class="stat-item text-warning"><el-icon><StarFilled /></el-icon> {{ scope.row.favoriteCount || 0 }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="发布日期" align="center" prop="createTime" min-width="130">
        <template #default="scope">
          <span class="time-text">{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" fixed="right">
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

    <!-- 3. 对话框：补全内容 + 固定滚动逻辑 -->
    <el-dialog
        :title="title"
        v-model="open"
        width="750px"
        top="8vh"
        append-to-body
        :close-on-click-modal="false"
        class="industrial-dialog"
    >
      <div class="dialog-scroll-wrapper">
        <el-form ref="publishFormRef" :model="form" :rules="rules" label-width="100px">
          <!-- 基本信息行 -->
          <el-row :gutter="20">
            <el-col :span="16">
              <el-form-item label="展品名称" prop="itemName">
                <el-input v-model="form.itemName" placeholder="请输入展品名称" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="显示顺序" prop="sortOrder">
                <el-input-number v-model="form.sortOrder" controls-position="right" :min="0" style="width: 100%"/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="所属分类" prop="categoryId">
                <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                  <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="当前状态">
                <el-radio-group v-model="form.status">
                  <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="parseInt(dict.value)">{{dict.label}}</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 资源上传 -->
          <el-form-item label="封面图片">
            <image-upload v-model="form.coverImage" :limit="1"/>
          </el-form-item>
          <el-form-item label="3D模型文件">
            <file-upload v-model="form.modelFile" :file-type="['glb', 'gltf']" :file-size="100" />
          </el-form-item>
          <el-form-item label="展品简介">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入简要介绍" />
          </el-form-item>

          <!-- 富文本详情区 -->
          <el-form-item label="历史渊源">
            <editor v-model="form.history" :min-height="150"/>
          </el-form-item>
          <el-form-item label="制作工艺">
            <editor v-model="form.craft" :min-height="150"/>
          </el-form-item>

          <!-- 统计数据（仅修改时可见） -->
          <template v-if="form.itemId">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="浏览数" prop="viewCount">
                  <el-input v-model="form.viewCount" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="点赞数" prop="likeCount">
                  <el-input v-model="form.likeCount" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="收藏数" prop="favoriteCount">
                  <el-input v-model="form.favoriteCount" disabled />
                </el-form-item>
              </el-col>
            </el-row>
          </template>

          <el-form-item label="备注说明" prop="remark">
            <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
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

<script setup>
import { ref, reactive, onMounted, getCurrentInstance, toRefs } from 'vue';
import { listHeritage_manage, getHeritage_manage, delHeritage_manage, addHeritage_manage, updateHeritage_manage } from "@/api/heritage/heritage_manage";
import { listCategory } from "@/api/heritage/category";
import useUserStore from "@/store/modules/user";
import {Pointer, StarFilled, View} from "@element-plus/icons-vue";

const { proxy } = getCurrentInstance();
const { sys_normal_disable } = proxy.useDict('sys_normal_disable');
const userStore = useUserStore();

const myPublishList = ref([]);
const categoryOptions = ref([]);
const open = ref(false);
const loading = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, itemName: null, createBy: userStore.name },
  rules: {
    itemName: [{ required: true, message: "名称不能为空", trigger: "blur" }],
    categoryId: [{ required: true, message: "分类不能为空", trigger: "change" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listHeritage_manage(queryParams.value).then(response => {
    myPublishList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function handleUpdate(row) {
  reset();
  getHeritage_manage(row.itemId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "编辑展品信息 / EDIT";
  });
}

function submitForm() {
  proxy.$refs["publishFormRef"].validate(valid => {
    if (valid) {
      if (form.value.itemId != null) {
        updateHeritage_manage(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addHeritage_manage(form.value).then(() => {
          proxy.$modal.msgSuccess("发布成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  proxy.$modal.confirm(`确定删除 "${row.itemName}" 吗？`).then(() => {
    return delHeritage_manage(row.itemId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  });
}

function getCategoryList() {
  listCategory({ pageNum: 1, pageSize: 100 }).then(response => {
    categoryOptions.value = response.rows;
  });
}

function categoryFormat(row) {
  const category = categoryOptions.value.find(item => item.categoryId === row.categoryId);
  return category ? category.categoryName : row.categoryId;
}

function handleAdd() { reset(); open.value = true; title.value = "发布新展品 / NEW"; }
function cancel() { open.value = false; reset(); }
function reset() {
  form.value = { itemId: null, itemName: null, categoryId: null, coverImage: null, modelFile: null, description: null, history: null, craft: null, status: 0, sortOrder: 0, remark: null };
  if(proxy.$refs.publishFormRef) proxy.$refs.publishFormRef.resetFields();
}

onMounted(() => { getCategoryList(); getList(); });
</script>

<style scoped lang="scss">
.header-section {
  display: flex; justify-content: space-between; align-items: flex-end;
  border-bottom: 2px solid #000; padding-bottom: 18px; margin-bottom: 25px;
  .page-title { font-size: 26px; font-weight: 900; letter-spacing: -1px; margin: 0; }
  .page-subtitle { font-size: 13px; color: #999; margin-top: 5px; }
}

.industrial-add-btn {
  background: #000 !important; color: #fff !important; border: none !important; border-radius: 0;
  font-weight: 900; height: 42px; padding: 0 20px;
  &:hover { background: #333 !important; }
}

.industrial-table {
  border-radius: 0; border: 1px solid #ebeef5;
  :deep(.el-table__header) th {
    background-color: #fcfcfc !important; color: #000 !important; font-weight: 900 !important;
    font-size: 13px; height: 50px;
  }
  :deep(.el-table__row) { height: 70px; }
}

.item-name-bold { font-weight: 700; color: #000; font-size: 14px; text-align: center}
.stat-group { display: flex; justify-content: space-around; align-items: center; padding: 0 10px; }
.stat-item {
  display: inline-flex; align-items: center; gap: 5px; font-family: 'D-DIN', sans-serif;
  font-weight: 600; font-size: 14px; color: #666;
}
.text-danger { color: #F56C6C; }
.text-warning { color: #E6A23C; }
.op-group { display: flex; justify-content: center; gap: 12px; }
.industrial-tag { border-radius: 0; border: 1px solid #ddd; background: #fff; color: #333; font-weight: 600; }
.pagination-wrapper { margin-top: 40px; display: flex; justify-content: center; }
:deep(.pagination-container) { background: transparent !important; }

/* 对话框核心滚动样式 */
.dialog-scroll-wrapper {
  max-height: 65vh;
  overflow-y: auto;
  padding: 0 20px;
  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: #eee; border-radius: 4px; }
}
:deep(.el-dialog__body) {
  padding: 20px 0 10px 0 !important;
}
</style>