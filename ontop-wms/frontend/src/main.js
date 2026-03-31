import { createApp } from 'vue'
import App from './App.vue'
import './style.css' 

// 1. NHẬP ROUTER (Phải tạo file src/router/index.js trước nhé)
import router from './router' 

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'bootstrap/dist/css/bootstrap.min.css' 
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
// Note: Icons are now imported locally in components for better tree-shaking and build stability.

const app = createApp(App)

// 2. KÍCH HOẠT ROUTER (Dòng này sẽ giải quyết lỗi injection)
app.use(router) 

app.use(ElementPlus)
app.mount('#app')