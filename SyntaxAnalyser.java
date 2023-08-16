import java.io.IOException;

public class SyntaxAnalyser extends AbstractSyntaxAnalyser{
    private String filename;

    /**
     * Constructor, create lexical analyser object
     * @param filename
     */
    public SyntaxAnalyser(String filename){
        this.filename = filename;
        try {
            //initialize lexical analyser
            lex = new LexicalAnalyser(filename);
        } catch (Exception e) {
            // TODO: handle exception
            System.err.println("Lexical Analyser failed to load");
        }
    }

    /**
     * Generate error messages
     * @param expected the expected symbol in string
     * @param symbol the id of the symbol that we got
     * @return
     */
    private String createErrorMessage_nonTerminal(String expected, Token token){
        //"error – expected t, found nextSymbol, at line/char "
        //String errorMessage = "expected <" + expected + ">, found <" + Token.getName(symbol) + ">, at line/char " + Integer.toString(nextToken.lineNumber);
        //String errorMessage = "line/char " + Integer.toString(token.lineNumber) + ": thrown from \"" + Token.getName(token.symbol) + "\" in <" + expected + ">";
        String errorMessage = "File \"" + filename + "\", line/char " + Integer.toString(token.lineNumber) + " in " + expected ;
        //String errorMessage = "line/char " + Integer.toString(token.lineNumber) + ": expected <" + expected + ">, found <" + Token.getName(token.symbol) + ">";
        return errorMessage;
    }

    private String createErrorMessage_Terminal(Token token, String symbol){
        String errorMessage = "File \"" + filename + "\", line/char " + Integer.toString(token.lineNumber) + ": expected \"" + symbol + "\" symbol found \"" + Token.getName(token.symbol) + "\" symbol instead";
        return errorMessage;
    }

