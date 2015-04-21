#!/usr/bin/env bash

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

# Built-in variables:
echo "Script's name: $0"
echo "Last program return value: $?"
echo "Script's PID: $$"
echo "Number of arguments: $#"
echo "Scripts arguments: $@"
echo "Scripts arguments seperated in different variables: $1 $2..."

# Read from input
echo "What's your name"
read NAME
echo hello, $NAME!

# We have the usual if structure:
# use 'man test' for more info about conditionals
# Note that `[` is actually a command
if [ $NAME != $USER ]
then
    echo "Your name isn't your username"
else
    echo "Your name is your username"
fi

if [ 1 == 1 ] && [ 2 == 2 ]
then
    echo "true"
else
    echo "false"
fi

# heredoc
cat > hello.py << EOF
#!/usr/bin/env python
from __future__ import print_function
import sys
print("#stdout", file=sys.stdout)
print("#stderr", file=sys.stderr)
EOF

# redirections
python hello.py > "output.out"
python hello.py 2> "error.err"
python hello.py > "output-and-error.log" 2>&1
python hello.py > /dev/null 2>&1

# Run a command and print its file descriptor (e.g. /dev/fd/123)
# see: man fd
ls <(echo "#helloworld")

# Overwrite output.txt with "#helloworld":
cat > output.out <(echo "#helloworld")
echo "#helloworld" > output.out
echo "#helloworld" | cat > output.out
echo "#helloworld" | tee output.out >/dev/null

# Cleanup temporary files verbosely (add '-i' for interactive)
rm -v output.out error.err output-and-error.log hello.py

# Commands can be substituted within other commands using $( ):
echo "There are $(ls | wc -l) items here."

# Bash uses a case statement that works similarly to switch in Java and C++:
case "$1" in
    #List patterns for the conditions you want to meet
    0)
        echo "There is a zero."
        ;;
    1)
        echo "There is a one."
        ;;
    *)
        echo "It is not null."
        ;;
esac

# for loop
for VARIABLE in {1..3}
do
    echo "$VARIABLE"
done

# Or write it the "traditional for loop" way:
for ((a=1; a <= 3; a++))
do
    echo $a
done

for OUTPUT in $(ls)
do
    wc -l "$OUTPUT"
done
