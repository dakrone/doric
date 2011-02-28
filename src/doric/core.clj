(ns doric.core
  (:refer-clojure :exclude [format name join split])
  (:use [clojure.string :only [join split]])
  (:require [clojure.contrib.string :as s]))

(defn- title-case-word [w]
  (if (zero? (count w))
    w
    (str (Character/toTitleCase (first w))
         (subs w 1))))

(defn title-case [s]
  (join " " (map title-case-word (split s #"\s"))))

(defn align [col & [data]]
  (or (keyword (:align col))
      :left))

(defn format [col & [data]]
  (or (:format col)
      identity))

(defn title [col & [data]]
  (or (:title col)
      (title-case
       (.replaceAll (clojure.core/name (:name col))
                    "-" " "))))

(defn title-align [col & [data]]
  (keyword (or (:title-align col)
               (:align col)
               :center)))

(defn width [col & [data]]
  (or (:width col)
      (apply max (map count (cons (:title col)
                                  (map str data))))))

(defn format-cell [col s]
  (str ((:format col) s)))

(defn align-cell [col s align]
  (let [width (:width col)
        s (s/take width s)
        len (count s)
        pad #(apply str (take % (repeat " ")))
        padding (- width len)
        half-padding (/ (- width len) 2)]
    (case align
          :left (str s (pad padding))
          :right (str (pad padding) s)
          :center (str (pad (Math/ceil half-padding))
                       s
                       (pad (Math/floor half-padding))))))

(defn th [col]
  (align-cell col (:title col) (:title-align col)))

(defn td [col row]
  (align-cell col ((:name col) row) (:align col)))

(defn header [cols]
  (for [col cols]
    (th col)))

(defn body [cols rows]
  (for [row rows]
    (for [col cols]
      (td col row))))

(defn- col-data [col rows]
  (map (:name col) rows))

(defn- column1 [col & [data]]
  {:align (align col data)
   :format (format col data)
   :title (title col data)
   :title-align (title-align col data)})

(defn- columns1 [cols rows]
  (for [col cols]
    (merge col
           (column1 col (col-data col rows)))))

(defn- format-rows [cols rows]
  (for [row rows]
    (into {}
          (for [col cols :let [name (:name col)] ]
            [name (format-cell col (name row))]))))

(defn- column2 [col & [data]]
  {:width (width col data)})

(defn- columns2 [cols rows]
  (for [col cols]
    (merge col
           (column2 col (col-data col rows)))))

(defn render [table]
  (apply str
         (for [tr table]
           (str "| " (join " | " tr) " |\n"))))

(defn table [cols rows]
  (let [cols (columns1 cols rows)
        rows (format-rows cols rows)
        cols (columns2 cols rows)]
    (render (cons (header cols) (body cols rows)))))