import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
    plugins: [vue()],
    server: {
        port: 3000,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
            }
        },
        headers: {
            'Content-Security-Policy': "script-src 'self' 'unsafe-eval';"
        }
    },
    resolve: {
        alias: {
            '@': '/src',
            'buffer': 'buffer'
        }
    },
    build: {
        target: 'es2015',
        minify: 'terser',
        terserOptions: {
            compress: {
                drop_console: true,
                drop_debugger: true
            }
        }
    }
})
