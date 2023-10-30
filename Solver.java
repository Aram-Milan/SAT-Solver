
// IN1002 Introduction to Algorithms
// Coursework 2022/2023
//
// Submission by
// ARAM GHOLIKIMILAN
// ARAM.GHOLIKIMILAN@CITY.AC.UK

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Solver {

    private int[][] clauseDatabase = null;
    private int numberOfVariables = 0;


    /* You answers go below here */

    // Part A.1
    // Worst case complexity : O(v)
    // Best case complexity : O(1)
    public boolean checkClause(int[] assignment, int[] clause) {
        for (int literal : clause) {
            if (assignment[Math.abs(literal)] * literal > 0) {
                return true;
            }
        }
        return false;
    }

    // Part A.2
    // Worst case complexity : O(c*l)
    // Best case complexity : O(l)
    public boolean checkClauseDatabase(int[] assignment, int[][] clauseDatabase) {
        for (int[] clause : clauseDatabase) {
            if (!checkClause(assignment, clause)) {
                return false;
            }
        }
        return true;
    }

    // Part A.3
    // Worst case complexity : O(v)
    // Best case complexity : O(1)
    public int checkClausePartial(int[] partialAssignment, int[] clause) {
        int numOfNegative = 0;
        for (int literal : clause) {
            int assign = partialAssignment[Math.abs(literal)];
            if (assign * literal > 0)
                return 1;
            if(assign * literal<0)
                numOfNegative++;
        }
        if (numOfNegative==clause.length)
            return -1;
        else
            return 0;
    }

    // Part A.4
    // Worst case complexity : O(v)
    // Best case complexity : O(1)
    public int findUnit(int[] partialAssignment, int[] clause) {
        int unknownCount = 0;
        int unknownLiteral = 0;
        for (int literal : clause) {
            int variableValue = partialAssignment[Math.abs(literal)];
            if(variableValue* literal>0){
                return 0;
            }
            if (variableValue == 0 && literal != unknownLiteral) {
                unknownCount++;
                unknownLiteral = literal;
                }
            if (unknownCount > 1) {
                return 0;
            }
        }
        return unknownLiteral;
    }

    // Part B
    // I think this can solve all of them (1-2-3-4-5-6-7-8-9-10-11-12-13-14-15)
    int[] checkSat(int[][] clauseDatabase) {
        // Initialize an array of assignments with all variables unassigned
        int[] newAssignments = new int[numberOfVariables+1];
        // Run the DPLL algorithm to find a satisfying assignment
        if (dpll(newAssignments)) {
            for(int a=1;a<numberOfVariables+1;a++){
                if(newAssignments[a]==0){
                    newAssignments[a]=1;
                }
            }
            return newAssignments;
        }
            return null;

    }
    private boolean dpll(int[] assignments) {

        if (isAssignmentComplete(assignments)) {
            return checkClauseDatabase(assignments, this.clauseDatabase);
        }

        boolean unitFound = true;
        int unit;
        while (unitFound) {
            unitFound = false;
            for (int[] clause : this.clauseDatabase) {
                int checkPC = checkClausePartial(assignments, clause);
                if (checkPC == -1) {
                    return false;
                }
                if (checkPC == 0) {
                    // find unit clause
                    unit = findUnit(assignments, clause);
                    if (unit != 0 && assignments[Math.abs(unit)] == 0) {
                        assignments[Math.abs(unit)] = (unit > 0) ? 1 : -1;
                        unitFound = true;
                    } else {
                        if (unit != 0 && assignments[Math.abs(unit)] != 0) {
                            return false;
                        }
                    }
                }
            }

        }

        int emptyIndex = highestIndexNo(assignments);
        if (emptyIndex == 0) {
            return true;
        }

        int[] newTrueAssignment = assignments.clone();
        newTrueAssignment[emptyIndex] = 1;

        if (dpll(newTrueAssignment)) {
            for (int i = 0; i < newTrueAssignment.length; i++) {
                assignments[i] = newTrueAssignment[i];
            }
            return true;
        }

        int[] newFalseAssignment = assignments.clone();
        newFalseAssignment[emptyIndex] = -1;

        if (dpll(newFalseAssignment)) {
            for (int i = 0; i < newFalseAssignment.length; i++) {
                assignments[i] = newFalseAssignment[i];
            }
            return true;
        }
        assignments[emptyIndex]=0;
        return false;

    }
    private int highestIndexNo (int[] assignments){
        int currentH=0;
        int index=0;
        int mostOccurring;
        for(int i=1;i<numberOfVariables+1;i++){
            if(assignments[i]!=0){
                continue;
            }
            mostOccurring =0;
            for(int[] clause:clauseDatabase){
                if(checkClausePartial(assignments,clause)==0 && (clauseContains(clause,i) || clauseContains(clause,-i))){
                    mostOccurring++;
                }
            }
            if(mostOccurring>currentH){
                index=i;
                currentH=mostOccurring;
            }
        }
        return index;
    }
    private boolean clauseContains(int[] clause,int variable){
        for(int literal : clause){
            if(literal==variable){
                return true;
            }
        }
        return false;
    }
    private static boolean isAssignmentComplete(int[] assignment) {
        for (int i = 1; i < assignment.length; i++) {
            if (assignment[i] == 0) {
                return false;
            }
        }
        return true;
    }
    /*****************************************************************\
     *** DO NOT CHANGE! DO NOT CHANGE! DO NOT CHANGE! DO NOT CHANGE! ***
     *******************************************************************
     *********** Do not change anything below this comment! ************
     \*****************************************************************/

    public static void main(String[] args) {
        try {
            Solver mySolver = new Solver();

            System.out.println("Enter the file to check");

            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(isr);
            String fileName = br.readLine();

            int returnValue = 0;

            Path file = Paths.get(fileName);
            BufferedReader reader = Files.newBufferedReader(file);
            returnValue = mySolver.runSatSolver(reader);

            return;

        } catch (Exception e) {
            System.err.println("Solver failed :-(");
            e.printStackTrace(System.err);
            return;

        }
    }

    public int runSatSolver(BufferedReader reader) throws Exception, IOException {

        // First load the problem in, this will initialise the clause
        // database and the number of variables.
        loadDimacs(reader);

        // Then we run the part B algorithm
        int [] assignment = checkSat(clauseDatabase);

        // Depending on the output do different checks
        if (assignment == null) {
            // No assignment to check, will have to trust the result
            // is correct...
            System.out.println("s UNSATISFIABLE");
            return 20;

        } else {
            // Cross check using the part A algorithm
            boolean checkResult = checkClauseDatabase(assignment, clauseDatabase);

            if (checkResult == false) {
                throw new Exception("The assignment returned by checkSat is not satisfiable according to checkClauseDatabase?");
            }

            System.out.println("s SATISFIABLE");

            // Check that it is a well structured assignment
            if (assignment.length != numberOfVariables + 1) {
                throw new Exception("Assignment should have one element per variable.");
            }
            if (assignment[0] != 0) {
                throw new Exception("The first element of an assignment must be zero.");
            }
            for (int i = 1; i <= numberOfVariables; ++i) {
                if (assignment[i] == 1 || assignment[i] == -1) {
                    System.out.println("v " + (i * assignment[i]));
                } else {
                    throw new Exception("assignment[" + i + "] should be 1 or -1, is " + assignment[i]);
                }
            }

            return 10;
        }
    }

    // This is a simple parser for DIMACS file format
    void loadDimacs(BufferedReader reader) throws Exception, IOException {
        int numberOfClauses = 0;

        // Find the header line
        do {
            String line = reader.readLine();

            if (line == null) {
                throw new Exception("Found end of file before a header?");
            } else if (line.startsWith("c")) {
                // Comment line, ignore
                continue;
            } else if (line.startsWith("p cnf ")) {
                // Found the header
                String counters = line.substring(6);
                int split = counters.indexOf(" ");
                numberOfVariables = Integer.parseInt(counters.substring(0,split));
                numberOfClauses = Integer.parseInt(counters.substring(split + 1));

                if (numberOfVariables <= 0) {
                    throw new Exception("Variables should be positive?");
                }
                if (numberOfClauses < 0) {
                    throw new Exception("A negative number of clauses?");
                }
                break;
            } else {
                throw new Exception("Unexpected line?");
            }
        } while (true);

        // Set up the clauseDatabase
        clauseDatabase = new int[numberOfClauses][];

        // Parse the clauses
        for (int i = 0; i < numberOfClauses; ++i) {
            String line = reader.readLine();

            if (line == null) {
                throw new Exception("Unexpected end of file before clauses have been parsed");
            } else if (line.startsWith("c")) {
                // Comment; skip
                --i;
                continue;
            } else {
                // Try to parse as a clause
                ArrayList<Integer> tmp = new ArrayList<Integer>();
                String working = line;

                do {
                    int split = working.indexOf(" ");

                    if (split == -1) {
                        // No space found so working should just be
                        // the final "0"
                        if (!working.equals("0")) {
                            throw new Exception("Unexpected end of clause string : \"" + working + "\"");
                        } else {
                            // Clause is correct and complete
                            break;
                        }
                    } else {
                        int var = Integer.parseInt(working.substring(0,split));

                        if (var == 0) {
                            throw new Exception("Unexpected 0 in the middle of a clause");
                        } else {
                            tmp.add(var);
                        }

                        working = working.substring(split + 1);
                    }
                } while (true);

                // Add to the clause database
                clauseDatabase[i] = new int[tmp.size()];
                for (int j = 0; j < tmp.size(); ++j) {
                    clauseDatabase[i][j] = tmp.get(j);
                }
            }
        }

        // All clauses loaded successfully!
        return;
    }

}

