(ns elliot.server
  "The main entry point into the program. Starts the project server."
  (:require [elliot.templates.home :as home]
            [elliot.templates.search :as search-template]
            [elliot.search :as search])
  (:use [compojure.core :only [defroutes GET POST DELETE ANY context]]
        org.httpkit.server
        ring.middleware.params
        ring.middleware.resource))

(defn search
  "Takes a query and returns the search hiccup/html component data"
  [query]
  (search-template/render (search/go query)))

(defn home-route
  "Returns the full html page with search results included"
  [{{terms "terms"} :params}]
  {:status 200
   :body  (home/render (search {:q terms}))})

(defroutes all-routes
  (GET "/" [] home-route))

(def app
  (->  all-routes
       wrap-params
       (wrap-resource "public")))

(defonce server (atom nil))

(defn stop-server
  "Stops server, when an http-kit server named `server` exists"
  []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(defn -main
  "Starts or restarts server"
  [& args]
  (if (not (nil? @server))
    (stop-server))
  (reset! server (run-server #'app {:port 8080})))
