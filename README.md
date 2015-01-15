# clj-cn-mmseg

clj-cn-mmseg是一个用clojure实现的mmseg中文分词工具包。

## 使用

使用Leiningen，在`project.clj`文件中dependencies中加入如下依赖:

```clojure
[clj-cn-mmseg "0.1.0"]
```

使用Maven，则在`pom.xml`文件中加入：

```xml
<dependency>
  <groupId>clj-cn-mmseg</groupId>
  <artifactId>clj-cn-mmseg</artifactId>
  <version>0.1.0</version>
</dependency>
```

```clojure
=> (require [clj-cn-mmseg.core :as mmseg])
```

然后就可以使用`mmseg/mmseg-seg-only`得到只分词的结果，和用`mmseg/mmseg`得到
分词中的词的额外信息。如下两个例子所示：

```clojure
clj-cn-mmseg.core=> (mmseg-seg-only "转坊间传说：亚洲杯前，习大大找体育总局\
局长刘鹏谈话:我不管你书记去机场接你……然後，刘鹏找国足领队教练和队员谈话: 踢得好了，\
坐澳航回来，踢得不好，坐马航回来……然後，就贏了。")
"转 坊间 传说 : 亚洲杯 前 , 习大大 找 体育总局 局长 刘鹏 谈话 : 我 不管 你 用 \
洋 教练 还是 本土 教练 , 踢 得 好 了 , 我 来 机场 接 你 , 踢 得 不好 , 岐山 \
书记 去 机场 接 你 … … 然 後 , 刘鹏 找 国 足 领队 教练 和 队员 谈话 :   踢 \
得 好 了 , 坐 澳航 回来 , 踢 得 不好 , 坐 马航 回来 … … 然 後 , 就 贏 了 。"
```

```clojure
clj-cn-mmseg.core=> (mmseg "转坊间传说：亚洲杯前，习大大找体育总局局长刘\
鹏谈话:我不管你用洋教练还然後，刘鹏找国足领队教练和队员谈话: 踢得好了，坐澳航\
回来，踢得不好，坐马航回来……然後，就贏了。")
({:word "转", :nature {:v true}} {:word "坊间", :nature {:n true}} \
{:word "传说", :nature {:split-only true, :v true, :n true}} \
{:word ":", :nature {}} {:word "亚洲杯", :nature {:split-only true}} \
{:word "前", :nature {:n true}} {:word ",", :nature {}} \
{:word "习大大", :nature {:split-only true, :per true, :redirect "习近平"}} \
{:word "找", :nature {:v true}} {:word "体育总局", :nature {:split-only true}}\
{:word "局长", :nature {:split-only true}} {:word "刘鹏", :nature {:per true}}\
{:word "谈话", :nature {:split-only true, :n true}} {:word ":", :nature {}}\
{:word "我", :nature {:pron true}} {:word "不管", :nature {:split-only true}}\
{:word "你", :nature {:pron true}} {:word "用", :nature {:v true, :n true}}\
{:word "洋", :nature {:adj true, :n true}} {:word "教练", :nature \
{:split-only true, :v true, :n true}} {:word "还是", :nature {:adv true}} \
{:word "本土", :nature {:split-only true, :n true}} {:word "教练", :nature \
{:split-only true, :v true, :n true}} {:word ",", :nature {}} \
{:word "踢", :nature {:v true}} {:word "得", :nature {:v true}} \
{:word "好", :nature {:adj true, :v true}} {:word "了", :nature {:v true}} \
{:word ",", :nature {}} {:word "我", :nature {:pron true}} \
{:word "来", :nature {:v true}} {:word "机场", :nature {:split-only true, \
:loc true, :n true}} {:word "接", :nature {:v true}} {:word "你", :nature \
{:pron true}} {:word ",", :nature {}} {:word "踢", :nature {:v true}} \
{:word "得", :nature {:v true}} {:word "不好", :nature {:split-only true}} \
{:word ",", :nature {}} {:word "岐山", :nature {:loc true}} \
{:word "书记", :nature {:split-only true, :n true}} {:word "去", :nature {}} \
{:word "机场", :nature {:split-only true, :loc true, :n true}} \
{:word "接", :nature {:v true}} {:word "你", :nature {:pron true}} \
{:word "…", :nature {}} {:word "…", :nature {}} {:word "然", :nature \
{:adv true}} {:word "後", :nature {}} {:word ",", :nature {}} {:word "刘鹏",\
:nature {:per true}} {:word "找", :nature {:v true}} {:word "国", :nature
{:n true, :pron true}} {:word "足", :nature {:adj true, :adv true}}\
{:word "领队", :nature {:split-only true, :v true, :n true}} \
{:word "教练", :nature {:split-only true, :v true, :n true}} \
{:word "和", :nature {:adj true, :v true, :prep true, :n true}} \
{:word "队员", :nature {:split-only true, :n true}} \
{:word "谈话", :nature {:split-only true, :n true}} {:word ":", :nature {}} \
{:word " ", :nature {}} {:word "踢", :nature {:v true}} \
{:word "得", :nature {:v true}} {:word "好", :nature {:adj true, :v true}}\
{:word "了", :nature {:v true}} {:word ",", :nature {}} \
{:word "坐", :nature {:v true}} {:word "澳航", :nature {:company true, \
:redirect "澳洲航空"}} {:word "回来", :nature {:split-only true, :v true}} \
{:word ",", :nature {}} {:word "踢", :nature {:v true}} {:word "得", :nature \
{:v true}} {:word "不好", :nature {:split-only true}} {:word ",", :nature {}} \
{:word "坐", :nature {:v true}} {:word "马航", :nature {:split-only true}} \
{:word "回来", :nature {:split-only true, :v true}} {:word "…", :nature {}} \
{:word "…", :nature {}} {:word "然", :nature {:adv true}} {:word "後", \
:nature {}} {:word ",", :nature {}} {:word "就", :nature {:prep true, \
:v true, :adv true}} {:word "贏", :nature {}} {:word "了", :nature {:v true}} \
{:word "。", :nature {}})
```

