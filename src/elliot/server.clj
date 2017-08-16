(ns elliot.server
  (:use [compojure.route :only [files not-found]]
        [compojure.handler :only [site]] 
        [compojure.core :only [defroutes GET POST DELETE ANY context]]
        org.httpkit.server
        ring.middleware.params))

(defn home [req]
  {:status 200})

(defroutes all-routes
  (GET "/" [] home))

(def app
  (wrap-params all-routes))

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

