import { createApp } from 'vue'
import App from './App.vue'
import './style.css' 

// 1. NHẬP ROUTER (Phải tạo file src/router/index.js trước nhé)
import router from './router' 

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import 'bootstrap/dist/css/bootstrap.min.css' 
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 2. KÍCH HOẠT ROUTER (Dòng này sẽ giải quyết lỗi injection)
app.use(router) 

app.use(ElementPlus)
app.mount('#app')