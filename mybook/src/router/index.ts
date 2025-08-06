import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
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
      path: '/create-book/:bookId?',
      name: 'CreateBookView',
      component: () => import('../views/books/CreateBookView.vue'),
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
      path: '/book-detail/:id',
      name: 'book-detail',
      component: () => import('../views/books/BookDetailView.vue')
    },
    {
      path: '/group-timeline/:id',
      name: 'group-timeline',
      component: () => import('../views/groups/GroupTimelineView.vue')
    },
    {
      path: '/integrated-group-book/:id',
      name: 'integrated-group-book',
      component: () => import('../views/groups/IntegratedGroupBookView.vue')
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
})

router.beforeEach((to, from, next) => {
  const isLoggedIn = !!localStorage.getItem('accessToken');

  if (to.meta.requiresAuth && !isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } });
  } else {
    next();
  }
});

export default router
