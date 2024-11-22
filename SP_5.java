import java.io.BufferedReader; 
import java.io.FileReader; 
import java.io.FileWriter; 
import java.io.IOException; 
import java.io.PrintWriter; 
import java.util.ArrayList; 
import java.util.Arrays; 
import java.util.HashMap; 
import java.util.List; 
import java.util.Map; 
 
public class SP_5 { 
    // File names 
    private static final String PREVIOUS_OUTPUT_FILE = "output.txt"; 
    private static final String MACRO_CALL_INPUT_FILE = "input1.txt"; 
    private static final String EXPANDED_OUTPUT_FILE = "expanded_output.txt"; 
 
    static class MacroInfo { 
        String name; 
        int pp, kp, mdtp, kpdtp, sstp, evn; 
        List<String> PNTAB = new ArrayList<>(); 
 
        MacroInfo(String name, int pp, int kp, int mdtp, int kpdtp, int sstp, int evn) { 
            this.name = name; 
            this.pp = pp; 
            this.kp = kp; 
            this.mdtp = mdtp; 
            this.kpdtp = kpdtp; 
            this.sstp = sstp; 
            this.evn = evn; 
        } 
    } 
 
    static Map<String, MacroInfo> macroInfoMap = new HashMap<>(); 
    static List<String> MDT = new ArrayList<>(); 
    static List<String> KPDTAB = new ArrayList<>(); 
    static List<String> EVNTAB = new ArrayList<>(); 
    static List<String> SSNTAB = new ArrayList<>(); 
    static List<Integer> SSTAB = new ArrayList<>(); 
    static List<List<String>> APTAB = new ArrayList<>(); 
 
    public static void main(String[] args) throws IOException { 
        loadPreviousOutput(PREVIOUS_OUTPUT_FILE); 
        expandMacros(MACRO_CALL_INPUT_FILE, EXPANDED_OUTPUT_FILE); 
        System.out.println("Macro expansion completed. Output written to " + EXPANDED_OUTPUT_FILE); 
    } 
 
    static void loadPreviousOutput(String filename) throws IOException { 
        BufferedReader br = new BufferedReader(new FileReader(filename)); 
        String line; 
        String currentSection = ""; 
 
        while ((line = br.readLine()) != null) { 
            line = line.trim(); // Remove leading/trailing whitespace 
            if (line.startsWith("Macro Information Table:")) { 
                currentSection = "MNT"; 
                br.readLine(); // Skip header 
            } else if (line.startsWith("MDT (Macro Definition Table):")) { 
                currentSection = "MDT"; 
                br.readLine(); // Skip header 
            } else if (line.startsWith("KPDTAB (Keyword Parameter Default Table):")) { 
                currentSection = "KPDTAB"; 
                br.readLine(); // Skip header 
            } else if (line.startsWith("EVNTAB (Expansion Variable Name Table):")) { 
                currentSection = "EVNTAB"; 
                br.readLine(); // Skip header 
            } else if (line.startsWith("SSNTAB (Sequencing Symbol Name Table):")) { 
                currentSection = "SSNTAB"; 
                br.readLine(); // Skip header 
            } else if (line.startsWith("SSTAB (Sequencing Symbol Table):")) { 
                currentSection = "SSTAB"; 
                br.readLine(); // Skip header 
            } else if (!line.isEmpty()) { 
                switch (currentSection) { 
                    case "MNT": 
                        String[] mntParts = line.split("\\s+"); 
                        if (mntParts.length >= 7) { 
                            try { 
                                MacroInfo info = new MacroInfo(mntParts[0],  
                                    Integer.parseInt(mntParts[1]),  
                                    Integer.parseInt(mntParts[2]),  
                                    Integer.parseInt(mntParts[4]),  
                                    Integer.parseInt(mntParts[5]),  
                                    Integer.parseInt(mntParts[6]),  
                                    Integer.parseInt(mntParts[3])); 
                                macroInfoMap.put(mntParts[0], info); 
                            } catch (NumberFormatException e) { 
                                System.err.println("Error parsing MNT line: " + Arrays.toString(mntParts)); 
                            } 
                        } 
                        break; 
                    case "MDT": 
                        MDT.add(line.split("\\s+", 2)[1]); 
                        break; 
                    case "KPDTAB": 
                        KPDTAB.add(line.split("\\s+", 2)[1]); 
                        break; 
                    case "EVNTAB": 
                        EVNTAB.add(line.split("\\s+", 2)[1]); 
                        break; 
                    case "SSNTAB": 
                        SSNTAB.add(line.split("\\s+", 2)[1]); 
                        break; 
                    case "SSTAB": 
                        SSTAB.add(Integer.parseInt(line.split("\\s+", 2)[1])); 
                        break; 
                } 
            } 
        } 
        br.close(); 
 
        // Load PNTAB for each macro 
        for (MacroInfo info : macroInfoMap.values()) { 
            String[] mdtLineParts = MDT.get(info.mdtp).split("\\s+"); 
            for (int i = 0; i < info.pp + info.kp; i++) { 
                if (i + 1 < mdtLineParts.length) { 
                    info.PNTAB.add(mdtLineParts[i + 1].substring(1)); 
                } else { 
                    info.PNTAB.add("PARAM" + i); // Handle missing parameters 
                } 
            } 
        } 
    } 
 
