import { createRouter, createWebHistory } from 'vue-router'

import ReversiGame from '@/components/reversi-game.vue'
import ReversiHome from '@/components/reversi-home.vue'

const routes = [
  { path: '/', component: ReversiHome },
  { path: '/game', component: ReversiGame },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
