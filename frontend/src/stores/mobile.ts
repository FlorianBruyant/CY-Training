import { defineStore } from "pinia";
import { computed, onMounted, ref, watch } from "vue";

export const useMobileStore = defineStore("screen", () => {
    const width = ref<number>(typeof window !== "undefined" ? window.innerWidth : 1024);

    const breakpoint = 800;

    // Update width on resize
    const updateWidth = () => {
        width.value = window.innerWidth;
    };

    if (typeof window !== "undefined") {
        onMounted(() => {
            window.addEventListener("resize", updateWidth);
        });
    }

    // Reactive computed breakpoint
    const isMobile = computed(() => width.value < breakpoint);
    const isDesktop = computed(() => width.value >= breakpoint);

    // Watch and update <body> class automatically
    watch(
        isMobile,
        (mobile) => {
            document.body.classList.toggle("mobile", mobile);
        },
        { immediate: true }
    );

    watch(
        isDesktop,
        (desktop) => {
            document.body.classList.toggle("desktop", desktop);
        },
        { immediate: true }
    );

    return {
        isMobile,
        isDesktop
    };
});

export type MobileStore = ReturnType<typeof useMobileStore>;
