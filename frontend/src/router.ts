import { createRouter, createWebHistory } from "vue-router";
import { useStateStore } from "./stores/state";

const SITE_TITLE = "CY Training";

export function createMainRouter() {
    const router = createRouter({
        history: createWebHistory(""),
        routes: [
            {
                path: "/",
                name: "index",
                component: () => import("./views/IndexView.vue"),
                meta: {
                    title: SITE_TITLE
                }
            },
            {
                path: "/:pathMatch(.*)*",
                name: "404",
                component: () => import("./views/errors/404View.vue"),
                meta: {
                    title: "404 - " + SITE_TITLE
                }
            }
        ]
    });

    const state = useStateStore();

    router.beforeEach((to, _from, next) => {
        document.title = to.meta.title as string;
        state.error = null;
        state.loading = true;
        next();
    });

    router.afterEach(() => {
        state.loading = false;
    });

    router.onError((error) => {
        state.error = error;
        state.loading = false;
    });

    return router;
}
