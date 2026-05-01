<template>
  <div class="app-container heritage-admin-page">
    <div class="heritage-toolbar-row">
      <h2 class="page-title heritage-toolbar-row__title">视频管理</h2>
      <el-form
        :model="queryParams"
        ref="queryRef"
        :inline="true"
        class="heritage-toolbar-row__filters industrial-toolbar-form--no-label"
        label-width="0"
      >
        <el-form-item prop="title">
          <el-input v-model="queryParams.title" placeholder="视频标题" clearable @keyup.enter="handleQuery" style="width: 168px" />
        </el-form-item>
        <el-form-item prop="status">
          <el-select v-model="queryParams.status" placeholder="审核状态" clearable style="width: 120px">
            <el-option v-for="dict in heritage_audit_status" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="heritage-toolbar-row__actions">
        <el-button class="industrial-add-btn" icon="Plus" @click="handleAdd">录入新视频</el-button>
        <right-toolbar :search="false" @queryTable="getList" class="header-inline-tools" />
      </div>
    </div>

    <el-table
        v-loading="loading"
        :data="videoList"
        @selection-change="handleSelectionChange"
        border
        stripe
        size="small"
        class="industrial-table"
    >
      <el-table-column type="selection" width="44" align="center"/>
      <el-table-column type="index" label="序号" align="center" width="52" />

      <el-table-column label="视频封面" align="center" prop="coverImage" width="108">
        <template #default="scope">
          <el-image
              v-if="scope.row.coverImage"
              :src="getAssetUrl(scope.row.coverImage)"
              :preview-src-list="[getAssetUrl(scope.row.coverImage)]"
              fit="cover"
              style="width: 88px; height: 50px; border-radius: 4px; vertical-align: middle;"
          />
          <span v-else class="no-cover">无封面</span>
        </template>
      </el-table-column>

      <el-table-column label="视频标题" align="left" prop="title" min-width="200">
        <template #default="scope">
          <span class="item-name-bold">{{ scope.row.title }}</span>
        </template>
      </el-table-column>

      <el-table-column label="关联资产" align="center" prop="itemId" width="180">
        <template #default="scope">
          <el-tag size="small" effect="plain" class="industrial-tag">
            {{ itemFormat(scope.row.itemId) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="视频时长" align="center" prop="duration" width="100">
        <template #default="scope">
          <span>{{ formatDuration(scope.row.duration) }}</span>
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

      <el-table-column label="操作" align="center" width="200" fixed="right" class-name="heritage-op-col">
        <template #default="scope">
          <div class="op-group">
            <el-button link type="primary" icon="VideoPlay" @click="handlePlay(scope.row)">播放</el-button>
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog :title="title" v-model="open" width="600px" top="8vh" append-to-body :close-on-click-modal="false">
      <div class="dialog-scroll-wrapper">
        <el-form ref="videoFormRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="视频标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入视频标题" />
          </el-form-item>

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

          <el-form-item label="封面图片" prop="coverImage">
            <file-upload v-model="form.coverImage" :file-type="['jpg', 'png', 'jpeg']" :file-size="10" />
          </el-form-item>

          <el-form-item label="视频文件" prop="videoUrl">
            <file-upload v-model="form.videoUrl" :file-type="['mp4', 'webm', 'ogg']" :file-size="100" />
          </el-form-item>

          <el-form-item label="视频时长" prop="duration">
            <el-input-number v-model="form.duration" :min="0" :max="7200" placeholder="秒" style="width: 100%" />
          </el-form-item>

          <el-form-item label="视频简介" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入视频简介或解说词" />
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

    <el-dialog title="视频播放" v-model="playOpen" width="90%" append-to-body destroy-on-close class="responsive-video-dialog">
      <div class="video-player-container">
        <video
            ref="videoPlayer"
            :src="getAssetUrl(currentVideo.videoUrl)"
            controls
            autoplay
            class="responsive-video"
        >
          您的浏览器不支持视频播放
        </video>
        <div class="video-info" v-if="currentVideo.title">
          <h3>{{ currentVideo.title }}</h3>
          <p v-if="currentVideo.description">{{ currentVideo.description }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="Video">
import { ref, reactive, onMounted, getCurrentInstance, toRefs } from 'vue';
import { listVideo, getVideo, delVideo, addVideo, updateVideo } from "@/api/heritage/ video.js";
import { listHeritage_manage } from "@/api/heritage/heritage_manage";

const { proxy } = getCurrentInstance();
const { heritage_audit_status, sys_normal_disable } = proxy.useDict('heritage_audit_status', 'sys_normal_disable');

// 获取资源完整URL
const getAssetUrl = (url) => {
  if (!url) return '';
  // 如果已经是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }
  // 否则添加API前缀
  return import.meta.env.VITE_APP_BASE_API + url;
};

const videoList = ref([]);
const itemOptions = ref([]);
const open = ref(false);
const playOpen = ref(false);
const loading = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const currentVideo = ref({});

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, title: null, status: null },
  rules: {
    title: [{ required: true, message: "视频标题不能为空", trigger: "blur" }],
    videoUrl: [{ required: true, message: "请上传视频文件", trigger: "change" }],
    itemId: [{ required: true, message: "请选择关联展品", trigger: "change" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listVideo(queryParams.value).then(response => {
    videoList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function getItemList() {
  listHeritage_manage({ pageNum: 1, pageSize: 200 }).then(res => {
    itemOptions.value = res.rows;
  });
}

function itemFormat(itemId) {
  if (!itemId) return "未关联";
  const item = itemOptions.value.find(i => String(i.itemId) === String(itemId));
  return item ? item.itemName : itemId;
}

function formatDuration(seconds) {
  if (!seconds) return '--';
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}:${s.toString().padStart(2, '0')}`;
}

function handlePlay(row) {
  currentVideo.value = row;
  playOpen.value = true;
}

function handleUpdate(row) {
  reset();
  const _videoId = row.videoId || ids.value;
  getVideo(_videoId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改视频档案";
  });
}

function submitForm() {
  proxy.$refs["videoFormRef"].validate(valid => {
    if (valid) {
      const action = form.value.videoId ? updateVideo : addVideo;
      action(form.value).then(() => {
        proxy.$modal.msgSuccess("操作成功");
        open.value = false;
        getList();
      });
    }
  });
}

function handleDelete(row) {
  const _ids = row.videoId || ids.value;
  proxy.$modal.confirm('是否确认删除数据项？').then(() => delVideo(_ids)).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  });
}

function handleAdd() { reset(); open.value = true; title.value = "录入新视频"; }
function cancel() { open.value = false; reset(); }
function reset() {
  form.value = { videoId: null, title: null, videoUrl: null, coverImage: null, itemId: null, duration: null, status: "0" };
  if (proxy.$refs.videoFormRef) proxy.$refs.videoFormRef.resetFields();
}

function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery(); }
function handleSelectionChange(selection) {
  ids.value = selection.map(i => i.videoId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

onMounted(() => {
  getItemList();
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

.no-cover { color: #999; font-size: 12px; }

.video-player-container {
  text-align: center;
  .responsive-video {
    width: 100%;
    max-height: 70vh;
    object-fit: contain;
    background: #000;
    border-radius: 4px;
  }
  .video-info {
    text-align: left;
    margin-top: 20px;
    h3 { font-size: 18px; font-weight: 700; margin-bottom: 10px; }
    p { color: #666; font-size: 14px; line-height: 1.6; }
  }
}

// 响应式视频弹窗样式
:deep(.responsive-video-dialog) {
  .el-dialog__body {
    padding: 20px !important;
  }
}

@media (max-width: 768px) {
  .video-player-container {
    .responsive-video {
      max-height: 50vh;
    }
    .video-info {
      h3 { font-size: 16px; }
      p { font-size: 12px; }
    }
  }
}
</style>
