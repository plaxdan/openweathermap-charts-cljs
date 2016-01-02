(ns openweathermap-charts-cljs.weather
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(defonce
  API-KEY
  "09448515209ecf384f731797c1b4a417")

(defonce
  URL-DAILY-FORECAST
  "http://api.openweathermap.org/data/2.5/forecast/daily?mode=json&q=")

(defn- construct-url
  [city & {:keys [units count]
           :or {units "metric" count 5}}
          cfg]
  (str URL-DAILY-FORECAST city
    "&units=" units
    "&cnt=" count
    "&appid=" API-KEY))

(defn weather-channel
  "Fetches daily forecast data from the server and returns
  a core.async channel. Available options:
  - units ('metric')
  - count (5)"
  [city & cfg]
  (let [url (construct-url city cfg)]
    (http/jsonp url)))
