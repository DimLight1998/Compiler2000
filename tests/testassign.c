int printf(char *format, ...);
int scanf(char *format, ...);

int main() {
    int a = 1;
    int b = 2;
    int c = 3;
    a = b = c;
    printf("%d %d %d\n", a, b, c);
    return 0;
}