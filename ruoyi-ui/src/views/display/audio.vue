<template>
  <div class="echoes-container">
    <header class="echoes-header">
      <div class="left-tools">
        <div class="title-group">
          <h2 class="main-title">
            <span class="seal-mark">白族</span>
            <span class="text-part">三道余音</span>
          </h2>
          <p class="subtitle">听见苍山洱海的回响，感受非遗声音的韵律</p>
        </div>
        <div class="tab-switcher">
          <div class="tab-item" :class="{ active: currentTab === 'video' }" @click="switchTab('video')">视频影像</div>
          <div class="tab-item" :class="{ active: currentTab === 'audio' }" @click="switchTab('audio')">音频档案</div>
        </div>
      </div>
      <div class="right-info">
        <!-- 搜索框 -->
        <div class="search-box" :class="{ 'is-focused': isSearchFocused }">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索标题..."
            clearable
            prefix-icon="Search"
            @input="handleSearch"
            @focus="isSearchFocused = true"
            @blur="isSearchFocused = false"
            class="search-input"
          />
        </div>
        <div class="count-tag">TOTAL: {{ filteredList.length }}</div>
      </div>
    </header>

    <main class="echoes-content" v-loading="loading">
      <!-- 视频列表 -->
      <el-row :gutter="30" v-if="currentTab === 'video'">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in filteredVideoList" :key="item.videoId">
          <div class="video-card" @click="handlePlayVideo(item)">
            <div class="cover-wrapper">
              <img :src="getAssetUrl(item.coverImage || item.cover_image)" class="cover-img" />
              <div class="play-overlay">
                <el-icon class="play-icon">
                  <VideoPlay />
                </el-icon>
              </div>
              <div class="card-tag">{{ item.itemName || '非遗影像' }}</div>
              <div class="duration-tag" v-if="item.duration">{{ formatDuration(item.duration) }}</div>
            </div>
            <div class="card-info">
              <h3 class="t-title">{{ item.title }}</h3>
              <div class="t-meta">
                <span><el-icon><User /></el-icon> {{ item.createBy || '佚名' }}</span>
                <span>{{ parseTime(item.createTime, '{y}.{m}.{d}') }}</span>
              </div>
            </div>
          </div>
        </el-col>
        <!-- 空状态 -->
        <el-col :span="24" v-if="filteredVideoList.length === 0 && !loading">
          <div class="empty-state">
            <el-empty description="暂无视频数据" />
          </div>
        </el-col>
      </el-row>

      <!-- 音频列表 -->
      <el-row :gutter="30" v-if="currentTab === 'audio'">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in filteredAudioList" :key="item.audioId">
          <div class="audio-card" :class="{ 'playing': currentAudio?.audioId === item.audioId }" @click="handlePlay(item)">
            <div class="cover-wrapper">
              <img :src="getAssetUrl(item.coverImage || item.cover_image)" class="cover-img" />
              <div class="play-overlay" :class="{ 'show': currentAudio?.audioId === item.audioId && isPlaying }">
                <el-icon class="play-icon">
                  <VideoPause v-if="currentAudio?.audioId === item.audioId && isPlaying" />
                  <VideoPlay v-else />
                </el-icon>
              </div>
              <div class="card-tag">{{ item.itemName || '民族调式' }}</div>
            </div>
            <div class="card-info">
              <h3 class="t-title">{{ item.title }}</h3>
              <div class="t-meta">
                <span><el-icon><User /></el-icon> {{ item.performer || '佚名' }}</span>
                <span>{{ parseTime(item.createTime, '{y}.{m}.{d}') }}</span>
              </div>
            </div>
          </div>
        </el-col>
        <!-- 空状态 -->
        <el-col :span="24" v-if="filteredAudioList.length === 0 && !loading">
          <div class="empty-state">
            <el-empty description="暂无音频数据" />
          </div>
        </el-col>
      </el-row>
    </main>

    <!-- 音频播放器 -->
    <transition name="player-pop">
      <div class="compact-player" v-if="currentAudio && currentTab === 'audio'">
        <div class="player-inner">

          <div class="section-info">
            <div class="mini-cover" :class="{ 'spinning': isPlaying }">
              <img :src="getAssetUrl(currentAudio.coverImage || currentAudio.cover_image)" />
            </div>
            <div class="meta-data">
              <div class="title">{{ currentAudio.title }}</div>
              <div class="sub">{{ currentAudio.performer || '采风档案' }}</div>
            </div>
          </div>

          <div class="section-interaction">
            <div class="visualizer-container">
              <canvas ref="canvasRef" class="mini-wave"></canvas>
            </div>
            <div class="progress-wrapper">
              <span class="time">{{ formatTime(currentTime) }}</span>
              <div class="slider-box" @mousedown="startDrag" ref="sliderRef">
                <div class="rail">
                  <div class="fill" :style="{ width: progress + '%' }">
                    <div class="knob" v-if="isPlaying"></div>
                  </div>
                </div>
              </div>
              <span class="time">{{ formatTime(duration) }}</span>
            </div>
          </div>

          <div class="section-ctrl">
            <button class="play-main-btn" @click="togglePlayState">
              <el-icon v-if="isPlaying"><VideoPause /></el-icon>
              <el-icon v-else><VideoPlay /></el-icon>
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 视频播放器弹窗 -->
    <el-dialog
        v-model="videoDialogVisible"
        :width="dialogWidth"
        append-to-body
        destroy-on-close
        class="immersive-video-dialog"
        :fullscreen="isMobile"
    >
      <template #header>
        <div class="dialog-header">
          <span class="dialog-title">{{ currentVideo?.title || '视频播放' }}</span>
        </div>
      </template>
      <div class="video-player-container">
        <video
            ref="videoPlayerRef"
            :src="currentVideo?.videoUrl ? getAssetUrl(currentVideo.videoUrl) : ''"
            controls
            autoplay
            class="responsive-video-player"
            @click.stop
        >
          您的浏览器不支持视频播放
        </video>
        <div class="video-info" v-if="currentVideo">
          <div class="video-meta">
            <span class="meta-item" v-if="currentVideo.itemName">
              <el-icon><Collection /></el-icon> {{ currentVideo.itemName }}
            </span>
            <span class="meta-item" v-if="currentVideo.duration">
              <el-icon><Timer /></el-icon> {{ formatDuration(currentVideo.duration) }}
            </span>
          </div>
          <p class="video-description" v-if="currentVideo.description">{{ currentVideo.description }}</p>
        </div>
      </div>
    </el-dialog>

    <audio
        ref="audioRef"
        @timeupdate="onTimeUpdate"
        @loadedmetadata="onLoadedMetadata"
        @ended="isPlaying = false"
        crossorigin="anonymous"
    ></audio>

    <!-- 页脚：放在根容器内部，保证单根节点 -->
    <SiteFooter />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, getCurrentInstance, nextTick, computed } from 'vue';
