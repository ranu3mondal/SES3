import java.io.BufferedReader; 
import java.io.BufferedWriter; 
import java.io.FileReader; 
import java.io.FileWriter; 
import java.io.IOException; 
import java.util.ArrayList; 
import java.util.List; 
 
class SP_1 { 
    static final int MAX_SYMBOLS = 100; 
    static final int MAX_CODE_LINES = 100; 
    static final int MAX_LITERALS = 100; 
    static final int MAX_LINE_LENGTH = 100; 
 
    static class MOTEntry { 
        String name; 
        String type; 
        int opcode; 
        int size;  // Add size field 
 
        MOTEntry(String name, String type, int opcode, int size) { 
            this.name = name; 
            this.type = type; 
            this.opcode = opcode; 
            this.size = size;  // Initialize size 
        } 
    } 
 
    static class Register { 
        String name; 
        String name1; 
        int code; 
 
        Register(String name, String name1, int code) { 
            this.name = name; 
            this.name1 = name1; 
            this.code = code; 
        } 
    } 
 
    static class Symbol { 
        String name; 
        int address; 
 
        Symbol(String name, int address) { 
            this.name = name; 
            this.address = address; 
        } 
    } 
 
    static class Literal { 
        String name; 
        int address; 
 
        Literal(String name, int address) { 
            this.name = name; 
            this.address = address; 
        } 
    } 
 
    static class IntermediateCode { 
        String operation; 
        int code; 
        int reg; 
        String operandType; 
        int operandValue; 
        int lc; 
 
        IntermediateCode(String operation, int code, int reg, String 
operandType, int operandValue, int lc) { 
            this.operation = operation; 
            this.code = code; 
            this.reg = reg; 
            this.operandType = operandType; 
            this.operandValue = operandValue; 
            this.lc = lc; 
        } 
    } 
 
    static MOTEntry[] mot = { 
        new MOTEntry("ADD", "IS", 20, 2), 
        new MOTEntry("SUB", "IS", 21, 3), 
        new MOTEntry("MUL", "IS", 22, 2), 
        new MOTEntry("MOVER", "IS", 23, 2), 
        new MOTEntry("MOV", "IS", 24, 2), 
        new MOTEntry("MOVEM", "IS", 25, 2), 
        new MOTEntry("BC", "IS", 26, 2), 
        new MOTEntry("DIV", "IS", 27, 2), 
        new MOTEntry("START", "AD", 10, 0), 
        new MOTEntry("END", "AD", 11, 0), 
        new MOTEntry("ORIGIN", "AD", 12, 0), 
        new MOTEntry("EQU", "AD", 13, 0), 
        new MOTEntry("LTORG", "AD", 14, 0), 
        new MOTEntry("DS", "DL", 1, 1), 
        new MOTEntry("DC", "DL", 2, 1) 
    }; 
 
    static Register[] registers = { 
        new Register("AREG", "R", 1), 
        new Register("BREG", "R", 2), 
        new Register("CREG", "R", 3), 
        new Register("DREG", "R", 4) 
    }; 
 
    static List<Symbol> symbolTable = new ArrayList<>(); 
    static List<Literal> literalTable = new ArrayList<>(); 
    static List<IntermediateCode> intermediateCode = new ArrayList<>(); 
    static int locationCounter = 0; 
 
