<template>
  <div
    class="thread-detail-page"
    :class="{ 'fade-leave-active': isLeaving, 'fade-enter-active': isEntering }"
  >
    <!-- 顶部导航栏 -->
    <Header title="帖子" :show-create-thread-btn="false" />

    <!-- 主内容区域 -->
    <main class="main-content">
      <div class="container">
        <!-- 面包屑导航 -->
        <nav class="breadcrumb">
          <span @click="goHome" class="breadcrumb-link">首页</span>
          <span class="breadcrumb-separator">></span>
          <span class="breadcrumb-current">帖子详情</span>
        </nav>

        <!-- 帖子详情卡片 -->
        <div class="thread-detail-card">
          <!-- 帖子标题 -->
          <h1 class="thread-title">{{ thread.title }}</h1>

          <!-- 板块标识 -->
          <div class="section-tag" @click="goToSection(thread.sectionId)">
            <i class="fas fa-folder"></i>
            <span>{{ thread.sectionName || "未知板块" }}</span>
          </div>

          <!-- 作者信息区 -->
          <div class="author-info">
            <div
              class="author-avatar"
              @click="goToUserProfile(thread.authorId)"
            >
              <img
                v-if="thread.authorAvatar"
                :src="thread.authorAvatar"
                alt="作者头像"
              />
              <i v-else class="fas fa-user-circle"></i>
            </div>
            <div class="author-meta">
              <div
                class="author-name"
                @click="goToUserProfile(thread.authorId)"
              >
                {{ thread.author }}
              </div>
              <div class="post-time">{{ formatTime(thread.createdAt) }}</div>
            </div>
          </div>

          <!-- 帖子内容区 -->
          <div class="thread-content" v-html="thread.content"></div>

          <!-- 帖子统计信息 -->
          <div class="thread-stats">
            <span class="stat-item">
              <i class="fas fa-eye"></i> {{ thread.views }} 浏览
            </span>
            <span class="stat-item">
              <i class="fas fa-comment"></i> {{ thread.replies }} 回复
            </span>
          </div>
        </div>

        <!-- 评论区 -->
        <div class="comments-section">
          <!-- 评论标签页 -->
          <div class="comments-tabs">
            <div
              class="tab-item"
              :class="{ active: activeTab === 'comments' }"
              @click="activeTab = 'comments'"
            >
              评论 {{ comments.length }}
            </div>
            <div
              class="tab-item"
              :class="{ active: activeTab === 'likes' }"
              @click="activeTab = 'likes'"
            >
              赞与转发
            </div>
          </div>

          <!-- 评论排序 -->
          <div class="comments-sort">
            <span
              class="sort-option"
              :class="{ active: sortType === 'hot' }"
              @click="sortType = 'hot'"
              >最热</span
            >
            <span class="sort-separator">|</span>
            <span
              class="sort-option"
              :class="{ active: sortType === 'latest' }"
              @click="sortType = 'latest'"
              >最新</span
            >
          </div>

          <!-- 发表评论框 -->
          <div class="comment-input-section">
            <div class="comment-input-wrapper">
              <div
                class="current-user-avatar"
                :class="{ 'not-logged-in': !isLoggedIn }"
                @click="handleAvatarClick"
              >
                <img
                  v-if="isLoggedIn && userAvatar"
                  :src="userAvatar"
                  alt="用户头像"
                />
                <i v-else-if="isLoggedIn" class="fas fa-user-circle"></i>
                <span v-else class="login-text">登录</span>
              </div>
              <div class="input-area">
                <textarea
                  v-model="newComment"
                  placeholder="请输入评论"
                  rows="3"
                  @keyup.enter.ctrl="submitComment"
                  :disabled="!isLoggedIn"
                ></textarea>
                <div class="input-actions">
                  <span class="input-tip">Ctrl + Enter 发送</span>
                  <button
                    class="submit-btn"
                    :disabled="
                      !newComment.trim() || isSubmitting || !isLoggedIn
                    "
                    @click="submitComment"
                  >
                    {{ isSubmitting ? "发送中..." : "发送" }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 评论列表 -->
          <div class="comments-list">
            <div
              v-for="comment in sortedComments"
              :key="comment.id"
              class="comment-item"
            >
              <!-- 置顶标识 -->
              <div v-if="comment.isPinned" class="pinned-badge">置顶</div>

              <div class="comment-main">
                <div
                  class="comment-avatar"
                  @click="goToUserProfile(comment.authorId)"
                >
                  <img
                    v-if="comment.authorAvatar"
                    :src="comment.authorAvatar"
                    alt="评论者头像"
                  />
                  <i v-else class="fas fa-user-circle"></i>
                </div>
                <div class="comment-content-wrapper">
                  <div
                    class="comment-author"
                    @click="goToUserProfile(comment.authorId)"
                  >
                    {{ comment.author }}
                  </div>
                  <div class="comment-text">{{ comment.content }}</div>
                  <div class="comment-actions">
                    <span class="comment-time">{{ comment.time }}</span>
                    <button
                      class="action-btn"
                      :class="{ active: comment.isLiked }"
                      @click="toggleCommentLike(comment)"
                    >
                      <i class="fas fa-thumbs-up"></i>
                      {{ comment.likes || "点赞" }}
                    </button>
                    <button class="action-btn" @click="replyToComment(comment)">
                      <i class="fas fa-comment"></i> 回复
                    </button>
                  </div>

                  <!-- 回复列表 -->
                  <div
                    v-if="comment.replies && comment.replies.length > 0"
                    class="replies-list"
                  >
                    <div
                      v-for="reply in comment.replies"
                      :key="reply.id"
                      class="reply-item"
                    >
                      <div
                        class="reply-avatar"
                        @click="goToUserProfile(reply.authorId)"
                      >
                        <img
                          v-if="reply.authorAvatar"
                          :src="reply.authorAvatar"
                          alt="回复者头像"
                        />
                        <i v-else class="fas fa-user-circle"></i>
                      </div>
                      <div class="reply-content-wrapper">
                        <div class="reply-header">
                          <span
                            class="reply-author"
                            @click="goToUserProfile(reply.authorId)"
                            >{{ reply.author }}</span
                          >
                          <span v-if="reply.replyTo" class="reply-to">
                            回复
                            <span
                              class="reply-target"
                              @click="goToUserProfile(reply.replyToId)"
                              >@{{ reply.replyTo }}</span
                            >
                          </span>
                        </div>
                        <div class="reply-text">{{ reply.content }}</div>
                        <div class="reply-actions">
                          <span class="reply-time">{{ reply.time }}</span>
                          <button
                            class="action-btn"
                            :class="{ active: reply.isLiked }"
                            @click="toggleReplyLike(reply)"
                          >
                            <i class="fas fa-thumbs-up"></i>
                            {{ reply.likes || "点赞" }}
                          </button>
                          <button
                            class="action-btn"
                            @click="replyToReply(comment, reply)"
                          >
                            回复
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- 回复输入框 -->
                  <div
                    v-if="replyingTo === comment.id"
                    class="reply-input-wrapper"
                  >
                    <textarea
                      v-model="replyContent"
                      :placeholder="`回复 ${comment.author}:`"
                      rows="2"
                      @keyup.enter.ctrl="submitReply(comment)"
                    ></textarea>
                    <div class="reply-actions-row">
                      <button class="cancel-btn" @click="cancelReply">
                        取消
                      </button>
                      <button
                        class="submit-btn"
                        :disabled="!replyContent.trim() || isSubmittingReply"
                        @click="submitReply(comment)"
                      >
                        {{ isSubmittingReply ? "发送中..." : "发送" }}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="page-footer">
      <div class="container">
        <p>© 2024 Talk2Me论坛 - 技术支持：Vue 3 + Spring Boot</p>
      </div>
    </footer>

    <!-- 右侧悬浮点赞按钮 -->
    <div class="floating-actions">
      <button
        class="floating-btn like-btn"
        :class="{ active: thread.isLiked, loading: isLikeLoading }"
        @click="toggleLike"
        :disabled="isLikeLoading"
      >
        <i class="fas fa-thumbs-up"></i>
        <span class="like-count">{{ thread.likes || 0 }}</span>
      </button>
      <button class="floating-btn" @click="scrollToComments">
        <i class="fas fa-comment"></i>
        <span class="action-label">评论</span>
      </button>
      <button class="floating-btn" @click="shareThread">
        <i class="fas fa-share"></i>
        <span class="action-label">分享</span>
      </button>
    </div>

    <!-- 回到顶部按钮 -->
    <button v-if="showBackToTop" @click="scrollToTop" class="back-to-top-btn">
      <i class="fas fa-arrow-up"></i>
    </button>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, computed, inject } from "vue";
