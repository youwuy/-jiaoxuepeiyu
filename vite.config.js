import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://jiao.pei.luoyan.xin',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://jiao.pei.luoyan.xin',
        changeOrigin: true
      }
    }
  },
  test: {
    environment: 'node',
    include: ['tests/**/*.test.ts']
  }
});
