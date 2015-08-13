doubleMe x = x + x
doubleSmallNumber x = if x > 100 then x else x * 2

str = "test"                    -- == ['t', 'e', 's', 't']

1:[2, 3]                        -- => [1, 2, 3] == 1:2:3:[]
"Martin Liu" !! 4               -- => i

[3, 2] > [2, 1, 0]              -- => True
head [1, 2, 3]                  -- => 1
last [1, 2, 3]                  -- => 3
tail [1, 2, 3]                  -- => [2, 3]
init [1, 2, 3]                  -- => [1, 2]
length [1, 2, 3]                -- => 3

take 2 [1, 2, 3]                -- => [1, 2]
drop 2 [1, 2, 3]                -- => [3]
maximum [1, 2, 3]               -- => 3
minimum [1, 2, 3]               -- => 1
sum [1, 2, 3]                   -- => 6
product [1, 2, 3]               -- => 6

-- 中缀函数
elem 1 [1, 2, 3]                -- => True
1 `elem` [1, 2, 3]              -- => True
4 `elem` [1, 2, 3]              -- => False

[1..3]                          -- => [1, 2, 3]
['a'..'d']                      -- => ['a', 'b', 'c', 'd']
