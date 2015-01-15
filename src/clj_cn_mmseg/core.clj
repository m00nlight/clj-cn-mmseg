(ns clj-cn-mmseg.core
  (require [clj-cn-mmseg.mmseg :as m]))

(defn mmseg
  "Segment Chinese text with additional information of the word."
  [^String text]
  (m/mmseg m/trie text))

(defn mmseg-seg-only
  "Only return the segment result without additional information
of the words."
  [^String text]
  (m/mmseg-seg-only m/trie text))
