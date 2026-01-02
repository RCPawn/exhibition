<template>
  <div class="app-container">
    <!-- 1. 顶部工业风标题与操作区 -->
    <div class="header-section">
      <div class="title-group">
        <h2 class="page-title">非遗分类管理</h2>
        <p class="page-subtitle">定义和维护非遗物品的所属门类及其编码规范</p>
      </div>
      <div class="header-actions">
        <el-button class="industrial-add-btn" icon="Plus" @click="handleAdd" v-hasPermi="['heritage:category:add']">新增分类</el-button>
        <el-button class="industrial-export-btn" icon="Download" @click="handleExport" v-hasPermi="['heritage:category:export']">导出</el-button>
      </div>
    </div>

    <!-- 2. 检索表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" class="industrial-search-form">
      <el-form-item label="分类名称" prop="categoryName">
        <el-input v-model="queryParams.categoryName" placeholder="搜索名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px">
          <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 3. 数据表格：重构比例与分布 -->
    <el-table
        v-loading="loading"
        :data="categoryList"
        @selection-change="handleSelectionChange"
        border
        class="industrial-table"
        style="width: 100%"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column type="index" label="序号" align="center" width="70" />

      <!-- 分类名称：按要求居中显示 -->
      <el-table-column label="分类名称" align="center" prop="categoryName" min-width="180">
        <template #default="scope">
          <span class="category-name-bold">{{ scope.row.categoryName }}</span>
        </template>
      </el-table-column>

      <el-table-column label="分类编码" align="center" prop="categoryCode" min-width="150">
        <template #default="scope">
          <span class="code-text">{{ scope.row.categoryCode }}</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status" size="small" />
        </template>
      </el-table-column>

      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span class="time-text">{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>

      <!-- 操作：固定宽度，右侧锁定 -->
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template #default="scope">
          <div class="op-group">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['heritage:category:edit']">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['heritage:category:remove']">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 4. 分页居中 -->
    <div class="pagination-wrapper">
      <pagination
          v-show="total>0"
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
      />
    </div>

    <!-- 5. 对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" top="10vh" append-to-body>
      <el-form ref="categoryRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input v-model="form.categoryCode" placeholder="请输入分类编码" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" controls-position="right" :min="0" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="parseInt(dict.value)">{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Category">
import { listCategory, getCategory, delCategory, addCategory, updateCategory } from "@/api/heritage/category"

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict('sys_normal_disable')

const categoryList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const categoryRef = ref(null);

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, categoryName: null, status: null },
  rules: {
    categoryName: [{ required: true, message: "分类名称不能为空", trigger: "blur" }],
    status: [{ required: true, message: "状态不能为空", trigger: "change" }],
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listCategory(queryParams.value).then(response => {
    categoryList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() { open.value = false; reset(); }
function reset() {
  form.value = { categoryId: null, categoryName: null, categoryCode: null, sortOrder: 0, status: 0, remark: null };
  proxy.resetForm("categoryRef")
}
function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery(); }
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.categoryId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}
function handleAdd() { reset(); open.value = true; title.value = "添加非遗分类 / NEW"; }
function handleUpdate(row) {
  reset()
  const _categoryId = row.categoryId || ids.value
  getCategory(_categoryId).then(response => {
    form.value = response.data; open.value = true; title.value = "修改非遗分类 / EDIT";
  })
}
function submitForm() {
  if (!categoryRef.value) return;
  categoryRef.value.validate(valid => {
    if (valid) {
      if (form.value.categoryId != null) {
        updateCategory(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功"); open.value = false; getList();
        })
      } else {
        addCategory(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功"); open.value = false; getList();
        })
      }
    }
  })
}
function handleDelete(row) {
  const _categoryIds = row.categoryId || ids.value
  proxy.$modal.confirm('是否确认删除选中的分类数据？').then(function() {
    return delCategory(_categoryIds)
  }).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); })
}
function handleExport() {
  proxy.download('heritage/category/export', { ...queryParams.value }, `category_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped lang="scss">
/* --- 布局头风格 --- */
.header-section {
  display: flex; justify-content: space-between; align-items: flex-end;
  border-bottom: 2px solid #000; padding-bottom: 18px; margin-bottom: 25px;
  .page-title { font-size: 26px; font-weight: 900; letter-spacing: -1px; margin: 0; }
  .page-subtitle { font-size: 13px; color: #999; margin-top: 5px; }
}

/* --- 工业风按钮 --- */
.industrial-add-btn {
  background: #000 !important; color: #fff !important; border: none !important; border-radius: 0;
  font-weight: 900; height: 42px; padding: 0 20px;
}
.industrial-export-btn {
  background: #fff !important; color: #000 !important; border: 1.5px solid #000 !important; border-radius: 0;
  font-weight: 900; height: 42px; padding: 0 20px;
}

/* --- 搜索区域 --- */
.industrial-search-form {
  background: #fcfcfc; padding: 15px; margin-bottom: 20px; border: 1px solid #eee;
}

/* --- 表格样式重构 --- */
.industrial-table {
  border-radius: 0; border: 1px solid #ebeef5;
  :deep(.el-table__header) th {
    background-color: #fcfcfc !important; color: #000 !important; font-weight: 900 !important;
    font-size: 13px; height: 50px; text-transform: uppercase;
  }
  :deep(.el-table__row) { height: 70px; }
}

.category-name-bold { font-weight: 700; color: #000; font-size: 14px; }
.code-text { font-family: 'D-DIN', monospace; font-weight: 600; color: #666; }
.time-text { font-size: 12px; color: #999; }

.op-group { display: flex; justify-content: center; gap: 12px; }

/* 强制底部分页容器居中 */
.pagination-wrapper { display: flex; justify-content: center; margin-top: 30px; }
:deep(.pagination-container) { background: transparent !important; }
</style>