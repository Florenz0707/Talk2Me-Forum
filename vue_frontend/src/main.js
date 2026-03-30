import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import "./theme/variables.css";
import "./theme/shared.css";
import { cssVariables } from "./theme/config";
import { authApi } from "./utils/api";

// 创建Vue应用实例
const app = createApp(App);

// 应用路由
app.use(router);

// 将CSS变量应用到根元素
const rootElement = document.documentElement;
Object.keys(cssVariables).forEach((key) => {
  rootElement.style.setProperty(key, cssVariables[key]);
});

if (typeof window !== "undefined") {
  window.__TALK2ME_AUTH_DEBUG__ = {
    getSnapshot: () => authApi.getAuthDebugInfo(),
    printSnapshot: () => authApi.debugAuth("window"),
    getResolvedUserId: () => authApi.getUserIdFromToken(),
    getTokenPayload: () => authApi.getTokenPayload(),
  };
}

// 挂载应用
app.mount("#app");
