<template>
  <div
    class="create-thread-page"
    :class="{ 'fade-leave-active': isLeaving, 'fade-enter-active': isEntering }"
  >
    <!-- 页面头部 -->
    <Header title="发布新帖子" :showCreateThreadBtn="false" />

    <!-- 主内容区域 -->
    <main class="main-content">
      <div class="container">
        <!-- 面包屑导航 -->
        <nav class="breadcrumb">
          <router-link to="/home">首页</router-link> &gt; 发布新帖子
        </nav>

        <!-- 发帖表单 -->
        <div class="create-thread-form">
          <!-- 板块选择 -->
          <div class="form-section">
            <h2 class="section-title">选择板块</h2>
            <select
              v-model="formData.sectionId"
              class="form-select"
              :disabled="loadingSections"
            >
              <option value="" disabled>请选择板块</option>
              <option
                v-for="section in sections"
                :key="section.id"
                :value="section.id"
              >
                {{ section.name }}
              </option>
            </select>
          </div>

          <div class="form-section">
            <h2 class="section-title">帖子标题</h2>
            <input
              v-model="formData.title"
              type="text"
              class="form-input"
              placeholder="请输入帖子标题"
              maxlength="100"
            />
          </div>

          <!-- 文本编辑器 -->
          <div class="form-section">
            <h2 class="section-title">帖子内容</h2>

            <!-- 工具栏 -->
            <div class="editor-toolbar">
              <button
                @click="handleFormatting('bold')"
                class="toolbar-btn"
                title="粗体 (Ctrl+B)"
              >
                B
              </button>
              <button
                @click="handleFormatting('italic')"
                class="toolbar-btn"
                title="斜体 (Ctrl+I)"
              >
                I
              </button>
              <button
                @click="handleFormatting('underline')"
                class="toolbar-btn"
                title="下划线 (Ctrl+U)"
              >
                U
              </button>
              <button
                @click="handleFormatting('header1')"
                class="toolbar-btn"
                title="标题1"
              >
                H1
              </button>
              <button
                @click="handleFormatting('header2')"
                class="toolbar-btn"
                title="标题2"
              >
                H2
              </button>
              <button
                @click="handleFormatting('list')"
                class="toolbar-btn"
                title="列表"
              >
                • 列表
              </button>
              <button
                @click="handleFormatting('link')"
                class="toolbar-btn"
                title="链接"
              >
                🔗
              </button>
              <button
                @click="handleFormatting('image')"
                class="toolbar-btn"
                title="图片"
              >
                🖼️
              </button>
              <button
                @click="handleFormatting('quote')"
                class="toolbar-btn"
                title="引用"
              >
                💬
              </button>
              <button
                @click="handleFormatting('code')"
                class="toolbar-btn"
                title="代码"
              >
                📄
              </button>
              <button
                @click="handleFormatting('clear')"
                class="toolbar-btn"
                title="清除格式"
              >
                ⌫
              </button>
            </div>

            <!-- 文本输入区域 -->
            <textarea
              v-model="formData.content"
              class="editor-textarea"
              placeholder="请输入帖子内容"
              rows="15"
            ></textarea>

            <!-- 预览区域 -->
            <div class="preview-section">
              <h3 class="preview-title">预览</h3>
              <div class="preview-content" v-html="previewContent"></div>
            </div>
          </div>

          <!-- 图片上传 -->
          <div class="form-section">
            <h2 class="section-title">图片上传</h2>
            <div class="image-upload">
              <input
                type="file"
                id="image-upload"
                @change="handleImageUpload"
                accept="image/*"
                multiple
                class="file-input"
              />
              <label for="image-upload" class="upload-label">
                <span class="upload-icon">📷</span>
                <span>选择图片</span>
              </label>
            </div>

            <!-- 图片预览 -->
            <div
              v-if="uploadedImages.length > 0"
              class="image-preview-container"
            >
              <div
                v-for="(image, index) in uploadedImages"
                :key="index"
                class="image-preview-item"
              >
                <img :src="image.url" alt="预览图片" class="preview-image" />
                <button @click="removeImage(index)" class="remove-image-btn">
                  ×
                </button>
              </div>
            </div>
          </div>

          <!-- 草稿管理 -->
          <div class="form-section">
            <h2 class="section-title">草稿管理</h2>
            <div class="draft-actions">
              <button @click="saveDraft" class="btn btn-secondary">
                保存草稿
              </button>
              <button @click="toggleDrafts" class="btn btn-secondary">
                {{ showDrafts ? "收起草稿" : "查看草稿" }}
              </button>
            </div>

            <!-- 草稿列表 -->
            <div v-if="showDrafts" class="drafts-list">
              <h3>已保存的草稿</h3>
              <div v-if="savedDrafts.length === 0" class="no-drafts">
                暂无保存的草稿
              </div>
              <ul v-else class="draft-items">
                <li
                  v-for="(draft, index) in savedDrafts"
                  :key="index"
                  class="draft-item"
                >
                  <div class="draft-info">
                    <div class="draft-title">
                      {{ draft.title || "无标题草稿" }}
                    </div>
                    <div class="draft-time">
                      {{ formatTime(draft.savedAt) }}
                    </div>
                  </div>
                  <div class="draft-buttons">
                    <button
                      @click="loadDraft(index)"
                      class="btn btn-small btn-primary"
                    >
                      加载
                    </button>
                    <button
                      @click="deleteDraft(index)"
                      class="btn btn-small btn-danger"
                    >
                      删除
                    </button>
                  </div>
                </li>
              </ul>
            </div>
          </div>

          <!-- 提交区域 -->
          <div class="form-section">
            <div class="submit-actions">
              <button @click="submitThread" class="btn btn-primary btn-large">
                发布帖子
              </button>
              <router-link to="/home" class="btn btn-secondary btn-large"
                >取消</router-link
              >
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 状态提示 -->
    <transition name="toast">
      <div v-if="toast.show" class="toast" :class="toast.type">
        {{ toast.message }}
      </div>
    </transition>

    <!-- 回到顶部按钮 -->
    <button v-if="showBackToTop" @click="scrollToTop" class="back-to-top-btn">
      <i class="fas fa-arrow-up"></i>
    </button>

    <!-- 页脚 -->
    <footer class="page-footer">
      <div class="container">
        <p>© 2024 Talk2Me论坛 - 技术支持：Vue 3 + Spring Boot</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import Header from "../components/Header.vue";
