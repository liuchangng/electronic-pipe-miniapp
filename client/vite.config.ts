import { defineConfig } from 'vite'
import { createRequire } from 'module'

const require = createRequire(import.meta.url)
const uniModule = require('@dcloudio/vite-plugin-uni')
const uni = uniModule.default || uniModule

export default defineConfig({
  plugins: [
    uni(),
  ],
})