import { listArchiveAudio } from "@/api/heritage/audio.js";
import { listArchiveVideo } from "@/api/heritage/ video.js";
import { VideoPlay, VideoPause, User, Collection, Timer } from '@element-plus/icons-vue';
import SiteFooter from "@/components/SiteFooter.vue";

const { proxy } = getCurrentInstance();
const loading = ref(true);
const currentTab = ref('video'); // 默认展示视频
const audioList = ref([]);
const videoList = ref([]);
const searchKeyword = ref(''); // 搜索关键词
const isSearchFocused = ref(false); // 搜索框焦点状态
const currentAudio = ref(null);
const isPlaying = ref(false);
const progress = ref(0);
const currentTime = ref(0);
const duration = ref(0);

// 视频相关
const videoDialogVisible = ref(false);
const currentVideo = ref(null);
const videoPlayerRef = ref(null);

// 响应式对话框宽度
const isMobile = computed(() => window.innerWidth <= 768);
const dialogWidth = computed(() => {
  if (isMobile.value) return '100%';
  const width = window.innerWidth;
  if (width < 1200) return '85%';
  if (width < 1600) return '75%';
  return '1200px';
});

const audioRef = ref(null);
const canvasRef = ref(null);
const sliderRef = ref(null);
let audioCtx = null;
let analyser = null;
let animationId = null;

