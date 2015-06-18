;;; 2.3.4 Huffman encoding tree
(ns sicp.huffman
  (:require [clojure.pprint :as pp]))

(defn make-leaf
  [symbol weight]
  (list 'leaf symbol weight))
(defn leaf?
  [node]
  (= 'leaf (first node)))
(defn symbol-leaf
  [leaf]
  (second leaf))
(defn weight-leaf
  [leaf]
  (nth leaf 2))

(defn left-branch [tree] (first tree))
(defn right-branch [tree] (second tree))
(defn symbols [tree]
  (if (leaf? tree)
    (list (symbol-leaf tree))
    (nth tree 2)))
(defn weight [tree]
  (if (leaf? tree)
    (weight-leaf tree)
    (nth tree 3)))
(defn make-code-tree
  [left right]
  (list left
        right
        (concat (symbols left) (symbols right))
        (+ (weight left) (weight right))))

;; decoding
(defn choose-branch [bit tree]
  (cond
    (zero? bit)
    (left-branch tree)

    (= 1 bit)
    (right-branch tree)

    :else
    (throw (Exception. (str "bad bit -- CHOOSE-BRANCH" bit)))))
(defn decode [bits tree]
  (let [decode-1
        (fn decode-1 [bits current-branch]
          (if (empty? bits)
            '()
            (let [next-branch
                  (choose-branch (first bits) current-branch)]
              (if (leaf? next-branch)
                (cons (symbol-leaf next-branch)
                      (decode-1 (rest bits) tree))
                (decode-1 (rest bits) next-branch)))))]
    (decode-1 bits tree)))

(defn adjoin-set [x set]
  (cond
    (empty? set)
    (list x)

    (< (weight x) (weight (first set)))
    (cons x set)

    :else
    (cons (first set)
          (adjoin-set x (rest set)))))

(defn make-leaf-set [pairs]
  (if (empty? pairs)
    '()
    (let [pair (first pairs)]
      (adjoin-set (make-leaf (first pair)   ; symbol
                             (second pair)) ; frequency
                  (make-leaf-set (rest pairs))))))

;; encoding
(declare encode-symbol)
(defn encode
  [message tree]
  (if (empty? message)
    '()
    (concat (encode-symbol (first message) tree)
            (encode (rest message) tree))))

(defn encode-symbol [symbol tree]
  (let [is-contains (fn [symbols symbol]
                      (not
                       (nil?
                        (some (hash-set symbol) symbols))))]
    (cond
      (or (empty? tree)
          (leaf? tree))
      '()

      (is-contains (symbols tree) symbol)
      (if (is-contains (symbols (left-branch tree)) symbol)
        (cons 0 (encode-symbol symbol (left-branch tree)))
        (cons 1 (encode-symbol symbol (right-branch tree))))

      :else
      (throw (Exception. (str "Unknown symbol -- ENCODE-SYMBOL" symbol))))))


;;; Generating Huffman tree
(declare successive-merge)
(defn generate-huffman-tree [pairs]
  (successive-merge (make-leaf-set pairs)))

(defn successive-merge [leaf-set]
  (cond
    (< (count leaf-set) 2)
    (first leaf-set)

    :else
    (successive-merge
     (adjoin-set
      (make-code-tree (first leaf-set)
                      (second leaf-set))
      (rest (rest leaf-set))))))
