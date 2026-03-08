import { useMediaQuery } from "@vueuse/core";
import { defineStore } from "pinia";

export type Theme = "light" | "dark" | "system";
export type ThemeColor = "light" | "dark";

const STORAGE_KEY = "theme";

export const useThemeStore = defineStore("theme", {
    state: () => ({
        theme: (localStorage.getItem(STORAGE_KEY) as Theme) || "system",
        color: document.documentElement.dataset.theme as ThemeColor
    }),

    actions: {
        setTheme(theme: Theme) {
            this.theme = theme;
            localStorage.setItem(STORAGE_KEY, theme);
            this.applyTheme();
        },

        applyTheme() {
            const root = document.documentElement;

            if (this.theme === "system") {
                const prefersDark = useMediaQuery("(prefers-color-scheme: dark)").value;
                root.dataset.theme = prefersDark ? "dark" : "light";
            } else {
                root.dataset.theme = this.theme;
            }
        }
    }
});

export type ThemeStore = ReturnType<typeof useThemeStore>;
