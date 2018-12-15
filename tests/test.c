int printf(char *format, ...);
int scanf(char *format, ...);

int main()
{
    int a[50];
    a[9] = 10;
    int *b;
    b = a;
    int c;
    c = a[8];
    c = b[3];

    return 0;
}