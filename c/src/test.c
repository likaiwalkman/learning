#include <stdio.h>
#include "extern.h"
#include <string.h>
#include <stdlib.h>

/* A #define line defines a symbolic name or symbolic constant to be a particular string of characters */
#define LOWER 0                 /* lower limit of table */
#define UPPER 300               /* upper limit */
#define STEP 20                 /* step size */

/* macro */
#define max(A, B) (A > B ? A : B)
#define multi(A, B) (A * B)

/* struct */
struct person
{
    char *name;
    int age;
    struct person *friend;
};

struct nlist {
    struct nlist *next;
    char *name;
    char *defn;
};
#define HASHSIZE 101
static struct nlist *hashtab[HASHSIZE];


void testFor(){
    printf("fahr to celsius:\n");
    int fahr;
    for (fahr = LOWER; fahr <= UPPER; fahr += STEP) {
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
    int i;
    for (i = 0; i < 10; i++) {
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

void testStruct(){
    struct person m = {"Martin Liu", 26};
    struct person d = {"Daisy", 26};
    m.friend = &d;
    struct person *martin = &m;
    printf("name: %s\n" ,martin->name);
    printf("age: %d\n" ,martin->age);
    printf("friend: %s\n" ,martin->friend->name);

    // bit-field
    struct {
        unsigned int is_keyword : 1; /* length: 1 */
        unsigned int is_extern : 1;
        unsigned int is_static : 1;
    } flags;
    flags.is_extern = 1;
    printf("%d", flags.is_extern);
}

unsigned hash (char *s){
    unsigned hashval;
    for (hashval = 0; *s != '\0'; s++) {
        hashval = *s + 31 * hashval;
    }
    return hashval % HASHSIZE;
}

struct nlist *lookup(char *s){
    struct nlist *np;
    for (np = hashtab[hash(s)]; np != NULL; np = np->next) {
        if (strcmp(s, np->name) == 0)
            return np;
    }
    return NULL;
}

struct nlist *install(char *name, char *defn){
    struct nlist *np;
    unsigned hashval;
    if ((np = lookup(name)) == NULL){ /* not found */
        np = (struct nlist *)malloc(sizeof(*np));
        if (np == NULL || (np->name = strdup(name)) == NULL){
            return NULL;
        }
        hashval = hash(name);
        np->next = hashtab[hashval];
        hashtab[hashval] = np;
    } else {
        free((void *) np->defn);
    }

    if ((np->defn = strdup(defn)) == NULL)
        return NULL;
    return np;
}

void testHashTable(){
    install("test", "22");
    printf("%s\n", lookup("test")->name);
    printf("%s\n", lookup("test")->defn);
}

void testTypedef(){
    typedef char* string;
    string s = "this is a test";
    printf("%s", s);
}

/*
   union is similar to struct, but it's attributes share the same memory space
   联合在同一时间只有一个成员是可见有效的
 */
void testUnion(){
    union U1 {
        char c;
        int i;
        double d;
    };
    union U1 u;
    u.c = 'A';
    printf("%c\n", u.c);        /* A */
    printf("-------\n");
    u.i = 12345;
    printf("%c\n", u.c);        /* 9, overlap by u.i */
    printf("%d\n", u.i);        /* 12345 */
    printf("-------\n");
    u.d = 3.24;
    printf("%c\n", u.c);        /* empty, overlap by u.d */
    printf("%d\n", u.i);        /* 515396076, overlap by u.d */
    printf("%f\n", u.d);        /* 3.240000 */
    printf("-------\n");
    u.i = 123456;
    printf("%c\n", u.c);
    printf("%d\n", u.i);
    printf("%f\n", u.d);
}

/* test input: "25 Dec 1988" */
void testScanf(){
    int day, year;
    char monthname[20];
    scanf("%d %s %d", &day, monthname, &year);
    printf("%d %s %d" ,day, monthname, year);
}

void testFile(){
    FILE *fp;
    fp = fopen("src/test.c", "r");
    char c;
    while ((c = (char)getc(fp)) != '\n') {
        printf("%c", c);
    }
}

void testCommand(){
    system("ls");
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
    //testFunc(testArg, argc, argv);
    //testStruct();
    //testHashTable();
    //testTypedef();
    //testUnion();
    //testScanf();
    //testFile();
    testCommand();
}
