;; atom?
(define atom?
  (lambda (x)
    (and (not (pair? x))(not (null? x)))))

;; lat?
(define lat?
  (lambda (l)
    (cond
     ((null? l) #t)
     ((atom? (car l)) (lat? (cdr l)))
     (else #f)
     )))

;; member?
(define member?
  (lambda (a l)
    (and
     (not (null? l))
     (or
      (eq? a (car l))
      (member? a (cdr l))))))

;; rember? remove the first occurrence of an atom in a list

(define rember
  (lambda (a l)
    (cond
     ((null? l) '())
     ((eq? a (car l)) (cdr l))
     (else (cons (car l)
                 (rember a (cdr l)))))))

#|
firsts, The function firsts takes one argument, a list, which is either a null
list or contains only non-empty lists. It builds another list composed of the
first S-expression of each internal list
|#
(define firsts
  (lambda (l)
    (cond
     ((null? l) '())
     (else (cons (car (car l)) (firsts (cdr l)))))))

;; insertR
(define insertR
  (lambda (new old lat)
    (cond
     ((null? lat) '())
     ((eq? old (car lat))
      (cons old (cons new (cdr lat))))
     (else (cons (car lat)
                 (insertR new old (cdr lat))))
     )))

;; insertL
(define insertL
  (lambda (new old lat)
    (cond
     ((null? lat) '())
     ((eq? old (car lat)) (cons new lat))
     (else (cons (car lat) (insertL new old (cdr lat))))
     )))

;; (subst new old lat) replaces the first occurrence of old in the lat with new
(define subst
  (lambda (new old lat)
    (cond
     ((null? lat) '())
     ((eq? old (car lat))
      (cons new (cdr lat)))
     (else (cons (car lat) (subst new old (cdr lat)))))))

;; (subst2 new o1 o2 lat) replaces either the first occurence of o1 or o2 in the lat with new
(define subst2
  (lambda (new o1 o2 lat)
    (cond
     ((null? lat) '())
     ((or
       (eq? o1 (car lat))
       (eq? o2 (car lat)))
      (cons new (cdr lat)))
     (else (cons (car lat) (subst2 new o1 o2 (cdr lat))))
     )))

;; (multirember atom lat) remove all the occurences of atom in lat
(define multirember
  (lambda (atom lat)
    (cond
     ((null? lat) lat)
     ((eq? atom (car lat)) (multirember atom (cdr lat)))
     (else (cons (car lat) (multirember atom (cdr lat))))
     )))

;; (multiinsertR new old lat)
(define multiinsertR
  (lambda (new old lat)
    (cond
     ((null? lat) lat)
     ((eq? old (car lat))
      (cons old (cons new
                      (multiinsertR new old (cdr lat)))))
     (else (cons (car lat)
                 (multiinsertR new old (cdr lat))))
     )))

;; (multiinsertL new old lat)
(define multiinsertL
  (lambda (new old lat)
    (cond
     ((null? lat) '())
     ((eq? old (car lat))
      (cons new (cons old (multiinsertL new old (cdr lat)))))
     (else (cons (car lat) (multiinsertL new old (cdr lat)))))))

;; (multisubst new old lat)
(define multisubst
  (lambda (new old lat)
    (cond
     ((null? lat) '())
     ((eq? old (car lat))
      (cons new (multisubst new old (cdr lat))))
     (else (cons (car lat) (multisubst new old (cdr lat)))))))

;; define add1
(define add1
  (lambda (n)
    (1+ n)))

;; define sub1
(define sub1
  (lambda (n)
    (1- n)))

;; define +
(define +
  (lambda (n m)
    (cond
     ((zero? m) n)
     (else (add1 (+ n (sub1 m)))))))

;; define -
(define -
  (lambda (n m)
    (cond
     ((zero? m) n)
     (else (sub1 (- n (sub1 m)))))))

;; addtup, sum all the element of a tuple
(define addtup
  (lambda (tup)
    (cond
     ((null? tup) 0)
     (else (+
            (car tup)
            (addtup (cdr tup)))))))

;; define *
(define *
  (lambda (n m)
    (cond
     ((zero? m) 0)
     (else (+ n (* n (sub1 m)))))))

;; (tup+ tup1 tup2), add each element of 2 tups, for example: (tup+ '(1 2) '(3 4)) => '(4 6)
(define tup+
  (lambda (tup1 tup2)
    (cond
     ((null? tup1) tup2)
     ((null? tup2) tup1)
     (else (cons
            (+ (car tup1) (car tup2))
            (tup+ (cdr tup1) (cdr tup2)))))))

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

;; define **
(define **
  (lambda (n m)
    (cond
     ((zero? m) 1)
     (else (* n (** n (sub1 m)))))))

;; define /
(define /
  (lambda (n m)
    (cond
     ((< n m) 0)
     (else (add1 (/ (- n m) m))))))

;; define %
(define %
  (lambda (n m)
    (cond
     ((< n m) n)
     ((= n m) 0)
     (else
      (% (- n m) m)))))

;; length of lat
(define length
  (lambda (lat)
    (cond
     ((null? lat) 0)
     ((atom? lat) 1)
     (else (add1 (length (cdr lat)))))))

;; (pick n lat), get the element of lat in position n
(define pick
  (lambda (n lat)
    (cond
     ((zero? (sub1 n)) (car lat))
     (else (pick (sub1 n) (cdr lat))))))

;; (rempick 2 '(1 2 3) => '(1 3)
(define rempick
  (lambda (n lat)
    (cond
     ((zero? (sub1 n)) (cdr lat))
     (else (cons (car lat) (rempick (sub1 n) (cdr lat)))))))

;; (no-nums lat), remove all the numbers of the lat
(define no-nums
  (lambda (lat)
    (cond
     ((null? lat) '())
     ((number? (car lat))
      (no-nums (cdr lat)))
     (else (cons (car lat)
                 (no-nums (cdr lat)))))))

;; (all-nums lat), use all the numbers of the lat
(define all-nums
  (lambda (lat)
    (cond
     ((null? lat) '())
     ((number? (car lat))
      (cons (car lat) (all-nums (cdr lat))))
     (else (all-nums (cdr lat))))))

;; eqan?
(define eqan?
  (lambda (a1 a2)
    (cond
     ((and (number? a1) (number? a2))
      (= a1 a2))
     ((or (number? a1) (number? a2))
      #f)
     (else (eq? a1 a2)))))

;; occur
(define occur
  (lambda (a lat)
    (cond
     ((null? lat) 0)
     ((eq? a (car lat))
      (add1 (occur a (cdr lat))))
     (else (occur a (cdr lat))))))

;; one?
(define one?
  (lambda (n)
    (= n 1)))

;; (rempick n lat) removes the nth atom from a lat
(define rempick
  (lambda (n lat)
    (cond
     ((null? lat) '())
     ((zero? n) lat)
     ((one? n) (cdr lat))
     (else (cons (car lat) (rempick (sub1 n) (cdr lat))))
     )))

;; rember*
(define rember*
  (lambda (a lat)
    (cond
     ((null? lat) '())
     ((atom? (car lat))
      (cond
       ((eq? a (car lat))(rember* a (cdr lat)))
       (else (cons (car lat)(rember* a (cdr lat))))))
     (else (cons (rember* a (car lat)) (rember* a (cdr lat)))))))

;; (insertR* new old l)
(define insertR*
  (lambda (new old l)
    (cond
     ((null? l)'())
     ((atom? (car l))
      (cond
       ((eq? old (car l))
        (cons old (cons new (insertR* new old (cdr l)))))
       (else (cons (car l) (insertR* new old (cdr l))))))
     (else (cons (insertR* new old (car l)) (insertR* new old (cdr l)))))))

;; (occur* a l) counts how many times a occurs in l
(define occur*
  (lambda (a l)
    (cond
     ((null? l) 0)
     ((atom? (car l))
      (cond
       ((eq? a (car l)) (add1 (occur* a (cdr l))))
       (else (occur* a (cdr l)))))
     (else (+ (occur* a (car l)) (occur* a (cdr l)))))))

;; (subst* new old l) replaces all the old to new in l
(define subst*
  (lambda (new old l)
    (cond
     ((null? l) '())
     ((atom? (car l))
      (cond
       ((eq? old (car l))
        (cons new (subst* new old (cdr l))))
       (else (cons (car l) (subst* new old (cdr l))))))
     (else (cons (subst* new old (car l)) (subst* new old (cdr l)))))))

;; insertL*
(define insertL*
  (lambda (new old l)
    (cond
     ((null? l) '())
     ((atom? (car l))
      (cond
       ((eq? old (car l)) (cons new (cons (car l) (insertL* new old (cdr l)))))
       (else (cons (car l) (insertL* new old (cdr l))))))
     (else (cons (insertL* new old (car l)) (insertL* new old (cdr l)))))))

;; (member* a l)
(define member*
  (lambda (a l)
    (cond
     ((null? l) #f)
     ((atom? (car l))
      (or (eq? a (car l))
          (member* a (cdr l))))
     (else (or (member* a (car l))
               (member* a (cdr l)))))))

;; (leftmost l) finds the leftmost atom in a non-empty list of S-expressions that  does not contain  the  empty list
(define leftmost
  (lambda (l)
    (cond
     ((atom? (car l)) (car l))
     (else (leftmost (car l))))))

;; eqlist?
(define eqlist?
  (lambda (l1 l2)
    (cond
     ((and (null? l1) (null? l2)) #t)
     ((or (null? l1) (null? l2)) #f)
     ((and (atom? (car l1)) (atom? (car l2)))
      (and (eq? (car l1) (car l2)) (eqlist? (cdr l1) (cdr l2))))
     ((and (pair? (car l1)) (pair? (car l2)))
      (and (eqlist? (car l1) (car l2)) (eqlist? (cdr l1) (cdr l2))))
     (else #f)
     )))

;; equal?
(define equal?
  (lambda (s1 s2)
    (cond
     ((and (atom? s1) (atom? s2))
      (eq? s1 s2))
     ((or (atom? s1) (atom? s2)) #f)
     (else (eqlist? s1 s2)))))

;; rewrite eqlist? by equal?, recursively
(define eqlist?
  (lambda (l1 l2)
    (cond
     ((and (null? l1) (null? l2)) #t)
     ((or (null? l1) (null? l2)) #f)
     (else
      (and (equal? (car l1) (car l2))
           (eqlist? (cdr l1) (cdr l2)))))))

;; numbered?, determines whether a representation of an arithmetic expression contains only numbers besides the +, x, and t.

(define numbered?
  (lambda (aexp)
    (cond
     ((atom? aexp) (number? aexp))
     ((and
       (not (null? (cdr aexp)))
       (or (eq? (car (cdr aexp)) '+)
           (eq? (car (cdr aexp)) '-)
           (eq? (car (cdr aexp)) '*)
           (eq? (car (cdr aexp)) '**)
           (eq? (car (cdr aexp)) '/)))
      (and
       (numbered? (car aexp))
       (numbered? (car (cdr (cdr aexp))))))
     (else #f))))

;; (value ?) returns natural value of a numbered arithmetic expression
(define value
  (lambda (nexp)
    (cond
     ((not (numbered? nexp)) '())
     ((atom? nexp) nexp)
     ((or
       (null? (value (car nexp)))
       (null? (value (car (cdr (cdr nexp))))))
      '())
     (else
      ;; eval the expression, eg. (eval '(+ 1 2))
      (eval `(,(car (cdr nexp))
              ,(value (car nexp))
              ,(value (car (cdr (cdr nexp)))))
            (scheme-report-environment 5))))))

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

;; set?
(define set?
  (lambda (lat)
    (cond
     ((null? lat) #t)
     ((member? (car lat) (cdr lat))
      #f)
     (else (set? (cdr lat))))))

;; makeset, make a lat to a set
(define makeset
  (lambda (lat)
    (cond
     ((null? lat) '())
     ((member? (car lat) (cdr lat))
      (makeset (cdr lat)))
     (else (cons (car lat) (makeset (cdr lat)))))))

;; subset?
(define subset?
  (lambda (set1 set2)
    (cond
     ((null? set1) #t)
     (else
      (and (member? (car set1) set2)
           (subset? (cdr set1) set2))))))

;; eqset?
(define eqset?
  (lambda (set1 set2)
    (and (subset? set1 set2)
         (subset? set2 set1))))

;; intersect?, check at least one atom in set1 is in set2
(define intersect?
  (lambda (set1 set2)
    (cond
     ((null? set1) #f)
     (else
      (or (member? (car set1) set2)
          (intersect? (cdr set1) set2))))))

;; (intersect set1 set2), get intersect of two set
(define intersect
  (lambda (set1 set2)
    (cond
     ((null? set1) '())
     ((member? (car set1) set2)
      (cons (car set1)
            (intersect (cdr set1) set2)))
     (else
      (intersect (cdr set1) set2)))))

;; (union set1 set2), get union of two set
(define union
  (lambda (set1 set2)
    (cond
     ((null? set1) set2)
     ((member? (car set1) set2)
      (union (cdr set1) set2))
     (else
      (cons (car set1)
            (union (cdr set1) set2))))))

;; (difference set1, set2), return all in set1 that not in set2
(define difference
  (lambda (set1 set2)
    (cond
     ((null? set1) '())
     ((member? (car set1) set2)
      (difference (cdr set1) set2))
     (else
      (cons (car set1)
            (difference (cdr set1) set2))))))

;; (intersectall l-set), return intersect of all set of the list
(define intersectall
  (lambda (l-set)
    (cond
     ;; teminal state
     ((null? (cdr l-set)) (car l-set))
     (else (intersect (car l-set)
                      (intersectall (cdr l-set)))))))

;; (a-pair? list), return whether list has only two element
(define a-pair?
  (lambda (list)
    (equal? 2 (length list))))

;; first
(define first
  (lambda (l)
    (car l)))

;; build, build a list by two atom
(define build
  (lambda (a1 a2)
    (cons a1
          (cons a2 '()))))

;; second
(define second
  (lambda (l)
    (car (cdr l))))

;; third
(define third
  (lambda (l)
    (car (cdr (cdr l)))))

;; (fun? rel), rel means a list of pair; fun? check if (firsts rel) is a set
(define fun?
  (lambda (rel)
    (set? (firsts rel))))

;; (revpair pair), reverse a pair
(define revpair
  (lambda (pair)
    (cond
     ((null? pair) '())
     (else
      (build (second pair) (first pair))))))

;; (revrel rel), reverse a rel
(define revrel
  (lambda (rel)
    (cond
     ((null? rel) '())
     (else
      (cons (revpair (car rel))
            (revrel (cdr rel)))))))

;; (fullfun? rel), like fun?, but require second of the pairs is also a set
(define fullfun?
  (lambda (rel)
    (and
     (fun? rel)
     (fun? (revrel rel)))))

;;; Chapter 8, the power of abstraction
;; rember-f, like rember, but pass a function to check equality
(define rember-f
  (lambda (test? atom list)
    (cond
     ((null? list) '())
     ((test? atom (car list))
      (cdr list))
     (else
      (cons (car list)
            (rember-f test? atom (cdr list)))))))

;; eq?-c
(define eq?-c
  (lambda (x)
    (lambda (y)
      (eq? x y))))

;; rewrite rember-f
(define rember-f
  (lambda (test?)
    (lambda (atom list)
      (cond
       ((null? list) '())
       ((test? atom (car list))
        (cdr list))
       (else
        (cons (car list)
              ((rember-f test?) atom (cdr list))))))))

;; insertL-f
(define insertL-f
  (lambda (test?)
    (lambda (new old lat)
      (cond
       ((null? lat) '())
       ((test? old (car lat)) (cons new lat))
       (else
        (cons (car lat)
              ((insertL-f test?) new old (cdr lat))))))))

;; insertR-f
(define insertR-f
  (lambda (test?)
    (lambda (new old lat)
      (cond
       ((null? lat) '())
       ((test? old (car lat))
        (cons (car lat)
              (cons new (cdr lat))))
       (else
        (cons (car lat)
              ((insertR-f test?) new old (cdr lat))))))))

;; insert-g, can insertL or insertR
(define insert-g
  (lambda (test? seq)
    (lambda (new old lat)
      (cond
       ((null? lat) '())
       ((test? old (car lat))
        (seq new old (cdr lat)) )
       (else
        (cons (car lat)
              ((insert-g test? seq) new old (cdr lat))))))))

;; rewrite insertL
(define insertL
  (insert-g equal?
            (lambda (new old lat)
              (cons new (cons old lat)))))

;; rewrite insertR
(define insertR
  (insert-g equal?
            (lambda (new old lat)
              (cons old (cons new lat)))))

;; rewrite subst, ie. replace
(define subst
  (insert-g equal?
            (lambda (new old lat)
              (cons new lat))))

;; rewrite rember
(define rember
  (lambda (a l)
    ((insert-g equal?
               (lambda (new old l)
                 l))
     #f a l)))

;; multremberT
(define multiremberT
  (lambda (test? lat)
    (cond
     ((null? lat) '())
     ((test? (car lat))
      (multiremberT test? (cdr lat)))
     (else
      (cons (car lat)
            (multiremberT test? (cdr lat)))))))

;; (multirember&co atom list collector), CPS style
;; 遍历list, 将不等于atom的item和等于atom的item分别收集起来，成为两个list, 并作为参数传递给collector
;; (collector newlat seen), newlat都是不等于atom的，而seen是等于atom的items
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

;; multiinsertLR&co
(define multiinsertLR&co
  (lambda (new oldL oldR lat col)
    (cond
     ((null? lat)
      (col '() 0 0))
     ((equal? (car lat) oldL)
      (multiinsertLR&co new oldL oldR (cdr lat)
                        (lambda (l lnum rnum)
                          (col (cons new (cons oldL l))
                               (add1 lnum) rnum))))
     ((equal? (car lat) oldR)
      (multiinsertLR&co new oldL oldR (cdr lat)
                        (lambda (l lnum rnum)
                          (col (cons oldR (cons new l))
                               lnum (add1 rnum)))))
     (else
      (multiinsertLR&co new oldL oldR (cdr lat)
                        (lambda (l lnum rnum)
                          (col (cons (car lat) l)
                               lnum rnum)))))))
;; even?
(define even?
  (lambda (n)
    (= (* (/ n 2) 2) n)))

;; even-only*, filter a list, remove odds
(define even-only*
  (lambda (l)
    (cond
     ((null? l) '())
     ((atom? (car l))
      (cond
       ((even? (car l))
        (cons (car l)
              (even-only* (cdr l))))
       (else
        (even-only* (cdr l)))))
     (else
      (cons (even-only* (car l))
            (even-only* (cdr l)))))))

;; even-only*&co
(define even-only*&co
  (lambda (l col)
    (cond
     ((null? l)
      (col '() 1 0))
     ((atom? (car l))
      (cond
       ((even? (car l))
        (even-only*&co (cdr l)
                       (lambda (newl multi sum)
                         (col (cons (car l) newl)
                              (* (car l) multi)
                              sum))))
       (else
        (even-only*&co (cdr l)
                       (lambda (newl multi sum)
                         (col newl multi
                              (+ (car l) sum)))))))
     (else
      (even-only*&co (car l)
                     (lambda (nl mt sm)
                       (even-only*&co (cdr l)
                                      (lambda (newl multi sum)
                                        (col (cons nl newl)
                                             (* mt multi)
                                             (+ sm sum))))))))))

;;; Chapter 9

;;; functions not total, which may cause dead loop
;; looking, (keep-looking '(3 a 1)) will dead loop
(define keep-looking
  (lambda (a num lat)
    (cond
     ((number? num)
      (keep-looking a (pick num lat) lat))
     (else
      (equal? a num)))))

(define looking
  (lambda (a lat)
    (keep-looking a (pick 1 lat) lat)))

;; shuffle, (shuffle '((a) (b)) will dead loop
(define shuffle
  (lambda (pora)
    (cond
     ((atom? pora) pora)
     ((a-pair? (first pora))
      (shuffle (revpair pora)))
     (else
      (build (first pora)
             (shuffle (second pora)))))))

;; a total function
(define A
  (lambda (n m)
    (cond
     ((zero? n) (add1 m))
     ((zero? m) (A (sub1 n) 1))
     (else
      (A (sub1 n) (A n (sub1 m)))))))

;; 停机问题
#|
(will-stop?)
(define last-try
  (lambda (x)
    (and (will-stop? last-try) (deadloop))))
|#

;; rewrite length, without define
(((lambda (mk-length)
    (mk-length mk-length))
  (lambda (mk-length)
    ((lambda (length)
       (lambda (l)
         (cond
          ((null? l) 0)
          (else
           (add1
            (length (cdr l)))))))
     (lambda (x)
       ((mk-length mk-length) x))))) '(a b c d))

;; Y Combinator
;; lambda f. (lambda x. (f(x x)) lambda x. (f(x x))
;; 不动点,代入可得 (Y f) = (f (Y f))
(define Y
  (lambda (F)
    ((lambda (f)
       (f f))
     (lambda (f)
       (F (lambda (x) ((f f) x)))))))

;;; Chapter 10

;; entry is (keys values), keys and values are all lists
;; lookup-in-entry
(define lookup-in-entry-help
  (lambda (name keys values f)
    (cond
     ((null? keys) (f name))
     ((equal? (car keys) name)
      (car values))
     (else
      (lookup-in-entry-help name
                            (cdr keys)
                            (cdr values)
                            f)))))
(define lookup-in-entry
  (lambda (name entry entry-f)
    (lookup-in-entry-help name
                          (first entry)
                          (second entry)
                          entry-f)))

;; table is a list of entries
(define lookup-in-table
  (lambda (name table table-f)
    (cond
     ((null? table) (table-f name))
     (else
      (lookup-in-entry name
                       (car table)
                       (lambda (name)
                         (lookup-in-table name
                                          (cdr table)
                                          table-f)))))))

;;; types
#|
*const
*quote
*identifier
*lambda
*cond
*application.
|#

;;; try to write a interpreter

(define expression-to-action
  (lambda (e)
    (cond
     ((atom? e)(atom-to-action e))
     (else (list-to-action e)))))

(define atom-to-action
  (lambda (e)
    (cond
     ((number? e) *const)
     ((eq? e #t) *const)
     ((eq? e #f) *const)
     ((eq? e 'cons) *const)
     ((eq? e 'car) *const)
     ((eq? e 'cdr) *const)
     ((eq? e 'null?) *const)
     ((eq? e 'eq?) *const)
     ((eq? e 'atom?) *const)
     ((eq? e 'zero?) *const)
     ((eq? e 'add1) *const)
     ((eq? e 'sub1) *const)
     ((eq? e 'number?) *const)
     (else *identifier))))

(define list-to-action
  (lambda (e)
    (cond
     ((atom? (car e))
      (cond
       ((eq? (car e) (quote quote)) *quote)
       ((eq? (car e) 'lambda) *lambda)
       ((eq? (car e) 'cond) *cond)
       (else *application)))
     (else *application))))

;; the intepreter, it likes eval
(define value
  (lambda (e)
    (meaning e '())))

(define meaning
  (lambda (e table)
    ((expression-to-action e) e table)))

;; *const
(define *const
  (lambda (e table)
    (cond
     ((number? e) e)
     ((eq? e #t) #t)
     ((eq? e #f) #f)
     (else
      (build 'primitive e)))))

;; *quote
(define text-of second)
(define *quote
  (lambda (e table)
    (text-of e)))

;; *identifier
(define initial-table
  (lambda (name)
    name))
(define *identifier
  (lambda (e table)
    (lookup-in-table e table initial-table)))

;; *lambda
(define table-of first)
(define formals-of second)
(define body-of third)
(define *lambda
  (lambda (e table)
    (build 'non-primitive
           ;; (lambda (x) x) eg.
           (cons table (cdr e)))))

;; *cond
(define else?
  (lambda (x)
    (cond
     ((atom? x)
      (eq? x 'else))
     (else #f))))
(define question-of first)
(define answer-of second)
;; evaluate conditions
(define evcon
  (lambda (conds table)
    (cond
     ((else? (question-of (car conds)))
      (meaning (answer-of (car conds)) table))
     ((meaning (question-of (car conds)) table)
      (meaning (answer-of (car conds)) table))
     (else
      (evcon (cdr conds) table)))))
(define *cond
  (lambda (e table)
    (evcon (cdr e) table)))

;; *application, application is func call, like (add1 1)
;; evaluate lists of args
(define evlis
  (lambda (args table)
    (cond
     ((null? args) '())
     (else
      (cons (meaning (car args) table)
            (evlis (cdr args) table))))))

;; ((lambda (x) x) 1)
(define *application
  (lambda (e table)
    (apply
     (meaning (function-of e) table)
     (evlis (arguments-of e) table))))

(define function-of car)
(define arguments-of cdr)

;; primitive and non-primitive
;;(primitive primitive-name) and (non-primitive (table formals body)) to present functions
(define primitive?
  (lambda (l)
    (eq? (first l) 'primitive)))
(define non-primitive?
  (lambda (l)
    (eq? (first l) 'non-primitive)))

;; Apply, run funcs with args
(define apply
  (lambda (fun vals)
    (cond
     ((primitive? fun)
      (apply-primitive
       ;; (primitive cons) eg.
       (second fun) vals))
     ((non-primitive? fun)
      (apply-closure
       ;; (NON
       ;;
       ;;-primitive ((((x)(2))) (x) (add1 x)))
       (second fun) vals)))))

(define :atom?
  (lambda (x)
    (cond
     ((null? x) #f)
     ((atom? x) #t)
     ((eq? (car x) 'primitive) #t)
     ((eq? (car x) 'non-primitive) #t)
     (else #f))))
;; apply-primitive
(define apply-primitive
  (lambda (name vals)
    (cond
     ((eq? name 'cons)
      (cons (first vals) (second vals)))
     ((eq? name 'car)
      (car (first vals)))
     ((eq? name 'cdr)
      (cdr (first vals)))
     ((eq? name 'null?)
      (null? (first vals)))
     ((eq? name 'eq?)
      (eq? (first vals) (second vals)))
     ((eq? name 'atom?)
      (:atom? (first vals)))
     ((eq? name 'zero?)
      (zero? (first vals)))
     ((eq? name 'add1)
      (add1 (first vals)))
     ((eq? name 'sub1)
      (sub1 (first vals)))
     ((eq? name 'number?)
      (number? (first vals))))))
(define new-entry build)
(define extend-table build)
;; apply-closure
(define apply-closure
  (lambda (closure vals)
    (meaning (body-of closure)
             (extend-table
              (new-entry
               (formals-of closure)
               vals)
              (table-of closure)))))

;;; Test

(value 'a)

(lookup-in-table 'c '(((a b c) (1 2 3)) ((d e f) (4 5 6)))
                 (lambda (n) n))
(lookup-in-entry 'd '((a b c) (1 2 3)) (lambda (n) n))

((Y (lambda (length)
      (lambda (l)
        (cond
         ((null? l) 0)
         (else
          (add1 (length (cdr l)))))))) '(a b c))
(shuffle '(((a) b) c))
(a-pair? (first '(a (a b))))

(looking 'a '(6 2 4 a 5 7 3 ))

(even-only*&co '(1 (2 3 (6 5)) 4 3)
               (lambda (l m s)
                 (display "\nmulti of even:\n")
                 (display m)
                 (display "\nsum of odd:\n")
                 (display s)
                 (display "\n")
                 l))
(even-only* '(3 4 (5 6 (8 1)) 5 0))     ; -> '(4 (6 (8)) 0)
(even? 2)
(/ 3 2)

(multiinsertLR&co 'new 'ol 'or '(a b ol c or d or e f ol g)
                  (lambda (l lnum rnum)
                    (display "\nlnum is: ")
                    (display lnum)
                    (display "\nrnum is: ")
                    (display rnum)
                    l))

(multirember&co 'a '(a b c a d e f a g)
                (lambda (newlat seen)
                  (length seen)))       ; -> 3
(multiremberT (lambda (atom)
                (equal? atom 'a))
              '(a b c d a e f))         ; -> '(b c d e f)
(rember 'a '(c a b))                    ; -> '(c b)
(subst 'a 'b '(c b))                    ; -> '(c a)
(insertR 'a 'b '(c b))                  ; -> '(c b a)
(insertL 'a 'b '(c b))                  ; -> '(c a b)
((insert-g equal? #t) 'a 'b '(c b))     ; -> '(c a b)
((insertR-f equal?) 'a 'b '(c b))       ; -> '(c b a)
((insertL-f equal?) 'a 'b '(c b))       ; -> '(c a b)
((eq?-c 3) 5)                           ; -> #f
((rember-f equal?) '(a) '(a b (a) c))

(fullfun? '((a b) (c b)))               ; -> #f
(revrel '((a b) (c d)))                 ; -> '((b a) (d c))
(fun? '((8 3) (4 2) (7 6) (6 2) (3 4))) ; -> #t
(a-pair? '(a (b)))
(intersectall '((a b) (b c d) (e f b))) ; -> '(b)
(difference '(a b c) '(d b e a f))      ; -> '(c)

(occur* 'a '(1 a (2 a) (3 (4 (5 a) a))))
(member* 'chips ' ((potato)  (chip3s ((with) fish) (ch2ips))))
(leftmost '(((a) b) c))
(eqlist? '(a (b)) '(a (b c)))
(eqlist? '(a (b c)) '(a (b c)))
(equal? 'a 'a)                          ; -> #t
(equal? '(a) '(a))                      ; -> #t

(numbered? '(1 + (2 * 3)))              ; -> #t
(numbered? '(1))                        ; -> #f

(value '(5 + (2 * 3)))                  ; -> 11

(edd1 '())
(zub1 '(()))

(set? '(a b c d a))                     ; -> #f
(set? '(apple 3 pear 4 9))              ; -> #t

(makeset '(a b c a d b d))              ; -> '(c a b d)
(subset? '(a b c) '(a c d b))           ; -> #t
(eqset? '(a b c) '(a c b))              ; -> #t

(intersect? '(a b c) '(d e a g))        ; -> #t
(intersect '(a b c) '(d b e a f))       ; -> '(a)
(union '(a b c) '(d b e a f))           ; -> '(c d b e a f)

;; Commandments
#|
1. Always ask, null? for atom/lat, zero? for number; when S-expression, ask (null? l), (atom? (car l)) and else.

2. cons => use to build list

3. When build a list, describe the first typical element, and then cons it onto the natural recursion

4. Always change at least one argument while recurring.(否则无法停止).
It must be changed to be closer to termination.
When lat, use (cdr lat) when number, use (sub1 n)
, when S-expression, use (car l) and (cdr l) if (null? l) is false and (atom? (car l)) is false

5. 考虑终止条件，应选择不改变当前value的条件: when +, use 0; when *, use 1; when cons, use ()

6. Simplify only after the function is correct, 当之前的函数是正确的时候，可以利用相互递归来简化它们。
如`eqlist?'和`equal?'互相依赖

7. Recur  on  the  subparts  that  are  of  the  same  nature:
•  On  the  sublists  of  a  list
•  On  the  sub  expressions  of  an  arithmetic  expression

8. Use help functions to abstract from representations

9. `Abstract' common patterns with a new function.

10. Build functions to collect more than one value at a time.
通过包装function产生新的function, 让新的function来collect本次调用产生的数据

|#
