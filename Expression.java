package expr_eval;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structure.Stack;

/*Purpose of program :
Input a text file which contains the values of specific variables, either scalars or arrays, then user can subsequently enter a 
mathematical expression which uses the variables in the text file to compute the value of the expression. This program reads from 
the text file, loads content into respective variables, and implements a two stack algorithm to compute the value of expression. This 
program only can handle the following operations: + - * / with parentheses. Separate Driver Required. 
Example:
test1.txt

a 4
b 5
c (2,3) (4,5)

Enter Expression:  a + c[a] + c[2*(b-a)]
Result: 12.0


*/ 
public class Expression {

	String expr;                
	ArrayList<ScalarVariable> scalars;   
	ArrayList<ArrayVariable> arrays;
    public static final String delims = " \t*+-/()[]";
    public Expression(String expr) {
        this.expr = expr;
    }

    /**
     * Populates the scalars and arrays lists with characters for scalar and array
     * variables in the expression. For every variable, a SINGLE variable is created and stored,
     * even if it appears more than once in the expression.
     * At this time, values for all variables are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     */
    public void buildVariable() {
    	scalars = new ArrayList<ScalarVariable>();
    	 arrays = new ArrayList<ArrayVariable>();
    	String alpha = "";
    	String beta = this.expr;
    	for (int i = 0; i < beta.length(); i++) {
    		if (beta.charAt(i) != ' ') {
    			alpha += beta.charAt(i);
    		}
    	}


    	for (int j = 0; j < alpha.length(); j++) {
    			char x = alpha.charAt(j);
    			boolean arrtype = false;
    		if (Character.isLetter(x)) {
    			String curname = "";
    			while (j < alpha.length() && Character.isLetter(alpha.charAt(j))) {
    				curname += alpha.charAt(j);
    				j++;
    				}
    			if (j < alpha.length() && alpha.charAt(j) == '[') {
    				arrtype = true;
    			}
    			if (arrtype) {
    				if (!containsarr(curname)) {
    				ArrayVariable arrcurr= new ArrayVariable(curname);
    				arrays.add(arrcurr);
    				}
    			}
    			else {
    				if (!containsscal(curname)) {
    				ScalarVariable sccurr = new ScalarVariable(curname);
    				scalars.add(sccurr);
    				}
    			}
    		}
    	}
    	this.expr = alpha;
    }
    
