import { createRouter, createWebHistory } from 'vue-router'

// 기존 페이지들
import IntroView from '../views/general/IntroView.vue'
import HomeView from '../views/general/HomeView.vue'
import BookstoreView from '../views/books/BookstoreView.vue'
import CreateBookView from '../views/books/CreateBookView.vue'
import MyLibraryView from '../views/books/MyLibraryView.vue'
import LoginView from '../views/auth/LoginView.vue'
import SignupView from '../views/auth/SignupView.vue'
import MyPageView from '../views/auth/MyPageView.vue'
import PasswordResetView from '../views/auth/PasswordResetView.vue'
import ProfileEditView from '../views/auth/ProfileEditView.vue'
import GroupBookshelfView from '../views/groups/GroupBookshelfView.vue'
import GroupBookCreationView from '../views/groups/GroupBookCreationView.vue'
import BookDetailView from '../views/books/BookDetailView.vue'
import GroupTimelineView from '../views/groups/GroupTimelineView.vue'
import IntegratedGroupBookView from '../views/groups/IntegratedGroupBookView.vue'
import AuthorPageView from '../views/auth/AuthorPageView.vue'
import AboutView from '../views/general/AboutView.vue'
import CommunityView from '../views/general/CommunityView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ✅ 인트로 진입 (한옥 3D 씬)
    {
      path: '/',
      name: 'intro',
      component: IntroView
    },
    // ✅ 기존 HomeView는 '/home'으로 이동
    {
      path: '/home',
      name: 'home',
      component: HomeView
    },
    {
      path: '/bookstore',
      name: 'bookstore',
      component: BookstoreView
    },
    {
      path: '/create-book/:bookId?',
      name: 'CreateBookView',
      component: CreateBookView,
      props: true
    },
    {
      path: '/my-library',
      name: 'my-library',
      component: MyLibraryView
    },
    {
      path: '/my-page',
      name: 'my-page',
      component: MyPageView
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/signup',
      name: 'signup',
      component: SignupView
    },
    {
      path: '/password-reset',
      name: 'password-reset',
      component: PasswordResetView
    },
    {
      path: '/profile-edit',
      name: 'profile-edit',
      component: ProfileEditView
    },
    {
      path: '/group-bookshelf',
      name: 'group-bookshelf',
      component: GroupBookshelfView
    },
    {
      path: '/group-book-creation',
      name: 'group-book-creation',
      component: GroupBookCreationView
    },
    {
      path: '/book-detail/:id',
      name: 'book-detail',
      component: BookDetailView
    },
    {
      path: '/group-timeline/:id',
      name: 'group-timeline',
      component: GroupTimelineView
    },
    {
      path: '/integrated-group-book/:id',
      name: 'integrated-group-book',
      component: IntegratedGroupBookView
    },
    {
      path: '/author/:authorId',
      name: 'author-page',
      component: AuthorPageView
    },
    {
      path: '/about',
      name: 'about',
      component: AboutView
    },
    {
      path: '/community',
      name: 'community',
      component: CommunityView
    }
  ]
})

export default router
