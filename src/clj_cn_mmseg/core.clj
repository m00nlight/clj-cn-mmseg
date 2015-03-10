(ns clj-cn-mmseg.core
  (require [clj-cn-mmseg.mmseg :as m]
           [clojure.java.io :as io]))

(defn mmseg
  "Segment Chinese text with additional information of the word."
  [^String text]
  (m/mmseg m/trie text))

(defn mmseg-seg-only
  "Only return the segment result without additional information
of the words."
  [^String text]
  (m/mmseg-seg-only m/trie text))


(defn entity
  [text]
  (map (fn [x]
         (cond
          (get-in x [:nature :company]) (str (:word x) "/org")
          (get-in x [:nature :per]) (str (:word x) "/per")
          (get-in x [:nature :loc]) (str (:word x) "/loc")
          :else (str (:word x))))
       (-> text mmseg)))


(defn entity-test
  [^String input-file ^String output-file]
  (with-open [rdr (io/reader input-file)]
    (with-open [wrdr (io/writer output-file)]
      (doseq [line (line-seq rdr)]
        (let [tmp (entity line)
              ents (filter #(or (.contains % "/per")
                                (.contains % "/loc")
                                (.contains % "/org"))
                           tmp)]
          (doseq [x ents]
            (.write wrdr (str x "\n"))))))))
