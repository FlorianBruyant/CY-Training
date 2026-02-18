<script lang="ts">
    export default {
        props: {
            buttonSubmit: {
                type: [String, null],
                default: "Confirmer",
                validator(v: string | null) {
                    return v == null || v.length > 0;
                }
            },
            buttonCancel: {
                type: [String, null],
                default: null
            },
            isDanger: {
                type: Boolean,
                default: false
            },
            // get the name of the icon on https://fonts.google.com/icons
            buttonIcon: {
                type: [String, null],
                default: "help",
                validator(v: string | null) {
                    return v == null || v.length > 0;
                }
            },
            buttonText: {
                type: [String, null],
                default: null,
                validator(v: string | null) {
                    return v == null || v.length > 0;
                }
            },
            hideBackground: {
                type: Boolean,
                default: true
            }
        },
        emits: ["response"],
        computed: {
            customDialog() {
                return this.$refs.customDialog as HTMLDialogElement;
            }
        },
        methods: {
            getResponse(value: boolean) {
                this.customDialog.close();
                this.$emit("response", value);
            }
        }
    };
</script>

<template>
    <button
        type="button"
        v-bind="$attrs"
        :class="{ 'hide-background': hideBackground, 'show-background': !hideBackground }"
        @click.prevent="customDialog.showModal()">
        <p>
            <span class="material-symbols-rounded">{{ buttonIcon }}</span>
            {{ buttonText }}
        </p>
    </button>
    <dialog id="customDialog" ref="customDialog" @click.prevent="getResponse(false)">
        <slot></slot>
        <div class="button-container">
            <button
                v-if="buttonCancel != null && buttonCancel.length > 0"
                type="button"
                autofocus
                @click.prevent="getResponse(false)">
                {{ buttonCancel }}
            </button>
            <button
                v-if="buttonSubmit != null"
                type="submit"
                :class="{ danger: isDanger }"
                @click.prevent="getResponse(true)">
                {{ buttonSubmit }}
            </button>
        </div>
    </dialog>
</template>

<style scoped lang="scss">
    .show-background {
        p {
            margin: 0;
        }
    }

    .hide-background {
        background-color: transparent;
        box-shadow: none;
        width: min-content;
        height: 32px;
        margin: 0 4px 0 0;
        padding: 4px;

        p {
            display: flex;
            gap: 0.2em;
            flex-direction: row;
            justify-content: center;
            align-content: center;
            align-items: center;
            margin: 0 auto;
            padding: 0;
            text-align: center;
            font-weight: normal;
            font-style: normal;
            line-height: 1;
            letter-spacing: normal;
            text-transform: none;
            display: inline-block;
            white-space: nowrap;
            word-wrap: normal;
            direction: ltr;
        }
    }

    .button-container {
        display: flex;
        flex-direction: column;
        justify-content: end;
        gap: 1em;
    }

    .danger {
        background-color: red;
        font-size: bold;
    }
</style>
