(ns clj-cn-mmseg.core
  (require [clj-cn-mmseg.mmseg :as m]
           [clojure.java.io :as io]))

(defn mmseg
  "Segment Chinese text with additional information of the word."
  [^String text]
  (m/mmseg m/trie text))

(defn mmseg-seg-only
  "Only return the segment result without additional information
of the words. The result words is split by space.
Type :: String -> String"
  [^String text]
  (m/mmseg-seg-only m/trie text))


(defn ner
  "Name entity extraction from text. Return an String list, the
name entity will be marked as XX/per, XX/loc or XX/org."
  [text]
  (map (fn [x]
         (if (>= (count (:word x)) 2)
           (cond
            (get-in x [:nature :company]) (str (:word x) "/org")
            (get-in x [:nature :loc]) (str (:word x) "/loc")
            (get-in x [:nature :per]) (str (:word x) "/per")
            :else (str (:word x)))
           (:word x)))
       (-> text mmseg)))
