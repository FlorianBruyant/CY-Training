import type { Composer } from "vue-i18n";
import type { StateStore } from "../stores/state";

// we're adding the store types used in the main.ts
// same for i18n, since eslint doesn't seem to recognize it
declare module "@vue/runtime-core" {
    interface ComponentCustomProperties {
        $state: StateStore;
        $t: Composer["t"];
    }
}

export {};
