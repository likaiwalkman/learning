#!/bin/sh

## Variables
# Note there's no space
my_name='test'
my_name='martin'
echo "Double quote: My name is $my_name"
echo 'Single quote: My name is $my_name'

## Strings
# concatation
greeting="hello, "$my_name" !"
greeting_1="hello, ${my_name} !"
echo $greeting $greeting_1
# get string length, using `#`
echo "My name's length is" ${#my_name}                          # 6
# sub string
echo "${my_name}'s substring with index 4~5 is: "${my_name:3:2} # ti
# string replace
echo ${my_name/art}             # min
echo ${my_name/art/###}         # m###in
# default value, works for null/""/0
echo ${my_name:-"default"}      # martin
echo ${nonexist:-"default"}     # default
