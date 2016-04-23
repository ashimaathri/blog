(ns blog.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [selmer.parser :refer [render-file cache-off!]]))

(cache-off!)

(defroutes app-routes
  (GET "/" [] (render-file "templates/index.html" {:title "Ashima's Blog"}))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
  (wrap-defaults site-defaults)))
