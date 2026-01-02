<template>
  <div class="app-container">
    <!-- 1. 顶部标题与操作区 -->
    <div class="header-section">
      <div class="title-group">
        <h2 class="page-title">非遗展品管理</h2>
        <p class="page-subtitle">系统全量数据监控、审核及资产归档控制台</p>
      </div>
      <div class="header-actions">
        <el-button class="industrial-add-btn" icon="Plus" @click="handleAdd">新增展品</el-button>
        <el-button class="industrial-export-btn" icon="Download" @click="handleExport">导出</el-button>
      </div>
    </div>

    <!-- 2. 检索表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" class="industrial-search-form">
      <el-form-item label="名称" prop="itemName">
        <el-input v-model="queryParams.itemName" placeholder="搜索名称" clearable style="width: 200px"/>
      </el-form-item>
      <el-form-item label="类别" prop="categoryId">
        <el-cascader
            v-model="queryParams.categoryId"
            :options="categoryOptions"
            :props="{
              value: 'categoryId',
              label: 'categoryName',
              children: 'children',
              emitPath: false,
              checkStrictly: true  /* 搜索时允许只选大类 */
            }"
            placeholder="分类筛选"
            clearable
            style="width: 200px"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 100px">
          <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value"/>
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
        :data="heritage_manageList"
        @selection-change="handleSelectionChange"
        border
        class="industrial-table"
        style="width: 100%"
    >
      <el-table-column type="selection" width="50" align="center"/>
      <el-table-column type="index" label="序号" align="center" width="60"/>

      <el-table-column label="封面" align="center" width="90">
        <template #default="scope">
          <image-preview :src="scope.row.coverImage" :width="45" :height="45" class="table-cover"/>
        </template>
      </el-table-column>

      <el-table-column label="展品名称" align="center" prop="itemName" min-width="160" :show-overflow-tooltip="true">
        <template #default="scope">
          <span class="item-name-bold">{{ scope.row.itemName }}</span>
        </template>
      </el-table-column>

      <el-table-column label="所属项目" align="center" prop="categoryId" width="150">
        <template #default="scope">
          <el-tag size="small" effect="plain" class="industrial-tag">{{ categoryFormat(scope.row) }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status" size="small"/>
        </template>
      </el-table-column>

      <el-table-column label="数据统计 (览/赞/藏)" align="center" min-width="200">
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

      <el-table-column label="发布人" align="center" prop="createBy" width="110"/>

      <el-table-column label="创建日期" align="center" prop="createTime" width="120">
        <template #default="scope">
          <span class="time-text">{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" width="140" fixed="right">
        <template #default="scope">
          <div class="op-group">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
                       v-hasPermi="['heritage:heritage_manage:edit']">修改
            </el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)"
                       v-hasPermi="['heritage:heritage_manage:remove']">删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 4. 分页 -->
    <div class="pagination-wrapper">
      <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum"
                  v-model:limit="queryParams.pageSize" @pagination="getList"/>
    </div>

    <!-- 5. 对话框 -->
    <el-dialog :title="title" v-model="open" width="750px" top="8vh" append-to-body :close-on-click-modal="false">
      <div class="dialog-scroll-wrapper">
        <el-form ref="heritage_manageRef" :model="form" :rules="rules" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="16">
              <el-form-item label="展品名称" prop="itemName">
                <el-input v-model="form.itemName" placeholder="请输入名称"/>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="显示顺序" prop="sortOrder">
                <el-input-number v-model="form.sortOrder" controls-position="right" style="width: 100%"/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <!-- 核心修复：树形级联选择器 -->
              <el-form-item label="类别" prop="categoryId">
                <el-cascader
                    v-model="form.categoryId"
                    :options="categoryOptions"
                    :props="{
                      value: 'categoryId',
                      label: 'categoryName',
                      children: 'children',
                      emitPath: false,    /* 只要最后那个ID */
                      checkStrictly: true /* 允许选择任意一级，不仅仅是最后一级 */
                    }"
                    placeholder="分类筛选"
                    clearable
                    style="width: 200px"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="当前状态">
                <el-radio-group v-model="form.status">
                  <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="parseInt(dict.value)">
                    {{ dict.label }}
                  </el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="封面图片">
            <image-upload v-model="form.coverImage" :limit="1"/>
          </el-form-item>
          <el-form-item label="3D模型文件">
            <file-upload v-model="form.modelFile" :file-type="['glb', 'gltf']"/>
          </el-form-item>
          <el-form-item label="展品简介">
            <el-input v-model="form.description" type="textarea" :rows="3"/>
          </el-form-item>
          <el-form-item label="历史渊源">
            <editor v-model="form.history" :min-height="150"/>
          </el-form-item>
          <el-form-item label="制作工艺">
            <editor v-model="form.craft" :min-height="150"/>
          </el-form-item>

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

          <el-form-item label="备注">
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

