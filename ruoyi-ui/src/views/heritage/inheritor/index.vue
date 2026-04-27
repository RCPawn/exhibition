<template>
  <div class="app-container heritage-admin-page">
    <!-- 1. 顶部标题与操作区 -->
    <div class="header-section">
      <div class="title-group">
        <h2 class="page-title">传承人管理</h2>
      </div>
      <div class="header-actions">
        <el-button class="industrial-add-btn" icon="Plus" @click="handleAdd" v-hasPermi="['heritage:inheritor:add']">新增传承人</el-button>
        <el-button class="industrial-export-btn" icon="Download" @click="handleExport" v-hasPermi="['heritage:inheritor:export']">导出</el-button>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" class="header-inline-tools" />
      </div>
    </div>

    <!-- 2. 检索表单：引入级联搜索 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" class="industrial-search-form">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="queryParams.name" placeholder="输入姓名" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>

      <!-- 搜索栏级联选择 -->
      <el-form-item label="所属项目" prop="categoryId">
        <el-cascader
            v-model="queryParams.categoryId"
            :options="categoryOptions"
            :props="{
            value: 'categoryId',
            label: 'categoryName',
            children: 'children',
            emitPath: false,
            checkStrictly: true
          }"
            placeholder="选择项目大类/小类"
            clearable
            style="width: 220px"
        />
      </el-form-item>

      <el-form-item label="级别" prop="level">
        <el-select v-model="queryParams.level" placeholder="称号级别" clearable style="width: 130px">
          <el-option v-for="dict in heritage_level" :key="dict.value" :label="dict.label" :value="dict.value" />
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
        :data="inheritorList"
        @selection-change="handleSelectionChange"
        border
        stripe
        size="small"
        class="industrial-table"
        style="width: 100%"
    >
      <el-table-column type="selection" width="44" align="center" />
      <el-table-column type="index" label="序号" align="center" width="56" />

      <el-table-column label="肖像" align="center" width="72">
        <template #default="scope">
          <image-preview :src="scope.row.avatar" :width="40" :height="40" class="table-avatar"/>
        </template>
      </el-table-column>

      <el-table-column label="姓名" align="left" prop="name" min-width="96" :show-overflow-tooltip="true">
        <template #default="scope">
          <span class="item-name-bold">{{ scope.row.name }}</span>
        </template>
      </el-table-column>

      <el-table-column label="称号级别" align="center" prop="level" min-width="108">
        <template #default="scope">
          <dict-tag :options="heritage_level" :value="scope.row.level" />
        </template>
      </el-table-column>

      <!-- 核心修复：显示分类名称 -->
      <el-table-column label="所属非遗项目" align="left" prop="categoryId" min-width="140" :show-overflow-tooltip="true">
        <template #default="scope">
          <el-tag size="small" effect="plain" class="industrial-tag table-tag-ellipsis">
            {{ categoryFormat(scope.row.categoryId) }}
          </el-tag>
        </template>
      </el-table-column>

<!--      <el-table-column label="状态" align="center" prop="status" width="250">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status" size="small" />
        </template>
      </el-table-column>-->

      <el-table-column label="创建日期" align="center" prop="createTime" width="110">
        <template #default="scope">
          <span class="time-text">{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" width="156" fixed="right" class-name="heritage-op-col">
        <template #default="scope">
          <div class="op-group">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['heritage:inheritor:edit']">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['heritage:inheritor:remove']">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <pagination
          v-show="total>0"
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
      />
    </div>

    <!-- 5. 对话框：级联选择器 + 内部滚动 -->
    <el-dialog
        :title="title"
        v-model="open"
        width="720px"
        top="8vh"
        append-to-body
        :close-on-click-modal="false"
    >
      <div class="dialog-scroll-wrapper">
        <el-form ref="inheritorRef" :model="form" :rules="rules" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="姓名" prop="name">
                <el-input v-model="form.name" placeholder="请输入姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="称号级别" prop="level">
                <el-select v-model="form.level" placeholder="请选择级别" style="width: 100%">
                  <el-option v-for="dict in heritage_level" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <!-- 表单级联选择 -->
              <el-form-item label="所属项目" prop="categoryId">
                <el-cascader
                    v-model="form.categoryId"
                    :options="categoryOptions"
                    :props="{
                    value: 'categoryId',
                    label: 'categoryName',
                    children: 'children',
                    emitPath: false
                  }"
                    placeholder="选择具体非遗小类"
                    filterable
                    style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="当前状态">
                <el-radio-group v-model="form.status">
                  <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">{{dict.label}}</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="个人肖像">
            <image-upload v-model="form.avatar" :limit="1" />
          </el-form-item>

          <el-form-item label="个人简介">
            <editor v-model="form.introduction" :min-height="180"/>
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

