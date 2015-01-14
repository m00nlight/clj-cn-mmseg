;;;; file :  src/clj_cn_mmseg/trie.clj

;;; This file contains the function of the trie structure to store the
;;; dictionaries for later nlp task.

(ns clj-cn-mmseg.trie
  (require [clojure.string :as s]
           [clojure.set :as set]))


(defn- add-to-trie
  "Add element to trie. The element can take optional argument which 
specify the nature of the words. Feature should be an string indicate
the property of the word.
Ex:{\\h {\\e {\\l {\\l {\\c {:nature {:n true}}, \\o {:nature {:n true}}}}}}}
The nature hash of the nodes is the nature of the word from root to the 
current node. 
The feature is an hash indicate the nature of the word. If it is ommited,
it will have a default value of {:split-only true}, which indicate the word
is an word for segment only.
"
  ([trie x] (add-to-trie trie x {:split-only true}))
  ([trie x features]
     (assoc-in trie x (merge-with merge (get-in trie x)
                                  {:nature features}))))


(defn- in-trie?
  "Testing whether an element is in the trie structure."
  [trie x]
  (not (empty? (get-in trie x))))

(defn get-natures
  "Get the natures of a word in the trie."
  [trie x]
  (:nature (get-in trie x) {}))


(defn longest-prefix-match
  "Get the longest prefix match of xs from the trie. Return the matched result,
return the cut word result and natures in an hash."
  [trie xs]
  (letfn [(f1 [x stack acc]
            (cons (apply str (reverse (cons x stack)))
                  acc))
          (help [trie xs stack acc]
            (cond
             ;; the input is empty collection, nothing to do
             (empty? xs) acc
             ;; no match any more, return the result
             (empty? (trie (first xs))) acc
             ;; current node has natures, it is a word in the directionary
             (not (empty? (:nature (trie (first xs)))))
             (recur (trie (first xs)) (rest xs) (cons (first xs) stack)
                    (f1 (first xs) stack acc))
             ;; The character is in the trie, but current node is not an leaf
             ;; node of the trie.
             :else (recur (trie (first xs)) (rest xs) (cons (first xs) stack)
                          acc)))]
    (let [matches (help trie xs '() '())]
      {:word (str (if (empty? matches) (first xs) (first matches)))
       :nature (get-natures trie (first matches))})))


(defn build-trie
  "Add the words in coll to existring trie, default build with prefix, if
give the function, it will build the trie in the setting style."
  ([trie coll] (build-trie trie coll identity))
  ([trie coll f]
     (reduce (fn [acc x] (if (:nature x)
                           (add-to-trie acc (f (:word x)) (:nature x))
                           (add-to-trie acc (f x)))) trie coll)))
