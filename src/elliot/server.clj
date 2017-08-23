(ns elliot.server
  (:require [elliot.templates.home :as home]
            [elliot.templates.search :as search-template])
  (:use [compojure.route :only [files not-found]]
        [compojure.handler :only [site]] 
        [compojure.core :only [defroutes GET POST DELETE ANY context]]
        [elliot.search :as search]
        org.httpkit.server
        ring.middleware.params
        ring.middleware.resource))

(defn search [query]
  (search-template/render (if (not (nil? (query :q)))
                            (search/go query)
                            {:docs []})))

(defn example-search []
  (search-template/render search-template/mockup-data))

(defn home-route [{{terms "terms"} :params}]
  (println terms)
  {:status 200
   :body  (home/render (search {:q terms}))})

(defn example-route [req]
  {:status 200
   :body (home/render (example-search))})


(defroutes all-routes
  (GET "/" [] home-route)
  (GET "/example" [] example-route))

(def app
  (->  all-routes
       wrap-params
       (wrap-resource "public")))

(defonce server (atom nil))

(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(defn -main [& args]
  ;; The #' is useful when you want to hot-reload code
  ;; You may want to take a look: https://github.com/clojure/tools.namespace
  ;; and http://http-kit.org/migration.html#reload
  (reset! server (run-server #'app {:port 8080})))

