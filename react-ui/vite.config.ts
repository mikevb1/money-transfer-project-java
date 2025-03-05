import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import {viteEnvs} from "vite-envs";

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    viteEnvs()
  ],
  build: {
    outDir: 'dist',
  },
  server: {
    port: 8081,  // Specify the port where your app is running
  },
  // define: {
  //   'import.meta.env.VITE_BACKEND_URL': JSON.stringify('http://localhost:8081')
  // }
})
