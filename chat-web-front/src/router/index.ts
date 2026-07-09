import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Register.vue')
    },
    {
      path: '/',
      component: () => import('@/layout/MainLayout.vue'),
      redirect: '/chat',
      children: [
        {
          path: 'chat',
          name: 'chat',
          component: () => import('@/views/ChatList.vue')
        },
        {
          path: 'chat/p/:userId',
          name: 'private-chat',
          component: () => import('@/views/ChatWindow.vue'),
          props: (route) => ({
            chatType: 'private',
            chatId: Number(route.params.userId),
            chatName: (route.query.name as string) || ''
          })
        },
        {
          path: 'chat/g/:groupId',
          name: 'group-chat',
          component: () => import('@/views/ChatWindow.vue'),
          props: (route) => ({
            chatType: 'group',
            chatId: Number(route.params.groupId),
            chatName: (route.query.name as string) || ''
          })
        },
        {
          path: 'contacts',
          name: 'contacts',
          component: () => import('@/views/Contacts.vue')
        }
      ]
    }
  ]
})

router.beforeEach((to, _from) => {
  const token = localStorage.getItem('token')
  const isAuthPage = to.path === '/login' || to.path === '/register'

  if (!token && !isAuthPage) {
    return '/login'
  }
  if (token && isAuthPage) {
    return '/chat'
  }
})

export default router
