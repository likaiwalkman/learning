<!--
{
"summary": "scheme简洁优雅强大，直指编程本质。学之能去芜存精，助我们提升编程水平。"
}
-->
## Scheme初探

### 基本介绍

最近在学习[Scheme](http://zh.wikipedia.org/wiki/Scheme)，来自MIT的一个著名的[Lisp](http://zh.wikipedia.org/wiki/Lisp)方言。它诞生于1975年，和c语言(1972)算是同龄，对比现在当道的90后语言(java, javascript, php, python, ruby... )，在这日新月异的程序界，算得上是“老掉牙”了。

那么问题来了，有这工夫，为啥不学学新潮的[Go](http://zh.wikipedia.org/wiki/Go), [Rust](http://zh.wikipedia.org/wiki/Rust)，偏偏学一个这么古老的语言？

答案很简单: Scheme简洁优雅强大，直指编程本质。学之能去芜存精，助我们提升编程水平。

关于Scheme的书籍最有名的当属MIT的[《计算机程序的构造和解释》](http://zh.wikipedia.org/wiki/%E8%A8%88%E7%AE%97%E6%A9%9F%E7%A8%8B%E5%BA%8F%E7%9A%84%E6%A7%8B%E9%80%A0%E5%92%8C%E8%A7%A3%E9%87%8B)（SICP，或“魔法书”），到08年为止，曾三十年为作为MIT计算机科学的入门课程。

不过SICP虽“说”是`入门`，其实又厚又深，所以我选择了另一本经典书籍作为入门：[The Little Schemer](http://book.douban.com/subject/1632977/)。这本书仅仅200页，且通篇都由`question&answer`的对话构成，由浅入深，深入浅出，从lambda以及几个built-in函数出发，一路推导出各种运算方法、数据结构，并深入到continuation，停机问题，Y-combinator, 甚至最后直接写了个简单的解释器出来，令人惊叹。

事实上，本文可算是`The Little Schemer`的笔记和总结。

### Let's start
我使用的scheme实现是[Guile](http://www.gnu.org/software/guile/), 然后在[emacs](http://zh.wikipedia.org/wiki/Emacs)中使用了[王垠](http://baike.baidu.com/view/1928287.htm)的scheme配置, 配置文件在[这里](https://github.com/martin-liu/prelude/blob/master/personal/scheme.el)
#### Scheme基本语法
Scheme的语法就是Lisp语法，括号套括号...很多人不爽lisp满屏的括号，觉得可读性差。不过，可读性应该是用缩进来保证的，任何语言的code，不用换行和缩进，都不能谈可读性。

事实上，由于Lisp的程序和数据是同一种表达方式(列表)——本质上就是[AST](http://zh.wikipedia.org/wiki/%E6%8A%BD%E8%B1%A1%E8%AA%9E%E6%B3%95%E6%A8%B9)的前缀表示——这给了lisp无与伦比的表达力和扩展性。

下面简单说一下其语法元素:
* 原子（atom）和列表（list）
表达式只有两种, `atom`和`list`, atom是number或者symbol(类似于string), list就是那坨括号

```scheme
3                                       ; 数字3
1.14                                    ; 数字1.14
\#t                                     ; boolean True
\#f                                     ; boolean False
abc                                     ; simbol abc
()                                      ; empty list
(abc xyz)                               ; list
(+ 1 (- 3 2))                           ; nested list
```

* [lambda](http://zh.wikipedia.org/wiki/%CE%9B%E6%BC%94%E7%AE%97)
```scheme
 (lambda (x)
   (+ x 1))
```
其实就是匿名函数，等同于javascript里的:
```javascript
function(x){
  return x + 1;
}
```

* 基本操作符，built-in函数

注意这里不是在说Lisp的7公理，也不是列出所有scheme的built-in函数，而是在`The Little Schemer`中**必需**的函数
  1. quote

  `(quote x)`返回x, 等价的写法是`'x`, `'`是一个语法糖。quote的作用是`区分代码和数据`，比如`(+ 1 2)`表示代码，执行结果为3；而`'(+ 1 2)`表示数据，执行结果为`(+ 1 2)`这个list
  2. cons, 用来构造list

  `(cons 'a 'b)`返回`(a . b)`, 这个叫做dotted pair。 dotted pair是list的基本组成元素，`(cons 'a '())`返回的是`(a . ())`, 我们将这种结构叫做list, 并`简写`为`(a)`；同理，`(cons 'a '(b))`返回`(a . (b . ()))`, 我们简写为`(a b)`
  3. car, 返回list的第一个元素

  `(car '(a b)`返回`a`
  4. cdr, 返回除第一个元素的所有元素组成的表

  `(cdr '(a b c))`返回`(b c)`
  5. cond, and, or, not, 条件语句

  ```scheme
  (cond
     (condition1 return1)
     (condition2 return2)
     ...
     else default)
  ```
  6. null?, 判断是否为空list

    `(null? '())`返回`#t`, `(null? OTHERS)`返回`#f`
  7. eq?, 判断是否相等
  8. atom?, 判断是否为atom, 不是list的就是atom
  9. zero?, 判断数字是否为0
  10. add1, 数字加1
  11. sub1, 数字减1
  12. number?, 判断是否为数字

好了，没有了，就这些，让我们开始奇妙的编程之旅吧！

等等 :bangbang: 纳尼？就这些？它喵的，`+、-、*、/、>、<`都没有, for loop, while loop也没有，这能编程？:open_mouth:

别急，让我们一点点来，让我们尝试去构造它们，看看那些熟悉的编程元素，是否真的必不可少。在这个过程里，也让我们去思考也去寻找，有关编程的一些更本质的东西。

#### recursion(递归)
没有循环，那我们如何去处理重复操作？答案是递归。比如阶乘：
```scheme
(define fact
  (lambda (x)
    (cond
      ((eq? x 1) 1)
      (else (* x (fact (- x 1)))))))
```
递归强大而易读，有简洁的数学美。我们只需要：
1. 设置一个`终止条件`，以便递归返回，比如`(eq? x 1)`则返回`1`
2. 改变参数值，让它`朝终止条件靠近`(这样才能最终结束)，比如`(- x 1)`
3. 以新的参数，递归调用自身，此时我们`假设`这个函数已经是work的，并以此填充表达式

可以看到，递归和[数学归纳法](http://zh.wikipedia.org/wiki/%E6%95%B0%E5%AD%A6%E5%BD%92%E7%BA%B3%E6%B3%95)十分类似：先考虑`x = 1`的情况，再假设`x = k - 1`时是work的，然后考虑`x = k`时的情况。这个过程充分展现了强大的数学美。

然而，我们常常听到一个说法：避免递归，多用循环(迭代)。这在一般情况下（比如c、java程序里）是正确的，因为递归会不断进行函数调用，系统需要保存调用信息和返回地址到[调用栈](http://zh.wikipedia.org/wiki/%E8%B0%83%E7%94%A8%E6%A0%88)，这样不仅性能慢，而且容易栈溢出。

不过在[函数式编程](http://zh.wikipedia.org/zh-hk/%E5%87%BD%E6%95%B8%E7%A8%8B%E5%BC%8F%E8%AA%9E%E8%A8%80)语言里，一般都支持[尾递归](http://zh.wikipedia.org/wiki/%E5%B0%BE%E8%B0%83%E7%94%A8)优化（javascript的话，ES6将支持尾递归优化, excited!），可以很好的解决这个问题。不过在这里我们主要考虑编程的思想，优化之类的问题先不多谈。

另外，如果细心的话，你可能会发现，刚才的阶乘算法里的`define`, 并不在之前提到的built-in的list里。实际上，define也可以只是一个语法糖。难道给函数命名也不是必需的？关于这一点，我们会在之后的`Y-combinator`一节得到答案。而现在，让我们先认为define存在并且work。

#### 实现加减乘除
让我们来实现那些基本的运算。注意，我们暂时只考虑自然数的情况。
* 加法

我们可以使用`add1`, `sub1`以及`zero?`来实现加法。对于`n - m`, 当`m`为`0`时，返回`n`，这是设置一个停止条件；然后让`m`往`0`逼近，递归调用加法函数即可。代码如下:
```scheme
(define +
  (lambda (n m)
    (cond
     ((zero? m) n)
     (else (add1 (+ n (sub1 m)))))))
```
* 减法
```scheme
(define -
  (lambda (n m)
    (cond
     ((zero? m) n)
     (else (sub1 (- n (sub1 m)))))))
```
* 乘法, 有了加法，乘法就自然有了
```scheme
(define *
  (lambda (n m)
    (cond
     ((zero? m) 0)
     (else (+ n (* n (sub1 m)))))))
```
* >, <, 还有数字的`=`, 因为`eq?`是一个更大的scope
```scheme
;; define >
(define >
  (lambda (n m)
    (cond
     ((zero? n) #f)
     ((zero? m) #t)
     (else (> (sub1 n) (sub1 m))))))
;; define <
(define <
  (lambda (n m)
    (cond
     ((zero? m) #f)
     ((zero? n) #t)
     (else (< (sub1 n) (sub1 m))))))
;; define =
(define =
  (lambda (n m)
    (cond
     ((> n m) #f)
     ((< n m) #f)
     (else #t))))
```
* 除法
```scheme
(define /
  (lambda (n m)
    (cond
     ((< n m) 0)
     (else (add1 (/ (- n m) m))))))
```
* 求余
```scheme
(define %
  (lambda (n m)
    (cond
     ((< n m) n)
     ((= n m) 0)
     (else
      (% (- n m) m)))))
```

漂亮！可以看到，通过lambda和递归，我们只使用`zero?`, `add1`, `sub1`，便实现了加减乘除，求余，还有大于、小于、相等的判定功能。:smile:

#### 邱奇数
值得一题的是，`The Little Schemer`里还略微探讨了一下类似[邱奇数](http://zh.wikipedia.org/wiki/%E9%82%B1%E5%A5%87%E6%95%B0)的问题，当然不是真的邱奇数，而是类似邱奇数的简化版，且同样只考虑自然数。

简单的说，就是用`()`表示0，`(())`表示1，`(()())`表示2，循环往复。
```scheme
;; use '() to represent 0, '(()) represent 1
(define sero?
  (lambda (n)
    (null? n)))
(define edd1
  (lambda (n)
    (cons '() n)))
(define zub1
  (lambda (n)
    (cdr n)))
```
以上是新版本的`zero?`, `add1`和`sub1`。有了这几个，根据上一节的code，我们就可以`抛开数字`，玩转各种运算了! :smirk:

#### 常用数据结构和操作
Scheme里只有链表，不过，其它数据结构都可以由链表来模拟或生成，比如array, set, map(table)

当数组用：
```scheme
;; (pick n lat), get the element of lat in position n
(define pick
  (lambda (n lat)
    (cond
     ((zero? (sub1 n)) (car lat))
     (else (pick (sub1 n) (cdr lat))))))
```

Set:
```scheme
;; makeset, make a lat to a set
(define makeset
  (lambda (lat)
    (cond
     ((null? lat) '())
     ((member? (car lat) (cdr lat))
      (makeset (cdr lat)))
     (else (cons (car lat) (makeset (cdr lat)))))))
```
这里`member?`的代码就不贴出了

关于Table(map), 我们一般会构造`Entry`这样的结构，在scheme里可以表示为`(keys values)`这样的形式，而`table(map)`就是`entry`的list。

至于一些相应的操作方法，由于本文并非流水帐(:sweat:), 此处就不贴出了

#### continuation
跟着`The Little Schemer`的步伐，我们会一路build出越来越复杂的方法，也会开始学会`abstract`, 通过复用来简化code。不过，这些其实也并没有太出彩的地方。

但到了第8章，我们将会碰到一个神奇的东西，[continuation](http://en.wikipedia.org/wiki/Continuation)。

讲continuation之前，我先简单说一下[continuation-passing style (CPS)](http://en.wikipedia.org/wiki/Continuation-passing_style)。
在JavaScript程序里, 我们经常会用到`回调函数`，比如ajax call：
```javascript
$.getJSON("ajax/test.json", function(data) {
    console.log(data);
});
```
`getJSON`异步拿到数据后，便会执行pass进去的function，而这个funcion就是continuation了。

有朋友可能会觉得，哎哟，不就是回调嘛，这有什么了不起的？

当然不只是回调。所谓continuation，其实是对程序的[控制流](http://en.wikipedia.org/wiki/Control_flow)的抽象表示。上面的例子里，`getJSON`执行完后，控制流转到传入的匿名函数，在这里，实际上控制流是由我们所控制。
如果使用这种style，我们甚至不再需要`return`。
比如将
```javascript
function f(a){
    return a;
}
```
改成：
```javascript
function f(a, continuation) {
  continuation(a);
}
```
事实上很多编译器都会做这种事情。

而更重要的是，使用Continuation我们可以实现更复杂的控制流，比如[Exception(try-catch block)](http://en.wikipedia.org/wiki/Exception_handling), [coroutine(协程)](http://en.wikipedia.org/wiki/Continuation), [generator](http://en.wikipedia.org/wiki/Generator_(computer_programming))

对于编译器来说，可以容易的把这些复杂的结构脱糖处理，变成简单的CPS，这将极大的简化编译器的实现。

关于continuation的话题可以非常大，这里篇幅有限，暂不深入讨论。我将单独开一篇博文详谈。

回到`The Little Schemer`, 我不得不贴一下里面讲解continuation的code, 非常之赞，因为真正的在解决问题，而不是如我这样空泛的举例:
```scheme
(define multirember&co
  ;; param: atom, list, collector
  (lambda (a l col)
    (cond
     ((null? l)
      (col '() '()))
     ((equal? a (car l))
      (multirember&co a (cdr l)
                      ;; 每次(cdr l), 都包一次col, seen经过(cons (car l))，收集了所有等于a的atom
                      (lambda (newlat seen)
                        (col newlat
                             (cons (car l) seen)))))
     (else
      (multirember&co a (cdr l)
                      (lambda (newlat seen)
                        (col (cons (car l) newlat) seen)))))))
```
这里很重要的一点是，我们现在并没有提供`局部变量`的功能，但是通过continuation来巧妙的做collect工作。从这个角度来讲，局部变量也可以是语法糖!

为了便于不熟悉scheme的人理解，我用javascript翻译了一下，可在chrome develop tool或者firebug里have a try。注意，里面的局部变量仅仅是为了写的方便，理解这个意思就行。
```javascript
var multirember = function(a, arr, collector) {
  var tmp;
  if (arr.length === 0) {
    return collector([], []);
  } else {
    tmp = arr.shift();
    if (a === tmp) {
      return multirember(a, arr, function(notseen, seen) {
        seen = [a].concat(seen);
        return collector(notseen, seen);
      });
    } else {
      return multirember(a, arr, function(notseen, seen) {
        notseen = [tmp].concat(notseen);
        return collector(notseen, seen);
      });
    }
  }
};
multirember(1, [1, 2, 3, 1, 4, 5, 1, 6], function(notseen, seen) {
  console.log("notseen is: " + notseen);
  return console.log("seen is: " + seen);
  });
// result:
// notseen is: 2,3,4,5,6
// seen is: 1,1,1
```

#### halting problem
经过continuation的一知半解和意犹未尽，让我们缓一缓，来看看[停机问题](http://zh.wikipedia.org/zh-hk/%E5%81%9C%E6%9C%BA%E9%97%AE%E9%A2%98)。所谓停机问题，就是判断任意一个程序是否会在有限的时间之内结束运行的问题。

首先，我们写一个无限递归永不停机的函数：
```scheme
(define eternity
 (lambda (x)
  (eternity x)))
```

然后，我们假设存在一个函数`will-stop?`能判断一个函数是否会停机。然后我们再构建一个绝妙的矛盾的函数：
```scheme
(define last-try
 (lambda (x)
  (and (will-stop? last-try)
       (eternity x))))
```
如果`last-try`会停机，即`(will-stop? last-try)`返回#t，将执行`eternity`，于是无限递归永不停机；如果`last-try`不会停机，即`(will-stop? last-try)`返回#f，last-try停机并返回#f。
无论哪种情况，都是矛盾的，于是证明`will-stop?`不存在，停机问题得解。

这样看起来还是蛮简单的嘛，不过，没看过我肯定想不出来。。。啥也不说了，拜谢图灵!

### Y Combinator
在`The Little Schemer`第九章里，还有一个大名鼎鼎的东西，[Y combinator](http://en.wikipedia.org/wiki/Fixed-point_combinator#Y_combinator)。我也曾看过一些关于它的文章，但大多都深奥晦涩，难以捉摸。而在`The Little Schemer`里，完全用code的方式来一步步引导出来，不过，为了接受度，且让我使用js来描述。

还记得我们之前提到`define`其实不是必需的么？想一想，如果我们只有lambda，也就是只有匿名函数，可以实现递归么？

让我们试一试。

从一个简单的递归开始：

```javascript
var f = function(n){
    if (n == 1){
        return 1;
    } else {
        return n * f(n-1);
    }
}
```

由于只有匿名函数，所以`f(n-1)`是不存在的，那么这个写法就不正确了。

不过，如果我们能给出`真正的递归函数`f，那我们就可以写出以下的函数:

```javascript
var F = function(f){
    return function(n){
        if (n == 1){
            return 1;
        } else {
            return n * f(n-1);
        }
    }
}
```
这个函数是可以给出的，因为`f(n-1)`里的`f`是外界传进来的，所以它是有意义的。而`var F`, 我们可以当做语法糖，因为函数定义里没有引用到它。不过，给出了`F`也不能解决问题，因为我们不知道如何给出`f`。

但是没关系，让我们一点点尝试，或许能慢慢逼近正确答案。

让我们看看`F(f)`的结果是什么?
```javascript
function(n){
    if (n == 1){
        return 1;
    } else {
        return n * f(n-1);
    }
}
```
这个`f`是我们传进去的真正的递归函数，而如果f是真正的递归函数，那么很明显，`F(f)`就是`f`本身。

也就是说，`F(f) = f`。很好，虽然我们还不能给出`f`，但我们找到了`F`和`f`的关系。让我们继续尝试，看能否通过`F`来找到`f`。

我们现在知道，`F(f)`就等于我们要找的递归函数。而F和f的定义看起来是很类似的，那我们来试试`F(F)`。`F(F)`展开的结果是:
```javascript
F(F) = function(n){
    if (n == 1){
        return 1;
    } else {
        return n * F(n-1);
    }
}
```
你应该能注意到，`F(n-1)`不对，因为`F`接受一个函数作为参数。而如果从理论上递归函数的定义来看，应该是`F(F)(n-1)`才对。可以做到么？可以！我们再写一个函数G:
```javascript
var G = function(f){
    return function(n){
        if (n == 1){
            return 1;
        } else {
            return n * f(f)(n-1); // 这里是f(f)
        }
    }
}
```
这里`G`和`F`唯一的区别就是里面递归调用的是`f(f)`而不是`f`。现在`G(G)`的展开里会是这样：
```javascript
G(G) = function(n){
    if (n == 1){
        return 1;
    } else {
        return n * G(G)(n-1);
    }
}
```
这不就是递归函数的定义么？看起来似乎没什么问题，`G`的定义里并没有引用`G`，那么理论上说`G(G)`就是我们的递归函数了。
让我们来测试一下，`G(G)(5); // return 120`，漂亮！我们成功了！原来只需要匿名函数，我们就可以实现递归！:beers:

不过，稍等一下，还差一点点，这还不是Y-combinator，因为还不够美。让我们更进一步，把`f(f)`再抽象一下:
```javascript
var G = function(f){
  return (function(f) {
    return function(n) {
      if (n === 1) {
        return 1;
      } else {
        return n * f(n - 1);
      }
    };
  })(f(f));
}
```
不难看出，这个定义等价于之前`G`的定义。
而根据之前的`F`的定义，这实际上就是:
```javascript
var G = function(f){
  return F(f(f));
}
```
由于`G(G)`是我们的递归函数，于是我们可以定义函数Y，使得`Y(F) == G(G)`:
```javascript
var Y = function(F){
    var G = function(f){
        return F(f(f));
    }
    return G(G);
}
```
这个`Y`就是我们的Y-combinator了，只要把一个形似`F`的函数丢给`Y`，就可以获得一个完美的递归函数了！
我们已经测试过了`G(G)`, 让我们再试试`Y(F)`。咦，不对啊，`Uncaught RangeError: Maximum call stack size exceeded`，难道我们推理有误？

好吧，这是最后一个坑了。问题出在`F(f(f))`，实际上我们需要在`else`分支执行`f(f)(n-1)`，而由于JS是没有[Lazy Evaluation](http://en.wikipedia.org/wiki/Lazy_evaluation)的，于是`F(f(f))`里的`f(f)`会直接执行。让我们来fix这个bug:
```javascript
var Y = function(F){
    var G = function(f){
        return F(function(x){
            return f(f)(x);
        });
    }
    return G(G);
}
```
最后，让我们把语法糖去掉，看看完全匿名函数写成的递归函数:
```javascript
(function(F){
    return (function(G){
        return G(G);
    })(function(f){
        return F(function(x){
            return f(f)(x);
        });
    });
})(function(f){
    return function(n){
        if (n == 1){
            return 1;
        } else {
            return n * f(n-1);
        }
    }
})(5); // output 120
```

#### Interpreter
`The Little Schemer`的第十章，主要是讲如何用scheme实现一个简单的scheme解释器，虽然只支持built-in的方法和lambda，但已然十分强大，其实现过程真正体现了`数据即程序`的特点。不过这个code就真是一大坨了，在此就不张贴了。

### Commandments
`The Little Schemer`里总结了十条诫律，非常有价值，在此罗列如下：
1. Always ask, null? for atom/lat, zero? for number; when S-expression, ask (null? l), (atom? (car l)) and else.

2. cons => use to build list

3. When build a list, describe the first typical element, and then cons it onto the natural recursion

4. Always change at least one argument while recurring.(否则无法停止).
It must be changed to be closer to termination.
When lat, use (cdr lat); when number, use (sub1 n); when S-expression, use (car l) and (cdr l) if (null? l) is false and (atom? (car l)) is false

5. 考虑终止条件，应选择不改变当前value的条件: when +, use 0; when *, use 1; when cons, use ()

6. Simplify only after the function is correct, 当之前的函数是正确的时候，可以利用相互递归来简化它们。
如`eqlist?'和`equal?'互相依赖

7. Recur on the subparts that are of the same nature:
   * On the sublists of a list
   * On the sub expressions of an arithmetic expression

8. Use help functions to abstract from representations

9. `Abstract' common patterns with a new function.

10. Build functions to collect more than one value at a time.
通过包装function产生新的function, 让新的function来collect本次调用产生的数据

### Code

### reference
[Wikipedia](http://zh.wikipedia.org/)
[CPS Lecture](https://cgi.soic.indiana.edu/~c311/lib/exe/fetch.php?media=cps-notes.scm)