const getAssetUrl = (url) => url ? import.meta.env.VITE_APP_BASE_API + url : '';
const formatTime = (s) => `${Math.floor(s / 60)}:${Math.floor(s % 60).toString().padStart(2, '0')}`;
const formatDuration = (seconds) => {
  if (!seconds) return '--';
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}:${s.toString().padStart(2, '0')}`;
};

const getList = async () => {
  loading.value = true;
  try {
    // 同时获取音频和视频列表数据
    const [audioRes, videoRes] = await Promise.all([
      listArchiveAudio(),
      listArchiveVideo()
    ]);
    audioList.value = audioRes.rows || [];
    videoList.value = videoRes.rows || [];
  } catch (error) {
    console.error('获取数据失败:', error);
    proxy.$modal.msgError('获取数据失败');
  } finally {
    loading.value = false;
  }
};

const switchTab = (tab) => {
  currentTab.value = tab;
  searchKeyword.value = ''; // 切换标签时清空搜索
  // 切换标签时暂停音频播放
  if (audioRef.value) {
    audioRef.value.pause();
    isPlaying.value = false;
  }
  // 如果对应列表为空，重新获取数据
  if (tab === 'audio' && audioList.value.length === 0) {
    getList();
  } else if (tab === 'video' && videoList.value.length === 0) {
    getList();
  }
};

const handlePlay = (item) => {
  if (currentAudio.value?.audioId === item.audioId) {
    togglePlayState();
    return;
  }
  currentAudio.value = item;
  audioRef.value.src = getAssetUrl(item.audioUrl);
  isPlaying.value = false;
  setTimeout(() => togglePlayState(), 100);
};

const handlePlayVideo = (item) => {
  currentVideo.value = item;
  videoDialogVisible.value = true;
};

// 搜索处理
const handleSearch = () => {
  // 搜索逻辑在计算属性中实现
};

// 过滤后的视频列表
const filteredVideoList = computed(() => {
  if (!searchKeyword.value) return videoList.value;
  const keyword = searchKeyword.value.toLowerCase();
  return videoList.value.filter(item =>
    item.title?.toLowerCase().includes(keyword) ||
    item.itemName?.toLowerCase().includes(keyword) ||
    item.createBy?.toLowerCase().includes(keyword)
  );
});

// 过滤后的音频列表
const filteredAudioList = computed(() => {
  if (!searchKeyword.value) return audioList.value;
  const keyword = searchKeyword.value.toLowerCase();
  return audioList.value.filter(item =>
    item.title?.toLowerCase().includes(keyword) ||
    item.itemName?.toLowerCase().includes(keyword) ||
    item.performer?.toLowerCase().includes(keyword)
  );
});

// 当前过滤后的列表
const filteredList = computed(() => {
  return currentTab.value === 'video' ? filteredVideoList.value : filteredAudioList.value;
});

const togglePlayState = () => {
  if (!audioCtx) initAudioContext();
  isPlaying.value ? audioRef.value.pause() : audioRef.value.play();
  isPlaying.value = !isPlaying.value;
  if (isPlaying.value) nextTick(drawWave);
};

const initAudioContext = () => {
  audioCtx = new (window.AudioContext || window.webkitAudioContext)();
  analyser = audioCtx.createAnalyser();
  const source = audioCtx.createMediaElementSource(audioRef.value);
  source.connect(analyser);
  analyser.connect(audioCtx.destination);
  analyser.fftSize = 64;
};

const drawWave = () => {
  const canvas = canvasRef.value;
  if (!canvas || !analyser) return;
  const ctx = canvas.getContext('2d');
  const dpr = window.devicePixelRatio || 1;
  const dataArray = new Uint8Array(analyser.frequencyBinCount);

  const render = () => {
    animationId = requestAnimationFrame(render);
    analyser.getByteFrequencyData(dataArray);
    const avg = dataArray.reduce((a, b) => a + b) / dataArray.length;

    canvas.width = canvas.offsetWidth * dpr;
    canvas.height = canvas.offsetHeight * dpr;
    ctx.scale(dpr, dpr);
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    ctx.beginPath();
    ctx.lineWidth = 2;
    ctx.strokeStyle = '#5E2F2F'; // 使用矿石红作为波动线颜色

    const w = canvas.offsetWidth;
    const h = canvas.offsetHeight;

    for (let x = 0; x <= w; x += 2) {
      const slice = (avg / 255) * h * 0.8;
      const y = (h / 2) + Math.sin(x * 0.05 + Date.now() * 0.006) * slice;
      if (x === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    }
    ctx.stroke();
  };
  render();
};

const startDrag = (e) => {
  const move = (ev) => {
    const rect = sliderRef.value.getBoundingClientRect();
    let pos = (ev.clientX - rect.left) / rect.width;
    pos = Math.max(0, Math.min(1, pos));
    progress.value = pos * 100;
    audioRef.value.currentTime = pos * duration.value;
  };
  const up = () => {
    document.removeEventListener('mousemove', move);
    document.removeEventListener('mouseup', up);
  };
  document.addEventListener('mousemove', move);
  document.addEventListener('mouseup', up);
  move(e);
};

const onTimeUpdate = () => {
  currentTime.value = audioRef.value.currentTime;
  progress.value = (currentTime.value / duration.value) * 100;
};
const onLoadedMetadata = () => { duration.value = audioRef.value.duration; };

onMounted(getList);
onUnmounted(() => {
  cancelAnimationFrame(animationId);
  if (audioCtx) audioCtx.close();
});
</script>

<style scoped lang="scss">
$bg-paper: #ffffff;
$ink: #1A1A1A;
$mineral-red: #5E2F2F;

.echoes-container {
  min-height: 100vh;
  background: #ffffff;
  display: flex;
  flex-direction: column;
}

.echoes-content {
  flex: 1;
  min-height: 0;
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
  padding: 0 40px 120px;

  /* 修复 Flex 布局下 Element Plus 栅格宽度坍缩的问题 */
  :deep(.el-row) {
    width: 100% !important;
    display: flex;
    flex-wrap: wrap;
  }
}

/* 顶部 Tab - 白族非遗风格 */
.echoes-header {
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
  padding: 40px 40px 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-bottom: 2px solid #eee;

  .left-tools {
    display: flex;
    flex-direction: column;
    gap: 20px;

    .title-group {
      .main-title {
        font-size: 42px;
        font-weight: 900;
        letter-spacing: 8px;
        margin: 0 0 12px 0;
        display: flex;
        align-items: center;
        gap: 16px;
        color: #1a1a1a;

        .seal-mark {
          background: $mineral-red;
          color: #fff;
          font-size: 18px;
          padding: 6px 14px;
          border-radius: 4px;
          font-weight: 700;
          letter-spacing: 4px;
          box-shadow: 2px 2px 8px rgba(198, 40, 40, 0.3);
        }

        .text-part {
          font-weight: 900;
        }
      }

      .subtitle {
        margin: 0;
        color: #888;
        font-size: 15px;
        letter-spacing: 2px;
      }
    }

    .tab-switcher {
      display: inline-flex;
      border-radius: 10px;
      overflow: hidden;
      background: #f0f0f0;
      box-shadow: 0 2px 8px rgba(0,0,0,0.08);

      .tab-item {
        flex: 1;
        min-width: 120px;
        padding: 12px 28px;
        font-size: 15px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        color: #666;
        position: relative;
        white-space: nowrap;
        user-select: none;
        text-align: center;

        &.active {
          background: #000;
          color: #fff;
          box-shadow: 0 2px 8px rgba(0,0,0,0.2);
        }

        &:not(.active):hover {
          background: #e0e0e0;
          color: #333;
        }
      }
    }
  }

  .right-info {
    display: flex;
    align-items: center;
    gap: 16px;

    .search-box {
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

      .search-input {
        width: 200px;
        transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

        :deep(.el-input__wrapper) {
          border-radius: 8px;
          box-shadow: 0 2px 8px rgba(0,0,0,0.06);
          padding: 8px 12px;
          transition: all 0.3s;

          &:hover {
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
          }

          &.is-focus {
            box-shadow: 0 0 0 1px #000 inset;
          }
        }

        :deep(.el-input__inner) {
          font-size: 13px;
          color: #333;

          &::placeholder {
            color: #999;
          }
        }

        :deep(.el-input__prefix) {
          color: #999;
        }
      }

      // 焦点时向左延伸
      &.is-focused .search-input {
        width: 320px;
      }
    }

    .count-tag {
      font-family: 'Courier New', monospace;
      font-weight: bold;
      border: 2px solid #1a1a1a;
      padding: 6px 14px;
      font-size: 13px;
      border-radius: 6px;
      background: #fff;
      box-shadow: 3px 3px 0 rgba(0,0,0,0.1);
      white-space: nowrap;
    }
  }
}

/* 音频卡片布局 - B站风格 */
.audio-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 30px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  }

  &.playing {
    box-shadow: 0 0 0 2px #667eea, 0 8px 24px rgba(102, 126, 234, 0.2);
  }

  .cover-wrapper {
    height: 200px;
    position: relative;
    overflow: hidden;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
    }

    &:hover img {
      transform: scale(1.08);
    }

    .play-overlay {
      position: absolute;
      inset: 0;
      background: rgba(0,0,0,0.3);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: all 0.3s;
      backdrop-filter: blur(2px);

      &.show {
        opacity: 1;
        background: rgba(0,0,0,0.5);
      }

      .play-icon {
        font-size: 48px;
        color: #fff;
        filter: drop-shadow(0 2px 8px rgba(0,0,0,0.3));
        transition: transform 0.3s;
      }

      &:hover .play-icon {
        transform: scale(1.15);
      }
    }

    .card-tag {
      position: absolute;
      top: 12px;
      left: 12px;
      background: rgba(255,255,255,0.95);
      padding: 4px 10px;
      font-size: 11px;
      font-weight: 600;
      border-radius: 6px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      backdrop-filter: blur(8px);
    }
  }

  .card-info {
    padding: 16px;

    h3 {
      font-size: 15px;
      font-weight: 600;
      margin: 0 0 10px;
      color: #1a1a1a;
      line-height: 1.4;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .t-meta {
      font-size: 12px;
      color: #999;
      display: flex;
      justify-content: space-between;
      align-items: center;

      span {
        display: flex;
        align-items: center;
        gap: 4px;

        .el-icon {
          font-size: 14px;
        }
      }
    }
  }
}

/* 视频卡片布局 - B站风格 */
.video-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 30px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  }

  .cover-wrapper {
    height: 200px;
    position: relative;
    overflow: hidden;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
    }

    &:hover img {
      transform: scale(1.08);
    }

    .play-overlay {
      position: absolute;
      inset: 0;
      background: rgba(0,0,0,0.3);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: all 0.3s;
      backdrop-filter: blur(2px);

      &:hover {
        opacity: 1;
        background: rgba(0,0,0,0.5);
      }

      .play-icon {
        font-size: 48px;
        color: #fff;
        filter: drop-shadow(0 2px 8px rgba(0,0,0,0.3));
        transition: transform 0.3s;
      }

      &:hover .play-icon {
        transform: scale(1.15);
      }
    }

    .card-tag {
      position: absolute;
      top: 12px;
      left: 12px;
      background: rgba(255,255,255,0.95);
      padding: 4px 10px;
      font-size: 11px;
      font-weight: 600;
      border-radius: 6px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      backdrop-filter: blur(8px);
    }

    .duration-tag {
      position: absolute;
      bottom: 12px;
      right: 12px;
      background: rgba(0,0,0,0.75);
      color: #fff;
      padding: 4px 8px;
      font-size: 11px;
      font-weight: 600;
      border-radius: 6px;
      backdrop-filter: blur(8px);
    }
  }

  .card-info {
    padding: 16px;

    h3 {
      font-size: 15px;
      font-weight: 600;
      margin: 0 0 10px;
      color: #1a1a1a;
      line-height: 1.4;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .t-meta {
      font-size: 12px;
      color: #999;
      display: flex;
      justify-content: space-between;
      align-items: center;

      span {
        display: flex;
        align-items: center;
        gap: 4px;

        .el-icon {
          font-size: 14px;
        }
      }
    }
  }
}

/* 优化后的紧凑播放器 */
.compact-player {
  position: fixed; bottom: 30px; left: 50%; transform: translateX(-50%);
  width: 90%; max-width: 850px; // 宽度变短，更精致
  background: $ink; color: #fff; border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.4); z-index: 2000;

  .player-inner {
    height: 80px; display: flex; align-items: center; padding: 0 25px; gap: 20px;
  }

  // 1. 信息区
  .section-info {
    display: flex; align-items: center; gap: 12px; width: 180px;
    .mini-cover {
      width: 44px; height: 44px; border-radius: 8px; overflow: hidden; border: 1px solid rgba(255,255,255,0.1);
      &.spinning { animation: rotate 8s linear infinite; }
      img { width: 100%; height: 100%; object-fit: cover; }
    }
    .meta-data {
      overflow: hidden;
      .title { font-size: 13px; font-weight: bold; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
      .sub { font-size: 10px; color: #666; margin-top: 2px; }
    }
  }

  // 2. 交互核心区
  .section-interaction {
    flex: 1; display: flex; flex-direction: column; justify-content: center; gap: 8px;
    .visualizer-container {
      height: 24px; width: 100%;
      .mini-wave { width: 100%; height: 100%; display: block; }
    }
    .progress-wrapper {
      display: flex; align-items: center; gap: 12px;
      .time { font-family: monospace; font-size: 10px; color: #444; width: 35px; }
      .slider-box {
        flex: 1; height: 12px; display: flex; align-items: center; cursor: pointer;
        .rail {
          width: 100%; height: 2px; background: rgba(255,255,255,0.1); position: relative;
          .fill {
            height: 100%; background: $mineral-red; position: absolute;
            .knob {
              position: absolute; right: -4px; top: -3px; width: 8px; height: 8px;
              background: #fff; border-radius: 50%; box-shadow: 0 0 10px $mineral-red;
            }
          }
        }
      }
    }
  }

  // 3. 控制区
  .section-ctrl {
    .play-main-btn {
      width: 48px; height: 48px; border-radius: 50%; background: #fff; color: $ink;
      border: none; display: flex; align-items: center; justify-content: center;
      font-size: 22px; cursor: pointer; transition: 0.3s;
      &:hover { transform: scale(1.1); background: $mineral-red; color: #fff; }
    }
  }
}

@keyframes rotate { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.player-pop-enter-active, .player-pop-leave-active { transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.player-pop-enter-from { opacity: 0; transform: translate(-50%, 40px) scale(0.9); }

.count-tag {
  font-family: 'Courier New', Courier, monospace;
  font-weight: bold;
  border: 1px solid #000;
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 6px;
  background: #fff;
}

// 空状态样式
.empty-state {
  padding: 60px 0;
  text-align: center;

  :deep(.el-empty) {
    .el-empty__description {
      color: #999;
      font-size: 14px;
    }
  }
}

// 视频播放器样式
.video-player-container {
  text-align: center;
  padding: 0;

  .responsive-video-player {
    width: 100%;
    max-height: 75vh;
    object-fit: contain;
    background: #000;
    border-radius: 4px;
    display: block;
  }

  .video-info {
    text-align: left;
    margin-top: 20px;
    padding: 0 10px;

    .video-meta {
      display: flex;
      gap: 20px;
      margin-bottom: 12px;
      flex-wrap: wrap;

      .meta-item {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-size: 14px;
        color: #666;
        background: #f5f5f5;
        padding: 4px 12px;
        border-radius: 12px;

        .el-icon {
          font-size: 16px;
        }
      }
    }

    .video-description {
      color: #666;
      font-size: 14px;
      line-height: 1.8;
      margin: 0;
      white-space: pre-wrap;
    }
  }
}

// 沉浸式视频弹窗样式
:deep(.immersive-video-dialog) {
  .el-dialog__header {
    padding: 15px 20px;
    border-bottom: 1px solid #eee;
    margin: 0;
  }

  .el-dialog__body {
    padding: 0;
    max-height: 85vh;
    overflow-y: auto;
  }

  .dialog-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .dialog-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }
}

// 移动端适配
@media (max-width: 768px) {
  .video-player-container {
    .responsive-video-player {
      max-height: 50vh;
    }

    .video-info {
      padding: 15px;

      .video-meta {
        .meta-item {
          font-size: 12px;
          padding: 3px 10px;
        }
      }

      .video-description {
        font-size: 13px;
      }
    }
  }

  :deep(.immersive-video-dialog) {
    .el-dialog__header {
      padding: 12px 15px;
    }

    .dialog-title {
      font-size: 14px;
    }
  }
}

// 大屏幕优化
@media (min-width: 1920px) {
  .video-player-container {
    .responsive-video-player {
      max-height: 80vh;
    }
  }
}
</style>