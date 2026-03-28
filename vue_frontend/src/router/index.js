import { createRouter, createWebHistory } from "vue-router";
import UserView from "../views/UserView.vue";
import ColorTest from "../components/ColorTest.vue";
import { getAuthToken } from "../utils/authStorage";

// 检查是否已登录的函数
const isAuthenticated = () => {
  const accessToken = getAuthToken();
  return accessToken !== null && accessToken !== "";
};

const routes = [
  {
    path: "/",
    redirect: "/home",
  },
  {
    path: "/color-test",
    name: "ColorTest",
    component: ColorTest,
  },
  {
    path: "/user",
    name: "User",
    component: UserView,
    meta: { requiresAuth: true }, // 添加路由元信息，表示需要认证
  },
  {
    path: "/user/:id",
    name: "OtherUser",
    component: () => import("../views/OtherUserView.vue"),
  },
  {
    path: "/home",
    name: "Home",
    component: () => import("../views/HomeView.vue"),
  },
  {
    path: "/create-thread",
    name: "CreateThread",
    component: () => import("../views/CreateThreadView.vue"),
    meta: { requiresAuth: true }, // 添加路由元信息，表示需要认证
  },
  {
    path: "/thread/:id",
    name: "ThreadDetail",
    component: () => import("../views/ThreadDetailView.vue"),
  },
  {
    path: "/sections",
    name: "Sections",
    component: () => import("../views/SectionsView.vue"),
  },
  {
    path: "/section/:id",
    name: "Section",
    component: () => import("../views/SectionView.vue"),
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL || "/"),
  routes,
});

// 路由守卫
router.beforeEach((to, from, next) => {
  if (to.matched.some((record) => record.meta.requiresAuth)) {
    // 检查是否已登录
    if (!isAuthenticated()) {
      // 未登录，重定向到首页（通过Header弹窗登录）
      next({ path: "/home" });
    } else {
      // 已登录，继续访问
      next();
    }
  } else {
    // 不需要认证的页面，直接访问
    next();
  }
});

export default router;
