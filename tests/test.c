void printf(char* format, ...);

int gl;

int sum(int a, int b)
{
char* s;
s = "alpha";
    gl = 8;
    int p;
    p = 3;
    if(p == 1 + a) {
        a = b + 3;
    }
    while(a > 0) {
        a  = a - 1;
        b = b + 1;
        p = b + a;
        do {
          b = b && a;
          p = p - 1;
        } while (p > 0);
    }
    a = a + b;
    b = b * p;
    return a + b;
}

int main()
{
    int a;
    int b;
    a = 3;
    b = 7;
    int c;
    int i;

    for(i = 0; i < 10; i = i + 1) {
        a = a + b;
        b = a;
    }
    c = sum(a * b, a + b);
    gl = gl + gl;
    return 0;
}

void foo() {

}