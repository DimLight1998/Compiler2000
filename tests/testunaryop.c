int printf(char *format, ...);
int scanf(char *format, ...);

char buffer[20];

int main() {
    int a = 1;
    int b = 2;
    int c = 3;
    int d = 4;
    int e = 0;
    scanf("%19[^\n]%*c", buffer);
    int i;
    for(i = 0; buffer[i]; i++) {
        e = e * 10 + buffer[i] - 48;
    }
    printf("%d\n", e);
    printf("%d\n", e + a++);
    printf("%d\n", e + ++b);
    printf("%d\n", e + c--);
    printf("%d\n", e + --d);
    return 0;
}