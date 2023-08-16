public class Generate extends AbstractGenerate{
    public Generate() {

    }

    public Generate(String string) {

    }

    

    /**
     * When this function gets called it throws a CompilationException error with the explanatory message
     */
    @Override
    public void reportError( Token token, String explanatoryMessage ) throws CompilationException{
        throw new CompilationException(explanatoryMessage);
    } 
}
