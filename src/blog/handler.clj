(ns blog.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.util.response :refer [redirect]]
            [selmer.parser :refer [render-file cache-off!]]
            [clojurewerkz.cassaforte.client :as client]
            [clojure.string :refer [join replace trim]]
            [clojurewerkz.cassaforte.cql :refer :all]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(cache-off!)

(let [session (client/connect ["127.0.0.1"])]
  (create-keyspace session "ashima_blog"
                   (if-not-exists)
                   (with {:replication
                          {"class" "SimpleStrategy"
                           "replication_factor" 1}})))

(let [session (client/connect ["127.0.0.1"])]
  (use-keyspace session "ashima_blog")
  (create-table session :posts
                (if-not-exists)
                (column-definitions {:title :varchar
                                     :content :varchar
                                     :url :varchar
                                     :published_at :timestamp
                                     :primary-key [:url]})))

(defn convert-title-to-url [title]
  (let [pattern #"(\s+)|(\/)"
        dash "-"]
  (replace (trim title) pattern dash)))

(defn create-article [title content]
  (let [session (client/connect ["127.0.0.1"])
        url (convert-title-to-url title)]
    (use-keyspace session "ashima_blog")
    (insert session :posts
            {:title title
             :url url
             :content content
             :published_at (new java.util.Date)})
    url))

(defn get-article [postid]
  (let [session (client/connect ["127.0.0.1"])
        table "posts"]
    (use-keyspace session "ashima_blog")
    (first (select session table (where [[= :url postid]])))))

(defroutes app-routes
  (GET "/" [] (render-file "templates/index.html" {:title "Ashima's Blog"}))

  (GET "/new-article" []
       (render-file
         "templates/new_article.html"
         {:title "Add Article" :anti-forgery-field (anti-forgery-field)}))

  (POST "/articles" [title content]
        (let [url (create-article title content)]
        (redirect (str "/posts/" url))))

  (GET "/posts/:postid" [postid]
       (let [post (get-article postid)]
          (:content post)))

  (route/not-found "Not Found"))

(def app
  (-> app-routes
  (wrap-defaults site-defaults)))
