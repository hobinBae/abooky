import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const mainRoutes: Array<RouteRecordRaw> = [
  // ✅ 인트로 진입 (한옥 3D 씬)
  {
    path: '/',
    name: 'intro',
    component: () => import('../views/general/IntroView.vue')
  },
  // ✅ 기존 HomeView는 '/home'으로 이동
  {
    path: '/home',
    name: 'home',
    component: () => import('../views/general/HomeView.vue')
  },
  {
    path: '/bookstore',
    name: 'bookstore',
    component: () => import('../views/books/BookstoreView.vue')
  },
  {
    path: '/create-book',
    name: 'CreateBook',
    component: () => import('../views/books/CreateBookView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/continue-writing',
    name: 'ContinueWriting',
    component: () => import('../views/books/ContinueWritingView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/book-editor/:bookId?',
    name: 'BookEditor',
    component: () => import('../views/books/BookEditorView.vue'),
    props: true,
    meta: { requiresAuth: true }
  },
  {
    path: '/my-library',
    name: 'my-library',
    component: () => import('../views/books/MyLibraryView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/my-page',
    name: 'my-page',
    component: () => import('../views/auth/MyPageView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/auth/LoginView.vue')
  },
  {
    path: '/signup',
    name: 'signup',
    component: () => import('../views/auth/SignupView.vue')
  },
  {
    path: '/password-reset',
    name: 'password-reset',
    component: () => import('../views/auth/PasswordResetView.vue')
  },
  {
    path: '/auth/callback',
    name: 'social-login-callback',
    component: () => import('../views/auth/SocialLoginCallbackView.vue')
  },
  {
    path: '/profile-edit',
    name: 'profile-edit',
    component: () => import('../views/auth/ProfileEditView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/group-book-lobby',
    name: 'group-book-lobby',
    component: () => import('../views/groups/GroupBookLobbyView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/group-book-creation',
    name: 'group-book-creation',
    component: () => import('../views/groups/GroupBookCreationView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/group-book-editor/:groupId/:bookId?',
    name: 'group-book-editor',
    component: () => import('../views/groups/GroupBookEditorView.vue'),
    props: true,
    meta: { requiresAuth: true }
  },
  {
    path: '/book-detail/:id',
    name: 'book-detail',
    component: () => import('../views/books/BookDetailView.vue')
  },
  {
    path: '/bookstore/book/:id',
    name: 'BookstoreBookDetail',
    component: () => import('../views/books/BookstoreBookDetailView.vue'),
    props: true
  },
  {
    path: '/group-book-detail/:groupId/:bookId',
    name: 'group-book-detail',
    component: () => import('../views/groups/GroupBookDetailView.vue'),
    props: true,
    meta: { requiresAuth: true }
  },
  {
    path: '/group-timeline/:id',
    name: 'group-timeline',
    component: () => import('../views/groups/GroupTimelineView.vue')
  },
  {
    path: '/author/:authorId',
    name: 'author-page',
    component: () => import('../views/auth/AuthorPageView.vue')
  },
  {
    path: '/about',
    name: 'about',
    component: () => import('../views/general/AboutView.vue')
  }
]

interface TestModule {
  testRoutes: RouteRecordRaw[]
}

// 개발(DEV) 단계에서만 /test 라우트 등록
// let allRoutes = mainRoutes
// if (import.meta.env.DEV) {
//   const testRouteFiles = import.meta.glob('./test.ts', { eager: true })
//   const testModule = testRouteFiles['./test.ts']
//   if (testModule && typeof testModule === 'object' && 'testRoutes' in testModule) {
//     allRoutes = [...mainRoutes, ...(testModule as TestModule).testRoutes]
//   }
// }

// DEV 제한 제거
const testRouteFiles = import.meta.glob('./test.ts', { eager: true })
const testModule = testRouteFiles['./test.ts']
let allRoutes = mainRoutes
if (testModule && typeof testModule === 'object' && 'testRoutes' in testModule) {
  allRoutes = [...mainRoutes, ...(testModule as TestModule).testRoutes]
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: allRoutes
})

import { useAuthStore } from '@/stores/auth'

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
