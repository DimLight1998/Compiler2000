int printf(char *format, ...);
int scanf(char *format, ...);

int StringLength(char *str)
{
    int len;
    len = 0;
    while (str[len] != 0)
    {
        len = len + 1;
    }
    return len;
}

int main()
{
    printf("Please input a string (shorter than 1024 chars):\n");
    char input[1 << 10];
    scanf("%[^\n]", input);
    int inputLen;
    inputLen = StringLength(input);
    if (inputLen == 0)
    {
        printf("Error, you input an empty string!\n");
        return 1;
    }
    else
    {
        int half;
        half = inputLen / 2;
        int i;
        i = 0;
        int isPalindrome;
        isPalindrome = 1;
        for (i = 0; i < half; i = i + 1)
        {
            if (input[i] != input[inputLen - 1 - i])
            {
                isPalindrome = 0;
            }
        }
        if (isPalindrome == 1)
        {
            printf("True\n");
        }
        else
        {
            printf("False\n");
        }
    }

    return 0;
}