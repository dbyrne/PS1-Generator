(ns ps1
  (:use [domina :only [append! by-id destroy! set-value! value]]
        [domina.events :only [listen!]]
        [domina.xpath :only [xpath]])
  (:require [clojure.browser.repl :as repl]))

;; (repl/connect "http://localhost:9000/repl")

(def render-map
  {"\\u" "YourName"
   "\\h" "YourHost"
   "\\w" "/your/dir"
   "\\@" "12:00 AM"
   "\\t" "12:00:00"
   "\\$?" "0"})
                

(def ps1 "export PS1=\"\\[$(tput setaf 4)\\]hello\\[$(tput sgr0)\\]")

(def elements (atom []))
(def last-color (atom -1))

(defn set-output [s]
  (set-value! (by-id "output") s))

(defn dropdown-option [id]
  (let [x (by-id id)]
    (aget (.-options x) (.-selectedIndex x))))

(defn dropdown-value [id]
  (.-value (dropdown-option id)))

(defn dropdown-label [id]
  (.-label (dropdown-option id)))

(defn set-text []
  (let [v (dropdown-value "textType")]
    (set-value! (by-id "arbText") v)
    (if (= v "")
      true
      false)))

(defn clear []
  (reset! elements [])
  (reset! last-color -1)
  (set-output "")
  (destroy! (xpath "//div/*")))

(defn insert []
  (let [color (dropdown-value "colorSelect")
        color-label (dropdown-label "colorSelect")
        text (value (by-id "arbText"))]
    (if (= color @last-color)
      (swap! elements conj text)
      (swap! elements conj (str "\\[$(tput setaf " color ")\\]" text)))

    (reset! last-color color)

    
    (append! (xpath "//div") (str "<span style='color:" color-label "'>" (render-map text text) "</span>"))
    
    (set-output
     (str "export PS1=\"" (apply str @elements) "\\[$(tput sgr0)\\]\";"))))
          
(listen! (by-id "insertButton") :click insert)
(listen! (by-id "clearButton") :click clear)
(set! (.-onchange  (by-id "textType")) set-text)
