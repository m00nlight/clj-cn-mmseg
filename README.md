# clj-cn-mmseg

clj-cn-mmseg是一个用clojure实现的mmseg中文分词工具包。

## 使用

使用Leiningen，在`project.clj`文件中dependencies中加入如下依赖:

```clojure
[clj-cn-mmseg "0.1.2"]
```

使用Maven，则在`pom.xml`文件中加入：

```xml
<dependency>
  <groupId>clj-cn-mmseg</groupId>
  <artifactId>clj-cn-mmseg</artifactId>
  <version>0.1.2</version>
</dependency>
```

```clojure
=> (require [clj-cn-mmseg.core :as mmseg])
```

### 分词

分词提供了两种，一种是`mmseg`返回分词的详细型习，包括每个词可能的信息，比如是否可能是人名，地名，机构名，
或者是维基百科重定向页面等，另外一种是`mmseg-simple`只返回分词的结果，用空格分开，不包含任何其他信息。

下面是使用`mmseg-seg-only`的例子：

```clojure
clj-cn-mmseg.core=> (mmseg-seg-only "转坊间传说：亚洲杯前，习大大找体育总局\
局长刘鹏谈话:我不管你书记去机场接你……然後，刘鹏找国足领队教练和队员谈话: 踢得好了，\
坐澳航回来，踢得不好，坐马航回来……然後，就贏了。")
"转 坊间 传说 : 亚洲杯 前 , 习大大 找 体育总局 局长 刘鹏 谈话 : 我 不管 你 用 \
洋 教练 还是 本土 教练 , 踢 得 好 了 , 我 来 机场 接 你 , 踢 得 不好 , 岐山 \
书记 去 机场 接 你 … … 然 後 , 刘鹏 找 国 足 领队 教练 和 队员 谈话 :   踢 \
得 好 了 , 坐 澳航 回来 , 踢 得 不好 , 坐 马航 回来 … … 然 後 , 就 贏 了 。"
```


使用`mmseg`的例子：

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

再如上面这个例子，可以还原**SB**和**卧槽**之类的网络语言。


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

```clojure
clj-cn-mmseg.core=> (mmseg "U.S.News综合大学排名是全美最具权威影响力最大的排名。")
({:word "U.S.News", :nature {:english true}} {:word "综合大学", :nature \
{:split-only true}} {:word "排名", :nature {:split-only true, :v true}}\
{:word "是", :nature {:adj true, :v true, :pron true}} {:word "全", :nature \
{:adj true, :adv true}} {:word "美", :nature {:adj true, :n true}} \
{:word "最", :nature {:adv true}} {:word "具", :nature {:unit true}} \
{:word "权威", :nature {:split-only true, :n true}} {:word "影响力", :nature \
{:split-only true}} {:word "最大", :nature {:split-only true}} {:word "的", \
:nature {:split-only true}} {:word "排名", :nature {:split-only true, \
:v true}} {:word "。", :nature {}})
```

上面所有的信息均来自维基百科生成的词典。在项目的`resources/`文件夹下。


### 命名实体

clj-cn-mmseg现在提供了基于词典的基本的命名实体提取，可以通过`ner`方法调用，返回的是结果是一个
String list, 包含后缀*/per*, */org*或者*/loc*的分别对应人名，机构名和地名。如下例所示：

```clojure
clj-cn-mmseg.core=> (ner "【英特尔:PC阵营设计不敌苹果公司 微软Surface应降价】美国IT网站CNET上周六撰文称，\
虽然î市面上有很多外形美观的高端PC，但要同时在价格与实特尔处理器，公司的笔记本和平板电脑，却几乎无法实现。\
英特尔高管建议微软在Surface平板电脑中采用最新一代英 ")
("【" "英特尔/org" ":" "PC" "阵营" "设计" "不敌" "苹果公司/org" " " "微软/org" "Surface" "应" \
"降价" "】" "美国/loc" "IT" "网站" "CNET" "上" "周六" "撰文" "称" "," "虽然" "î" "市面" "上" \
"有" "很" "多" "外形" "美观" "的" "高端" "PC" "," "但" "要" "同时" "在" "价格" "与" "实用" "性" \
"上" "比拼" "苹果公司/org" "的" "笔记本" "和" "平板" "电脑" "," "却" "几乎" "无法" "实现" "。" \
"英特尔/org" "高" "管" "建议" "微软/org" "在" "Surface" "平板" "电脑" "中" "采用" "最新" "一代" \
"英特尔/org" "处理器" ",")
```

## TODO
+ [X] 实现词典中不存在的英文词的组合
+ [X] 命名实体提取
+ [ ] 用户自定义辞典

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