    private boolean containsscal (String a) {
    	if (scalars.size() == 0) {return false;}
    	for (int i = 0; i < scalars.size(); i++) {
    		if (a.equals(scalars.get(i).name)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean containsarr (String a) {
    	if (arrays.size() == 0) {return false;}
    	for (int i = 0; i < arrays.size(); i++) {
    		if (a.equals(arrays.get(i).name)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Loads values for scalars and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     */
    public void loadVariableValues(Scanner sc) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String varl = st.nextToken();
            ScalarVariable scal = new ScalarVariable(varl);
            ArrayVariable arr = new ArrayVariable(varl);
            int scali = scalars.indexOf(scal);
            int arri = arrays.indexOf(arr);
            if (scali == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar character
                scalars.get(scali).value = num;
            } else { // array character
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,value) pairs
                while (st.hasMoreTokens()) {
                    String tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    
    /**
     * Evaluates the expression, and can use RECURSION to evaluate subexpressions and to evaluate array 
     * subscript expressions.
     * 
     * @param scalars The scalar array list, with values for all scalar items
     * @param arrays The array array list, with values for all array items
     * 
     * @return Result of evaluation
     */

    public double evaluate() {
    	System.out.println(this.expr);
    	Stack<Character> operators = new Stack<Character>();
    	Stack<Double> operands = new Stack<Double>();
    	for (int i = 0; i < expr.length(); i++) {
    		boolean arrtype = false;
    		char y = expr.charAt(i);
    		if (operators.isEmpty() && CharIsOp(y)) {
    			operators.push(y);
    			continue;
    		}
    		else if (CharIsOp(y)) {
    			char w = operators.peek();
    			 if (!isLower(w)) {
    				 double z1 = operands.pop();
    				 double z2 = operands.pop();
    				 char theta = operators.pop();
    				 double result = eval(z2,z1,theta);
    				 operands.push(result);
    				 operators.push(y);
    			 }
    			 else if (isLower(w) && isLower(y)) {
    				 double z1 = operands.pop();
    				 double z2 = operands.pop();
    				 char theta = w;
    				 operands.push(eval(z2,z1,theta));
    				 operators.push(y);
    			 }
    			 else {
    				 operators.push(y);
    			 }
    			 continue; 
    		}
    		if (Character.isLetter(y)) {
    			String varname = "";
    			while(i < expr.length() && Character.isLetter(expr.charAt(i))) {
    				varname += expr.charAt(i);
    				i++;
    			}
    			
    			if (i < expr.length() && expr.charAt(i) == '[') {
    				arrtype = true;
    			}
    			if (arrtype) {
    				int g = i;
    				i = arrfactor(i,this.expr);
    				String cac = this.expr.substring(g+1,i);
    				int gk = (int) evalexpr(cac);
    				operands.push(this.arrcheck(gk, varname));
    			}
    			else {
    				operands.push(this.scalcheck(varname));
    			}
    			i--;
    			continue;
    		}
    		if (Character.isDigit(y)) {
    			String consname = "";
    			while (i < expr.length() && Character.isDigit(expr.charAt(i))) {
    				 consname += expr.charAt(i);
    				 i++;
    			}
    			double ae = Double.parseDouble(consname);
    			operands.push(ae);
    			i--;
    			continue;
    		}
    		if (expr.charAt(i) == '(') {
    			String naam = "";
    			i++;
    			int counter = 1;
    				while (counter != 0) {
    					if (expr.charAt(i) == '(') {counter++;}
    					if (expr.charAt(i) == ')') {counter--;}
    					if (counter == 0) {break;}
    					naam += expr.charAt(i);
    					i++;
    				}
    			double edam = evalexpr(naam);
    			operands.push(edam);
    		}

    	}
    		while (operands.size() > 1 && operators.size() > 0) {
    			char et = operators.pop();
    			double r1 = operands.pop();
    			double r2 = operands.pop();
    			double res = eval (r2,r1,et);
    			operands.push(res);
    		}
    		return operands.pop();
    }
    
  private double evalexpr (String a) {
	  Stack<Character> operators = new Stack<Character>();
  	Stack<Double> operands = new Stack<Double>();
  	for (int i = 0; i < a.length(); i++) {
  		boolean arrtype = false;
  		char y = a.charAt(i);
  		if (operators.isEmpty() && CharIsOp(y)) {
  			operators.push(y);
  			continue;
  		}
  		else if (CharIsOp(y)) {
  			char w = operators.peek();
  			 if (!isLower(w)) {
  				 double z1 = operands.pop();
  				 double z2 = operands.pop();
  				 char theta = operators.pop();
  				 double result = eval(z2,z1,theta);
  				 operands.push(result);
  				 operators.push(y);
  			 }
  			 else if (isLower(w) && isLower(y)) {
  				 double z1 = operands.pop();
  				 double z2 = operands.pop();
  				 char theta = w;
  				 operands.push(eval(z2,z1,theta));
  				 operators.push(y);
  			 }
  			 else {
  				 operators.push(y);
  			 }
  			 continue; 
  		}
  		if (Character.isLetter(y)) {
  			String varname = "";
  			while(i < a.length() && Character.isLetter(a.charAt(i))) {
  				varname += a.charAt(i);
  				i++;
  			}
  			
  			if (i < a.length() && a.charAt(i) == '[') {
  				arrtype = true;
  			}
  			if (arrtype) {
  				int g = i;
  				i = arrfactor(i,a);
  				String exp = a.substring(g+1,i);
  				int aeta = (int) evalexpr(exp); 
  				operands.push(this.arrcheck(aeta,varname));
  			}
  			else {
  				operands.push(this.scalcheck(varname));
  			}
  			i--;
  			continue;
  		}
  		if (Character.isDigit(y)) {
  			String consname = "";
  			while (i < a.length() && Character.isDigit(a.charAt(i))) {
  				 consname += a.charAt(i);
  				 i++;
  			}
  			double ae = Double.parseDouble(consname);
  			operands.push(ae);
  			i--;
  			continue;
  		}
  		if (y == '(') {
			String dhokla = "";
			i++;
			int count = 1;
			while (count != 0) {
				if (a.charAt(i) == '(') {count++;}
				if (a.charAt(i) == ')') {count--;}
				if (count == 0) {break;}
				dhokla += a.charAt(i);
				i++;
			}
		double ketone = evalexpr(dhokla);
		operands.push(ketone);
			}
		}
  		while (operands.size() > 1 && operators.size() > 0) {
  			char et = operators.pop();
  			double r1 = operands.pop();
  			double r2 = operands.pop();
  			double res = eval (r2,r1,et);
  			operands.push(res);
  		}
  		return operands.pop();
    }
  
    private static int prenfactor (int i, String a) {
    	int kappa = i+1;
    	int bcount = 1;
    	int res = 0;
    	for (int j = kappa; j < a.length(); j++) {
    		if (a.charAt(j) == '[') {
    			bcount++;
    		}
    		if (a.charAt(j) == ']') {
    			bcount--;
    		}
    		if (bcount == 0) {
    			res = j;
    			break;
    		}
    	}
    	return res; 
    }
 
    private static int arrfactor(int i,String a) {
    	int kappa = i+1;
    	int bcount = 1;
    	int res = 0;
    	for (int j = kappa; j < a.length(); j++) {
    		if (a.charAt(j) == '[') {
    			bcount++;
    		}
    		if (a.charAt(j) == ']') {
    			bcount--;
    		}
    		if (bcount == 0) {
    			res = j;
    			break;
    		}
    	}
    	return res; 
    }
    
  
    
    private double arrcheck(int j, String a) {
    	for (int i = 0; i < arrays.size(); i++) {
    		if (a.equals(arrays.get(i).name)) {
    			return (double) arrays.get(i).values[j];
    		}
    	}
    	return -1;
    }
    
    private double scalcheck (String b) {
    	for (int i = 0; i < scalars.size(); i++) {
    		if (b.equals(scalars.get(i).name)) {
    			return (double) scalars.get(i).value;
    		}
    	}
    	return -1;
    }
    
    private static double eval (double a, double b, char c) {
    	if (c == '+') {
    		return a + b;
    	}
    	else if (c == '-') {
    		return a - b;
    	}
    	else if(c == '/') {
    		return a / b;
    	}
    	else if (c == '*') {
    		return a * b;
    	}
    	return -1;
    }
    
    private static boolean isLower (char x) {
    	if (x == '-' || x == '+') {return true;}
    	else {return false;}
    }
    private static boolean CharIsOp (char x) {
    	if (x == '+' || x == '-' || x == '*' || x == '/') {
    		return true;
    	}
    	else {return false;}
    }
    /**
     * Utility method, prints the characters in the scalars list
     */
    public void printScalars() {
        for (ScalarVariable ss: scalars) {
            System.out.println(ss);
        }
    }
    
    /**
     * Utility method, prints the characters in the arrays list
     */
    public void printArrays() {
    		for (ArrayVariable as: arrays) {
    			System.out.println(as);
    		}
    }

}



