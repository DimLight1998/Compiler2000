int printf(char *format, ...);
int scanf(char *format, ...);
char *memset(char *addr, int value, int size);

int StringLength(char *str)
{
    int len = 0;
    while (str[len])
    {
        len++;
    }
    return len;
}

int ComputeNext(int *next, char *str)
{
    int length = StringLength(str);
    if (length == 0)
        return 1;
    next[0] = 0;
    int p = 1;
    for (p = 1; p < length; p++)
    {
        int k = next[p - 1];
        while (k > 0 && str[k] != str[p])
        {
            k = next[k - 1];
        }

        if (k == 0 && str[p] != str[0])
        {
            next[p] = 0;
        }
        else
        {
            next[p] = k + 1;
        }
    }
    return 0;
}

int main()
{
    char source[65536];
    char pattern[1024];
    int next[1024];
    memset(source, 0, 65536);
    memset(pattern, 0, 1024);
    printf("Please input the source string (shorter than 2^16 chars):\n");
    scanf("%65535[^\n]%*c", source);
    printf("Please input the pattern string (shorter than 2^10 chars):\n");
    scanf("%1023[^\n]%*c", pattern);

    int patternLength = StringLength(pattern);
    if (ComputeNext(next, pattern) != 0)
    {
        printf("Pattern is empty!\n");
        return 1;
    }
    int sourceLength = StringLength(source);
    if (sourceLength == 0)
    {
        printf("Source is empty!\n");
        return 1;
    }

    int s = 0;
    int p = 0;
    int found = 0;
    while (s < sourceLength && p < patternLength)
    {
        while (s < sourceLength && p < patternLength && source[s] == pattern[p])
        {
            s++;
            p++;
        }

        if (p == patternLength)
        {
            printf("%d ", s - p);
            s = s - p + 1;
            p = 0;
            found = 1;
        }
        else if (s != sourceLength)
        {
            if (p == 0)
            {
                s++;
            }
            else
            {
                p = next[p - 1];
            }
        }
    }

    if (!found)
    {
        printf("False\n");
    }
    else
    {
        printf("\n");
    }

    return 0;
}