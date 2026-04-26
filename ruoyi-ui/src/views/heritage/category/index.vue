<template>
  <div class="app-container heritage-admin-page">
    <!-- 1. 顶部工业风标题与操作区 -->
    <div class="header-section">
      <div class="title-group">
        <h2 class="page-title">非遗分类管理</h2>
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

    <!-- 3. 数据表格 -->
    <el-table
        v-loading="loading"
        :data="categoryList"
        @selection-change="handleSelectionChange"
        border
        stripe
        size="small"
        class="industrial-table"
        style="width: 100%"
    >
      <el-table-column type="selection" width="44" align="center" />
      <el-table-column type="index" label="序号" align="center" width="52" />

      <el-table-column label="分类名称" align="left" prop="categoryName" min-width="120" :show-overflow-tooltip="true">
        <template #default="scope">
          <span class="category-name-bold">{{ scope.row.categoryName }}</span>
        </template>
      </el-table-column>

      <el-table-column label="分类编码" align="center" prop="categoryCode" min-width="120">
        <template #default="scope">
          <span class="code-text">{{ scope.row.categoryCode }}</span>
        </template>
      </el-table-column>

      <!-- 上级分类：修复显示逻辑 -->
      <el-table-column label="上级分类" align="center" min-width="140">
        <template #default="scope">
          <!-- 顶级分类显示绿色 -->
          <el-tag
              v-if="scope.row.parentId === 0"
              type="success"
              effect="plain"
              class="parent-tag"
          >
            顶级分类
          </el-tag>

          <!-- 子分类显示蓝色，这里会去 allCategoryList 里查找名称 -->
          <el-tag
              v-else
              type="primary"
              effect="plain"
              class="parent-tag"
          >
            {{ formatParentName(scope.row.parentId) }}
          </el-tag>
        </template>
      </el-table-column>

<!--      <el-table-column label="显示顺序" align="center" prop="sortOrder" width="100" />-->

      <el-table-column label="状态" align="center" prop="status" width="88">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status" size="small" />
        </template>
      </el-table-column>

      <el-table-column label="创建时间" align="center" prop="createTime" width="158">
        <template #default="scope">
          <span class="time-text">{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" width="156" fixed="right" class-name="heritage-op-col">
        <template #default="scope">
          <div class="op-group">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['heritage:category:edit']">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['heritage:category:remove']">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 4. 分页 -->
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

        <!-- 上级分类：修复下拉选项 -->
        <el-form-item label="上级分类" prop="parentId">
          <el-select v-model="form.parentId" placeholder="请选择上级分类" style="width: 100%">
            <!-- 这里的 options 来自于全量数据，不再受分页影响 -->
            <el-option
                v-for="item in parentOptions"
                :key="item.categoryId"
                :label="item.categoryName"
                :value="item.categoryId"
            />
          </el-select>
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

const categoryList = ref([]) // 表格用的（分页数据）
const allCategoryList = ref([]) // 【新增】专门用于字典匹配和下拉菜单的（全量数据）

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

/** 核心修复 1：加载全量分类数据（用于显示父级名称和下拉菜单） */
function getAllCategories() {
  // 设置一个足够大的 pageSize 以获取所有分类，或者如果不传分页参数后端返回全部则不传
  // 这里假设后端通过 pageNum: 1, pageSize: 1000 来获取全部
  listCategory({ pageNum: 1, pageSize: 1000 }).then(response => {
    allCategoryList.value = response.rows
  })
}

/** 获取表格数据（分页） */
function getList() {
  loading.value = true
  listCategory(queryParams.value).then(response => {
    categoryList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 核心修复 2：上级分类名称格式化 */
function formatParentName(parentId) {
  if (!parentId || parentId === 0) return '顶级分类';

  // 修改：不再从 categoryList (当前页) 找，而是从 allCategoryList (全量) 找
  const parent = allCategoryList.value.find(item => item.categoryId === parentId);
  return parent ? parent.categoryName : `未知父级(ID:${parentId})`;
}

/** 核心修复 3：下拉菜单选项计算 */
const parentOptions = computed(() => {
  const defaultOption = [{ categoryId: 0, categoryName: '无上级分类 (顶级)' }];

  // 修改：基于 allCategoryList 过滤，确保无论在第几页，下拉菜单都是完整的
  if (allCategoryList.value && allCategoryList.value.length > 0) {
    const roots = allCategoryList.value.filter(item =>
        // 1. 必须是顶级分类 (parentId === 0)
        item.parentId === 0 &&
        // 2. 排除掉自己 (修改时不能选自己当爹)
        item.categoryId !== form.value.categoryId
    );
    return [...defaultOption, ...roots];
  }
  return defaultOption;
});

function cancel() { open.value = false; reset(); }

function reset() {
  form.value = { categoryId: null, parentId: 0, categoryName: null, categoryCode: null, sortOrder: 0, status: 0, remark: null };
  proxy.resetForm("categoryRef")
}

function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery(); }

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.categoryId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加非遗分类 / NEW";
  // 打开弹窗时，刷新一下下拉菜单数据，防止有新数据
  getAllCategories();
}

function handleUpdate(row) {
  reset()
  const _categoryId = row.categoryId || ids.value
  getCategory(_categoryId).then(response => {
    form.value = response.data;
    if (form.value.parentId == null) {
      form.value.parentId = 0;
    }
    open.value = true;
    title.value = "修改非遗分类 / EDIT";
    // 打开弹窗时，刷新下拉菜单数据
    getAllCategories();
  })
}

function submitForm() {
  if (!categoryRef.value) return;
  categoryRef.value.validate(valid => {
    if (valid) {
      if (form.value.categoryId != null) {
        updateCategory(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList(); // 刷新表格
          getAllCategories(); // 刷新字典缓存
        })
      } else {
        addCategory(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList(); // 刷新表格
          getAllCategories(); // 刷新字典缓存
        })
      }
    }
  })
}

function handleDelete(row) {
  const _categoryIds = row.categoryId || ids.value
  proxy.$modal.confirm('是否确认删除选中的分类数据？').then(function() {
    return delCategory(_categoryIds)
  }).then(() => {
    getList();
    getAllCategories(); // 删除后也要更新缓存
    proxy.$modal.msgSuccess("删除成功");
  })
}

function handleExport() {
  proxy.download('heritage/category/export', { ...queryParams.value }, `category_${new Date().getTime()}.xlsx`)
}

// 初始化
getList();
getAllCategories(); // 【关键】初始化时获取一次全量数据
</script>

<style scoped lang="scss">
.category-name-bold { font-weight: 600; color: #303133; font-size: 13px; }
.code-text { font-family: ui-monospace, monospace; font-weight: 600; color: #606266; letter-spacing: 0.3px; }
.time-text { font-size: 12px; color: #909399; }

.parent-tag {
  font-weight: 500;
  border-radius: 4px;
  padding: 0 6px;
}

</style>