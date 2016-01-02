(ns openweathermap-charts-cljs.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<!]]
            [openweathermap-charts-cljs.weather :as w]))

; https://twitter.com/lynaghk/status/384907387787681792?replies_view=true&cursor=AHDCEyx5VwU
(extend-type js/NodeList
  ISeqable
  (-seq [nl] (array-seq nl 0)))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

(def app-state (atom {:cities ["Boston" "Denver" "Tokyo"]}))

(defonce cities-container (.getElementById js/document "cityList"))

(defn clear-children
  "Removes the children of the given parent."
  [parent]
  (set! (.-innerHTML parent) ""))

(defn clear-cities
  []
  (clear-children cities-container))

(defn child-adder
  "Returns a function which adds children to the given parent."
  [parent]
  #(.appendChild parent %))

(defn remove-classes
  [elements]
  (doseq [elem elements]
    (set! (.-className elem) "")))

(defn create-element
  "Utility function for creating a DOM element with attributes."
  [tag attrs]
  (let [el (.createElement js/document tag)]
    (doseq [[k v] (seq attrs)]
      (.setAttribute el k v))
    el))

(defn select-city
  [a]
  (let [parent-node (.-parentNode a)]
    (set! (.-className parent-node) "active")
    (.-innerText a)))

(defn show-data [data-chan]
  (let [map-el (.getElementById js/document "rawData")]
    (go
      (let [forecast (<! data-chan)]
        (set! (.-innerText map-el) forecast)))))

(defn on-click-city
  [click-event]
  (remove-classes (.-childNodes cities-container))
  (let [a (.-target click-event)]
    (let [city (select-city a)]
      (println "Chosen city:" city)
      (show-data (w/weather-channel city)))))

(defn make-city
  "Creates a button for the given city."
  [city]
  (let [li (create-element "li" {"role" "presentation"})]
    (.addEventListener li "click" on-click-city)
    (let [a (create-element "a" {"href" (str "#" city)})]
      (set! (.-innerText a) city)
      ((child-adder li) a)
      li)))

(defn add-cities
  "Main function."
  [cities]
  (let [city-buttons (map make-city cities)]
    (doseq [city city-buttons]
      ((child-adder cities-container) city))))

(defn reset []
  (clear-cities)
  (add-cities (@app-state :cities)))

(add-cities (@app-state :cities))

(defn on-js-reload
  []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  (reset))
