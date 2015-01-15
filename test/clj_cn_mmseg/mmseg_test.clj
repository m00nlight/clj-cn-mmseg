(ns clj-cn-mmseg.mmseg-test
  (:require [clojure.test :refer :all]
            [clj-cn-mmseg.mmseg :refer :all]
            [clj-cn-mmseg.test-utils :as tu]
            [clj-cn-mmseg.trie :as t]))

(defn abs-equal
  "Floating point equal function."
  [a, b]
  (< 1e-6 (Math/abs (- a b))))

(tu/with-private-fns [clj-cn-mmseg.mmseg [mean sd rule1 rule2 rule3 rule4
                                           eliminate-candidates
                                           candidate-prefix
                                           gen-candidates]]
  (deftest test-private-mean-function
    (testing "Testing for constant vector"
      (is (= 2.0 (mean [2,2,2,2,2]))))
    (testing "Testing for empty vector, should not produce exception"
      (is (= 0.0 (mean []))))
    (testing "Testing for normal cases."
      (is (= 2.5 (mean [2, 3])))))
  (deftest test-private-sd-function
    (testing "Testing for empty collection, should return 0.0"
      (is (= 0.0 (sd []))))
    (testing "Testing for constant vector, which sd is equal to 0.0"
      (is (= 0.0 (sd [2,2,2,2,2]))))
    (testing "Testing for normal cases."
      (is (= 0.5 (sd [2, 3])))
      (is (abs-equal 1.290994449 (sd [1,2,3,4])))))
  (deftest test-private-rule1
    (testing "Testing private rule1 function"
      (is (= '(("AAA", "BB", "CC"))
             (-> '(("AAA", "B", "CC") ("AAA", "BB", "CC")) rule1)))
      (is (= '(("AAA", "BB", "CC") ("AAA", "B", "CCC"))
             (-> '(("AAA", "BB", "CC") ("AAA", "B", "CC")
                   ("AAA", "B", "CCC")) rule1))))
    (testing "Testing for private rule2 function"
      (is (= '(("AA", "BB", "CC"))
             (-> '(("AA", "BB", "CC") ("AAA", "B", "C")) rule2))))
    (testing "Testing for private rule3 function"
      (is (= '(("AA", "BBB"))
             (-> '(("AA", "BBB") ("AAAA", "B")) rule3))))
    (testing "Testing for private rule4 function."
      (is (= '(("AA", "D", "EE"))
             (-> '(("AA", "BB", "C") ("AA", "D", "EE"))
                 (rule4 {"A" 30, "B" 40, "C" 342, "D" 123432}))))))
  (deftest test-private-eliminate-candidates
    (testing "Tesing result of go through the four rules."
      (is (= '("AA" "B" "CC")
             (eliminate-candidates '(("AA" "DD" "E")
                                     ("AA" "B" "CC")
                                     ("A" "F" "GG"))
                                   {"E" 23, "B" 3432, "F" 15})))))
  (deftest test-private-candidate-prefix
    (let [test-trie (t/build-trie {} '("hello" "hell" "cello" "hel"))]
      (testing "Tesing get candidate with is in the prefix of the trie"
        (is (= '("hello" "hell" "hel")
               (candidate-prefix test-trie "helloaxced")))
        (is (= '("cello")
               (candidate-prefix test-trie "celloar"))))
      (testing "Testing get candidate which is not the prefix of the trie."
        (is (= '(".")
               (candidate-prefix test-trie ".Say hello."))))))
  (deftest test-private-gen-candidates
    (let [test-trie (t/build-trie
                     {} '("hello" "hell" "cell" "hel" "ce" "llo"))]
      (testing "Testing normal case generate candidates."
        (is (= '(("b" "cell" "o")
                 ("b" "ce" "llo"))
               (gen-candidates test-trie "bcelloaaa"))))
      (testing "Testing less words chunks(<=2), with empty string concate"
        (is (= '(("hello", "a" "")
                 ("hell", "o", "a")
                 ("hel", "l", "o"))
               (gen-candidates test-trie "helloa")))))))



(tu/with-private-fns [clj-cn-mmseg.mmseg [combine-num-units-concurrency]]
  (deftest test-private-combine-num-units-concurrency
    (testing "Testing for normal numbers."
      (is (= '({:word "44亿美元"
                :nature {:concurrency true :meaning "美元"}})
             (combine-num-units-concurrency
              '({:word "4" :nature {:num true}}
                {:word "4" :nature {:num true}}
                {:word "亿" :nature {:num true}}
                {:word "美元"
                 :nature {:concurrency true
                          :meaning "美元"}})))))
    (testing "Tesing for number with comma in it"
      (is (= '({:word "43,234亿美元"
                :nature {:concurrency true :meaning "美元"}})
             (combine-num-units-concurrency
              '({:word "4" :nature {:num true}}
                {:word "3" :nature {:num true}}
                {:word "," :nature {:num true}}
                {:word "2" :nature {:num true}}
                {:word "3" :nature {:num true}}
                {:word "4" :nature {:num true}}
                {:word "亿" :nature {:num true}}
                {:word "美元"
                 :nature {:concurrency true
                          :meaning "美元"}})))))
    (testing "Tesing for concurrency symbol"
      (is (= '({:word "43,234$"
                :nature {:concurrency true :meaning "美元"}})
             (combine-num-units-concurrency
              '({:word "4" :nature {:num true}}
                {:word "3" :nature {:num true}}
                {:word "," :nature {:num true}}
                {:word "2" :nature {:num true}}
                {:word "3" :nature {:num true}}
                {:word "4" :nature {:num true}}
                {:word "$"
                 :nature {:concurrency true
                          :meaning "美元"}})))))
    (testing "Testing for floating point numbers."
      (is (= '({:word "2.32%"
                :nature {:unit true}})
             (combine-num-units-concurrency
              '({:word "2" :nature {:num true}}
                {:word "." :nature {:num true}}
                {:word "3" :nature {:num true}}
                {:word "2" :nature {:num true}}
                {:word "%" :nature {:unit true}})))))))