import { useRouter, useRoute } from "vue-router";
import { likeApi, replyApi, postApi, sectionApi } from "../utils/api";
import Header from "../components/Header.vue";

export default {
  name: "ThreadDetailView",
  components: {
    Header,
  },
  setup() {
    const router = useRouter();
    const route = useRoute();

    // 注入登录状态
    const isLoggedIn = inject("isLoggedIn", ref(false));

    // 页面动画控制
    const isLeaving = ref(false);
    const isEntering = ref(true);
    let navigationGuard = null;

    // 用户头像
    const userAvatar = ref("");

    // 处理头像点击
    const handleAvatarClick = () => {
      if (!isLoggedIn.value) {
        router.push("/login");
      }
    };

    // 帖子数据
    const thread = ref({
      id: null,
      title: "",
      author: "",
      authorId: null,
      authorAvatar: "",
      sectionId: null,
      sectionName: "",
      content: "",
      createdAt: "",
      views: 0,
      replies: 0,
      likes: 0,
      isLiked: false,
    });

    // 点赞加载状态
    const isLikeLoading = ref(false);

    // 评论相关
    const activeTab = ref("comments");
    const sortType = ref("hot");
    const newComment = ref("");
    const isSubmitting = ref(false);
    const replyingTo = ref(null);
    const replyContent = ref("");
    const isSubmittingReply = ref(false);

    // 评论列表
    const comments = ref([]);

    // 排序后的评论
    const sortedComments = computed(() => {
      let result = [...comments.value];
      if (sortType.value === "hot") {
        result.sort((a, b) => b.likes - a.likes);
      } else {
        result.sort(
          (a, b) => new Date(b.time).getTime() - new Date(a.time).getTime(),
        );
      }
      return result;
    });

    // 回到顶部按钮显示控制
    const showBackToTop = ref(false);

    // 返回首页
    const goHome = () => {
      router.push("/home");
    };

    // 跳转到用户主页
    const goToUserProfile = (userId) => {
      router.push(`/user/${userId}`);
    };

    // 跳转到板块页面
    const goToSection = (sectionId) => {
      if (sectionId) {
        router.push(`/section/${sectionId}`);
      }
    };

    // 格式化时间
    const formatTime = (timeString) => {
      if (!timeString) return "";
      const date = new Date(timeString);
      return date.toLocaleString("zh-CN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
      });
    };

    // 点赞功能
    const toggleLike = async () => {
      if (!isLoggedIn.value) {
        router.push("/login");
        return;
      }

      if (isLikeLoading.value) return;

      isLikeLoading.value = true;
      try {
        if (thread.value.isLiked) {
          await likeApi.unlike("post", thread.value.id);
          thread.value.isLiked = false;
          thread.value.likes--;
        } else {
          await likeApi.like({
            targetType: "post",
            targetId: thread.value.id,
          });
          thread.value.isLiked = true;
          thread.value.likes++;
        }
      } catch (error) {
        console.error("点赞失败:", error);
      } finally {
        isLikeLoading.value = false;
      }
    };

    // 评论点赞
    const toggleCommentLike = async (comment) => {
      if (!isLoggedIn.value) {
        router.push("/login");
        return;
      }

      try {
        if (comment.isLiked) {
          await likeApi.unlike("reply", comment.id);
          comment.isLiked = false;
          comment.likes--;
        } else {
          await likeApi.like({
            targetType: "reply",
            targetId: comment.id,
          });
          comment.isLiked = true;
          comment.likes++;
        }
      } catch (error) {
        console.error("评论点赞失败:", error);
      }
    };

    // 回复点赞
    const toggleReplyLike = async (reply) => {
      if (!isLoggedIn.value) {
        router.push("/login");
        return;
      }

      try {
        if (reply.isLiked) {
          await likeApi.unlike("reply", reply.id);
          reply.isLiked = false;
          reply.likes--;
        } else {
          await likeApi.like({
            targetType: "reply",
            targetId: reply.id,
          });
          reply.isLiked = true;
          reply.likes++;
        }
      } catch (error) {
        console.error("回复点赞失败:", error);
      }
    };

    // 提交评论
    const submitComment = async () => {
      if (!newComment.value.trim() || isSubmitting.value) return;

      isSubmitting.value = true;
      try {
        const response = await replyApi.createReply(thread.value.id, {
          content: newComment.value.trim(),
        });

        if (response.code === 200 && response.data) {
          const comment = {
            id: response.data.id,
            author: `用户${response.data.userId}`,
            authorId: response.data.userId,
            content: response.data.content,
            time: formatTime(response.data.createTime) || "刚刚",
            likes: response.data.likeCount || 0,
            isLiked: false,
            isPinned: false,
            replies: [],
          };

          comments.value.unshift(comment);
          newComment.value = "";
          thread.value.replies++;
        }
      } catch (error) {
        console.error("发表评论失败:", error);
      } finally {
        isSubmitting.value = false;
      }
    };

    // 回复评论
    const replyToComment = (comment) => {
      if (!isLoggedIn.value) {
        router.push("/login");
        return;
      }
      replyingTo.value = comment.id;
      replyContent.value = "";
    };

    // 回复回复
    const replyToReply = (comment, reply) => {
      if (!isLoggedIn.value) {
        router.push("/login");
        return;
      }
      replyingTo.value = comment.id;
      replyContent.value = `@${reply.author} `;
    };

    // 取消回复
    const cancelReply = () => {
      replyingTo.value = null;
      replyContent.value = "";
    };

    // 提交回复
    const submitReply = async (comment) => {
      if (!replyContent.value.trim() || isSubmittingReply.value) return;

      isSubmittingReply.value = true;
      try {
        const response = await replyApi.createReply(thread.value.id, {
          content: replyContent.value.trim(),
        });

        if (response.code === 200 && response.data) {
          const reply = {
            id: response.data.id,
            author: `用户${response.data.userId}`,
            authorId: response.data.userId,
            content: response.data.content,
            time: formatTime(response.data.createTime) || "刚刚",
            likes: response.data.likeCount || 0,
            isLiked: false,
          };

          if (!comment.replies) {
            comment.replies = [];
          }
          comment.replies.push(reply);
          replyContent.value = "";
          replyingTo.value = null;
        }
      } catch (error) {
        console.error("发表回复失败:", error);
      } finally {
        isSubmittingReply.value = false;
      }
    };

    // 滚动到评论区
    const scrollToComments = () => {
      const commentsSection = document.querySelector(".comments-section");
      if (commentsSection) {
        commentsSection.scrollIntoView({ behavior: "smooth" });
      }
    };

    // 分享帖子
    const shareThread = () => {
      if (navigator.share) {
        navigator.share({
          title: thread.value.title,
          url: window.location.href,
        });
      } else {
        // 复制链接到剪贴板
        navigator.clipboard.writeText(window.location.href);
        alert("链接已复制到剪贴板");
      }
    };

    // 回到顶部
    const scrollToTop = () => {
      window.scrollTo({
        top: 0,
        behavior: "smooth",
      });
    };

    // 处理滚动事件
    const handleScroll = () => {
      showBackToTop.value = window.scrollY > 300;
    };

    // 获取帖子详情
    const fetchThreadDetail = async () => {
      const threadId = route.params.id;
      if (!threadId) return;

      try {
        const response = await postApi.getPostById(threadId);
        if (response.code === 200 && response.data) {
          const post = response.data;
          thread.value = {
            id: post.id,
            title: post.title,
            author: post.username || `用户${post.userId}`,
            authorId: post.userId,
            authorAvatar: post.authorAvatar || "",
            sectionId: post.sectionId,
            sectionName: post.sectionName || "",
            content: post.content,
            createdAt: post.createTime,
            views: post.viewCount || 0,
            replies: post.replyCount || 0,
            likes: post.likeCount || 0,
            isLiked: post.isLiked || false,
          };

          // 若帖子数据中没有板块名称，则根据 sectionId 单独获取
          if (!thread.value.sectionName && post.sectionId) {
            try {
              const sectionRes = await sectionApi.getSectionById(
                post.sectionId,
              );
              if (sectionRes.code === 200 && sectionRes.data) {
                thread.value.sectionName = sectionRes.data.name || "";
              }
            } catch (sectionError) {
              console.error("获取板块信息失败:", sectionError);
            }
          }
        }
      } catch (error) {
        console.error("获取帖子详情失败:", error);
      }
    };

    // 获取评论列表
    const fetchComments = async () => {
      const threadId = route.params.id;
      if (!threadId) return;

      try {
        const response = await replyApi.getRepliesByPostId(threadId, {
          page: 1,
          size: 50,
        });
        if (response.code === 200 && response.data) {
          comments.value = response.data.records.map((reply) => ({
            id: reply.id,
            author: reply.username || `用户${reply.userId}`,
            authorId: reply.userId,
            authorAvatar: reply.authorAvatar || "",
            content: reply.content,
            time: formatTime(reply.createTime),
            likes: reply.likeCount || 0,
            isLiked: reply.isLiked || false,
            isPinned: false,
            replies: [],
          }));
        }
      } catch (error) {
        console.error("获取评论列表失败:", error);
      }
    };

    onMounted(() => {
      // 进入动画
      setTimeout(() => {
        isEntering.value = false;
      }, 500);

      // 添加滚动监听
      window.addEventListener("scroll", handleScroll);

      // 获取帖子详情
      fetchThreadDetail();
      fetchComments();

      // 设置路由守卫
      navigationGuard = router.beforeEach((to, from, next) => {
        if (from.path.startsWith("/thread/")) {
          isLeaving.value = true;
          setTimeout(() => {
            next();
          }, 500);
        } else {
          next();
        }
      });
    });

    onBeforeUnmount(() => {
      window.removeEventListener("scroll", handleScroll);
      if (navigationGuard) {
        navigationGuard();
      }
    });

    return {
      isLeaving,
      isEntering,
      isLoggedIn,
      userAvatar,
      thread,
      isLikeLoading,
      activeTab,
      sortType,
      newComment,
      isSubmitting,
      replyingTo,
      replyContent,
      isSubmittingReply,
      comments,
      sortedComments,
      showBackToTop,
      handleAvatarClick,
      goHome,
      goToUserProfile,
      goToSection,
      formatTime,
      toggleLike,
      toggleCommentLike,
      toggleReplyLike,
      submitComment,
      replyToComment,
      replyToReply,
      cancelReply,
      submitReply,
      scrollToComments,
      shareThread,
      scrollToTop,
    };
  },
};
</script>

