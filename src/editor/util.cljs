;
; A small utility library that is being built up for use with Node-Webkit apps
;
; The name needs to be changed later to reflect that it's more of a generic utility
; lib for node-webkit apps and not necessarily restricted to just the editor.
;
; Frank Hale <frankhale@gmail.com>
; http://github.com/frankhale/editor
; 9 May 2014
;

(ns editor.util
  (:require [jayq.core :as jq]))

; File related

(def fs (js/require "fs"))

(defn open-about-page []
  (str (.readFileSync fs "About.md")))

(defn read-file-sync [file]
  (.readFileSync fs (.-path file)))

(defn write-file-sync [file-path content]
  (.writeFileSync fs file-path content))

; NW.js related

(def nw-gui (js/require "nw.gui"))
(def window (.get (.-Window nw-gui)))

(defn log [msg]
  (.log js/console msg))

(defn nw-refresh []
  (.reload window))

(defn show-nw-dev-tools []
  (.showDevTools window))

(defn watch-window-close-event [func]
  (.on window "close" (fn []
                        (func)
                        (.close window true))))

; Collection related

; borrowed from: http://stackoverflow.com/a/8642069/170217
(defn indices [pred coll]
   (keep-indexed #(when (pred %2) %1) coll))

(defn find-map [m k v]
  "Finds a map within a vector of maps based on a key and value"
  (filter #(if (= (k %) v) %) m))

(defn find-map-without [m k v]
  "Finds all maps within a vector of maps that doesn't correspond to a key and value"
  (filter #(if (not= (k %) v) %) m))

(defn starts-with [a b]
  (first (map = a b)))

(defn without [elem coll]
  (filter #(if-not (= elem %) %) coll))

; borrowed from: http://stackoverflow.com/a/3249777/170217
(defn in?
  "true if seq contains elm"
  [seq elm]
  (some #(= elm %) seq))

; uses jayq (jQuery helper lib)

(defn create-option [elem val]
  (jq/append elem (str "<option value='" val "'>" val "</option>")))

(defn fill-select-with-options [elem items]
  (doall (map #(create-option elem %) items)))

(defn bind-element-event [elem event callback]
  "Binds a DOM element on change to a callback"
  (jq/bind elem event
           (fn [e] (this-as selector (callback selector)))))
