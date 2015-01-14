(ns clj-cn-mmseg.trie-test
  (:require [clojure.test :refer :all]
            [clj-cn-mmseg.trie :refer :all]
            [clj-cn-mmseg.test-utils :as tu]))

(tu/with-private-fns [clj-cn-mmseg.trie [add-to-trie in-trie?]]
  (deftest test-add-to-trie
    (testing "testing insert into empty trie"
      (is (= {\h {\e {\l {\l {\o {:nature {:split-only true}}}}}}}
             (add-to-trie {} "hello"))))
    (testing "testing insert into empty trie with feature"
      (is (= {\h {\e {\l {\l {\o {:nature {:n true}}}}}}}
             (add-to-trie {} "hello" {:n true}))))
    (testing "testing insert duplicate keys, should not duplicate in trie"
      (is (= {\h {\e {\l {\l {\o {:nature {:n true}}}}}}}
             (add-to-trie (add-to-trie {} "hello" {:n true})
                          "hello" {:n true}))))
    (testing "testing insert same word with different features"
      (is (= {\h {\e {\l {\l {\o {:nature {:v true, :n true}}}}}}}
             (add-to-trie (add-to-trie {} "hello" {:n true})
                          "hello" {:v true}))))
    (testing "testing insert different words"
      (is (= {\h {\e {\l {\l {\c {:nature {:n true}},
                              \o {:nature {:n true}}}}}}}
             (add-to-trie (add-to-trie {} "hello" {:n true})
                          "hellc" {:n true})))))
  (deftest test-in-trie?
    (let [test-trie (add-to-trie
                     (add-to-trie (add-to-trie {} "hello" {:v true})
                                  "hello" {:n true}) "hell" {:v true})]
      (testing "testing word not in the trie, should be false"
        (is (= false
               (in-trie? test-trie "hall"))))
      (testing "testing word in the trie, should return true"
        (is (= true
               (in-trie? test-trie "hello"))))
      (testing "testing word of prefix of antoher in the trie"
        (is (= true
               (in-trie? test-trie "hell"))))))
  (deftest test-get-natures
    (let [test-trie (add-to-trie
                     (add-to-trie (add-to-trie {} "hello" {:v true})
                                  "hello" {:n true})
                     "hell" {:v true})]
      (testing "testing get natures in the trie, should return a hash"
        (is (= {:v true, :n true}
               (get-natures test-trie "hello"))))
      (testing "testing get natures which is not in the trie, empty"
        (is (= {}
               (get-natures test-trie "world"))))))
  (deftest test-longest-prefix-match
    (let [test-trie (add-to-trie
                     (add-to-trie (add-to-trie {} "hello" {:v true})
                                  "hello" {:n true})
                     "hell" {:v true})]
      (testing "testing longest prefix match of words not in trie"
        (is (= {:word "c", :nature {}}
               (longest-prefix-match test-trie "cell"))))
      (testing "testing longest prefix match of only one words in trie"
        (is (= {:word "hell", :nature {:v true}}
               (longest-prefix-match test-trie "hellccd"))))
      (testing "testing longest prefix match of multiple words match"
        (is (= {:word "hello", :nature {:n true, :v true}}
               (longest-prefix-match test-trie "helloaaa"))))))
  (deftest test-build-trie
    (let [test-trie (add-to-trie
                     (add-to-trie (add-to-trie {} "hello" {:v true})
                                  "hello" {:n true})
                     "hell" {:v true})
          modify-trie (build-trie test-trie '({:word "hello"
                                               :nature {:meaning "Greet"}}))]
      (testing "Testing build trie with empty trie as initialization"
        (is (= {\w {\o {\r {\l {\d {:nature {:split-only true}}}}}},
                \h {\e {\l {\l {:nature {:split-only true},
                                \o {:nature {:split-only true}}}}}}}
               (build-trie {} '("hello" "hell" "world")))))
      (testing "Testing insert exsiting word to trie, should not change"
        (is (= test-trie
               (build-trie test-trie '({:word "hell", :nature {:v true}}))))
        (is (= test-trie
               (build-trie test-trie '({:word "hello", :nature {:n true}})))))
      (testing "Testing insert words not existing in the trie."
        (is (not (= test-trie (build-trie test-trie '("hello")))))
        (is (not (= test-trie modify-trie)))
        (is (not (nil? (:meaning (get-natures modify-trie "hello")))))
        (is (nil? (:split-only (get-natures modify-trie "hello")))))
      (testing "Building trie in suffix style"
        (is (= {\o {\l {\l {\e {\h {:nature {:split-only true}}}}}}}
               (build-trie {} '("hello") reverse)))
        (is (= {\o {\l {\l {\e {:nature {:split-only true},
                                \h {:nature {:split-only true}}}}}}}))))))










