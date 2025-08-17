import type { RouteRecordRaw } from 'vue-router'

export const testRoutes: Array<RouteRecordRaw> = [
  {
    path: '/test',
    name: 'test',
    component: () => import('../views/general/SimpleTest.vue')
  }
]
