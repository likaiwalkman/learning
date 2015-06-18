(ns sicp.huffman-test
  (:use midje.sweet)
  (:require [clojure.test :refer :all]
            [sicp.huffman :refer :all]))

(def sample-tree
  (make-code-tree
   (make-leaf 'A 4)
   (make-code-tree
    (make-leaf 'B 2)
    (make-code-tree (make-leaf 'D 1)
                    (make-leaf 'C 1)))))

(facts "About Huffman tree"
       (fact
        (make-leaf 'A 2) => '(leaf A 2)
        (symbols (make-leaf 'A 2)) => '(A)
        (weight (make-leaf 'A 2)) => 2
        )

       (fact "about decoding/encoding"
             (decode '(0 1 1 0 0) sample-tree) => '(A D A)

             (decode '(0 1 1 0 0 1 0 1 0 1 1 1 0) sample-tree)
             =>
             '(A D A B B C A)

             (encode '(A D A B B C A) sample-tree)
             =>
             '(0 1 1 0 0 1 0 1 0 1 1 1 0)
             )

       (fact "about generating Huffman Tree"
             (generate-huffman-tree '((A 4) (B 2) (C 1) (D 1)))
             =>
             sample-tree
             )
       )
