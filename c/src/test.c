#include <stdio.h>
#include "extern.h"
#include "init.c"

/* A #define line defines a symbolic name or symbolic constant to be a particular string of characters */
#define LOWER 0                 /* lower limit of table */
#define UPPER 300               /* upper limit */
#define STEP 20                 /* step size */

/* macro */
#define max(A, B) (A > B ? A : B)
#define multi(A, B) (A * B)

void testFor(){
    for (int fahr = LOWER; fahr <= UPPER; fahr += STEP) {
        printf("%3d %6.1f\n", fahr, (5.0/9.0)*(fahr-32));
    }
}

/* copy input to output */
void testInputOutput (){
    int c;
    while ((c = getchar()) != EOF) {
        putchar(c);
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

int main() {
    //testFor();
    //testInputOutput();
    //testArray();
    //testFunction();
    //testExtern();
    //testMacro();
    testPoint();
}
