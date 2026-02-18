import { defineStore } from "pinia";

export const useStateStore = defineStore("state", {
    state: () => ({
        loading: false,
        error:
            localStorage.getItem("error") ?
                (localStorage.getItem("error") as unknown as Error)
            :   null
    })
});

export type StateStore = ReturnType<typeof useStateStore>;
