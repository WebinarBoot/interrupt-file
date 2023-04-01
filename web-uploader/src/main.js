// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'

Vue.config.productionTip = false
import uploader from 'vue-simple-uploader'

import DatePicker from "ant-design-vue";
import 'ant-design-vue/dist/antd.css'; // or 'ant-design-vue/dist/antd.less'

Vue.use(DatePicker)
Vue.use(uploader)
/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: { App },
  template: '<App/>'
})
