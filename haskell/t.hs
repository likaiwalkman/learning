-- ranges
[1..20]
[1, 3..15]
take 10 (cycle [1, 2, 3])
take 10 (repeat 5)

-- List comprehension
[x * 2 | x <- [1..10], x * 2 > 12]
[x | x <- [50..100], x `mod` 7 == 3]

boomBangs xs = [ if x < 10 then "BOOM!" else "BANG!" | x <- xs, odd x]

length' xs = sum [1 | _ <- xs]
removeNonUppercase st = [c | c <- st, c `elem` ['A'..'Z']]


-- Tuple
fst (8,11)                      -- 8
snd (8,11)                      -- 11

zip [1,2,3,4,5] [5,5,5]         -- [(1,5),(2,5),(3,5)]

---------------------------------
-- Types
-- :t 'a'                          -- 'a' :: Char
-- :t 1                            -- 1 :: Num a => a
-- :t True                         -- True :: Bool
-- :t "HELLO!"                     -- "HELLO!" :: [Char]
-- :t (True, 'a')                  -- (True, 'a') :: (Bool, Char)

addThree :: Int -> Int -> Int -> Int
addThree x y z = x + y + z

-- Int, for 32-bit machine, have range from -2^31 to 2^31
-- Integer, no limited
-- Float
-- Double
-- Bool
-- Char
factorial :: Integer -> Integer
factorial n = product [1..n]


--------------------------------
-- Typeclasses
-- Eq, means that the type of a is Eq. Eq is implemented `==` and `\=`
-- :t (==)                         -- (==) :: Eq a => a -> a -> Bool
-- Ord, it's used for compare
-- :t (>=)                         -- (>=) :: Ord a => a -> a -> Bool
-- Show, can be transformed to String
show 3                          -- "3"
-- Read, can be transformed from String
read "3" + 1                    -- 4
-- Enum
['a'..'e']
-- Bounded, has range
minBound :: Char                -- '\NUL'
-- Num, number
-- Integral, Int and Integer
-- Floating, Float and Double


--------------------------------
-- Pattern matching
lucky :: (Integral a) => a -> String
lucky 7 = "LUCKY NUMBER SEVEN!"
lucky x = "Sorry, you're out of luck, pal!"

capital :: String -> String
capital "" = "Empty string, whoops!"
capital all@(x:xs) = "The first letter of " ++ all ++ " is " ++ [x]

-- Guards
bmiTell :: (RealFloat a) => a -> String
bmiTell bmi
    | bmi <= 18.5 = "You're underweight, you emo, you!"
    | bmi <= 25.0 = "You're supposedly normal. Pffft, I bet you're ugly!"
    | bmi <= 30.0 = "You're fat! Lose some weight, fatty!"
    | otherwise   = "You're a whale, congratulations!"

myCompare :: (Ord a) => a -> a -> Ordering
a `myCompare` b
    | a > b     = GT
    | a == b    = EQ
    | otherwise = LT

-- use where to create alias
initials :: String -> String -> String
initials firstname lastname = [f] ++ ". " ++ [l] ++ "."
    where (f:_) = firstname
          (l:_) = lastname

-- use let for local
4 * (let a = 9 in a + 1) + 2    -- 42
(let a = 2; b = 3 in a*b, let foo="Hey "; bar = "there!" in foo ++ bar) -- (6,"Hey there!")

-- use case expression
describeList :: [a] -> String
describeList xs = "The list is " ++ case xs of [] -> "empty."
                                               [x] -> "a singleton list."
                                               xs -> "a longer list."
-- it's same as:
describeList :: [a] -> String
describeList xs = "The list is " ++ what xs
    where what [] = "empty."
          what [x] = "a singleton list."
          what xs = "a longer list."


-- Curring
applyTwice :: (a -> a) -> a -> a
applyTwice f x = f (f x)
applyTwice (+3) 10              -- 16


-- Lambda, using `\`
(\x -> x + 1) 2                 -- 3
-- fold is reduce
sum' :: (Num a) => [a] -> a
sum' xs = foldl (\acc x -> acc + x) 0 xs


-- $
f a b c                         -- => ((f a) b) c
f $ a b c                       -- => (f ((a b) c))

-- .
f . g = \x -> f (g x)
map (negate . sum . tail) [[1..5],[3..6],[1..7]] -- [-14, -15, -27]