    static void expandMacros(String inputFile, String outputFile) throws 
IOException { 
        BufferedReader br = new BufferedReader(new FileReader(inputFile)); 
        PrintWriter pw = new PrintWriter(new FileWriter(outputFile)); 
        String line; 
 
        pw.println("Expanded Code:"); 
        pw.println("=============="); 
 
        while ((line = br.readLine()) != null) { 
            String[] parts = line.trim().split("\\s+"); 
            if (macroInfoMap.containsKey(parts[0])) { 
                expandMacroCall(parts, pw); 
            } else { 
                pw.println(line); 
            } 
        } 
 
        br.close(); 
 
        // Print APTAB in a tabular format 
        pw.println("\nAPTAB (Actual Parameter Table):"); 
        pw.println("==============================="); 
        for (int i = 0; i < APTAB.size(); i++) { 
            pw.println("Macro Call " + (i + 1) + ":"); 
            pw.println("Index\tValue"); 
            for (int j = 0; j < APTAB.get(i).size(); j++) { 
                pw.println(j + "\t" + APTAB.get(i).get(j)); 
            } 
            pw.println(); // Add a blank line between macro calls 
        } 
 
        pw.close(); 
    } 
 
    static void expandMacroCall(String[] parts, PrintWriter pw) { 
        String macroName = parts[0]; 
        MacroInfo info = macroInfoMap.get(macroName); 
        List<String> actualParams = new 
ArrayList<>(Arrays.asList(parts).subList(1, parts.length)); 
         
        List<String> aptabEntry = new ArrayList<>(info.PNTAB); 
        for (int i = 0; i < actualParams.size(); i++) { 
            if (i < info.pp) { 
                if (i < aptabEntry.size()) { 
                    aptabEntry.set(i, actualParams.get(i)); 
                } else { 
                    aptabEntry.add(actualParams.get(i)); 
                } 
            } else { 
                String[] keywordParam = actualParams.get(i).split("="); 
                int index = aptabEntry.indexOf(keywordParam[0]); 
                if (index != -1) { 
                    aptabEntry.set(index, keywordParam[1]); 
                } 
            } 
        } 
        APTAB.add(aptabEntry); 
 
        for (int i = info.mdtp + 1; i < MDT.size() && 
!MDT.get(i).equals("MEND"); i++) { 
String expandedLine = expandMacroLine(MDT.get(i), APTAB.size() - 1); 
pw.println(expandedLine); 
} 
} 
static String expandMacroLine(String line, int aptabIndex) { 
for (int i = 0; i < APTAB.get(aptabIndex).size(); i++) { 
line = line.replace("(P," + i + ")", 
APTAB.get(aptabIndex).get(i)); 
} 
for (int i = 0; i < EVNTAB.size(); i++) { 
line = line.replace("(E," + i + ")", EVNTAB.get(i)); 
} 
return line; 
} 
}