import { postApi, sectionApi } from "../utils/api";
import { getAuthToken } from "../utils/authStorage";

const router = useRouter();
const route = useRoute();

// 离开页面动画控制
const isLeaving = ref(false);
const isEntering = ref(true);
let navigationGuard = null;

// 回到顶部按钮控制
const showBackToTop = ref(false);

// 表单数据
const formData = ref({
  title: "",
  content: "",
  sectionId: null,
  images: [],
});

// 板块列表
const sections = ref([]);
const loadingSections = ref(false);

// 上传的图片
const uploadedImages = ref([]);

// 预览内容
const previewContent = computed(() => {
  return formatContentForPreview(formData.value.content);
});

// 草稿管理
const savedDrafts = ref([]);
const showDrafts = ref(false);

// 状态提示
const toast = ref({
  show: false,
  message: "",
  type: "success", // success, error, warning
});

// 格式化内容预览
const formatContentForPreview = (content) => {
  if (!content) return "";

  let formatted = content;

  // 基本格式化处理
  formatted = formatted.replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>"); // 粗体
  formatted = formatted.replace(/\*(.*?)\*/g, "<em>$1</em>"); // 斜体
  formatted = formatted.replace(/__(.*?)__/g, "<u>$1</u>"); // 下划线
  formatted = formatted.replace(/^# (.*$)/gm, "<h1>$1</h1>"); // 标题1
  formatted = formatted.replace(/^## (.*$)/gm, "<h2>$1</h2>"); // 标题2
  formatted = formatted.replace(/^- (.*$)/gm, "<li>$1</li>"); // 无序列表
  formatted = formatted.replace(
    /\[([^\]]+)\]\(([^)]+)\)/g,
    '<a href="$2" target="_blank">$1</a>',
  ); // 链接
  formatted = formatted.replace(
    /!\[([^\]]*)\]\(([^)]+)\)/g,
    '<img src="$2" alt="$1" />',
  ); // 图片
  formatted = formatted.replace(/^> (.*$)/gm, "<blockquote>$1</blockquote>"); // 引用
  formatted = formatted.replace(/```([^`]+)```/g, "<pre><code>$1</code></pre>"); // 代码块
  formatted = formatted.replace(/`([^`]+)`/g, "<code>$1</code>"); // 行内代码
  formatted = formatted.replace(/\n/g, "<br>"); // 换行

  return formatted;
};

