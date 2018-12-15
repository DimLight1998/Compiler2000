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

int IsDigit(char c)
{
    return (c <= 57 && c >= 48);
}

int IsSpace(char c)
{
    return (c == 9 || c == 10 || c == 32 || c == 13);
}

int GetOpPrecedence(char op)
{
    // 43 -> +
    // 45 -> -
    // 42 -> *
    // 47 -> /
    // 40 -> (
    // 41 -> )
    if (op == 43 || op == 45)
    {
        return 20;
    }
    else if (op == 42 || op == 47)
    {
        return 30;
    }
    else if (op == 40)
    {
        return 10;
    }
    else if (op == 41)
    {
        return 40;
    }
    else
    {
        return -1;
    }
}

int BinCalc(char op, int lhs, int rhs)
{
    int ans;
    if (op == 43)
    {
        ans = lhs + rhs;
    }
    else if (op == 45)
    {
        ans = lhs - rhs;
    }
    else if (op == 42)
    {
        ans = lhs * rhs;
    }
    else if (op == 47)
    {
        ans = lhs / rhs;
    }
    return ans;
}

int main()
{
    int numStack[1024];
    int numStackSize;
    numStackSize = 0;

    int opStack[1024];
    int opStackSize;
    opStackSize = 0;

    char buffer[65536];
    printf("Please input the string to calculate (shorter than 2^16 chars):\n");
    scanf("%[^\n]", buffer);

    int bufferLength;
    bufferLength = StringLength(buffer);
    int pos;
    pos = 0;
    for (pos = 0; pos < bufferLength; pos = pos + 1)
    {
        char curr;
        curr = buffer[pos];
        if (IsSpace(curr))
        {
        }
        else if (IsDigit(curr))
        {
            if (pos > 0 && IsDigit(buffer[pos - 1]))
            {
                numStack[numStackSize - 1] = numStack[numStackSize - 1] * 10 + curr - 48;
            }
            else
            {
                numStack[numStackSize] = curr - 48;
                numStackSize = numStackSize + 1;
            }
        }
        else
        {
            int precedence;
            precedence = GetOpPrecedence(curr);
            if (precedence == -1)
            {
                printf("Error, unexpected operator.\n");
                return 1;
            }

            if (
                curr != 41 &&
                (opStackSize == 0 || GetOpPrecedence(opStack[opStackSize - 1]) < precedence || curr == 40))
            {
                opStack[opStackSize] = curr;
                opStackSize = opStackSize + 1;
            }
            else
            {
                while (
                    (curr == 41 && opStack[opStackSize - 1] != 40) ||
                    (curr != 41 && opStackSize != 0 && GetOpPrecedence(opStack[opStackSize - 1]) >= precedence))
                {
                    char topOp;
                    topOp = opStack[opStackSize - 1];
                    opStackSize = opStackSize - 1;
                    int rhs;
                    rhs = numStack[numStackSize - 1];
                    int lhs;
                    lhs = numStack[numStackSize - 2];
                    numStack[numStackSize - 2] = BinCalc(topOp, lhs, rhs);
                    numStackSize = numStackSize - 1;
                }
                if (curr == 41)
                {
                    opStackSize = opStackSize - 1;
                }
                else
                {
                    opStack[opStackSize] = curr;
                    opStackSize = opStackSize + 1;
                }
            }
        }
    }

    while (numStackSize > 1)
    {
        char topOp;
        topOp = opStack[opStackSize - 1];
        opStackSize = opStackSize - 1;
        int rhs;
        rhs = numStack[numStackSize - 1];
        int lhs;
        lhs = numStack[numStackSize - 2];
        numStack[numStackSize - 2] = BinCalc(topOp, lhs, rhs);
        numStackSize = numStackSize - 1;
    }

    printf("%d\n", numStack[0]);
    return 0;
}