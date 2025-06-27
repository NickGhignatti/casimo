import {defineConfig} from "vite";
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";

export default defineConfig({
    base: "/casimo/deploy/",
    plugins: [scalaJSPlugin()],
})