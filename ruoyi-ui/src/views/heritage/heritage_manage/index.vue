<template>
  <div class="app-container heritage-admin-page">
    <div class="heritage-toolbar-row">
      <h2 class="page-title heritage-toolbar-row__title">非遗展品管理</h2>
      <el-form
        :model="queryParams"
        ref="queryRef"
        :inline="true"
        class="heritage-toolbar-row__filters industrial-toolbar-form--no-label"
        label-width="0"
      >
        <el-form-item prop="itemName">
          <el-input v-model="queryParams.itemName" placeholder="名称" clearable @keyup.enter="handleQuery" style="width: 176px" />
        </el-form-item>
        <el-form-item prop="categoryId">
          <el-cascader
            v-model="queryParams.categoryId"
            :options="categoryOptions"
            :props="{ value: 'categoryId', label: 'categoryName', children: 'children', emitPath: false, checkStrictly: true }"
            placeholder="类别"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item prop="status">
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 112px">
            <el-option v-for="dict in heritage_audit_status" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="heritage-toolbar-row__actions">
        <el-button class="industrial-add-btn" icon="Plus" @click="handleAdd">新增展品</el-button>
        <el-button class="industrial-export-btn" icon="Download" @click="handleExport">导出</el-button>
        <right-toolbar :search="false" @queryTable="getList" class="header-inline-tools" />
      </div>
    </div>

    <!-- 数据表格 -->
    <el-table
        v-loading="loading"
        :data="heritage_manageList"
        @selection-change="handleSelectionChange"
        border
        stripe
        size="small"
        class="industrial-table"
    >
      <el-table-column type="selection" width="44" align="center"/>
      <el-table-column type="index" label="序号" align="center" width="52"/>
      <el-table-column label="封面" align="center" width="72">
        <template #default="scope">
          <image-preview :src="scope.row.coverImage" :width="40" :height="40" class="table-cover"/>
        </template>
      </el-table-column>
      <el-table-column label="展品名称" align="left" prop="itemName" min-width="120" :show-overflow-tooltip="true">
        <template #default="scope">
          <span class="item-name-bold">{{ scope.row.itemName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="所属项目" align="center" prop="categoryId" min-width="120" :show-overflow-tooltip="true">
        <template #default="scope">
          <el-tag size="small" effect="plain" class="industrial-tag table-tag-ellipsis">{{ categoryFormat(scope.row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <dict-tag :options="heritage_audit_status" :value="scope.row.status" size="small"/>
        </template>
      </el-table-column>
      <el-table-column label="览 / 赞 / 藏" align="center" min-width="160">
        <template #default="scope">
          <div class="stat-group">
            <div class="stat-item"><el-icon><View /></el-icon> {{ scope.row.viewCount || 0 }}</div>
            <div class="stat-item text-danger"><el-icon><Pointer /></el-icon> {{ scope.row.likeCount || 0 }}</div>
            <div class="stat-item text-warning"><el-icon><StarFilled /></el-icon> {{ scope.row.favoriteCount || 0 }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="发布人" align="center" prop="createBy" min-width="88" :show-overflow-tooltip="true"/>
      <el-table-column label="创建日期" align="center" prop="createTime" width="108">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="156" fixed="right" class-name="heritage-op-col">
        <template #default="scope">
          <div class="op-group">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['heritage:heritage_manage:edit']">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['heritage:heritage_manage:remove']">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 4. 分页 -->
    <div class="pagination-wrapper">
      <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList"/>
    </div>

    <!-- 5. 对话框 -->
    <el-dialog :title="title" v-model="open" width="750px" top="8vh" append-to-body :close-on-click-modal="false">
      <div class="dialog-scroll-wrapper">
        <el-form ref="heritage_manageRef" :model="form" :rules="rules" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="16">
              <el-form-item label="展品名称" prop="itemName"><el-input v-model="form.itemName" placeholder="请输入名称"/></el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="显示顺序" prop="sortOrder"><el-input-number v-model="form.sortOrder" controls-position="right" style="width: 100%"/></el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="所属项目" prop="categoryId">
                <el-cascader v-model="form.categoryId" :options="categoryOptions" :props="{ value: 'categoryId', label: 'categoryName', children: 'children', emitPath: false }" placeholder="请选择具体项目" filterable style="width: 100%"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="当前状态">
                <el-radio-group v-model="form.status">
                  <el-radio v-for="dict in heritage_audit_status" :key="dict.value" :label="parseInt(dict.value)">{{ dict.label }}</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="封面图片"><image-upload v-model="form.coverImage" :limit="1"/></el-form-item>
          <el-form-item label="3D模型文件"><file-upload v-model="form.modelFile" :file-type="['glb', 'gltf']"/></el-form-item>
          <el-form-item label="展品简介"><el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入简介内容"/></el-form-item>
          <el-form-item label="历史渊源"><editor v-model="form.history" :min-height="150"/></el-form-item>
          <el-form-item label="制作工艺"><editor v-model="form.craft" :min-height="150"/></el-form-item>
          <el-row :gutter="20" v-if="form.itemId">
            <el-col :span="8"><el-form-item label="浏览数"><el-input v-model="form.viewCount" disabled/></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="点赞数"><el-input v-model="form.likeCount" disabled/></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="收藏数"><el-input v-model="form.favoriteCount" disabled/></el-form-item></el-col>
          </el-row>
          <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" placeholder="请输入备注"/></el-form-item>
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
import { listHeritage_manage, getHeritage_manage, delHeritage_manage, addHeritage_manage, updateHeritage_manage } from "@/api/heritage/heritage_manage"
import { listCategory, treeselect } from "@/api/heritage/category";
import { Pointer, StarFilled, View } from "@element-plus/icons-vue";

const { proxy } = getCurrentInstance();
const { heritage_audit_status } = proxy.useDict('heritage_audit_status');

const heritage_manageList = ref([]);
const categoryOptions = ref([]);
const flatCategories = ref([]);
const open = ref(false);
const loading = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, itemName: null, categoryId: null, status: null },
  rules: {
    itemName: [{ required: true, message: "展品名称不能为空", trigger: "blur" }],
    categoryId: [{ required: true, message: "分类不能为空", trigger: "change" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listHeritage_manage(queryParams.value).then(res => {
    heritage_manageList.value = res.rows;
    total.value = res.total;
    loading.value = false;
  });
}

function getCategoryTree() {
  listCategory({ pageNum: 1, pageSize: 100 }).then(res => {
    flatCategories.value = res.rows;
  });
  treeselect().then(res => {
    categoryOptions.value = res.data;
  });
}

function categoryFormat(row) {
  const category = flatCategories.value.find(i => String(i.categoryId) === String(row.categoryId));
  return category ? category.categoryName : row.categoryId;
}

function reset() {
  form.value = { itemId: null, itemName: null, categoryId: null, coverImage: null, modelFile: null, status: 0, sortOrder: 0, viewCount: 0, likeCount: 0, favoriteCount: 0 };
  proxy.resetForm("heritage_manageRef");
}

function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery(); }
function handleSelectionChange(selection) {
  ids.value = selection.map(i => i.itemId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

function handleAdd() { reset(); open.value = true; title.value = "添加非遗展品"; }
function handleUpdate(row) {
  reset();
  getHeritage_manage(row.itemId || ids.value).then(res => {
    form.value = res.data;
    open.value = true;
    title.value = "修改非遗展品";
  });
}

function submitForm() {
  proxy.$refs.heritage_manageRef.validate(valid => {
    if (!valid) return;
    const action = form.value.itemId ? updateHeritage_manage : addHeritage_manage;
    action(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.itemId ? "修改成功" : "新增成功");
      open.value = false;
      getList();
    });
  });
}

function handleDelete(row) {
  const _ids = row.itemId || ids.value;
  proxy.$modal.confirm('确定删除？').then(() => delHeritage_manage(_ids)).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  });
}

function handleExport() { proxy.download('heritage/heritage_manage/export', { ...queryParams.value }, `heritage_${Date.now()}.xlsx`); }

onMounted(() => { getCategoryTree(); getList(); });
</script>

<style scoped lang="scss">
.item-name-bold { font-weight: 600; color: #303133; font-size: 13px; }
.table-tag-ellipsis { display: inline-block; max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; vertical-align: middle; }
.stat-group { display: flex; flex-wrap: wrap; justify-content: center; align-items: center; gap: 6px 10px; }
.stat-item { display: inline-flex; align-items: center; gap: 4px; font-size: 12px; color: #606266; }
.text-danger { color: #F56C6C; }
.text-warning { color: #E6A23C; }
.industrial-tag { border-radius: 4px; border: 1px solid #ddd; color: #333; font-weight: 500; }
.dialog-scroll-wrapper {
  max-height: 65vh; overflow-y: auto; padding: 0 20px;
  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: #eee; border-radius: 4px; }
}
:deep(.el-dialog__body) { padding: 20px 0 10px 0 !important; }
</style>