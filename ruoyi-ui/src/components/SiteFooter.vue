<template>
  <footer class="site-footer">
    <div class="footer-content">
      <div class="footer-left">
        <span class="footer-brand">{{ brand }}</span>
        <span class="footer-copyright">{{ copyrightText }}</span>
      </div>

      <div class="footer-right">
        <span class="footer-text">{{ slogan }}</span>
        <span class="footer-author">{{ author }}</span>
        <span class="footer-heart">❤</span>
      </div>
    </div>
  </footer>
</template>

<script setup>
const props = defineProps({
  // 品牌名称
  brand: {
    type: String,
    default: '白族非遗档案'
  },
  // 版权文本（全中文）
  copyrightText: {
    type: String,
    default: '© 2026 白族非遗数字档案馆 保留所有权利'
  },
  // 标语
  slogan: {
    type: String,
    default: '用热爱和代码精心制作'
  },
  // 作者
  author: {
    type: String,
    default: 'rcpawn'
  }
});
</script>

<style lang="scss" scoped>
// 变量定义
$bai-cyan: #339af0;
$bai-red: #C62828;
$ink-black: #1a1a1a;
$footer-bg: #2c2c2c;
$footer-text-primary: #ffffff;
$footer-text-secondary: #a8a8a8;

// 基础样式
.site-footer {
  background: $footer-bg;
  padding: clamp(18px, 2.5vh, 26px) 0;
  flex-shrink: 0;

  .footer-content {
    max-width: 1600px;
    margin: 0 auto;
    padding: 0 clamp(40px, 5vw, 80px);
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 20px;
  }

  .footer-left {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .footer-brand {
      font-size: 15px;
      font-weight: 900;
      letter-spacing: 2px;
      color: $footer-text-primary;
    }

    .footer-copyright {
      font-size: 12px;
      color: $footer-text-secondary;
      letter-spacing: 1px;
    }
  }

  .footer-right {
    display: flex;
    align-items: center;
    gap: 10px;

    .footer-text {
      font-size: 13px;
      color: $footer-text-secondary;
      font-weight: 500;
    }

    .footer-author {
      font-size: 14px;
      font-weight: 900;
      color: $footer-text-primary;
    }

    .footer-heart {
      font-size: 15px;
      color: $bai-red;
      animation: heartbeat 1.5s ease-in-out infinite;
    }
  }
}

@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.15); }
}

// 响应式优化
@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    text-align: center;
    gap: 14px;
    padding: 0 clamp(30px, 4vw, 60px);
  }

  .footer-left,
  .footer-right {
    flex-direction: column;
    gap: 6px;
  }
}
</style>
