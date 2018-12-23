int a;

int printf(char *format, ...);

void process() {
    a = 2;
}

int b[5];

int main() {
    a = 1;
    process();
    printf("%d\n", a);
    int i;
    for(i = 0; i < 5; i = i + 1) {
        b[i] = i * 2;
        printf("%d ", b[i]);
    }
    printf("\n");
    return 0;
}