<script setup name="Heritage_manage">
import {
  listHeritage_manage,
  getHeritage_manage,
  delHeritage_manage,
  addHeritage_manage,
  updateHeritage_manage
} from "@/api/heritage/heritage_manage"
import {listCategory} from "@/api/heritage/category"; // 注意：这里确保引用了正确的分类列表接口
import {Pointer, StarFilled, View} from "@element-plus/icons-vue";

const {proxy} = getCurrentInstance()
const {sys_normal_disable} = proxy.useDict('sys_normal_disable')

const heritage_manageList = ref([])
const categoryOptions = ref([]) // 这里现在存储树形数据
const flatCategories = ref([])  // 新增：存储扁平数据，用于表格 label 转换
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {pageNum: 1, pageSize: 10, itemName: null, categoryId: null, status: null},
  rules: {
    itemName: [{required: true, message: "展品名称不能为空", trigger: "blur"}],
    categoryId: [{required: true, message: "分类不能为空", trigger: "change"}],
  }
})

const {queryParams, form, rules} = toRefs(data)

/** 查询列表 */
function getList() {
  loading.value = true
  listHeritage_manage(queryParams.value).then(response => {
    heritage_manageList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 核心修复：加载完整的分类数据并构建树 */
function getCategoryTree() {
  // 必须传入大分页，否则子类可能因为分页原因拿不到
  listCategory({pageNum: 1, pageSize: 100}).then(response => {
    // 1. 存储扁平列表，用于表格 ID 转文字
    flatCategories.value = response.rows;

    // 2. 使用若依内置 handleTree 转换为嵌套结构
    // 确保你的分类表主键是 categoryId，父键是 parentId
    categoryOptions.value = proxy.handleTree(response.rows, "categoryId", "parentId");

    // 3. 调试：如果还是没箭头，请看控制台打印的数据结构是否正确
    // console.log('树形结构:', categoryOptions.value);
  });
}

/** 核心修复：转换表格中的分类名称 */
function categoryFormat(row) {
  if (!row.categoryId || flatCategories.value.length === 0) {
    return row.categoryId;
  }
  // 使用 String() 强制转换，防止后端返回 Long 类型与前端数字对比失败
  const category = flatCategories.value.find(item =>
      String(item.categoryId) === String(row.categoryId)
  );

  return category ? category.categoryName : row.categoryId;
}

function cancel() {
  open.value = false;
  reset();
}

function reset() {
  form.value = {
    itemId: null,
    itemName: null,
    categoryId: null,
    coverImage: null,
    modelFile: null,
    status: 0,
    sortOrder: 0
  };
  proxy.resetForm("heritage_manageRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.itemId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加非遗展品";
}

function handleUpdate(row) {
  reset();
  const _itemId = row.itemId || ids.value;
  getHeritage_manage(_itemId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改非遗展品";
  });
}

function submitForm() {
  proxy.$refs["heritage_manageRef"].validate(valid => {
    if (valid) {
      if (form.value.itemId != null) {
        updateHeritage_manage(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addHeritage_manage(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  const _itemIds = row.itemId || ids.value
  proxy.$modal.confirm('确定删除？').then(() => delHeritage_manage(_itemIds)).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  });
}

function handleExport() {
  proxy.download('heritage/heritage_manage/export', {...queryParams.value}, `heritage_${new Date().getTime()}.xlsx`)
}

onMounted(() => {
  getCategoryTree(); // 初始加载分类树
  getList();
});
</script>

<style scoped lang="scss">
/* 保持你的工业风 CSS */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-bottom: 2px solid #000;
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
}

.industrial-export-btn {
  background: #fff !important;
  color: #000 !important;
  border: 1.5px solid #000 !important;
  border-radius: 0;
  font-weight: 900;
  height: 42px;
  padding: 0 20px;
}

.industrial-search-form {
  background: #fcfcfc;
  padding: 15px;
  margin-bottom: 20px;
  border: 1px solid #eee;
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
    text-transform: uppercase;
  }

  :deep(.el-table__row) {
    height: 75px;
  }
}

.item-name-bold {
  font-weight: 700;
  color: #000;
  font-size: 14px;
  text-align: center;
}

.stat-group {
  display: flex;
  justify-content: space-around;
  align-items: center;
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
  color: #333;
  font-weight: 600;
}

.time-text {
  font-size: 12px;
  color: #999;
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