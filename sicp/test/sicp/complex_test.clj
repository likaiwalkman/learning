(ns sicp.complex-test
  (:use midje.sweet)
  (:require [clojure.test :refer :all]
            [sicp.complex :refer :all]))

(facts "about complex number"
       (fact
        1 => 1))
