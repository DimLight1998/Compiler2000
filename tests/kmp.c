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

int ComputeNext(int *next, char *str)
{
    int length;
    length = StringLength(str);
    if (length == 0)
        return 1;
    next[0] = 0;
    int p;
    p = 1;
    for (p = 1; p < length; p = p + 1)
    {
        int k;
        k = next[p - 1];
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
    char source[1 << 16];
    char pattern[1 << 10];
    int next[1 << 10];
    printf("Please input the source string (shorter than 2^16 chars):\n");
    scanf("%[^\n]", source);
    printf("Please input the pattern string (shorter than 2^10 chars):\n");
    scanf("%[^\n]", pattern);

    int patternLength;
    patternLength = StringLength(pattern);
    if (ComputeNext(next, pattern) != 0)
    {
        printf("Pattern is empty!\n");
        return 1;
    }
    int sourceLength;
    sourceLength = StringLength(source);
    if (sourceLength == 0)
    {
        printf("Source is empty!\n");
        return 1;
    }

    int s;
    s = 0;
    int p;
    p = 0;
    int found;
    found = 0;
    while (s < sourceLength && p < patternLength)
    {
        while (s < sourceLength && p < patternLength && source[s] == pattern[p])
        {
            s = s + 1;
            p = p + 1;
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
                s = s + 1;
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