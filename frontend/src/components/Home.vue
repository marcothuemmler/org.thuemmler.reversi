<script setup lang="ts">
import { useHome } from '../composables/Home'

const home = useHome()
</script>

<template>
  <div class="container">
    <h1 class="title">Choose Game Mode</h1>
    <div class="card-container">
      <div class="card" @click="home.goToGame('Singleplayer')">
        <div class="icon">🎮</div>
        <h2>Singleplayer</h2>
      </div>
      <div class="card" @click="home.goToGame('Multiplayer')">
        <div class="icon">👥</div>
        <h2>Multiplayer</h2>
      </div>
    </div>
  </div>
</template>

<style scoped src="../styles/Home.css">
</style>
