(ns clj.genclass
  (:use [clojure.java.io :only (file)])
  (:import (java.awt Image Graphics2D)
           javax.imageio.ImageIO
           java.awt.image.BufferedImage
           java.awt.geom.AffineTransform))

(defn load-image
  [file-or-path]
  (-> file-or-path file ImageIO/read))  ; => #'clj.genclass/load-image
(defn resize-image
  ^BufferedImage [^Image original factor]
  (let [scaled (BufferedImage. (* factor (.getWidth original))
                               (* factor (.getHeight original))
                               (.getType original))]
    (.drawImage ^Graphics2D (.getGraphics scaled)
                original
                (AffineTransform/getScaleInstance factor factor)
                nil)
    scaled))                            ; => #'clj.genclass/resize-image
;; gen-class
(gen-class
 :name ResizeImage
 :main true
 :methods [^:static [resizeFile [String String double] void]
           ^:static [resize [java.awt.Image double] java.awt.image.BufferedImage]]) ; => nil
(def ^:private -resize resize-image)    ; => #'clj.genclass/-resize
(defn- -resizeFile
  [path outpath factor]
  (ImageIO/write (-> path load-image (resize-image factor))
                 "png"
                 (file outpath))) ; => #'clj.genclass/-resizeFile;
(defn -main
  [& [path outpath factor]]
  (when-not (and path outpath factor)
    (println "Usage: java -jar example-uberjar.jar ResizeImage [INFILE] [OUTFILE] [SCALE]")
    (System/exit 1))
  (-resizeFile path outpath (Double/parseDouble factor))) ; => #'clj.genclass/-main


;;; Custom Exception
(gen-class
 :name clj.genclass.CustomException
 :extends RuntimeException
 :implements [clojure.lang.IDeref]
 :constructors {[java.util.Map String] [String]
                [java.util.Map String Throwable] [String Throwable]}
 :init init
 :state info
 :methods [[getInfo [] java.util.Map]
           [addInfo [Object Object] void]]) ; => nil
(import 'clj.genclass.CustomException)
(defn- -init
  ([info message]
   ;; first is args to call super method, second is this
   [[message] (atom (into {} info))])
  ([info message ex]
   [[message ex] (atom (into {} info))])) ; => #'clj.genclass/-init
(defn- -deref
  [^CustomException this]
  @(.info this))
(defn- -getInfo
  [this]
  @this)
(defn- -addInfo
  [^CustomException this key value]
  (swap! (.info this) assoc key value))


(defn perform-operation
  [& [job priority :as args]]
  (throw (CustomException. {:arguments args} "Operation failed"))) ; => #'clj.genclass/perform-operation

(defn run-batch-job
  [customer-id]
  (doseq [[job priority] {:send-newsletter :low
                          :verify-billings :critical
                          :run-payroll :medium}]
    (try
      (perform-operation job priority)
      (catch CustomException e
        (swap! (.info e) merge {:customer-id customer-id
                                :timestamp (System/currentTimeMillis)})
        (throw e)))))                   ; => #'clj.genclass/run-batch-job
(try
  (run-batch-job 89045)
  (catch CustomException e
    (println "Error!" (.getMessage e) @e)))