如上例所是，可以看到*习大大*这个词语在百科中被重定向为*习近平*词条。其他则可能包含
这个词条的词性等信息。

```clojure
clj-cn-mmseg.core=> (mmseg "卧槽~这SB的炒作也太低级了吧")
({:word "卧槽", :nature {:meaning "我操"}} {:word "~", :nature {}} \
{:word "这", :nature {:pron true}} {:word "SB", :nature {:meaning "傻逼"}} \
{:word "的", :nature {:split-only true}} {:word "炒作", :nature \
{:split-only true, :v true}} {:word "也", :nature {:adv true}} \
{:word "太", :nature {:adv true}} {:word "低级", :nature {:split-only true, \
:adj true}} {:word "了", :nature {:v true}} {:word "吧", :nature \
{:split-only true}})
```

再如上面这个例子，可以还原*SB*和*卧槽*之类的网络语言。


对中文量词做了特殊处理，可以聚合数字加量词形式的词组，并且对常用货币单位也做了类似的，如下面
的例子：

```clojure
clj-cn-mmseg.core=> (mmseg "油价一百四十美元一桶时")
({:word "油价", :nature {:split-only true}}\
{:word "一百四十美元", :nature {:concurrency true, :meaning "美元"}}\
{:word "一", :nature {:num true}} {:word "桶", :nature {:n true}}\
{:word "时", :nature {:unit true}})
clj-cn-mmseg.core=> (mmseg "结果中了3.26亿美元…")
({:word "结果", :nature {:split-only true, :v true, :n true}} \
{:word "中", :nature {:n true, :v true}} {:word "了", :nature {:v true}} \
{:word "3.26亿美元", :nature {:concurrency true, :meaning "美元"}} \
{:word "…", :nature {}})
```


上面所有的信息均来自维基百科生成的词典。在项目的`resources/`文件夹下。



## TODO
+ [ ] 实现词典中不存在的英文词的组合

## Contribution

欢迎对该项目的任何形式的贡献，如果修改了原代码，请在提交pull request之前确保 `lein test`
能够通过。

```shell
lein test clj-cn-mmseg.core-test

lein test clj-cn-mmseg.dict-test

lein test clj-cn-mmseg.mmseg-test

lein test clj-cn-mmseg.test-utils

lein test clj-cn-mmseg.trie-test

Ran 20 tests containing 79 assertions.
0 failures, 0 errors.
```

## License

Copyright © 2015 m00nlight

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