<style scoped>
/* 页面容器 */
.thread-detail-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  position: relative;
}

/* 进入动画 */
.fade-enter-active {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    filter: blur(10px);
  }
  to {
    opacity: 1;
    filter: blur(0);
  }
}

/* 离开动画 */
.fade-leave-active {
  animation: fadeOut 0.5s ease-out forwards;
}

@keyframes fadeOut {
  from {
    opacity: 1;
    filter: blur(0);
  }
  to {
    opacity: 0;
    filter: blur(10px);
  }
}

/* 主内容区域 */
.main-content {
  padding: 20px 0;
}

.main-content .container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 面包屑导航 */
.breadcrumb {
  margin-bottom: 20px;
  font-size: 14px;
  color: #666;
}

.breadcrumb-link {
  cursor: pointer;
  color: var(--primary-color, #203060);
}

.breadcrumb-link:hover {
  text-decoration: underline;
}

.breadcrumb-separator {
  margin: 0 8px;
}

.breadcrumb-current {
  color: #999;
}

/* 帖子详情卡片 */
.thread-detail-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.thread-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
  line-height: 1.4;
}

/* 板块标识 */
.section-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background-color: #f0f0f0;
  border-radius: 4px;
  font-size: 13px;
  color: #666;
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.section-tag:hover {
  background-color: #e0e0e0;
  color: var(--primary-color);
}

