(ns aristotle.server
  "The main entry point into the program. Starts the project server."
  (:require [aristotle.templates.home :as home]
            [aristotle.templates.search :as search-template]
            [aristotle.templates.user-not-found :as unf]
            [aristotle.search :as search]
            [aristotle.config-loader :as config]
            [clojure.edn :as edn])
  (:use [compojure.core :only [defroutes GET POST DELETE ANY context]]
        org.httpkit.server
        ring.middleware.params
        ring.middleware.resource))

(defn search
  "Takes a query and returns the search hiccup/html component data"
  [query page req daemon-name]
  (search-template/render
   (search/go query daemon-name)
   (if (nil? page) 1 page)
   req
   daemon-name))

(def legacy-urls
  {"elliot.daemon.life" "/elliott-hulse"
   "elliott.daemon.life" "/elliott-hulse"
   "collin.daemon.life" "/collin-bell"
   "localhost:8080" "/collin-bell"})

(defn home-route [req]
  (let [host (get-in req [:headers "host"])]
    (println (str host (get legacy-urls host)))
    (if (contains? legacy-urls host)
      {:status 301
       :headers {"Location" (str "http://" host (get legacy-urls host))}})))

(defn daemon-route
  "Returns the full html page with search results included"
  [req]
  (println (get-in req [:headers "host"]))
  (let [{{daemon-name :daemon-name terms "terms" page "page" } :params} req]
    (if (:not-found (config/get-by-name daemon-name))
      {:status 200
       :body (unf/render daemon-name)}
     {:status 200
      :body  (home/render daemon-name
                          (search {:q terms
                                   :start (* 10
                                             (if (nil? page)
                                               0
                                               (- (Integer/parseInt page) 1)))}
                                  (if (nil? page)
                                    1 (Integer/parseInt page))
                                  req
                                  daemon-name))})))
(defroutes all-routes
  (GET "/" [] home-route)
  (GET "/:daemon-name" [] daemon-route))

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
  (reset! server (run-server #'app {:port (config/server :port)})))
