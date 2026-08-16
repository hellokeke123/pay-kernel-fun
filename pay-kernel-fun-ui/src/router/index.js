import Vue from 'vue'
import VueRouter from 'vue-router'
import BasicLayout from '@/layout/BasicLayout.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    component: BasicLayout,
    redirect: '/goods',
    children: [
      { path: 'goods', name: 'GoodsManage', component: () => import('@/views/GoodsManage.vue'), meta: { title: '商品管理' } },
      { path: 'orders', name: 'OrderManage', component: () => import('@/views/OrderManage.vue'), meta: { title: '订单管理' } },
      { path: 'trade-lab', name: 'TradeLab', component: () => import('@/views/TradeLab.vue'), meta: { title: '交易测试台' } }
    ]
  }
]

const router = new VueRouter({
  mode: 'hash',
  routes
})

export default router
