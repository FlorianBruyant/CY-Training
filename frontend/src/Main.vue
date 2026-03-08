<script lang="ts">
    import { RouterLink, RouterView } from "vue-router";
    import { type MobileStore, useMobileStore } from "./stores/mobile";
    import Api from "./utils/api";
    // import type { StateStore } from "./stores/state";

    const errors: { [key: string]: { code: string } } = {
        "Failed to fetch": {
            code: "error.fetch"
        },
        "Unexpected token .* is not valid JSON": {
            code: "error.server"
        },
        "Not found": {
            code: "error.notFound.page"
        },
        "TypeError": {
            code: "dev.typeError"
        }
    };

    export default {
        components: {
            RouterLink,
            RouterView
        },
        setup() {
            const mobileStore = useMobileStore();
            return { mobileStore };
        },
        computed: {
            error() {
                const $state = this.$state;
                // console.error($state);
                if ($state === null || $state === undefined || $state.error === null) return null;

                for (const [regex, message] of Object.entries(errors)) {
                    if ($state.error.toString().match(new RegExp(regex))) {
                        return { message: this.$t(message.code) as string };
                    }
                }

                return { message: $state.error.name + "\n" + $state.error.message };
            }
        },
        watch: {
            $route() {
                document.getElementById("site-header")?.classList.remove("open");
                document.body.classList.remove("nav-open");
            },
            mobileStore(oldValue, newValue: MobileStore) {
                if (newValue.isMobile) document.body.classList.add("mobile");
                else document.body.classList.remove("mobile");
            }
        },
        methods: {
            toggleNav() {
                document.getElementById("site-header")?.classList.toggle("open");
                document.body.classList.toggle("nav-open");
            },
            async test() {
                await Api.test.get();
            }
        }
    };
</script>

<template>
    <header id="site-header">
        <p>Header</p>
        <button @click="test()">Click me to test API!</button>
    </header>
    <main>
        <RouterView v-if="!error" v-slot="{ Component }">
            <KeepAlive>
                <component :is="Component" />
            </KeepAlive>
        </RouterView>
        <section v-else class="error">
            <span v-for="message in error.message.split('\n')" :key="message">{{ message }}</span>
        </section>
    </main>
    <footer>
        <p>Footer</p>
    </footer>
</template>

<style lang="scss" scoped>
    .error {
        display: flex;
        flex-direction: row;
    }

    main {
        flex: 1;
    }
</style>
