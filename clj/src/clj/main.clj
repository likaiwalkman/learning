(ns clj.main)
;;(use '[clj.basic])
;;(use '[clj.fp])
(use '[clj.concurrency])


;;;; Thinking
;;-----------------------------------------------------------
;; 1. Pure Function, 函数不依赖外部的状态，不改变外部的状态(side effect)，同样的输入对应固定的输出。这样的函数严谨，可靠，可测。
;; 对于有状态依赖的函数，我们一般需要mock data来测试，但是你永远无法保证能cover所有的state。而pure函数没有这个问题。
;; 但是关键是，真实的场景中，交互永远是状态依赖的。我们无法避免状态，从这个角度来讲，我们要做的，就是尽量保证Pure Function，然后把状态依赖收集起来，集中处理
;; Pure Function的好处：easy to reason about(易于推导), 便于测试, 可缓存化, 可并行化

;; 2. Memoize可能导致cache不被GC回收，产生memory leak

;; 3. Sequence大多是lazy的，且只有seq会存在laziness。在处理lazy的情况时，一定要注意尽量无side effects, 因为lazy seq只有在access时才会realize, 相应的代码如果有side effect, 将会难以控制
;; lazy seq常常会很大，甚至是无限的。这个时候, 如果对seq的head存在引用，后边的所有element将不能被GC，这容易导致内存溢出

;; 4. Persistence不是我们常见的持久化的概念，而是函数式编程中保证immutable数据的操作的performance的技术。Clojure里是通过Structural sharing来实现的，因为immutable, 所有共享不会发生预料之外的问题。为了实现Structural sharing, 几乎所有的数据结构都是由tree构建, 包括hashmap, hashset, sorted-map, sorted-set, vectors

;; 5. Immutable数据还可以方便的实现数据版本化

;; 6. Clojure futures are evaluated within a thread pool, and are instances of `java.util.concurrent.Future'

;; 7. Concurrency is the coordination of multiple threads; while Parallelism is an optimization technique used to efficiently utilize all of the available resources to improve the performance of `an operation'. 简单来说，Concurrency的重点在于通过多线程同时做多件事(因为多线程并不会有助于做同一件事，比如计算)；而Parallelism(并行)是分而治之的将大问题分解为小问题，然后`同时'的执行这些小问题以加快速度，而不管你是通过多核的方式，分布式（多机）的方式还是什么别的方式

;; 8. `Refs' are for Coordinated Synchronous access to "Many Identities". `Atoms' are for Uncoordinated synchronous access to a single Identity. `Agents' are for Uncoordinated asynchronous access to a single Identity. `Vars' are for thread local isolated identities with a shared default value.

;; 9. `ref-set' is for when don't care about the current value; `alter' will retry the whole transaction when conflicts; `commute' is an optimized alter, it will run twice(rerun *synchronously* when commit) to make sure the `commutative' calculation is correct

;; 10. STM有一些限制: 每个transaction必须是safe to retry的，因为可能会执行多次，所以一定要保证没有side effect。可以使用`io!'来wrap io操作，当在transaction里执行时, io!会自动throw IllegalStateException; 另外，the values held by refs must be `immutable', 否则容易出错; 最后，the shorter the transaction is, the easier it will be for STM
