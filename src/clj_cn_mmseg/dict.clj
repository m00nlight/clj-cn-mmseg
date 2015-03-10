;;;; file src/clj_cn_mmseg/dict.clj

;;; This file contains related functions with load the multiple dictionary

(ns clj-cn-mmseg.dict
  (require [clojure.string :as s]
           [clojure.tools.logging :as log]
           [clojure.set :as set]
           [clojure.java.io :as io]
           [clj-cn-mmseg.trie :as trie]))


(def natures-map
  {"名" :n, "动" :v, "形" :adj, "介" :prep, "叹" :inter,
   "代" :pron, "副" :adv, })

(defn word->natures
  "Generate the word natures from the Chinese dictionary."
  [word-and-natures]
  (let [[word natures] (s/split (s/trim word-and-natures) #"\s+")]
    {:word word
     :nature (into {} (map #(into [] [(get natures-map % :not-know) true])
                           (s/split natures #"/")))}))


(defn wiki-concept->phrase
  "Get the phrase representation of wiki concept. Wiki use underscore(_) to 
seperate the word in concept phrase, Country_data_Angola for example. And also
parentheses for disambiguation, like Ad_Lib_(字体). So the function will map 
wiki's concept token to the phrase representation."
  [concept]
  (let [words (s/split (-> concept s/trim s/lower-case) #"[_\s]")]
    (if (or (.endsWith (last words) ")")
            (.endsWith (last words) "）"))
      (interpose " " (drop-last words))
      (interpose " " words))))


(defn netlang->meaning
  "Map the network language to their meaningful statement. The network 
languages are stored under the resource/wikipedia_zh_cn.netlang, in the
form AAA/BB/CC <--> DD/EE/FF. "
  [pair-str]
  (let [tmp (s/split (s/trim pair-str) #"<-->")
        words (s/split (s/trim (first tmp)) #"/")]
    (map (fn [x] {:word x :nature {:meaning (-> tmp second s/trim)}}) words)))

(defn wikiper->per
  "Change wikipedia per information, like drop disambiguation information,
split of foreign names into chunks etc."
  [name-str]
  (let [names (s/split (s/trim name-str) #"·")]
    (if (= (count names) 1)
      (cons name-str '())
      (cons name-str (filter #(>= (count %) 3) names)))))

(defn wikiname->namelist
  "Generate a name list from the name entity of wiki, since it may use mid dot
in English names when expressed in Chinese. For example, 理查·泰勒 will generate
'(\"理查·泰勒\", \"理查\", \"泰勒\")"
  [name-str]
  (let [names (s/split (s/trim name-str) #"·")]
    (if (= (count names) 1)
      names
      (cons name-str (filter #(and (>= (count %) 3)
                                   (empty? (re-seq #"^[a-z\.A-Z]+$" %)))
                             names)))))


(defn wikiname->redirect
  "Redirect to the wiki concept of the origin meaning, now only do for 
personal name, 习大大 -> 习近平."
  [name-str]
  (let [[nick, name] (s/split (s/trim name-str) #"<-->")]
    (cons {:word nick :nature {:per true :redirect name}}
          (map (fn [x] {:word x :nature {:per true}})
               (rest (wikiname->namelist nick))))))

(defn wikinick->redirect
  [name-str type]
  (let [[nick ,name ] (s/split (s/trim name-str) #"<-->")]
    {:word nick, :nature {type true, :redirect name}}))

(defn- load-dict-file
  "Help function for load dictionary file."
  [fname line-fn]
  (log/info "Loading " fname " dictionary")
  (map line-fn (s/split (slurp (io/resource fname)) #"\r*\n")))

(defn concurrency->map
  "Map between concurrency symbol and name. The form should be
in the format N<-->S1/S2/S3, which means concurrency with name
N can be used as symbol S1, S2 or S3."
  [concurrency-str]
  (let [[name tmp] (s/split (s/trim concurrency-str) #"<-->")
        symbols (if-not (nil? tmp) (s/split tmp #"/") '())]
    (cons {:word name
           :nature {:concurrency true :meaning name}}
          (map (fn [x] {:word x :nature {:concurrency true, :meaning name}})
               symbols))))

(defn- load-splitonly-dict
  [fname]
  (load-dict-file fname identity))

(defn- load-feature-dict
  [fname]
  (load-dict-file fname word->natures))

(defn- load-type-dict
  "Type is an keyword indicate the type of the dictionary."
  [fname type]
  (load-dict-file fname (fn [x] {:word x :nature {type true}})))

(defn- load-person-name-dict
  [fname]
  (map (fn [x] {:word x :nature {:per true}})
       (flatten (load-dict-file fname wikiper->per))))

(defn- load-person-redirect
  [fname]
  (flatten (load-dict-file fname wikiname->redirect)))

(defn- load-redirect
  [fname type]
  (flatten (load-dict-file fname (fn [x] (wikinick->redirect x type)))))

(defn- load-location-name-dict
  [fname]
  (load-type-dict fname :loc))

(defn- load-idiom-dict
  [fname]
  (load-type-dict fname :idiom))

(defn- load-netlang-dict
  [fname]
  (flatten (load-dict-file fname netlang->meaning)))

(defn- load-concurrency-dict
  [fname]
  (flatten (load-dict-file fname concurrency->map)))


(defn load-dicts
  "Load dicts under the resource folder and build the trie for segment. 
It only take an which is a function, if f is identity, means build the 
trie in prefix style, if it is reverse, means build the trie in suffix
style. The two different trie can do longest-prefix matching segment and
longest-suffix matching segment correspondingly. Default is in the prefix
manner."
  ([] (load-dicts identity))
  ([f]
     (let [per (load-person-name-dict "wikipedia_zh_cn.per")
           per-redirect (load-person-redirect "wikipedia_zh_cn.redirect.per")
           company (load-type-dict "wikipedia_zh_cn.company" :company)
           company-redirect (load-redirect "wikipedia_zh_cn.redirect.company"
                                           :company)
           loc (load-location-name-dict "sogou.loc")
           feature (load-feature-dict "xiandai_feature_lexicon.dict")
           idiom (concat (load-idiom-dict "sogou_idioms.dict")
                         (load-idiom-dict "xiandai_idiom.dict"))
           netlang (load-netlang-dict "wikipedia_zh_cn.netlang")
           seg-only (concat
                     (load-splitonly-dict "sogou_segment_only.dict")
                     (load-splitonly-dict "xiandai_segment_only.dict")
                     (load-splitonly-dict "words.dict"))
           nums (load-type-dict "num.dict" :num)
           units (load-type-dict "all-unit.dict" :unit)
           other-name (load-type-dict "other_name.dict" :name)
           concurrency (load-concurrency-dict "concurrencies.dict")]
       (-> {}
           (trie/build-trie feature f)
           (trie/build-trie per f)
           (trie/build-trie per-redirect f)
           (trie/build-trie company f)
           (trie/build-trie company-redirect f)
           (trie/build-trie loc f)
           (trie/build-trie idiom f)
           (trie/build-trie nums f)
           (trie/build-trie units f)
           (trie/build-trie netlang f)
           (trie/build-trie concurrency f)
           (trie/build-trie other-name f)
           (trie/build-trie seg-only f)))))
