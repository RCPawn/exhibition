<template>
  <div class="app-container">
    <!-- 1. 顶部标题与操作区 -->
    <div class="header-section">
      <div class="title-group">
        <h2 class="page-title">我的发布</h2>
        <p class="page-subtitle">管理您上传的数字展品及其全生命周期状态</p>
      </div>
      <el-button class="industrial-add-btn" icon="Plus" @click="handleAdd">发布新展品</el-button>
    </div>

    <!-- 2. 数据表格 -->
    <el-table
        v-loading="loading"
        :data="myPublishList"
        border
        class="industrial-table"
        style="width: 100%"
    >
      <el-table-column type="index" label="序号" align="center" width="65"/>
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

      <!-- 类别列修复 -->
      <el-table-column label="所属类别" align="center" prop="categoryId" min-width="120">
        <template #default="scope">
          <el-tag size="small" effect="plain" class="industrial-tag">{{ categoryFormat(scope.row.categoryId) }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="当前状态" align="center" prop="status">
        <template #default="scope">
          <!-- 如果是被驳回状态(2)，增加气泡显示原因 -->
          <el-tooltip
              v-if="scope.row.status === 2"
              :content="'驳回原因：' + (scope.row.rejectReason || '内容不合规')"
              placement="top"
          >
            <dict-tag :options="heritage_audit_status" :value="scope.row.status" style="cursor: help"/>
          </el-tooltip>

          <dict-tag v-else :options="heritage_audit_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="数据统计 (览/赞/藏)" align="center" min-width="220">
        <template #default="scope">
          <div class="stat-group">
            <div class="stat-item">
              <el-icon>
                <View/>
              </el-icon>
              {{ scope.row.viewCount || 0 }}
            </div>
            <div class="stat-item text-danger">
              <el-icon>
                <Pointer/>
              </el-icon>
              {{ scope.row.likeCount || 0 }}
            </div>
            <div class="stat-item text-warning">
              <el-icon>
                <StarFilled/>
              </el-icon>
              {{ scope.row.favoriteCount || 0 }}
            </div>
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
      <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum"
                  v-model:limit="queryParams.pageSize" @pagination="getList"/>
    </div>

    <!-- 3. 对话框：级联选择器修复 -->
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
          <el-row :gutter="20">
            <el-col :span="16">
              <el-form-item label="展品名称" prop="itemName">
                <el-input v-model="form.itemName" placeholder="请输入展品名称"/>
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
              <el-form-item label="所属项目" prop="categoryId">
                <!-- 级联选择器：确保绑定的是 form.categoryId -->
                <el-cascader
                    v-model="form.categoryId"
                    :options="categoryOptions"
                    :props="{
                    value: 'categoryId',
                    label: 'categoryName',
                    children: 'children',
                    emitPath: false
                  }"
                    placeholder="选择项目大类/小类"
                    clearable
                    filterable
                    style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <!--            <el-col :span="12">
                          <el-form-item label="当前状态">
                            <el-radio-group v-model="form.status">
                              <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="parseInt(dict.value)">{{dict.label}}</el-radio>
                            </el-radio-group>
                          </el-form-item>
                        </el-col>-->
          </el-row>

          <el-form-item label="封面图片">
            <image-upload v-model="form.coverImage" :limit="1"/>
          </el-form-item>
          <el-form-item label="3D模型文件">
            <file-upload v-model="form.modelFile" :file-type="['glb', 'gltf']" :file-size="100"/>
          </el-form-item>
          <el-form-item label="展品简介">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入简要介绍"/>
          </el-form-item>

          <el-form-item label="历史渊源">
            <editor v-model="form.history" :min-height="150"/>
          </el-form-item>
          <el-form-item label="制作工艺">
            <editor v-model="form.craft" :min-height="150"/>
          </el-form-item>

          <template v-if="form.itemId">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="浏览数">
                  <el-input v-model="form.viewCount" disabled/>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="点赞数">
                  <el-input v-model="form.likeCount" disabled/>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="收藏数">
                  <el-input v-model="form.favoriteCount" disabled/>
                </el-form-item>
              </el-col>
            </el-row>
          </template>

          <el-form-item label="备注说明" prop="remark">
            <el-input v-model="form.remark" type="textarea" placeholder="请输入备注"/>
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
import {ref, reactive, onMounted, getCurrentInstance, toRefs} from 'vue';
import {
  listMyPublish, getMyPublish, addMyPublish, updateMyPublish, delMyPublish
} from "@/api/heritage/heritage_manage";
import {listCategory, treeselect} from "@/api/heritage/category";
import useUserStore from "@/store/modules/user";
import {Pointer, StarFilled, View} from "@element-plus/icons-vue";

