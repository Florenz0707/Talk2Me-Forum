import {createApp} from 'vue'
import App from './App.vue'
import router from './router'
import './theme/variables.css'
import {cssVariables} from './theme/config'

// 创建Vue应用实例
const app = createApp(App)

// 应用路由
app.use(router)

// 将CSS变量应用到根元素
const rootElement = document.documentElement
Object.keys(cssVariables).forEach(key => {
    rootElement.style.setProperty(key, cssVariables[key])
})

// 挂载应用
app.mount('#app')
