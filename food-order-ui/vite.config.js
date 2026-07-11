import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  base: '/online-food-order-processing-system/',
  plugins: [react()],
  server: {
    port: 5173
  }
})
