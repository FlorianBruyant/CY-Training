import { type ApiRequest } from "../models/api.model";

// Base of the url, can change in production, automaticaly given by wite
export const BaseUrl = import.meta.env.BASE_URL;
// URL of the backend, given by the .env files
// in prod, directly call the server
export const APIOrigin = import.meta.env.PROD ? "" : import.meta.env.VITE_BACKEND_ORIGIN;

const Api = {
    test: {
        get: () => {
            // eslint-disable-next-line no-console
            console.log(`This instance is in ${import.meta.env.PROD ? "prod" : "dev"} mode`);
            return sendApiRequest<ApiRequest<{ message: string }>>(
                "GET",
                "test",
                {},
                "Testing " + APIOrigin
            );
        }
    }
};

type Method = "GET" | "POST" | "PUT" | "DELETE";

function sendApiRequest<T = void>(
    method: Method,
    endpoint: string,
    parameters: { [key: string]: unknown } = {},
    message: string | undefined = undefined
): Promise<T> {
    return new Promise(function (resolve, reject) {
        if (message !== undefined) {
            // eslint-disable-next-line no-console
            console.info("[API] " + message);
        }

        const urlParameters = Object.entries(parameters)
            .filter(([k, v]) => k.length != 0 && v != undefined)
            .map(([k, v]) => {
                if (Array.isArray(v) || v instanceof Array) {
                    if (v.length == 0) {
                        return k + "[]=%00";
                    } else {
                        return v
                            .map(
                                (i) =>
                                    k +
                                    "[]=" +
                                    encodeURIComponent(
                                        typeof i === "object" && !Array.isArray(i) && i !== null ?
                                            JSON.stringify(i)
                                        :   i
                                    )
                            )
                            .join("&");
                    }
                } else if (v instanceof Date) {
                    return k + "=" + encodeURIComponent(v.toISOString());
                } else if (v === null) {
                    return k + "[]=%00";
                } else {
                    return k + "=" + encodeURIComponent(v as string);
                }
            })
            .join("&");

        const options: { method: Method; body?: string; headers?: { [keyCodes: string]: string } } =
            { method };

        if (method == "GET") {
            endpoint += "?" + urlParameters;
        } else {
            options.body = urlParameters;
            options.headers = { "Content-Type": "application/x-www-form-urlencoded" };
        }

        fetch(APIOrigin + "/api/" + endpoint, options)
            .then((res) => {
                if (res.headers.get("Content-Type") === "application/json") return res.json();
                else throw new Error("Unexpected response format: " + res.statusText);
            })
            .then(function (response) {
                if (!response.success) {
                    // eslint-disable-next-line no-console
                    console.error("[API] " + response.error);
                    reject(response.error);
                } else {
                    const data = response.data;
                    if (response.count !== undefined) data.count = response.count;
                    resolve(data);
                }
            })
            .catch(reject);
    });
}

// FIXME for future use, when we have passwords
//a function to hash a string with sha256 and return the hash in hex
// eslint-disable-next-line @typescript-eslint/no-unused-vars
async function hash(string: string) {
    const sourceBytes = new TextEncoder().encode(string);
    const disgest = await crypto.subtle.digest("SHA-256", sourceBytes);

    const hash = Array.from(new Uint8Array(disgest))
        .map((b) => b.toString(16).padStart(2, "0"))
        .join("");

    return hash;
}

export default Api;
