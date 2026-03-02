import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig(({ mode }) => {
  // Load environment variables
  const env = loadEnv(mode, process.cwd(), '')
  
  // Get backend port from environment variable or default to 8084
  const backendPort = env.VITE_BACKEND_PORT || '8084'
  const frontendPort = parseInt(env.VITE_FRONTEND_PORT) || 5173
  
  console.log(`Backend Port: ${backendPort}`)
  console.log(`Frontend Port: ${frontendPort}`)

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    },
    server: {
      port: frontendPort,
      host: '0.0.0.0',
      strictPort: false,  // Don't fail if port is occupied, use next available
      proxy: {
        '/api': {
          target: `http://localhost:${backendPort}`,
          changeOrigin: true,
          timeout: 60000,  // Increase timeout for backend connection
          proxyTimeout: 60000,
          retry: 3
        },
        '/uploads': {
          target: `http://localhost:${backendPort}`,
          changeOrigin: true
        }
      }
    }
  }
})
