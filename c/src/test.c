#include <stdio.h>
#include "extern.h"
#include <string.h>

/* A #define line defines a symbolic name or symbolic constant to be a particular string of characters */
#define LOWER 0                 /* lower limit of table */
#define UPPER 300               /* upper limit */
#define STEP 20                 /* step size */

/* macro */
#define max(A, B) (A > B ? A : B)
#define multi(A, B) (A * B)

void testFor(){
    printf("fahr to celsius:\n");
    for (int fahr = LOWER; fahr <= UPPER; fahr += STEP) {
        printf("%3d %6.1f\n", fahr, (5.0/9.0)*(fahr-32));
    }
}

/* copy input to output */
void testInputOutput (){
    int c, t = 0, b = 0;
    while ((c = getchar()) != EOF) {
        if (c == '\t'){
            t++;
        } else if (c == ' '){
            b++;
        }
        putchar(c);
        printf("\\t number is: %d\n", t);
        printf("space number is: %d\n", b);
    }
}

void testArray() {
    int c, i, nwhite, nother;
    int ndigit[10];

    nwhite = nother = 0;
    for (i = 0; i < 10; ++i) {
        ndigit[i] = 0;
    }

    while ((c = getchar()) != EOF) {
        if (c >= '0' && c <= '9') {
            ++ndigit[c - '0'];
        } else if (c == ' ' || c == '\n' || c == '\t') {
            ++nwhite;
        } else {
            ++nother;
        }
    }
    printf("digits =");
    for (i = 0; i < 10; i++) {
        printf(" %d", ndigit[i]);
    }
    printf(", white space = %d, other = %d\n", nwhite, nother);
}

int power(int base, int n) {
    int i, p;
    p = 1;
    for (i = 0; i < n; i++) {
        p = p * base;
    }
    return p;
}

void testFunction() {
    for (int i = 0; i < 10; i++) {
        printf("%d %d %d\n", i, power(2, i), power(-3, i));
    }
}

void testExtern() {
    extern int externValue;
    printf("extern value is %d", externValue);
}

/* macro is just text replace */
void testMacro() {
    int i = 3;
    int m = max(3, ++i);
    /* => 3 > ++i ? 3 : ++i */
    printf("max is %d", m);
    printf("\n");
    /* => 2 + 3 * 3 */
    printf("multi is %d", multi(2 + 3, 3));
}

void swap(int *px, int *py) {
    int temp;
    temp = *px;
    *px = *py;
    *py = temp;
}

void testPoint() {
    int i = 1;
    int j = 2;
    swap(&i, &j);
    printf("i is %d, j is %d", i, j);

    int a = 2;
    int* p = &a;
    int* q = &a;
    *p = 1;
    printf("\nq is %d", *q);
}

void testEnum(){
    enum months { JAN = 1, FEB, MAR, APR, MAY, JUN,
                  JUL, AUG, SEP, OCT, NOV, DEC };
    enum months m = NOV;
    printf("%d", m);
}

/* register告诉编译器，将变量存在寄存器以提高运行效率。但编译器可以不鸟它 */
void testRegister(register int i){
    register int j = i + 1;
    printf("i is: %d, j is %d" ,i ,j);
}

void doReverse(char s[], int len, int n){
    if (n == 0){
        return;
    } else {
        int i = n - 1, j = len - n;
        char temp;
        temp = s[i]; s[i] = s[j]; s[j] = temp;
        doReverse(s, len, n - 1);
    }
}

void reverse(char s[]){
    int l = strlen(s);
    doReverse(s, l, l/2);
}

void testReverse(){
    char s[] = "tesabc";
    reverse(s);
    printf("%s", s);
}

void testArg(int argc, char ** argv){
    printf("arg count: %d\n" ,argc - 1);
    printf("args:\n\t");
    while (--argc > 0){
        printf("%s\t" ,*++argv);
    }
}

void testFunc(void (*func)(), int argc, char ** argv){
    func(argc, argv);
}

int main(int argc, char **argv) {
    //testFor();
    //testInputOutput();
    //testArray();
    //testFunction();
    //testExtern();
    //testMacro();
    //testPoint();
    //testEnum();
    //testRegister(1);
    //testReverse();
    //testArg(argc, argv);
    testFunc(testArg, argc, argv);
}
