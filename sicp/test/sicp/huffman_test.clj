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

(def rock-song '((A 2) (NA 16) (BOOM 1) (SHA 3) (GET 2) (YIP 9) (JOB 2) (WAH 1)))
(def rock-song-tree (generate-huffman-tree rock-song))

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

       (fact "about rock-song"
             (encode '(GET A JOB) rock-song-tree)
             =>
             '(1 1 1 1 1 1 1 0 0 1 1 1 1 0)

             (encode '(SHA NA NA NA NA NA NA NA NA) rock-song-tree)
             =>
             '(1 1 1 0 0 0 0 0 0 0 0 0)

             (encode '(WAH YIP YIP YIP YIP YIP YIP YIP YIP YIP) rock-song-tree)
             =>
             '(1 1 0 1 0 1 0 1 0 1 0 1 0 1 0 1 0 1 0 1 0 1 0)
             )
       )