<script setup name="Inheritor">
import { listInheritor, getInheritor, delInheritor, addInheritor, updateInheritor } from "@/api/heritage/inheritor"
import { listCategory, treeselect } from "@/api/heritage/category";

const { proxy } = getCurrentInstance()
const { heritage_level, sys_normal_disable } = proxy.useDict('heritage_level', 'sys_normal_disable')

const inheritorList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const categoryOptions = ref([]); // 树形结构数据
const flatCategories = ref([]);  // 扁平化数据（用于翻译名称）

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, name: null, level: null, categoryId: null, status: null },
  rules: {
    name: [{ required: true, message: "姓名不能为空", trigger: "blur" }],
    level: [{ required: true, message: "级别不能为空", trigger: "change" }],
    categoryId: [{ required: true, message: "所属项目不能为空", trigger: "change" }],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 获取列表 */
function getList() {
  loading.value = true
  listInheritor(queryParams.value).then(response => {
    inheritorList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 获取分类数据：同时获取树形和扁平数据 */
const getCategoryTree = async () => {
  // 1. 获取扁平数据，用于表格 label 转换 (确保获取所有数据，防止分页导致显示ID)
  listCategory({ pageNum: 1, pageSize: 200 }).then(res => {
    flatCategories.value = res.rows;
  });
  // 2. 获取树形数据，用于修改框的级联选择器（后端构建，无需前端 handleTree）
  treeselect().then(res => {
    categoryOptions.value = res.data;
  });
};

/** 核心改进：翻译表格中的 ID 为名称 */
function categoryFormat(categoryId) {
  if (!categoryId) return "";
  const category = flatCategories.value.find(item => String(item.categoryId) === String(categoryId));
  return category ? category.categoryName : categoryId;
}

function cancel() { open.value = false; reset(); }
function reset() {
  form.value = { inheritorId: null, name: null, avatar: null, level: null, categoryId: null, introduction: null, status: "0" };
  proxy.resetForm("inheritorRef")
}

function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery(); }
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.inheritorId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

function handleAdd() { reset(); open.value = true; title.value = "添加传承人档案"; }

function handleUpdate(row) {
  reset()
  const _inheritorId = row.inheritorId || ids.value
  getInheritor(_inheritorId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改传承人档案";
  })
}

function submitForm() {
  proxy.$refs["inheritorRef"].validate(valid => {
    if (valid) {
      if (form.value.inheritorId != null) {
        updateInheritor(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功"); open.value = false; getList();
        })
      } else {
        addInheritor(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功"); open.value = false; getList();
        })
      }
    }
  })
}

function handleDelete(row) {
  const _inheritorIds = row.inheritorId || ids.value
  proxy.$modal.confirm('是否确认删除传承人档案？').then(function() {
    return delInheritor(_inheritorIds)
  }).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); })
}

function handleExport() {
  proxy.download('heritage/inheritor/export', { ...queryParams.value }, `inheritor_${new Date().getTime()}.xlsx`)
}

onMounted(() => {
  getCategoryTree();
  getList();
});
</script>

<style scoped lang="scss">
/* 表内细节（列表骨架见 assets/styles/heritage-admin-list.scss） */
.item-name-bold { font-weight: 600; color: #303133; font-size: 13px; }
.table-avatar { border: 1px solid #eee; border-radius: 4px; vertical-align: middle; }
.industrial-tag { border-radius: 4px; border: 1px solid #ddd; color: #333; font-weight: 500; max-width: 100%; }
.table-tag-ellipsis { display: inline-block; max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; vertical-align: middle; }
.time-text { font-size: 12px; color: #909399; }

/* 核心修复：对话框内部滚动容器 */
.dialog-scroll-wrapper {
  max-height: 60vh;
  overflow-y: auto;
  padding: 0 20px;
  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: #eee; border-radius: 4px; }
}

:deep(.el-dialog__body) {
  padding: 20px 0 10px 0 !important;
}
</style>