    private void printTabs(int num_of_tabs){
        for (int i = 0; i < num_of_tabs; i++){
            System.out.print("\t");
        }
    }
    /**
     * <StatementPart> non terminal function, calls at the very start of the process
     * 
     * statement part ::= begin <statement list> end
     */
    @Override
    public void _statementPart_() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<statement part>");
        int tabs = 0;
        Token currentNexToken = nextToken;
        try {
            // find begin symbol
            printTabs(tabs + 1);
            acceptTerminal(Token.beginSymbol);
            currentNexToken = nextToken;
            // call statementList()
            statementList(tabs + 1);
            // find end symbol
            printTabs(tabs + 1);
            acceptTerminal(Token.endSymbol);
            currentNexToken = nextToken;
        } catch (CompilationException e) {
            // only have to throw statementList error because acceptTerminal error is handled inside acceptTerminal
            throw new CompilationException(createErrorMessage_nonTerminal("<statement part>", currentNexToken), e);
        } catch (Exception e){
            throw e;
        }
        printTabs(tabs);
        myGenerate.finishNonterminal("<statement part>");
    }

    /**
     * Function to check if the next symbol shall be accepted or not, if yes then call insertTerminal() and getNextToken() to move on to the next token. If not accepted call reportError and throw CompilationException
     * @param symbol the next valid token
     */
    @Override
    public void acceptTerminal(int symbol) throws IOException, CompilationException {
        // TODO Auto-generated method stub

        if (nextToken.symbol == symbol){
            myGenerate.insertTerminal(nextToken);
            nextToken = lex.getNextToken();
        }else{
            myGenerate.reportError(nextToken, createErrorMessage_Terminal(nextToken, Token.getName(symbol)));
        }
    }
    
    /**
     * For handling a list of statements
     * 
     * <StatementList> non terminal calls <Statement> and if the next symbol is a ; then recurse until the list runs out
     * 
     * statement list ::= <statement> | <statement list> ; <statement>
     * @throws IOException
     * @throws CompilationException throws CompilationException with the Exception stack
     */
    public void statementList(int tabs) throws IOException, CompilationException {
        printTabs(tabs);
        myGenerate.commenceNonterminal("<statement list>");
        Token currentNexToken = nextToken;
        
        try{
            // single statement
            statement(tabs + 1);

            // multiple statements
            // if the next symbol is a ; then keep recurse statementList()
            if (nextToken.symbol == Token.semicolonSymbol){
                printTabs(tabs + 1);
            acceptTerminal(Token.semicolonSymbol);
                currentNexToken = nextToken;
                statementList(tabs + 1);
            }
        } catch (CompilationException e) {
            // TODO: handle exception
            throw new CompilationException(createErrorMessage_nonTerminal("<statement list>", currentNexToken), e);
        } catch (Exception e){
            throw e;
        }
        
        printTabs(tabs);
        myGenerate.finishNonterminal("<statement list>");
    }

    /**
     * For handling a particular type of statements
     * 
     * <Statement> that calls <assignment statement> or <if statement> or <while statement> or <procedure statement> or <until statement> or <for statement>
     * 
     * statement ::= <assignment statement> | <if statement> | <while statement> | <procedure statement> | <until statement> | <for statement>
     * @throws IOException
     * @throws CompilationException 
     */
    public void statement(int tabs) throws IOException, CompilationException {
        printTabs(tabs);
        myGenerate.commenceNonterminal("<statement>");
        Token currentNexToken = nextToken;
        try {   
            try {
                switch (nextToken.symbol) {
                    case Token.identifier:
                        assignmentStatement(tabs + 1);
                        break;
                    case Token.ifSymbol:
                        ifStatement(tabs + 1);  
                        break;
                    case Token.whileSymbol:
                        whileStatement(tabs + 1);
                        break;
                    case Token.callSymbol:
                        procedureStatement(tabs + 1);
                        break;  
                    case Token.untilSymbol:
                        untilStatement(tabs + 1);
                        break;
                    case Token.forSymbol:
                        forStatement(tabs + 1);
                        break;
                    default:
                        String errorMessage = "File \"" + filename + "\" line/char " + Integer.toString(nextToken.lineNumber) + ": expected " + "<AssignmentStatement> or <IfStatement> or <WhileStatement> or <ProcedureStatement> or <UntilStatement> or <ForStatement" + "> found \"" + Token.getName(nextToken.symbol) + "\" symbol instead";
                        throw new CompilationException(errorMessage);
                }
            } catch (Exception e){
                throw e;
            }
        } catch (CompilationException e) {
            // TODO: handle exception
            throw new CompilationException(createErrorMessage_nonTerminal("<statement>", currentNexToken), e);
        } catch (Exception e){
            throw e;
        }
        printTabs(tabs);
        myGenerate.finishNonterminal("<statement>");
    }

    /**
     * For handling assignment statements
     * 
     * <AssignmentStatement> accept an "identifier" symbol first and after the become symbol it accepts either an <Expression> or a StringConstant
     * 
     * assign statement ::= identifier := <expression> | identifier := stringConstant
     * @throws IOException
     * @throws CompilationException
     */
    public void assignmentStatement(int tabs) throws IOException, CompilationException {
        printTabs(tabs);
        myGenerate.commenceNonterminal("<assignment statement>");
        Token currentNexToken = nextToken;
        try{
            printTabs(tabs + 1);
            acceptTerminal(Token.identifier);
            currentNexToken = nextToken;
            printTabs(tabs + 1);
            acceptTerminal(Token.becomesSymbol);
            currentNexToken = nextToken;


            if (nextToken.symbol == Token.stringConstant) {     //if next token is a string constant
                printTabs(tabs + 1);
                acceptTerminal(Token.stringConstant);
                currentNexToken = nextToken;
            } else {
                expression(tabs + 1);
            }
        } catch (CompilationException e) {
            // TODO: handle exception
            throw new CompilationException(createErrorMessage_nonTerminal("<assignment statement>", currentNexToken), e);
        } catch (Exception e){
            throw e;
        }
        printTabs(tabs);
        myGenerate.finishNonterminal("<assignment statement>");
    }

    /**
     * For handling IF statements
     * 
     * <IfStatement> accepts an "if" symbol then a <Condition> then a "then" symbol then a <StatementList> and if the next one is
     * an "else" symbol then accept it and follow up with another <StatementList>. Accept the <end> and <if> symbols at the very end
     *  
     * if statement ::= if <condition> then <statement list> end if | if <condition> then <statement list> else <statement list> end if
     * @throws IOException
     * @throws CompilationException
     */
    public void ifStatement(int tabs) throws IOException, CompilationException {
        printTabs(tabs);
        myGenerate.commenceNonterminal("<if statement>");
        Token currentNexToken = nextToken;
        try {
            printTabs(tabs + 1);
            acceptTerminal(Token.ifSymbol);
            currentNexToken = nextToken;
            condition(tabs + 1);
            printTabs(tabs + 1);
            acceptTerminal(Token.thenSymbol);
            currentNexToken = nextToken;
            statementList(tabs + 1);
            if (nextToken.symbol == Token.elseSymbol) {
                printTabs(tabs + 1);
            acceptTerminal(Token.elseSymbol);
                currentNexToken = nextToken;
                statementList(tabs + 1);
            }
            printTabs(tabs + 1);
            acceptTerminal(Token.endSymbol);
            currentNexToken = nextToken;
            printTabs(tabs + 1);
            acceptTerminal(Token.ifSymbol);
            currentNexToken = nextToken;
        } catch (CompilationException e) {
            throw new CompilationException(createErrorMessage_nonTerminal("<if statement>", currentNexToken), e);
        } catch (Exception e) {
            throw e;
        }
        printTabs(tabs);
        myGenerate.finishNonterminal("<if statement>");
    }


    /**
     * For handling WHILE statements
     * 
     * <WhileStatement> accepts a "while" symbol then a <Condition> and "loop" symbol follow up with a <StatementList> then "end" and "loop" symbols to finish up
     * 
     * while statement ::= while <condition> loop <statement list> end loop
     * @throws IOException
     * @throws CompilationException
     */
    public void whileStatement(int tabs) throws IOException, CompilationException {
        printTabs(tabs);
        myGenerate.commenceNonterminal("<while statement>");
        Token currentNexToken = nextToken;
        try {
            printTabs(tabs + 1);
            acceptTerminal(Token.whileSymbol);
            currentNexToken = nextToken;
            condition(tabs + 1);
            printTabs(tabs + 1);
            acceptTerminal(Token.loopSymbol);
            currentNexToken = nextToken;
            statementList(tabs + 1);
            printTabs(tabs + 1);
            acceptTerminal(Token.endSymbol);
            currentNexToken = nextToken;
            printTabs(tabs + 1);
            acceptTerminal(Token.loopSymbol);
            currentNexToken = nextToken;

        } catch (CompilationException e) {
            throw new CompilationException(createErrorMessage_nonTerminal("<while statement>", currentNexToken), e);
        } catch (Exception e) {
            throw e;
        }
        printTabs(tabs);
        myGenerate.finishNonterminal("<while statement>");
    }

    /**
     * For handling Procedure Statements
     * 
     * <ProcedureStatement> accepts a "call" symbol then an "identifier" symbol and then a "(" symbol then an <ArgumentList> and finish up with a ")" symbol
     * 
     * procedure statement ::= call identifier ( <argument list> )
     * @throws IOException
     * @throws CompilationException
     */
    public void procedureStatement(int tabs) throws IOException, CompilationException {
        printTabs(tabs);
        myGenerate.commenceNonterminal("<procedure statement>");
        Token currentNexToken = nextToken;
        try {
            printTabs(tabs + 1);
            acceptTerminal(Token.callSymbol);
            currentNexToken = nextToken;
            printTabs(tabs + 1);
            acceptTerminal(Token.identifier);
            currentNexToken = nextToken;
            printTabs(tabs + 1);
            acceptTerminal(Token.leftParenthesis);
            currentNexToken = nextToken;
            argumentList(tabs + 1);
            printTabs(tabs + 1);
            acceptTerminal(Token.rightParenthesis);
            currentNexToken = nextToken;
        } catch (CompilationException e) {
            throw new CompilationException(createErrorMessage_nonTerminal("<procedure statement>", currentNexToken), e);
        } catch (Exception e) {
            throw e;
        }
        printTabs(tabs);
        myGenerate.finishNonterminal("<procedure statement>");
    }


    /**
     * For handling UNTIL statements
     * 
     * <UntilStatement> accepts "do" symbol then a <StatementList> and then an "until" symbol following up with a <Condition>
     * 
     * <until statement> ::= do <statement list> until <condition>
     * @throws IOException
     * @throws CompilationException
     */
    public void untilStatement(int tabs) throws IOException, CompilationException {
        printTabs(tabs);
        myGenerate.commenceNonterminal("<until statement>");
        Token currentNexToken = nextToken;
        try {
            printTabs(tabs + 1);
            acceptTerminal(Token.doSymbol);
            currentNexToken = nextToken;
            statementList(tabs + 1);
            printTabs(tabs + 1);
            acceptTerminal(Token.untilSymbol);
            currentNexToken = nextToken;
            condition(tabs + 1);
        } catch (CompilationException e) {
            throw new CompilationException(createErrorMessage_nonTerminal("<until statement>", currentNexToken), e);
        } catch (Exception e) {
            throw e;
        }
        printTabs(tabs);
        myGenerate.finishNonterminal("<until statement>");
    }

    /**
     * For handling FOR statements
     * 
     * <ForStatement> accepts a "for" and "(" symbol then a <AssignmentStatement> <Condition> <AssignmentStatement> seperated by a ";" symbol and a ")" symbol after. Then it
     * takes in a "do" symbol <StatementList> and finish up with the "end" and "loop" symbols
     * 
     * for statement ::= for ( <assignment statement> ; <condition> ; <assignment statement> ) do <statement list> end loop
     * @throws IOException
     * @throws CompilationException
     */
    public void forStatement(int tabs) throws IOException, CompilationException {
        printTabs(tabs);
        myGenerate.commenceNonterminal("<for statement>");
        Token currentNexToken = nextToken;
        try {
            printTabs(tabs + 1);
            acceptTerminal(Token.forSymbol);
            currentNexToken = nextToken;
            printTabs(tabs + 1);
            acceptTerminal(Token.leftParenthesis);
            currentNexToken = nextToken;
            assignmentStatement(tabs + 1);
            printTabs(tabs + 1);
            acceptTerminal(Token.semicolonSymbol);
            currentNexToken = nextToken;
            condition(tabs + 1);
            printTabs(tabs + 1);
            acceptTerminal(Token.semicolonSymbol);
            currentNexToken = nextToken;
            assignmentStatement(tabs + 1);
            printTabs(tabs + 1);
            acceptTerminal(Token.rightParenthesis);
            currentNexToken = nextToken;
            printTabs(tabs + 1);
            acceptTerminal(Token.doSymbol);
            currentNexToken = nextToken;
            statementList(tabs + 1);
            printTabs(tabs + 1);
            acceptTerminal(Token.endSymbol);
            currentNexToken = nextToken;
            printTabs(tabs + 1);
            acceptTerminal(Token.loopSymbol);
            currentNexToken = nextToken;
        } catch (CompilationException e) {
            throw new CompilationException(createErrorMessage_nonTerminal("<for statement>", currentNexToken), e);
        } catch (Exception e) {
            throw e;
        }
        printTabs(tabs);
        myGenerate.finishNonterminal("<for statement>");
    }

    /**
     * For handling list of arguments
     * 
     * <ArgumentList> accepts infinite "identifier" symbol seperated by a "," symbol
     * 
     * argument list ::= identifier | <argument list> , identifier
     * @throws IOException
     * @throws CompilationException
     */
    public void argumentList(int tabs) throws IOException, CompilationException {
        printTabs(tabs);
        myGenerate.commenceNonterminal("<argument list>");
        Token currentNexToken = nextToken;
        try {
            printTabs(tabs + 1);
            acceptTerminal(Token.identifier);
            currentNexToken = nextToken;
            if (nextToken.symbol == Token.commaSymbol) {
                printTabs(tabs + 1);
            acceptTerminal(Token.commaSymbol);
                currentNexToken = nextToken;
                argumentList(tabs + 1);
            }
        } catch (CompilationException e) {
            throw new CompilationException(createErrorMessage_nonTerminal("<argument list>", currentNexToken), e);
        } catch (Exception e) {
            throw e;
        }
        printTabs(tabs);
        myGenerate.finishNonterminal("<argument list>");
    }

    
    /**
     * For handling Conditions
     * 
     * <Condition> accepts a "identifier" symbol followed by a <ConditionOperator> and any one of out all three "identifier" "numberConstant" "stringConstant" symbols
     * 
     * condition ::= identifier <conditional operator> identifier | identifier <conditional operator> numberConstant | identifier <conditional operator> stringConstant
     * @throws IOException
     * @throws CompilationException
     */
    public void condition(int tabs) throws IOException, CompilationException {
        printTabs(tabs);
        myGenerate.commenceNonterminal("<condition>");
        Token currentNexToken = nextToken;
        try {
            printTabs(tabs + 1);
            acceptTerminal(Token.identifier);
            currentNexToken = nextToken;

            //???? might not need to throw new exception
            conditionalOperator(tabs + 1);
            
            if (nextToken.symbol == Token.identifier) {
                printTabs(tabs + 1);
            acceptTerminal(Token.identifier);
                currentNexToken = nextToken;
            } else if (nextToken.symbol == Token.numberConstant) {
                printTabs(tabs + 1);
            acceptTerminal(Token.numberConstant);
                currentNexToken = nextToken;
            } else if (nextToken.symbol == Token.stringConstant) {
                printTabs(tabs + 1);
            acceptTerminal(Token.stringConstant);
                currentNexToken = nextToken;
            }
            
        } catch (CompilationException e) {
            throw new CompilationException(createErrorMessage_nonTerminal("<condition>", currentNexToken), e);
        } catch (Exception e) {
            throw e;
        }
        printTabs(tabs);
        myGenerate.finishNonterminal("<condition>");
    }


    /**
     * For handling conditional operators
     * 
     * <ConditionalOperator> accepts any of these symbols, ">" ">=" "=" "/=" "<" "<="
     * 
     * <conditional operator> ::= > | >= | = | /= | < | <=
     * @throws IOException
     * @throws CompilationException
     */
    public void conditionalOperator(int tabs) throws IOException, CompilationException {
        printTabs(tabs);
        myGenerate.commenceNonterminal("<conditional operator>");
        Token currentNexToken = nextToken;
        try {
            switch (nextToken.symbol) {
                case Token.greaterThanSymbol:
                    printTabs(tabs + 1);
            acceptTerminal(Token.greaterThanSymbol);
                    currentNexToken = nextToken;
                    break;
                case Token.greaterEqualSymbol:
                    printTabs(tabs + 1);
            acceptTerminal(Token.greaterEqualSymbol);
                    currentNexToken = nextToken;
                    break;
                case Token.equalSymbol:
                    printTabs(tabs + 1);
            acceptTerminal(Token.equalSymbol);
                    currentNexToken = nextToken;
                    break;
                case Token.notEqualSymbol:
                    printTabs(tabs + 1);
            acceptTerminal(Token.notEqualSymbol);
                    currentNexToken = nextToken;
                    break;
                case Token.lessThanSymbol:
                    printTabs(tabs + 1);
            acceptTerminal(Token.lessThanSymbol);
                    currentNexToken = nextToken;
                    break;
                case Token.lessEqualSymbol:
                    printTabs(tabs + 1);
            acceptTerminal(Token.lessEqualSymbol);
                    currentNexToken = nextToken;
                    break;
                default:
                    myGenerate.reportError(nextToken, createErrorMessage_Terminal(nextToken, "ConditionalOperator: greaterThanSymbol, greaterEqualSymbol, equalSymbol, notEqualSymbol, lessThanSymbol, lessEqualSymbol"));
            }
        } catch (CompilationException e) {
            throw new CompilationException(createErrorMessage_nonTerminal("<conditional operator>", currentNexToken), e);
        } catch (Exception e) {
            throw e;
        }
        printTabs(tabs);
        myGenerate.finishNonterminal("<conditional operator>");
    }


    /**
     * For handling expressions
     * 
     * <Expression> accepts infinite amount of <Term> seperated by a "+" or "-" symbols
     * 
     * expression ::= <term> | <expression> + <term> | <expression> - <term>
     * @throws IOException
     * @throws CompilationException
     */
    public void expression(int tabs) throws IOException, CompilationException {
        printTabs(tabs);
        myGenerate.commenceNonterminal("<expression>");
        Token currentNexToken = nextToken;
        try {
            term(tabs + 1);

            if(nextToken.symbol == Token.plusSymbol) {
                printTabs(tabs + 1);
            acceptTerminal(Token.plusSymbol);
                currentNexToken = nextToken;
                expression(tabs + 1);
            } else if (nextToken.symbol == Token.minusSymbol) {
                printTabs(tabs + 1);
            acceptTerminal(Token.minusSymbol);
                currentNexToken = nextToken;
                expression(tabs + 1);
            }

        } catch (CompilationException e) {
            throw new CompilationException(createErrorMessage_nonTerminal("<expression>", currentNexToken), e);
        } catch (Exception e) {
            throw e;
        }
        printTabs(tabs);
        myGenerate.finishNonterminal("<expression>");
    }

    /**
     * For handling terms
     * 
     * <Term> accepts infinite amounts of <Factor> seperated by a "*" or "/" symbol
     * 
     * term ::= <factor> | <term> * <factor> | <term> / <factor>
     * @throws IOException
     * @throws CompilationException
     */
    public void term(int tabs) throws IOException, CompilationException {
        printTabs(tabs);
        myGenerate.commenceNonterminal("<term>");
        Token currentNexToken = nextToken;
        try {
            factor(tabs + 1);

            if(nextToken.symbol == Token.timesSymbol) {
                printTabs(tabs + 1);
            acceptTerminal(Token.timesSymbol);
                currentNexToken = nextToken;
                term(tabs + 1);
            } else if (nextToken.symbol == Token.divideSymbol) {
                printTabs(tabs + 1);
            acceptTerminal(Token.divideSymbol);
                currentNexToken = nextToken;
                term(tabs + 1);
            }
        } catch (CompilationException e) {
            throw new CompilationException(createErrorMessage_nonTerminal("<term>", currentNexToken), e);
        } catch (Exception e) {
            throw e;
        }
        printTabs(tabs);
        myGenerate.finishNonterminal("<term>");
    }

    /**
     * For handling factors
     * 
     * <Factor> accepts and "identifier" symbol or a "numberConstant" symbol or an <Expression> between a "(" and ")" symbol
     * 
     * factor ::= identifier | numberConstant | ( <expression> )
     * @throws IOException
     * @throws CompilationException
     */
    public void factor(int tabs) throws IOException, CompilationException {
        printTabs(tabs);
        myGenerate.commenceNonterminal("<factor>");
        Token currentNexToken = nextToken;
        try {
            if (nextToken.symbol == Token.identifier) {
                printTabs(tabs + 1);
            acceptTerminal(Token.identifier);
                currentNexToken = nextToken;
            } else if (nextToken.symbol == Token.numberConstant) {
                printTabs(tabs + 1);
            acceptTerminal(Token.numberConstant);
                currentNexToken = nextToken;
            } else {
                printTabs(tabs + 1);
            acceptTerminal(Token.leftParenthesis);
                currentNexToken = nextToken;
                expression(tabs + 1);
                printTabs(tabs + 1);
            acceptTerminal(Token.rightParenthesis);
                currentNexToken = nextToken;
            }
        } catch (CompilationException e) {
            throw new CompilationException(createErrorMessage_nonTerminal("<factor>", currentNexToken), e);
        } catch (Exception e) {
            throw e;
        }
        printTabs(tabs);
        myGenerate.finishNonterminal("<factor>");
    }
}
