package engine.util;

import java.util.ArrayList;
import java.util.List;
import jeco.core.util.bnf.BnfReader;
import jeco.core.util.bnf.Production;
import jeco.core.util.bnf.Rule;
import jeco.core.util.bnf.Symbol;

/**
 * Checks the correction of a grammar.
 * 
 * @author J. M. Colmenar
 */
public class GrammarChecker extends BnfReader {

    private boolean valid = true;
    private int maxWraps = 5;
    private List<String> errorMsgs;

    public List<String> getErrorMsgs() {
        return errorMsgs;
    }
    
    public GrammarChecker(String grammarFilePath) {
        super();
        errorMsgs = new ArrayList<>();        
        try {
            valid = this.load(grammarFilePath);
        } catch (Exception e) {
            valid = false;
            errorMsgs.add(e.getLocalizedMessage());
        }
    }

    /**
     * Test the grammar at hand and, if the test is not OK, fills in an error
     * message list.
     * 
     * @param errorMsgs
     * @return 
     */
    public boolean checkGrammar() {
        boolean errors = false;
        if (!valid) {
            // The grammar had problems on load.
            errorMsgs.add("Grammar has errors. Check the Console log.");
            errors = true;
        }
        
        // Check recursion
        if (!errors) {
            try {
                this.checkInfiniteRecursion();
            } catch (Exception ex) {
                errorMsgs.add(ex.getLocalizedMessage());
                errors = true;
            }
        }    
            
        // Process all the rules of the grammar:
        if (!errors) {
            int i = 0;
            while (!errors && (i<this.getRules().size())) {
                errors = processRule(this.getRules().get(i));
                if (errors) {
                    errorMsgs.add("Cannot process rule: "+i);
                }
                i++;
            }
        }
        
        return !errors;
    }

    public boolean processRule(Rule rule) {
        boolean errors = false;
        int prod = 0;
        while (!errors && (prod < rule.size())) {
            // Test each production:
            if (rule.get(prod).toString().equals("")) {
                errors = true;
                errorMsgs.add("Empty production in rule: "+rule);
            } else {
                errors = processProduction(rule.get(prod));
                prod++;
            }
        }
        return errors;
    }
    
    
    public boolean processProduction(Production currentProduction) {
        boolean error = false;
        for (Symbol symbol : currentProduction) {
            if (!symbol.isTerminal()) {
                Rule r = findRule(symbol);
                if (r == null) {
                    error = true;
                    errorMsgs.add("No rule for symbol "+symbol);
                }
                // Do not process the rule because they all are sequentially checked.
            }
        }
        return error;
        
    }
    
    
}