.section-tag i {
  font-size: 12px;
}

/* 作者信息区 */
.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  font-size: 48px;
  color: #ccc;
  transition: all 0.3s;
}

.author-avatar:hover {
  transform: scale(1.05);
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-size: 16px;
  font-weight: 500;
  color: var(--primary-color, #203060);
  cursor: pointer;
}

.author-name:hover {
  text-decoration: underline;
}

.post-time {
  font-size: 13px;
  color: #999;
}

/* 帖子内容区 */
.thread-content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 30px;
}

.thread-content :deep(p) {
  margin: 0 0 16px 0;
}

.thread-content :deep(h3) {
  font-size: 18px;
  font-weight: 600;
  margin: 24px 0 12px 0;
  color: #333;
}

.thread-content :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 16px 0;
}

/* 帖子统计 */
.thread-stats {
  display: flex;
  gap: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.stat-item {
  font-size: 14px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 评论区 */
.comments-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 评论标签页 */
.comments-tabs {
  display: flex;
  border-bottom: 2px solid #eee;
  margin-bottom: 16px;
}

.tab-item {
  padding: 12px 20px;
  font-size: 16px;
  color: #666;
  cursor: pointer;
  position: relative;
  transition: color 0.3s;
}

.tab-item:hover {
  color: var(--primary-color, #203060);
}

.tab-item.active {
  color: var(--primary-color, #203060);
  font-weight: 500;
}

.tab-item.active::after {
  content: "";
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background-color: var(--primary-color, #203060);
}

/* 评论排序 */
.comments-sort {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 14px;
}

.sort-option {
  color: #666;
  cursor: pointer;
  transition: color 0.3s;
}

.sort-option:hover,
.sort-option.active {
  color: var(--primary-color, #203060);
}

.sort-separator {
  color: #ddd;
}

/* 评论输入区 */
.comment-input-section {
  margin-bottom: 24px;
}

.comment-input-wrapper {
  display: flex;
  gap: 12px;
}

.current-user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: #ccc;
  flex-shrink: 0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.current-user-avatar:hover {
  transform: scale(1.05);
}

.current-user-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.current-user-avatar.not-logged-in {
  background-color: #e0e0e0;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.current-user-avatar.not-logged-in:hover {
  background-color: #d0d0d0;
  transform: scale(1.05);
}

.login-text {
  font-size: 12px;
  color: #333;
  font-weight: 500;
}

.input-area {
  flex: 1;
}

.input-area textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
  outline: none;
  transition: border-color 0.3s;
}

.input-area textarea:focus {
  border-color: var(--primary-color, #203060);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.input-tip {
  font-size: 12px;
  color: #999;
}

.submit-btn {
  background-color: var(--primary-color, #203060);
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.submit-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.cancel-btn {
  background-color: transparent;
  color: #666;
  border: 1px solid #ddd;
  padding: 8px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel-btn:hover {
  background-color: #f5f7fa;
}

.login-tip {
  text-align: center;
  padding: 30px;
  color: #999;
  font-size: 14px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 24px;
}

/* 评论列表 */
.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  position: relative;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.pinned-badge {
  display: inline-block;
  background-color: #ff6b6b;
  color: white;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  margin-bottom: 8px;
}

.comment-main {
  display: flex;
  gap: 12px;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  font-size: 40px;
  color: #ccc;
  flex-shrink: 0;
  transition: all 0.3s;
}

.comment-avatar:hover {
  transform: scale(1.05);
}

.comment-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.comment-content-wrapper {
  flex: 1;
}

.comment-author {
  font-size: 14px;
  font-weight: 500;
  color: var(--primary-color, #203060);
  cursor: pointer;
  margin-bottom: 6px;
}

.comment-author:hover {
  text-decoration: underline;
}

.comment-text {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 8px;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.action-btn {
  background: none;
  border: none;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: color 0.3s;
  padding: 4px 8px;
  border-radius: 4px;
}

.action-btn:hover {
  color: var(--primary-color, #203060);
  background-color: #f5f7fa;
}

.action-btn.active {
  color: var(--primary-color, #203060);
}

/* 回复列表 */
.replies-list {
  margin-top: 12px;
  padding-left: 0;
}

.reply-item {
  display: flex;
  gap: 10px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 8px;
}

.reply-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e8e8e8;
  font-size: 32px;
  color: #ccc;
  flex-shrink: 0;
  transition: all 0.3s;
}

.reply-avatar:hover {
  transform: scale(1.05);
}

.reply-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.reply-content-wrapper {
  flex: 1;
}

.reply-header {
  margin-bottom: 4px;
}

.reply-author {
  font-size: 13px;
  font-weight: 500;
  color: var(--primary-color, #203060);
  cursor: pointer;
}

.reply-author:hover {
  text-decoration: underline;
}

.reply-to {
  font-size: 13px;
  color: #666;
  margin-left: 4px;
}

.reply-target {
  color: var(--primary-color, #203060);
  cursor: pointer;
}

.reply-target:hover {
  text-decoration: underline;
}

.reply-text {
  font-size: 13px;
  line-height: 1.5;
  color: #333;
  margin-bottom: 6px;
}

.reply-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reply-time {
  font-size: 12px;
  color: #999;
}

/* 回复输入框 */
.reply-input-wrapper {
  margin-top: 12px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.reply-input-wrapper textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 13px;
  resize: vertical;
  outline: none;
  margin-bottom: 8px;
}

.reply-input-wrapper textarea:focus {
  border-color: var(--primary-color, #203060);
}

.reply-actions-row {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 页脚 */
.page-footer {
  background-color: #ffffff;
  padding: 20px 0;
  margin-top: 40px;
  text-align: center;
  color: #666;
  font-size: 14px;
}

/* 右侧悬浮功能区 */
.floating-actions {
  position: fixed;
  right: 30px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 99;
}

.floating-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: white;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
  font-size: 20px;
}

.floating-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.floating-btn.like-btn.active {
  background-color: var(--primary-color, #203060);
  color: white;
}

.floating-btn.like-btn.loading {
  opacity: 0.7;
  cursor: not-allowed;
}

.like-count {
  font-size: 12px;
  margin-top: 2px;
}

.action-label {
  font-size: 11px;
  margin-top: 2px;
}

/* 回到顶部按钮 */
.back-to-top-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: white;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
  font-size: 18px;
  z-index: 99;
}

.back-to-top-btn:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
  color: var(--primary-color, #203060);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .bili-header-bar .container {
    padding: 0 12px;
  }

  .center-search-container {
    margin: 0 20px;
    max-width: none;
  }

  .thread-detail-card {
    padding: 20px;
  }

  .thread-title {
    font-size: 20px;
  }

  .floating-actions {
    right: 15px;
  }

  .floating-btn {
    width: 48px;
    height: 48px;
    font-size: 18px;
  }

  .back-to-top-btn {
    right: 15px;
    bottom: 15px;
  }
}

@media (max-width: 480px) {
  .center-search-container {
    display: none;
  }

  .thread-content {
    font-size: 15px;
  }

  .comment-main {
    gap: 10px;
  }

  .floating-actions {
    bottom: 20px;
    top: auto;
    transform: none;
    flex-direction: row;
    right: 50%;
    transform: translateX(50%);
  }
}
</style>
