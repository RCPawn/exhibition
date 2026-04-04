<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="图集标题" prop="title">
        <el-input
            v-model="queryParams.title"
            placeholder="请输入图集标题"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option
              v-for="dict in sys_normal_disable"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd"
            v-hasPermi="['heritage:gallery:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="success"
            plain
            icon="Edit"
            :disabled="single"
            @click="handleUpdate"
            v-hasPermi="['heritage:gallery:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
            v-hasPermi="['heritage:gallery:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="warning"
            plain
            icon="Download"
            @click="handleExport"
            v-hasPermi="['heritage:gallery:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="galleryList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="图集ID" align="center" prop="galleryId" />
      <el-table-column label="图集标题" align="center" prop="title" />
      <el-table-column label="封面图" align="center" prop="coverUrl" width="100">
        <template #default="scope">
          <image-preview :src="scope.row.coverUrl" :width="50" :height="50"/>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['heritage:gallery:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['heritage:gallery:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
        v-show="total>0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
    />

    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form ref="galleryRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="图集标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入图集标题" />
        </el-form-item>
        <el-form-item label="封面图" prop="coverUrl">
          <image-upload v-model="form.coverUrl"/>
        </el-form-item>
        <el-form-item label="导语" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
                v-for="dict in sys_normal_disable"
                :key="dict.value"
                :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-divider content-position="center">图集明细图片</el-divider>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" icon="Plus" @click="handleAddHeritageGalleryImage">添加图片</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" icon="Delete" @click="handleDeleteHeritageGalleryImage">删除选中</el-button>
          </el-col>
        </el-row>
        <el-table :data="heritageGalleryImageList" :row-class-name="rowHeritageGalleryImageIndex" @selection-change="handleHeritageGalleryImageSelectionChange" ref="heritageGalleryImage">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="序号" align="center" prop="index" width="50"/>
          <el-table-column label="图片上传" prop="imageUrl" width="200">
            <template #default="scope">
              <image-upload
                  v-model="scope.row.imageUrl"
                  :limit="1"
                  :is-show-tip="false"
              />
            </template>
          </el-table-column>
          <el-table-column label="图片说明" prop="caption">
            <template #default="scope">
              <el-input v-model="scope.row.caption" placeholder="请输入图片说明" />
            </template>
          </el-table-column>
        </el-table>
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

<script setup name="Gallery">
import { listGallery, getGallery, delGallery, addGallery, updateGallery } from "@/api/heritage/gallery"

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict('sys_normal_disable')

const galleryList = ref([])
const heritageGalleryImageList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const checkedHeritageGalleryImage = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: null,
    status: null,
  },
  rules: {
    title: [{ required: true, message: "图集标题不能为空", trigger: "blur" }],
    coverUrl: [{ required: true, message: "封面图不能为空", trigger: "blur" }],
    status: [{ required: true, message: "状态不能为空", trigger: "change" }],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询列表 */
function getList() {
  loading.value = true
  listGallery(queryParams.value).then(response => {
    galleryList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    galleryId: null,
    title: null,
    coverUrl: null,
    description: null,
    status: "0",
    delFlag: null
  }
  heritageGalleryImageList.value = []
  proxy.resetForm("galleryRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.galleryId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加图集管理"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _galleryId = row.galleryId || ids.value
  getGallery(_galleryId).then(response => {
    form.value = response.data
    heritageGalleryImageList.value = response.data.heritageGalleryImageList || []
    open.value = true
    title.value = "修改图集管理"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["galleryRef"].validate(valid => {
    if (valid) {
      form.value.heritageGalleryImageList = heritageGalleryImageList.value
      if (form.value.galleryId != null) {
        updateGallery(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addGallery(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _galleryIds = row.galleryId || ids.value
  proxy.$modal.confirm('是否确认删除图集编号为"' + _galleryIds + '"的数据项？').then(function() {
    return delGallery(_galleryIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 从表序号 */
function rowHeritageGalleryImageIndex({ row, rowIndex }) {
  row.index = rowIndex + 1
}

/** 从表添加按钮操作 */
function handleAddHeritageGalleryImage() {
  let obj = {
    imageUrl: "",
    caption: ""
  }
  heritageGalleryImageList.value.push(obj)
}

/** 从表删除按钮操作 */
function handleDeleteHeritageGalleryImage() {
  if (checkedHeritageGalleryImage.value.length == 0) {
    proxy.$modal.msgError("请先选择要删除的数据")
  } else {
    const heritageGalleryImages = heritageGalleryImageList.value
    const checkedHeritageGalleryImages = checkedHeritageGalleryImage.value
    heritageGalleryImageList.value = heritageGalleryImages.filter(function(item) {
      return checkedHeritageGalleryImages.indexOf(item.index) == -1
    })
  }
}

/** 复选框选中数据 */
function handleHeritageGalleryImageSelectionChange(selection) {
  checkedHeritageGalleryImage.value = selection.map(item => item.index)
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('heritage/gallery/export', {
    ...queryParams.value
  }, `gallery_${new Date().getTime()}.xlsx`)
}

getList()
</script>