const {proxy} = getCurrentInstance();
const {heritage_audit_status} = proxy.useDict('heritage_audit_status');
const userStore = useUserStore();

const myPublishList = ref([]);
const categoryOptions = ref([]); // 树形数据
const flatCategories = ref([]);  // 扁平化数据（用于表格名称转换）
const open = ref(false);
const loading = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {pageNum: 1, pageSize: 10, itemName: null},
  rules: {
    itemName: [{required: true, message: "名称不能为空", trigger: "blur"}],
    categoryId: [{required: true, message: "分类不能为空", trigger: "change"}]
  }
});

const {queryParams, form, rules} = toRefs(data);

function getList() {
  loading.value = true;
  listMyPublish(queryParams.value).then(response => {
    myPublishList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 获取分类：同时获取树形和扁平数据 */
function getCategoryData() {
  // 1. 获取扁平数据，用于表格 label 转换 (确保获取所有数据，防止分页导致显示ID)
  listCategory({pageNum: 1, pageSize: 200}).then(res => {
    flatCategories.value = res.rows;
  });
  // 2. 获取树形数据，用于修改框的级联选择器
  treeselect().then(res => {
    categoryOptions.value = res.data;
  });
}

/** 格式化分类：从扁平列表中查找 */
function categoryFormat(categoryId) {
  if (!categoryId || flatCategories.value.length === 0) return categoryId;
  const category = flatCategories.value.find(item => String(item.categoryId) === String(categoryId));
  return category ? category.categoryName : categoryId;
}

function handleUpdate(row) {
  reset();
  getMyPublish(row.itemId).then(response => {
    form.value = response.data;
    // 强制转换 ID 为 Number 类型，防止回显失败
    if (form.value.categoryId) form.value.categoryId = Number(form.value.categoryId);
    open.value = true;
    title.value = "编辑展品信息 / EDIT";
  });
}

function submitForm() {
  proxy.$refs["publishFormRef"].validate(valid => {
    if (valid) {
      const action = form.value.itemId ? updateMyPublish : addMyPublish;
      action(form.value).then(() => {
        proxy.$modal.msgSuccess(form.value.itemId ? "修改成功" : "发布成功");
        open.value = false;
        getList();
      });
    }
  });
}

function handleDelete(row) {
  proxy.$modal.confirm(`确定删除 "${row.itemName}" 吗？`).then(() => {
    return delMyPublish(row.itemId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  });
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "发布新展品 / NEW";
}

function cancel() {
  open.value = false;
}

function reset() {
  form.value = {itemId: null, itemName: null, categoryId: null, coverImage: null, modelFile: null, status: 0};
  if (proxy.$refs.publishFormRef) proxy.$refs.publishFormRef.resetFields();
}

onMounted(() => {
  getCategoryData();
  getList();
});
</script>

<style scoped lang="scss">
/* 样式部分保持原样，增加文本左对齐优化 */
.item-name-bold {
  font-weight: 700;
  color: #000;
  font-size: 14px;
  display: block;
  text-align: center;
  padding-left: 10px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-bottom: 18px;
  margin-bottom: 25px;

  .page-title {
    font-size: 26px;
    font-weight: 900;
    letter-spacing: -1px;
    margin: 0;
  }

  .page-subtitle {
    font-size: 13px;
    color: #999;
    margin-top: 5px;
  }
}

.industrial-add-btn {
  background: #000 !important;
  color: #fff !important;
  border: none !important;
  border-radius: 0;
  font-weight: 900;
  height: 42px;
  padding: 0 20px;

  &:hover {
    background: #333 !important;
  }
}

.industrial-table {
  border-radius: 0;
  border: 1px solid #ebeef5;

  :deep(.el-table__header) th {
    background-color: #fcfcfc !important;
    color: #000 !important;
    font-weight: 900 !important;
    font-size: 13px;
    height: 50px;
  }

  :deep(.el-table__row) {
    height: 70px;
  }
}

.stat-group {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 0 10px;
}

.stat-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-family: 'D-DIN', sans-serif;
  font-weight: 600;
  font-size: 14px;
  color: #666;
}

.text-danger {
  color: #F56C6C;
}

.text-warning {
  color: #E6A23C;
}

.op-group {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.industrial-tag {
  border-radius: 0;
  border: 1px solid #ddd;
  background: #fff;
  color: #333;
  font-weight: 600;
}

.pagination-wrapper {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

:deep(.pagination-container) {
  background: transparent !important;
}

.dialog-scroll-wrapper {
  max-height: 65vh;
  overflow-y: auto;
  padding: 0 20px;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: #eee;
    border-radius: 4px;
  }
}

:deep(.el-dialog__body) {
  padding: 20px 0 10px 0 !important;
}
</style>