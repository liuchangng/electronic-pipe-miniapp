import { defineConfig } from 'vitest/config'
import { fileURLToPath } from 'url'
import vue from '@vitejs/plugin-vue'

const __dirname = fileURLToPath(new URL('.', import.meta.url))

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': `${__dirname}src`,
    },
  },
  test: {
    environment: 'node',
    globals: true,
    include: ['src/__tests__/**/*.test.ts'],
    exclude: ['node_modules/**'],
    setupFiles: ['src/__tests__/setup.ts'],
  },
})