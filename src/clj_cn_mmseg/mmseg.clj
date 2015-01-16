;;;; file : src/clj_cn_mmseg/mmseg.clj

(ns clj-cn-mmseg.mmseg
  "The mmseg namespace  for clj-cn-parser implement the mmseg algorithm 
for Chinese word segmentation.

See <http://technology.chtsai.org/mmseg/> for algrithm details."
  (require [clojure.string :as s]
           [clj-cn-mmseg.trie :as t]
           [clj-cn-mmseg.dict :as d]
           [clojure.java.io :as io]
           [clj-cn-mmseg.normalize :as norm]))

(defn- mean
  "Mean of the collections."
  [coll]
  (if (empty? coll) 0.0 (/ (reduce + 0.0 coll) (count coll))))

(defn- sd
  "Standard deviation of the collection."
  [coll]
  (let [avg (mean coll)]
    (if (empty? coll)
      0.0
      (Math/sqrt (/ (reduce + (map #(* (- % avg) (- % avg)) coll))
                    (count coll))))))

(defn- rule
  "Generalization of the four rules."
  [candidates f min-or-max]
  (if (= (count candidates) 1)
    candidates
    (let [tmp (apply min-or-max (map f candidates))]
      (filter #(< (Math/abs (- (f %) tmp)) 1e-6)
              (map (fn [x] (filter #(not (= % "")) x)) candidates)))))

(defn- rule1
  "Maximum length of chunks rules. A chunk is an list of three
elements tuple, the chunk of every list element is the sum of 
the three words in the chunk.

For example: AAA_BB_CC is AAA_B_CC, since the first tuple has 
length of 7 while the second only 6."
  [candidates]
  (rule candidates (fn [x] (count (apply str x))) max))

(defn- rule2
  "Maximum length of the average length of words in the chunk.

For example: AA_BB_CC is better than AAA_B_C, since the first
tuple has average word length of 2, while the second is 5/3."
  [candidates]
  (rule candidates (fn [x] (mean (map count x))) max))

(defn- rule3
  "Minimum standard deviation of the chunk."
  [candidates]
  (rule candidates (fn [x] (sd (map count x))) min))


(defn- rule4
  "Single characters' freedom sum should be maximum.

For example: AA_BB_C and AA_D_EE, if D is more often used as a 
character in Chinese, we should use the segmentation of AA_D_EE."
  [candidates freq]
  (rule candidates
        (fn [x] (reduce (fn [acc e] (if (= (count e) 1)
                                      (+ acc (Math/log (get freq e 1.0)))
                                      acc)) 0.0 x)) max))

(def freq-map (reduce (fn [acc x]
                        (let [[word freq] (s/split x #"\s+")]
                          (assoc acc word (read-string freq))))
                      {}
                      (s/split (slurp (io/file
                                       (io/resource "CNCharacter.freq")))
                               #"\r*\n")))

(defn- eliminate-candidates
  "Use the four rule, to eliminate the candidates. If the final result,
have multiple candidates, take the first one. Otherwise, take the only
remain candidates for the result."
  [candidates freq]
  (first (-> candidates rule1 rule2 rule3 (rule4 freq))))

(def trie (d/load-dicts))

(defn- candidate-prefix
  "Generate all prefix in the dictionary from the beginning of the texts."
  [trie texts]
  (letfn [(f1 [x stack acc]
            (cons (apply str (reverse (cons x stack)))
                  acc))
          (help [trie xs stack acc]
            (cond
             (empty? xs) acc
             (empty? (trie (first xs))) acc
             ;; current node has natures, so it is a word in dictionary
             (not (empty? (:nature (trie (first xs)))))
             (recur (trie (first xs)) (rest xs) (cons (first xs) stack)
                    (f1 (first xs) stack acc))
            ;; else, current node is not an word in directionary
             :else (recur (trie (first xs)) (rest xs) (cons (first xs) stack)
                          acc)))]
    (let [matches (help trie texts '() '())]
      (if (empty? matches)
        (cons (str (first texts)) '())
        matches))))

(defn gen-candidates
  "Generate all candidates for mmseg to use."
  [trie texts]
  (for [x (candidate-prefix trie texts)
        y (candidate-prefix trie (drop (count x) texts))
        z (candidate-prefix trie (drop (+ (count x) (count y)) texts))]
    [x y z]))

(defn- mmseg-help
  "Help function for MMSeg main function."
  [trie texts acc]
  (if (empty? texts)
    (map (fn [x] {:word x :nature (t/get-natures trie x)})
         (reverse acc))
    (let [candidates (gen-candidates trie texts)
          candidate (-> candidates (eliminate-candidates freq-map))]
      (recur trie (drop (count (first candidate)) texts)
             (cons (first candidate) acc)))))

(defn- combine-english
  "Combine continusous English letters to form English words."
  [words-with-natures]
  (letfn [(all-letters? [s] (not (empty? (re-seq #"^[a-zA-Z]+$" s))))
          (help [words stack acc]
            (if (empty? words)
              (reverse acc)
              (let [[x & xs] words]
                (cond
                 ;; current node all english letters
                 (all-letters? (:word x))
                 (recur xs (cons (:word x) stack) acc)
                 ;; current node is ".", the stack should not empty
                 ;; and the previous should be capital letters.
                 (and (= "." (:word x))
                      (not (empty? stack))
                      (not (empty? xs))
                      (not (empty? (re-seq #"[A-Z]+" (first stack)))))
                 (recur xs (cons (:word x) stack) acc)
                 ;; all other case, cons stack result to acc and
                 ;; current node to acc
                 :else
                 (if (empty? stack)
                   (recur xs '() (cons x acc))
                   (recur xs '()
                          (cons x (cons {:word (apply str (reverse stack))
                                         :nature {:english true}}
                                        acc))))))))]
    (help words-with-natures '() '())))

(defn- combine-num-units-concurrency
  "Combine continuous number follow an unit to form a meaningful phrase."
  [word-with-natures]
  (letfn [(help [words stack acc]
            (if (empty? words)
              (reverse acc)
              (let [[x & xs] words]
                (cond
                 ;; first words is an number or the type of 1,000 style of
                 ;; numbers.
                 (or (get-in x [:nature :num] false)
                     (and (or (= (:word x) ",") (= (:word x) "."))
                          (not (empty? stack))
                          (not (empty? xs))
                          (get-in x [:nature :num] (first xs))))
                 (recur xs (cons (:word x) stack) acc)
                 ;; first word is unit and stack is holding numbers
                 (get-in x [:nature :unit] false) 
                 (recur xs '()
                        (cons {:word (str (apply str (reverse stack))
                                          (:word x))
                               :nature {:unit true}} acc))
                 ;; first word is concurrency and stack is holding numbers.
                 (get-in x [:nature :concurrency] false)
                 (recur xs '()
                        (cons {:word (str (apply str (reverse stack))
                                          (:word x))
                               :nature {:concurrency true,
                                        :meaning
                                        (get-in x [:nature :meaning])}}
                              acc))
                 ;; all other cases
                 :else
                 (if (empty? stack)
                   (recur xs stack (cons x acc))
                   (recur xs '() (cons x (cons
                                          {:word (apply str (reverse stack))
                                           :nature {:num true}}
                                               acc))))))))]
    (help word-with-natures '() '())))




(defn mmseg
  "Main function for MMSeg algorithm."
  [trie texts]
  (-> (mmseg-help trie (norm/fullwidth->halfwidth texts) '())
      combine-num-units-concurrency
      combine-english))


(defn mmseg-seg-only
  [trie texts]
  (let [res (mmseg trie texts)]
    (s/join " " (map :word res) )))
