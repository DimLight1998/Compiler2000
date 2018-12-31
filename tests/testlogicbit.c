int printf(char *format, ...);

int ret2() {
    printf("return 2\n");
    return 2;
}

int ret1() {
    printf("return 1\n");
    return 1;
}

int ret0() {
    printf("return 0\n");
    return 0;
}

int main() {
    printf("%d\n",ret1()&ret2());
    printf("%d\n",ret1()|ret2());
    printf("%d\n",ret1()&&ret2());
    printf("%d\n",ret0()&&ret2());
    printf("%d\n",ret1()||ret2());
    printf("%d\n",ret0()||ret2());
    printf("%d\n",~ret2());
    printf("%d\n",!ret2());
    printf("%d\n",!ret0());
    return 0;
}