// 处理格式化操作
const handleFormatting = (formatType) => {
  const textarea = document.querySelector(".editor-textarea");
  const start = textarea.selectionStart;
  const end = textarea.selectionEnd;
  const selectedText = textarea.value.substring(start, end);

  let newText = "";
  let newCursorPos = start;

  switch (formatType) {
    case "bold":
      newText = `**${selectedText}**`;
      newCursorPos = start + 2;
      break;
    case "italic":
      newText = `*${selectedText}*`;
      newCursorPos = start + 1;
      break;
    case "underline":
      newText = `__${selectedText}__`;
      newCursorPos = start + 2;
      break;
    case "header1":
      newText = `# ${selectedText}`;
      newCursorPos = start + 2;
      break;
    case "header2":
      newText = `## ${selectedText}`;
      newCursorPos = start + 3;
      break;
    case "list":
      newText = `- ${selectedText}`;
      newCursorPos = start + 2;
      break;
    case "link":
      if (selectedText) {
        newText = `[${selectedText}](url)`;
        newCursorPos = start + selectedText.length + 3;
      } else {
        newText = `[链接文本](url)`;
        newCursorPos = start + 5;
      }
      break;
    case "image":
      if (selectedText) {
        newText = `![${selectedText}](图片地址)`;
        newCursorPos = start + selectedText.length + 4;
      } else {
        newText = `![图片描述](图片地址)`;
        newCursorPos = start + 7;
      }
      break;
    case "quote":
      newText = `> ${selectedText}`;
      newCursorPos = start + 2;
      break;
    case "code":
      if (selectedText) {
        newText = `\`${selectedText}\``;
        newCursorPos = start + 1;
      } else {
        newText = `\`代码\``;
        newCursorPos = start + 1;
      }
      break;
    case "clear":
      newText = selectedText.replace(/[*_`>#-]/g, "");
      newCursorPos = start;
      break;
    default:
      return;
  }

  // 插入格式化文本
  formData.value.content =
    textarea.value.substring(0, start) +
    newText +
    textarea.value.substring(end);

  // 重新聚焦并设置光标位置
  setTimeout(() => {
    textarea.focus();
    const endPos = newCursorPos + (newText.length - selectedText.length);
    textarea.setSelectionRange(newCursorPos, endPos);
  }, 0);
};

// 处理图片上传
const handleImageUpload = (event) => {
  const files = event.target.files;

  for (let i = 0; i < files.length; i++) {
    const file = files[i];

    // 检查文件类型
    if (!file.type.startsWith("image/")) {
      showToast("请选择有效的图片文件", "error");
      continue;
    }

    // 检查文件大小（限制为5MB）
    if (file.size > 5 * 1024 * 1024) {
      showToast("图片大小不能超过5MB", "error");
      continue;
    }

    // 创建预览URL
    const reader = new FileReader();
    reader.onload = (e) => {
      uploadedImages.value.push({
        file: file,
        url: e.target.result,
      });
    };
    reader.readAsDataURL(file);
  }

  // 清空文件输入，以便可以再次选择相同的文件
  event.target.value = "";
};

// 移除图片
const removeImage = (index) => {
  uploadedImages.value.splice(index, 1);
  showToast("图片已移除", "success");
};

// 保存草稿
const saveDraft = () => {
  const draft = {
    ...formData.value,
    savedAt: new Date().toISOString(),
    images: uploadedImages.value.map((img) => ({
      url: img.url,
    })),
  };

  savedDrafts.value.unshift(draft);

  // 限制最多保存10个草稿
  if (savedDrafts.value.length > 10) {
    savedDrafts.value.pop();
  }

  // 保存到localStorage
  localStorage.setItem("threadDrafts", JSON.stringify(savedDrafts.value));

  showToast("草稿已保存", "success");
};

// 加载草稿
const loadDraft = (index) => {
  const draft = savedDrafts.value[index];
  formData.value = {
    title: draft.title || "",
    content: draft.content || "",
    images: [],
  };

  uploadedImages.value = draft.images || [];

  showDrafts.value = false;
  showToast("草稿已加载", "success");
};

// 删除草稿
const deleteDraft = (index) => {
  savedDrafts.value.splice(index, 1);
  localStorage.setItem("threadDrafts", JSON.stringify(savedDrafts.value));
  showToast("草稿已删除", "success");
};

// 切换草稿显示
const toggleDrafts = () => {
  showDrafts.value = !showDrafts.value;
};

// 表单验证
const validateForm = () => {
  if (!formData.value.sectionId) {
    showToast("请选择板块", "error");
    return false;
  }

  if (!formData.value.title.trim()) {
    showToast("请输入帖子标题", "error");
    return false;
  }

  if (formData.value.title.length > 100) {
    showToast("帖子标题不能超过100个字符", "error");
    return false;
  }

  if (!formData.value.content.trim()) {
    showToast("请输入帖子内容", "error");
    return false;
  }

  return true;
};

// 提交帖子
const submitThread = async () => {
  if (!validateForm()) return;

  try {
    // 构建帖子数据
    const postData = {
      sectionId: parseInt(formData.value.sectionId),
      title: formData.value.title,
      content: formData.value.content,
    };

    // 调用API创建帖子
    await postApi.createPost(postData);

    showToast("帖子发布成功！", "success");

    // 清除表单数据
    formData.value = {
      title: "",
      content: "",
      images: [],
    };
    uploadedImages.value = [];

    // 3秒后跳转到首页
    setTimeout(() => {
      router.push("/home");
    }, 3000);
  } catch (error) {
    console.error("发布帖子失败:", error);
    showToast(error.message || "发布失败，请重试", "error");
  }
};

// 显示提示
const showToast = (message, type = "success") => {
  toast.value = {
    show: true,
    message,
    type,
  };

  // 3秒后自动隐藏
  setTimeout(() => {
    toast.value.show = false;
  }, 3000);
};

// 格式化时间
const formatTime = (timeString) => {
  const date = new Date(timeString);
  const now = new Date();
  const diff = now - date;

  const minutes = Math.floor(diff / (1000 * 60));
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (minutes < 60) {
    return `${minutes}分钟前`;
  } else if (hours < 24) {
    return `${hours}小时前`;
  } else if (days < 30) {
    return `${days}天前`;
  } else {
    return date.toLocaleDateString();
  }
};

// 回到顶部函数
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: "smooth",
  });
};

// 处理滚动事件
const handleScroll = () => {
  // 当滚动距离超过300px时显示回到顶部按钮
  showBackToTop.value = window.scrollY > 300;
};

// 获取板块列表
const fetchSections = async () => {
  loadingSections.value = true;
  try {
    const response = await sectionApi.getAllSections();
    // 根据后端返回的数据结构获取板块列表
    if (response.data && Array.isArray(response.data)) {
      sections.value = response.data;
    } else if (response.data && response.data.records) {
      sections.value = response.data.records;
    } else {
      // 使用默认板块数据
      sections.value = [
        { id: 1, name: "软件开发与工程" },
        { id: 2, name: "硬件组件与架构" },
        { id: 3, name: "编程语言与框架" },
        { id: 4, name: "数据库与存储" },
        { id: 5, name: "网络与安全" },
        { id: 6, name: "人工智能与机器学习" },
        { id: 7, name: "云计算与大数据" },
        { id: 8, name: "移动开发" },
        { id: 9, name: "前端开发" },
        { id: 10, name: "后端开发" },
        { id: 11, name: "新兴技术" },
        { id: 12, name: "计算机科学理论" },
      ];
    }
  } catch (error) {
    console.error("获取板块列表失败:", error);
    // 使用默认板块数据
    sections.value = [
      { id: 1, name: "软件开发与工程" },
      { id: 2, name: "硬件组件与架构" },
      { id: 3, name: "编程语言与框架" },
      { id: 4, name: "数据库与存储" },
      { id: 5, name: "网络与安全" },
      { id: 6, name: "人工智能与机器学习" },
      { id: 7, name: "云计算与大数据" },
      { id: 8, name: "移动开发" },
      { id: 9, name: "前端开发" },
      { id: 10, name: "后端开发" },
      { id: 11, name: "新兴技术" },
      { id: 12, name: "计算机科学理论" },
    ];
  } finally {
    loadingSections.value = false;
  }
};

// 页面加载时初始化
onMounted(() => {
  // 进入动画：延迟清除 isEntering，让 keyframe 动画完整播放
  setTimeout(() => {
    isEntering.value = false;
  }, 600);

  // 检查token是否存在
  const token = getAuthToken();
  console.log("CreateThreadView onMounted - token:", token ? "存在" : "不存在");
  if (token) {
    console.log("Token值:", token.substring(0, 30) + "...");
  }

  // 获取板块列表
  fetchSections();

  // 加载保存的草稿
  const saved = localStorage.getItem("threadDrafts");
  if (saved) {
    savedDrafts.value = JSON.parse(saved);
  }

  // 添加滚动事件监听器
  window.addEventListener("scroll", handleScroll);

  // 设置导航守卫
  navigationGuard = router.beforeEach((to, from, next) => {
    if (from.path === route.path) {
      // 从当前页面离开，触发动画
      isLeaving.value = true;

      // 等待动画完成后再导航
      setTimeout(() => {
        next();
      }, 500); // 0.5秒动画时间
    } else {
      next();
    }
  });
});

// 组件卸载时清除导航守卫和滚动事件监听器
onBeforeUnmount(() => {
  if (navigationGuard) {
    navigationGuard(); // 调用返回的函数移除守卫
  }
  // 移除滚动事件监听器
  window.removeEventListener("scroll", handleScroll);
});

// 监听表单数据变化
watch([() => formData.value.title, () => formData.value.content], () => {
  // 可以在这里添加防抖逻辑
});
</script>

<style scoped>
.create-thread-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

/* 页面进入时的渐入动画 */
.create-thread-page.fade-enter-active {
  animation: createPageEnter 0.5s ease-out;
}

@keyframes createPageEnter {
  from {
    opacity: 0;
    filter: blur(10px);
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    filter: blur(0);
    transform: translateY(0);
  }
}

/* 页面离开时的淡出动画 */
.create-thread-page.fade-leave-active {
  opacity: 0;
  filter: blur(20px);
  transform: scale(0.95);
  transition: all 0.5s ease-in-out;
}

/* 确保所有子元素都继承过渡效果 */
.create-thread-page * {
  transition: all 2s ease-in-out;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  padding: 30px 0;
}

.main-content .container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 发帖表单 */
.create-thread-form {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.form-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
}

.form-input,
.form-select {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  transition: border-color 0.2s;
  background-color: white;
}

.form-input:focus,
.form-select:focus {
  border-color: #3498db;
  outline: none;
}

/* 编辑器 */
.editor-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: var(--spacing-md);
  padding: var(--spacing-sm);
  background-color: var(--forum-light-color);
  border-radius: var(--border-radius-sm);
  border: 1px solid #ddd;
  flex-wrap: wrap;
}

.toolbar-btn {
  padding: var(--spacing-sm) var(--spacing-md);
  background-color: white;
  border: 1px solid #ddd;
  border-radius: var(--border-radius-sm);
  cursor: pointer;
  font-size: var(--base-font-size);
  transition: all 0.2s;
  font-weight: 500;
}

.toolbar-btn:hover {
  background-color: var(--forum-primary-color);
  color: white;
  border-color: var(--forum-primary-color);
}

.editor-textarea {
  width: 100%;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  line-height: 1.6;
  resize: vertical;
  min-height: 200px;
  font-family: inherit;
}

.editor-textarea:focus {
  border-color: #3498db;
  outline: none;
}

/* 预览区域 */
.preview-section {
  margin-top: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.preview-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
}

.preview-content {
  line-height: 1.6;
  color: #333;
}

.preview-content strong {
  font-weight: bold;
}

.preview-content em {
  font-style: italic;
}

.preview-content u {
  text-decoration: underline;
}

/* 图片上传 */
.image-upload {
  display: inline-block;
  margin-bottom: 20px;
}

.file-input {
  display: none;
}

.upload-label {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background-color: #3498db;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.upload-label:hover {
  background-color: #2980b9;
}

.upload-icon {
  font-size: 20px;
}

.image-preview-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 15px;
  margin-top: 20px;
}

.image-preview-item {
  position: relative;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.preview-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.remove-image-btn {
  position: absolute;
  top: 5px;
  right: 5px;
  width: 24px;
  height: 24px;
  background-color: rgba(255, 0, 0, 0.8);
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.remove-image-btn:hover {
  background-color: rgba(255, 0, 0, 1);
}

/* 草稿管理 */
.draft-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.drafts-list {
  background-color: #f8f9fa;
  border-radius: 4px;
  padding: 20px;
  border: 1px solid #ddd;
}

.drafts-list h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
}

.no-drafts {
  color: #666;
  text-align: center;
  padding: 20px;
}

.draft-items {
  list-style: none;
  padding: 0;
  margin: 0;
}

.draft-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #ddd;
}

.draft-item:last-child {
  border-bottom: none;
}

.draft-info {
  flex: 1;
}

.draft-title {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.draft-time {
  font-size: 12px;
  color: #999;
}

.draft-buttons {
  display: flex;
  gap: 8px;
}

.btn-small {
  padding: 6px 12px;
  font-size: 12px;
}

/* 提交区域 */
.submit-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.btn-large {
  padding: 12px 30px;
  font-size: 16px;
  font-weight: 600;
}

/* 提示框 */
.toast {
  position: fixed;
  bottom: 30px;
  right: 30px;
  padding: 15px 25px;
  border-radius: 4px;
  color: white;
  font-weight: 500;
  z-index: 1000;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.toast.success {
  background-color: #2ecc71;
}

.toast.error {
  background-color: #e74c3c;
}

.toast.warning {
  background-color: #f39c12;
}

/* 动画 */
.toast-enter-active,
.toast-leave-active {
  transition:
    opacity 0.3s,
    transform 0.3s;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

/* 回到顶部按钮 */
.back-to-top-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: var(--primary-color);
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  z-index: 998;
  border: none;
  cursor: pointer;
}

.back-to-top-btn:hover {
  background-color: var(--secondary-color);
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .create-thread-form {
    padding: var(--spacing-xl);
  }

  .editor-toolbar {
    gap: var(--spacing-xs);
  }

  .toolbar-btn {
    padding: var(--spacing-xs) var(--spacing-sm);
    font-size: 12px;
  }

  .image-preview-container {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }

  .preview-image {
    height: 120px;
  }

  .draft-item {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  .draft-buttons {
    justify-content: flex-end;
  }

  .submit-actions {
    flex-direction: column;
  }

  /* 移动端回到顶部按钮 */
  .back-to-top-btn {
    width: 45px;
    height: 45px;
    font-size: 18px;
    bottom: 20px;
    right: 20px;
  }
}
</style>
