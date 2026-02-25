<script lang="ts">
export default {
  name: 'App',
}
</script>

<template>
  <div id="app">
    <main class="content">
      <router-view class="glass-card" />
    </main>
  </div>
</template>

<style scoped>
.content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  padding: 2rem 1rem;
  box-sizing: border-box;
}

.glass-card {
  background: #ffffff;
  border-radius: 20px;
  padding: 4rem 2rem;
  width: 90%;
  max-width: 700px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
  box-sizing: border-box;
}

@media (max-width: 768px) {
  .content {
    padding: 0;
    background-color: #f0f0f0;
  }
  .glass-card {
    background-color: #f0f0f0;
    padding: 2rem 1rem;
    border: none;
    border-radius: unset;
    box-shadow: none;
    width: 100%;
  }
}

@media (max-width: 480px) {
  .glass-card {
    background: unset;
  }
}
</style>
