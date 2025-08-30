<script>
import Game from './components/Game.vue'
import Home from './components/Home.vue'

export default {
  name: 'App',
  components: {
    Home,
    Game,
  },
}
</script>

<template>
  <div id="app">
    <nav class="navbar">
      <router-link class="nav-link" to="/">Home</router-link>
      <router-link class="nav-link" to="/game">Game</router-link>
    </nav>
    <main class="content">
      <router-view class="glass-card"></router-view>
    </main>
  </div>
</template>

<style scoped>

.navbar {
  display: flex;
  justify-content: center;
  gap: 2rem;
  width: 100%;
  padding: 1rem 0;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 0 0 12px 12px;
}

.nav-link {
  text-decoration: none;
  font-weight: 600;
  font-size: 1.1rem;
  padding: 0.5rem 1rem;
  border-radius: 12px;
  color: #333;
  transition: color 0.3s ease;
}

.nav-link:hover {
  color: #4f46e5;
}

.content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  padding: 3rem 1rem;
}

.glass-card {
  background: #ffffff;
  border-radius: 20px;
  padding: 4rem 2rem 4rem 2rem;
  width: 100%;
  max-width: 700px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
}

@media (max-width: 600px) {
  .nav-link {
    font-size: 1rem;
    padding: 0.4rem 0.8rem;
  }
  .glass-card {
    padding: 1.5rem;
  }
}
</style>
