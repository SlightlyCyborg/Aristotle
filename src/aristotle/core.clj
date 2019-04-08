(ns aristotle.core
  (:require [aristotle.config-loader :as config]
            [aristotle.server :as server]
            [aristotle.youtube-captions-downloader :as yt]
            [aristotle.video :as v]))

(defn parse-number
  "Reads a number from a string. Returns nil if not a number."
  [s]
  (if (re-find #"^-?\d+\.?\d*$" s)
    (read-string s)))

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

(defn get-captions []
  (put-output (str "Spitting out the URLs for "
                   (config/all :youtube-channel)))
  (yt/write-channel-video-urls)
  (put-output (str "Remove GoogleSRT cache.\n rm -rf .google2srt/"))
  (wait-on-user)
  (put-output "Open GoogleSRT.")
  (wait-on-user)
  (put-output (str "Navigate to video_ids/" (config/all :daemon-name) " and download the subs using the GoogleSRT tool"))
  (wait-on-user)
  (yt/gen-captions-to-delete)
  (put-output "Navigate to the subs folder and run:\n    chmod +x rm-script.sh\n    ./rm-script.sh")
  (wait-on-user))

(defn create-daemon-img []
  (put-output "Download img of the person")
  (wait-on-user)
  (put-output "Open up gimp")
  (wait-on-user)
  (put-output "Cut image and save as png")
  (wait-on-user)
  (put-output "Put image in resources/public/imgs/daemon_img.png")
  (wait-on-user))

(defn make-new-config [daemon-name]
  (put-output (str "Open up resources/" daemon-name "/config.edn"))
  (wait-on-user)
  (put-output "Edit the config options including:\n:solr-connection\n:solr-block-connection\n:daemon-name\n:youtube-channel\n:headline\n:srt-source-folders")
  (wait-on-user)
  (put-output (str "Edit the :port to be " (+ 1 (config/all :port))))
  (wait-on-user)
  (put-output "Save the config.edn")
  (wait-on-user)
  (put-output "Reloaded config")
  (wait-on-user))

(defn start-server []
  (put-output (str "Starting sever on localhost:" (config/all :port)))
  (server/-main))

(defn create-solr-cores []
  (put-output "SSH into the solr server")
  (wait-on-user)
  (put-output "Navigate to /usr/local/solr/solr-6.6.0/bin")
  (wait-on-user)
  (put-output "$ su solr")
  (wait-on-user)
  (put-output (str "Type the following:\n./solr create -c "
                   (get-in config/all [:solr-connection :core])
                   "\n./solr create -c "
                   (get-in config/all [:solr-block-connection :core])))
  (wait-on-user)
  (put-output "exit")
  (put-output "exit"))

(defn index-docs []
  (put-output "Double check that the settings in config are correct\nIncorrect settings could mess up other indicies")
  (wait-on-user)
  (v/index-all-videos)
  (v/index-all-video-blocks)
  (put-output "Finished indexing!"))

(defn transfer-non-git-files []
  (put-output "scp over the following files:")
  (put-output "   resources/public/imgs/daemon_img.png")
  (put-output "   resources/config.edn"))


(defn start [daemon-name]
  (create-daemon-img)
  (make-new-config daemon-name)
  (create-solr-cores)
  (get-captions)
  (index-docs)
  (transfer-non-git-files))
