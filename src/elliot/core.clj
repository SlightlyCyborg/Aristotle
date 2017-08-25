(ns elliot.core)

(defn get-input []
  (read-line))

(defn change-input-fn [the-fn]
  (defn get-input []
    (the-fn)))

(defn wait-on-user []
  (get-input))

(defn change-wait-on-user-fn [the-fn]
  (defn wait-on-user []
    (the-fn)))

(defn put-output [output]
  (println output))

(defn change-output-fn [the-fn]
  (defn put-output [output]
    (the-fn output)))


(defn create-daemon-img []
  (put-output "Download img of the person")
  (wait-on-user)
  (put-output "Open up gimp")
  (wait-on-user)
  (put-output "Cut image and save as png")
  (wait-on-user)
  (put-output "Put image in resources/public/imgs/daemon_img.png")
  (wait-on-user))


(defn start [])
