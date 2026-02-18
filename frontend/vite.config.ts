import { env } from "node:process";
import { fileURLToPath, URL } from "node:url";
import vue from "@vitejs/plugin-vue";
import vueJsx from "@vitejs/plugin-vue-jsx";
import { defineConfig } from "vite";
import vueDevTools from "vite-plugin-vue-devtools";
// import { createRequire } from 'node:module'
// const require = createRequire(import.meta.url);
// const env = loadEnv(mode, process.cwd());

// https://vite.dev/config/
export default defineConfig({
    plugins: [vue(), vueJsx(), vueDevTools()],
    base: env.VITE_BASE_URL,
    resolve: {
        alias: {
            "@": fileURLToPath(new URL("./src", import.meta.url))
        }
    },
    build: {
        rollupOptions: {
            input: ["./index.html"]
        }
    }
});
