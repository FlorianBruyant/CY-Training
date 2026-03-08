import { createPinia } from "pinia";
import { createApp } from "vue";
import { createI18n } from "vue-i18n";
import en from "./locales/en.json";
import fr from "./locales/fr.json";
import Main from "./Main.vue";
import { createMainRouter } from "./router";
import { useStateStore } from "./stores/state";
import { useThemeStore } from "./stores/theme";
import "./main.scss";

init();

async function init() {
    const app = createApp(Main);

    app.use(createPinia());
    app.use(createMainRouter());

    app.use(
        createI18n({
            legacy: false,
            locale: "fr",
            fallbackLocale: "fr",
            messages: {
                en,
                fr
            }
        })
    );

    app.config.globalProperties.$state = useStateStore();

    const theme = useThemeStore();
    theme.applyTheme();

    app.mount("#app");
}
