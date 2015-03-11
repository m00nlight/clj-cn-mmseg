(ns clj-cn-mmseg.dict-test
  (:require [clojure.test :refer :all]
            [clj-cn-mmseg.dict :refer :all]
            [clojure.string :as s]))

(deftest test-word->natures
  (testing "testing words with one nature."
    (is (= {:word "哀乐", :nature {:n true}}
           (word->natures "哀乐    名"))))
  (testing "testing words with multiple natures."
    (is (= {:word "挨", :nature {:v true, :prep true}}
           (word->natures "挨      动/介")))
    (is (= {:word "安", :nature {:adj true, :v true, :pron true}}
           (word->natures "安      形/动/代"))))
  (testing "testing words with new line at the end, should trim newline"
    (is (= {:word "挨", :nature {:v true, :prep true}}
           (word->natures "挨      动/介\n")))))



(deftest test-wiki-concept->phrase
  (testing "Testing for english wiki concepts without disambuity"
    (let [res (wiki-concept->phrase "Country_data_Gambia")]
      ;; length should be right
      (is (= 5 (count res)))
      ;; should all be lowercase word
      (is (every? #(= (s/lower-case %) %) res))))
  (testing "Testing concept with disambiguations."
    (let [res (wiki-concept->phrase "Ace_(塔罗牌)")]
      (is (= 1 (count res)))
      (is (= "ace" (first res)))))
  (testing "Testing wiki concept which have multiple space(_ in concept)"
    (let [res (wiki-concept->phrase "J._A._哈普")]
      (is (= 5 (count res)))
      (is (= "j." (first res)))
      (is (= "a." (nth res 2)))))
  (testing "Testing concept with space in it and disambiguations."
    (let [res (wiki-concept->phrase "亨利三世 (神圣罗马帝国)")
          res2 (wiki-concept->phrase "理查·泰勒 (數學家)")]
      (is (= 1 (count res)))
      (is (= "亨利三世" (first res)))
      (is (= 1 (count res2)))
      (is (= "理查·泰勒" (first res2))))))

(deftest test-wikiname->namelist
  (testing "Testing for normal Chinese name without mid dot "
    (is (= '("亨利三世")
           (wikiname->namelist "亨利三世")))
    (is (= '("Angelababy")
           (wikiname->namelist "Angelababy"))))
  (testing "Testing for English name with mid dot in it."
    (is (= '("理查·泰勒")
           (wikiname->namelist "理查·泰勒")))))

(deftest test-netlang->meaning
  (testing "Testing for one to one map."
    (is (= '({:word "LZSB", :nature {:meaning "楼主傻逼"}})
           (netlang->meaning "LZSB <--> 楼主傻逼")))
    (is (= '({:word "深井冰", :nature {:meaning "神经病"}})
           (netlang->meaning "深井冰 <--> 神经病")))
    (is (= '({:word "PPMM", :nature {:meaning "漂亮的美眉/婆婆妈妈"}})
           (netlang->meaning "PPMM <--> 漂亮的美眉/婆婆妈妈"))))
  (testing "Testing for multiple to one map."
    (is (= '({:word "民煮", :nature {:meaning "民主"}}
             {:word "民猪", :nature {:meaning "民主"}}
             {:word "皿煮", :nature {:meaning "民主"}}
             {:word "免煮", :nature {:meaning "民主"}})
           (netlang->meaning "民煮/民猪/皿煮/免煮 <--> 民主")))))


(deftest test-wikiname->redirect
  (testing "Tesing for Chinese name redirect."
    (is (= '({:word "习大大" :nature {:per true :redirect "习近平"}})
           (wikiname->redirect "习大大<-->习近平"))))
  (testing "Tesing English name with space in it."
    (is (= '({:word "Angela Baby" :nature {:per true :redirect "Angelababy"}})
           (wikiname->redirect "Angela Baby<-->Angelababy"))))
  (testing "Tesing English name with mid dot in it."
    (is (= '({:word "托马斯·拉尼尔·威廉斯"
              :nature {:per true :redirect "田纳西·威廉斯"}}
             {:word "托马斯" :nature {:per true}}
             {:word "拉尼尔" :nature {:per true}}
             {:word "威廉斯" :nature {:per true}})
           (wikiname->redirect "托马斯·拉尼尔·威廉斯<-->田纳西·威廉斯"))))
  (testing "Tesing English name with mid dot and one character in it."
    (is (= '({:word "卡尔·A·佩特里"
              :nature {:per true :redirect "卡尔·亚当·佩特里"}}
             {:word "佩特里" :nature {:per true}})
           (wikiname->redirect "卡尔·A·佩特里<-->卡尔·亚当·佩特里")))))

(deftest test-wikiper->per
  (testing "Testing for non-English names with no disambiguations."
    (is (= '("徐昕")
           (wikiper->per "徐昕")))
    (is (= '("佐天仁皇后")
           (wikiper->per "佐天仁皇后"))))
  (testing "Testing for English names with no disambiguations."
    (is (= '("西斯·格瑞辛格" "格瑞辛格")
           (wikiper->per "西斯·格瑞辛格")))
    ;; should drop one characters in English name in result
    (is (= '("皮埃尔·德·顾拜旦" "皮埃尔" "顾拜旦")
           (wikiper->per "皮埃尔·德·顾拜旦"))))
  (testing "Testing for name with space in it."
    (is (= '("50 Cent")
           (wikiper->per "50 Cent")))))


(deftest test-concurrency->map
  (testing "Tesing for mapping concurrency symbols."
    (is (= '({:word "人民币" :nature {:concurrency true
                                      :meaning "人民币"}}
             {:word "¥" :nature {:concurrency true :meaning "人民币"}}
             {:word "RMB" :nature {:concurrency true :meaning "人民币"}})
           (concurrency->map "人民币<-->¥/RMB"))))
  (testing "Testing for mapping concurrency with only one symbol."
    (is (= '({:word "克朗" :nature {:concurrency true
                                    :meaning "克朗"}}
             {:word "Kr" :nature {:concurrency true :meaning "克朗"}})
           (concurrency->map "克朗<-->Kr"))))
  (testing "Testing for mapping concurrency with no symbol."
    (is (= '({:word "卢比" :nature {:concurrency true
                                    :meaning "卢比"}})
           (concurrency->map "卢比")))))

