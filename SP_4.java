import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SP_4 {
    static class MacroInfo {
        String name;
        int pp, kp, mdtp, kpdtp, sstp, evn;
        List<String> PNTAB = new ArrayList<>(); // Each macro has its own PNTAB

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
    static List<String> PNTAB = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("input.txt"));
        String line;

        while ((line = br.readLine()) != null) {
            if (line.trim().startsWith("MACRO")) {
                processMacroDefinition(br);
            }
        }

        br.close();

        // Print all tables with proper labels and index numbers
        System.out.println("Macro Information Table:");
        System.out.println("Name\t#PP\t#KP\t#EV\tMDTP\tKPDTP\tSSTP");
        for (MacroInfo info : macroInfoMap.values()) {
            System.out.printf("%s\t%d\t%d\t%d\t%d\t%d\t%d\n",
                info.name, info.pp, info.kp, info.evn, info.mdtp, info.kpdtp, info.sstp);
        }

        System.out.println("\nMDT (Macro Definition Table):");
        System.out.println("Index\tDefinition");
        for (int i = 0; i < MDT.size(); i++) {
            System.out.println(i + "\t" + MDT.get(i));
        }

        // Print separate PNTAB for each macro
        System.out.println("\nPNTAB for INCR Macro:");
        printPNTAB("INCR");

        System.out.println("\nPNTAB for DECR Macro:");
        printPNTAB("DECR");

        System.out.println("\nKPDTAB (Keyword Parameter Default Table):");
        System.out.println("Index\tParameter");
        for (int i = 0; i < KPDTAB.size(); i++) {
            System.out.println(i + "\t" + KPDTAB.get(i));
        }

        System.out.println("\nEVNTAB (Expansion Variable Name Table):");
        System.out.println("Index\tVariable");
        for (int i = 0; i < EVNTAB.size(); i++) {
            System.out.println(i + "\t" + EVNTAB.get(i));
        }

        System.out.println("\nSSNTAB (Sequencing Symbol Name Table):");
        System.out.println("Index\tSymbol");
        for (int i = 0; i < SSNTAB.size(); i++) {
            System.out.println(i + "\t" + SSNTAB.get(i));
        }

        System.out.println("\nSSTAB (Sequencing Symbol Table):");
        System.out.println("Index\tMDT Index");
        for (int i = 0; i < SSTAB.size(); i++) {
            System.out.println(i + "\t" + SSTAB.get(i));
        }
    }

    static void processMacroDefinition(BufferedReader br) throws IOException {
        String line = br.readLine();
        String[] parts = line.trim().split("\\s+");
        String macroName = parts[0];

        int pp = 0, kp = 0, evn = 0;
        List<String> macroPNTAB = new ArrayList<>();
        MacroInfo currentMacro = new MacroInfo(macroName, 0, 0, MDT.size(), KPDTAB.size(), SSTAB.size(), 0);

        for (int i = 1; i < parts.length; i++) {
            String paramName = parts[i].substring(1);
            if (paramName.contains("=")) {
                kp++;
                KPDTAB.add(paramName);
                if (!macroPNTAB.contains(paramName.split("=")[0])) {
                    macroPNTAB.add(paramName.split("=")[0]);
                }
            } else {
                pp++;
                if (!macroPNTAB.contains(paramName)) {
                    macroPNTAB.add(paramName);
                }
            }
        }

        currentMacro.PNTAB.addAll(macroPNTAB);

        while (!(line = br.readLine()).trim().equals("MEND")) {
            if (line.trim().startsWith("LCL")) {
                String[] evParts = line.trim().split("\\s+");
                for (int i = 1; i < evParts.length; i++) {
                    EVNTAB.add(evParts[i].substring(1));
                    evn++;
                }
            } else if (line.contains(".")) {
                String[] ssParts = line.trim().split("\\s+");
                String ssName = ssParts[0].substring(1);
                if (!SSNTAB.contains(ssName)) {
                    SSNTAB.add(ssName);
                    SSTAB.add(MDT.size());
                }
            }

            String processedLine = replaceParameters(line, macroPNTAB);
            MDT.add(processedLine);
        }

        MDT.add("MEND");

        currentMacro.pp = pp;
        currentMacro.kp = kp;
        currentMacro.evn = evn;
        macroInfoMap.put(macroName, currentMacro);
    }

    static String replaceParameters(String line, List<String> macroPNTAB) {
        for (int i = 0; i < macroPNTAB.size(); i++) {
            String param = macroPNTAB.get(i).trim().replace(",", "");
            String paramPlaceholder = "&" + param;
            if (line.contains(paramPlaceholder)) {
                line = line.replace(paramPlaceholder, "(P," + i + ")");
            }
        }

        for (int i = 0; i < EVNTAB.size(); i++) {
            String evnParam = "&" + EVNTAB.get(i).trim();
            if (line.contains(evnParam)) {
                line = line.replace(evnParam, "(E," + i + ")");
            }
        }

        return line;
    }

    static void printPNTAB(String macroName) {
        MacroInfo macroInfo = macroInfoMap.get(macroName);
        if (macroInfo != null) {
            System.out.println("Index\tParameter");
            for (int i = 0; i < macroInfo.PNTAB.size(); i++) {
                System.out.println(i + "\t" + macroInfo.PNTAB.get(i));
            }
        } else {
            System.out.println("No PNTAB found for macro: " + macroName);
        }
    }
}
