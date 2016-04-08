(defproject til "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.5.3"

  :dependencies [;; Server deps
                 [org.clojure/clojure       "1.8.0"]
                 [org.clojure/core.async    "0.2.374"
                  :exclusions [org.clojure/tools.reader]]
                 [http-kit                  "2.1.21-alpha2"]
                 [ring                      "1.4.0"]
                 [ring/ring-defaults        "0.1.5"]
                 [compojure                 "1.4.0"]
                 [hiccup                    "1.0.5"]
                 [com.taoensso/sente        "1.8.0"]
                 [com.taoensso/timbre       "4.3.0"]
                 [buddy/buddy-auth          "0.11.0"]
                 [buddy/buddy-hashers       "0.13.0"]
                 [org.postgresql/postgresql "9.4.1207"]
                 [com.layerware/hugsql      "0.4.5"]
                 [environ                   "1.0.2"]
                 [heroku-database-url-to-jdbc "0.2.2"]


                 ;; Client deps
                 [org.clojure/clojurescript "1.7.170"]
                 [cljsjs/marked             "0.3.5-0"]
                 [cljsjs/moment             "2.10.6-2"]
                 [cljsjs/jquery             "2.1.4-0"]
                 [cljsjs/hashids            "1.0.2-0"]
                 [reagent                   "0.5.1"]
                 [re-frame                  "0.7.0"]
                 [secretary                 "1.2.3"]]

  :plugins [[lein-figwheel "0.5.0-6"]
            [lein-cljsbuild "1.1.2" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src/clj"]

  :main til.server

  :uberjar-name "til.jar"

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src/cljs"]

                ;; If no code is to be run, set :figwheel true for continued automagical reloading
                :figwheel true

                :compiler {:main til.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/til.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}
               ;; This next build is an compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/compiled/til.js"
                           :main til.core
                           :optimizations :advanced
                           :externs ["resources/externs/window.js"]
                           :pretty-print false}}]}

  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1"

             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             :ring-handler til.server/handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"
             }
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.10"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}

             :uberjar {:prep-tasks ["compile" ["cljsbuild" "once" "min"]]
                       :env {:production true}
                       :aot :all
                       }})
