(defproject clj-cn-mmseg "0.1.3"
  :description "clj-cn-mmseg是一个用clojure实现的mmseg中文分词工具包。"
  :url "https://github.com/m00nlight/clj-cn-mmseg"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.logging "0.3.1"]]
  :scm {:name "git"
        :url "http://www.eclipse.org/legal/epl-v10.html"}
  :signing {:gpg-key "dot_wangyushi@yeah.net"}
  :deploy-repositories [["clojars" {:creds :gpg}]])
