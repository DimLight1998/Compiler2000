int printf(char *format, ...);
int scanf(char *format, ...);
char *memset(char *ptr, int value, int num);

int StringLength(char *str)
{
    int len = 0;
    while (str[len])
    {
        len++;
    }
    return len;
}

int main()
{
    printf("Please input a string (shorter than 1024 chars):\n");
    char input[1024];
    memset(input, 0, 1024);
    scanf("%1023[^\n]%*c", input);
    int inputLen = StringLength(input);
    if (inputLen == 0)
    {
        printf("Error, you input an empty string!\n");
        return 1;
    }
    else
    {
        int half = inputLen / 2;
        int i = 0;
        int isPalindrome = 1;
        for (i = 0; i < half; i++)
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