    static void processLine(String line) { 
        String label = null, opcode = null, operand1 = null, operand2 = null; 
        String[] tokens = line.split("[ ,\t]+"); 
        if (tokens.length == 0) return; 
        int index = 0; 
     
        // Check if the first token is a label 
        if (tokens[index].endsWith(":")) { 
            label = tokens[index++]; 
        } else if (tokens[index].equalsIgnoreCase("START") || 
tokens[index].equalsIgnoreCase("END") || isMnemonic(tokens[index])) { 
            // No label; proceed with opcode and operands 
        } else { 
            label = tokens[index++]; 
        } 
     
        if (index < tokens.length) opcode = tokens[index++]; 
        if (index < tokens.length) operand1 = tokens[index++]; 
        if (index < tokens.length) operand2 = tokens[index]; 
     
        // Add label to symbol table if it is not a register or constant 
        if (label != null && !isRegister(label) && !isConstant(label)) { 
            // Check if the label already exists in the symbol table 
            boolean isDuplicate = false; 
            for (Symbol s : symbolTable) { 
                if (s.name.equals(label)) { 
                    isDuplicate = true; 
                    break; 
                } 
            } 
            if (!isDuplicate) { 
                symbolTable.add(new Symbol(label, locationCounter)); 
            } 
        } 
     
        // Process opcode 
        if (opcode != null) { 
            boolean found = false; 
            for (MOTEntry motEntry : mot) { 
                if (opcode.equals(motEntry.name)) { 
                    found = true; 
                    if (motEntry.type != null) { 
                        switch (motEntry.type) { 
                            case "AD": 
                                if ("START".equals(opcode)) { 
                                    locationCounter = 
Integer.parseInt(operand1); 
                                    intermediateCode.add(new 
IntermediateCode("AD", motEntry.opcode, -1, "C", locationCounter, 
locationCounter)); 
                                } else if ("END".equals(opcode)) { 
                                    intermediateCode.add(new 
IntermediateCode("AD", motEntry.opcode, -1, null, 0, locationCounter)); 
                                } 
                                break; 
                            case "IS": 
                                int regCode = -1; 
                                if (operand1 != null) { 
                                    for (Register reg : registers) { 
                                        if (operand1.equals(reg.name)) { 
                                            regCode = reg.code; 
                                            break; 
                                        } 
                                    } 
                                    if (regCode == -1) { 
                                        System.out.println("Error: Invalid register " + operand1); 
                                        return; 
                                    } 
                                } 
                                if (operand2 != null) { 
                                    if (operand2.startsWith("=")) { 
                                        // Handling literals 
                                        boolean isDuplicateLiteral = false; 
                                        for (Literal lit : literalTable) { 
                                            if (lit.name.equals(operand2)) { 
                                                isDuplicateLiteral = true; 
                                                break; 
                                            } 
                                        } 
                                        if (!isDuplicateLiteral) { 
                                            literalTable.add(new 
Literal(operand2, -1)); 
                                        } 
                                        int literalIndex = literalTable.size() - 1; 
                                        intermediateCode.add(new 
IntermediateCode("IS", motEntry.opcode, regCode, "L", literalIndex, 
locationCounter)); 
                                    } else if (isConstant(operand2)) { 
                                        int constantValue = 
Integer.parseInt(operand2); 
                                        intermediateCode.add(new 
IntermediateCode("IS", motEntry.opcode, regCode, "C", constantValue, 
locationCounter)); 
                                    } else if (isRegister(operand2)) { 
                                        int secondRegCode = -1; 
                                        for (Register reg : registers) { 
                                            if (operand2.equals(reg.name)) { 
                                                secondRegCode = reg.code; 
                                                break; 
                                            } 
                                        } 
                                        intermediateCode.add(new 
IntermediateCode("IS", motEntry.opcode, regCode, "R", secondRegCode, 
locationCounter)); 
                                    } else { 
                                        int symIndex = -1; 
                                        for (int j = 0; j < 
symbolTable.size(); j++) { 
                                            if 
(operand2.equals(symbolTable.get(j).name)) { 
                                                symIndex = j; 
                                                break; 
                                            } 
                                        } 
                                        if (symIndex == -1) { 
                                            symbolTable.add(new 
Symbol(operand2, locationCounter)); 
                                            symIndex = symbolTable.size() - 1; 
                                        } 
                                        intermediateCode.add(new 
IntermediateCode("IS", motEntry.opcode, regCode, "S", symIndex, 
locationCounter)); 
                                    } 
                                } 
                                locationCounter += motEntry.size; 
                                break; 
                            case "DL": 
                                if ("DS".equals(opcode)) { 
                                    int size = Integer.parseInt(operand1); 
                                    symbolTable.get(symbolTable.size() - 
1).address = locationCounter; 
                                    intermediateCode.add(new 
IntermediateCode("DL", motEntry.opcode, -1, "C", size, locationCounter)); 
                                    locationCounter += size; 
                                } else if ("DC".equals(opcode)) { 
                                    int value = Integer.parseInt(operand1); 
                                    symbolTable.get(symbolTable.size() - 
1).address = locationCounter; 
                                    intermediateCode.add(new 
IntermediateCode("DL", motEntry.opcode, -1, "C", value, locationCounter)); 
                                    locationCounter++; 
                                } 
                                break; 
                            default: 
                                break; 
                        } 
                    } 
                    break; 
                } 
            } 
            if (!found) { 
                System.out.println("Error: Invalid instruction " + opcode); 
            } 
        } 
    } 
     
     
 
    static boolean isRegister(String token) { 
        for (Register reg : registers) { 
            if (token.equals(reg.name)) { 
                return true; 
            } 
        } 
        return false; 
    } 
 
    static boolean isConstant(String token) { 
        try { 
            Integer.parseInt(token); 
            return true; 
        } catch (NumberFormatException e) { 
            return false; 
        } 
    } 
 
    static boolean isMnemonic(String token) { 
        for (MOTEntry motEntry : mot) { 
            if (token.equals(motEntry.name)) { 
                return true; 
            } 
        } 
        return false; 
    } 
 
    static void readFileAndProcessLines(String filename) throws IOException { 
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) 
{ 
            String line; 
            while ((line = br.readLine()) != null) { 
                processLine(line.trim()); 
            } 
        } 
    } 
 
    static void writeOutputToFile(String filename) throws IOException { 
        BufferedWriter bw = new BufferedWriter(new FileWriter(filename)); 
     
        bw.write("Symbol Table:\n"); 
        for (int i = 0; i < symbolTable.size(); i++) { 
            bw.write(i + "    " + symbolTable.get(i).name + "    " + 
symbolTable.get(i).address + "\n"); 
        } 
        bw.write("\nIntermediate Code with LC:\n"); 
        bw.write("LC\n"); 
        for (IntermediateCode ic : intermediateCode) { 
            bw.write(ic.lc + "   (" + ic.operation + ", " + ic.code + ")   "); 
            if (ic.reg != -1) { 
                for (Register reg : registers) { 
                    if (reg.code == ic.reg) { 
                        bw.write("(" + reg.name1 + ", " + reg.code + ")   "); 
                        break; 
                    } 
                } 
            } 
            if (ic.operandType != null) { 
                bw.write("(" + ic.operandType + ", " + ic.operandValue + ")"); 
            } 
            bw.write("\n"); 
        } 
        bw.close(); 
    } 
     
     
    public static void main(String[] args) { 
        String inputFilename = "input.asm"; 
        String outputFilename = "out.txt"; 
        try { 
            readFileAndProcessLines(inputFilename); 
            writeOutputToFile(outputFilename); 
        } catch (IOException e) { 
            System.out.println("Error: " + e.getMessage()); 
        } 
    } 
} 