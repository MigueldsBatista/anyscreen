<script setup lang="ts">
import { store } from '@/store/index';

const increment = () => {
  store.commit('increment');
  console.log(store.state.count); // -> 1

};

</script>

<template>
  <div>hello world vue</div>
  <button @click="increment"> Press here to increment the store: {{ store.state.count }} </button>
</template>

<style scoped>

</style>
