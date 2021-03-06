package tops.model.classification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Level {

    public static final int ROOT = 0;

    public static final int C = 1;

    public static final int A = 2;

    public static final int T = 3;

    public static final int H = 4;

    public static final int S = 5;

    public static final int N = 6;

    public static final int I = 7;

    public static final int D = 8;

    public int depth;

    private String levelCode;

    private String fullCode;

    private List<Level> subLevels;

    private RepSet repSet;

    public Level() {
        this.depth = Level.ROOT;
        this.levelCode = "UNKNOWN";
        this.subLevels = new ArrayList<>();
        this.repSet = new RepSet();
    }

    public Level(int depth, String levelCode, String fullCode) {
        this();
        this.depth = depth;
        this.levelCode = levelCode;
        this.fullCode = fullCode;
    }

    public Level(int depth, String levelCode, String fullCode, String name) {
        this(depth, levelCode, fullCode);
    }

    public boolean equals(Object other) {
        if (other instanceof Level) {
            Level otherLevel = (Level) other;
            return this.fullCode.equals(otherLevel.getFullCode());
        }
        return false;
    }
    
    public int hashCode() {
        return fullCode.hashCode();
    }

    public static String levelName(int depth) {
        switch (depth) {
            case Level.ROOT:
                return "ROOT";
            case Level.C:
                return "Class";
            case Level.A:
                return "Architecture";
            case Level.T:
                return "Topology";
            case Level.H:
                return "Homology";
            case Level.S:
                return "Sequence";
            case Level.N:
                return "Non-Identical";
            case Level.I:
                return "Identical";
            case Level.D:
                return "Domain";
            default:
                return "Unknown";
        }
    }

    public void addSubLevel(Level subLevel) {
        this.subLevels.add(subLevel);
    }

    public Level getSubLevel(int index) {
        return this.subLevels.get(index);
    }

    public Level getSubLevel(String levelCode) {
        int index = this.indexOfCode(levelCode);
        if (index == -1) {
            return null;
        } else {
            return this.getSubLevel(index);
        }
    }

    public String getLevelCode() {
        return this.levelCode;
    }

    public String getFullCode() {
        return this.fullCode;
    }

    public int numberOfSubLevels() {
        return this.subLevels.size();
    }

    public boolean hasReps() {
        return this.repSet.size() > 0;
    }

    public boolean isSingleton() {
        return this.repSet.size() == 1;
    }

    public void addRep(Rep rep) {
        String code = rep.getCode();
        String[] bits = code.split("\\.");
        // if we are above the 'leaf' level
        if (this.depth < bits.length) {
            int subLevelPosition = this.depth;
            String subLevelCode = bits[subLevelPosition];
            Level existingLevel = this.getSubLevel(subLevelCode);
            // no subLevel found
            if (existingLevel == null) {
                StringBuilder fullCodeBuilder = new StringBuilder();
                for (int i = 0; i <= subLevelPosition; i++) {
                    fullCodeBuilder.append(bits[i]).append(".");
                }
                existingLevel = new Level(this.depth + 1, subLevelCode, fullCodeBuilder.toString());
                this.addSubLevel(existingLevel);
            }
            existingLevel.addRep(rep);
        } else if (bits.length == this.depth) {
            this.repSet.addRep(rep);
        } else {
            System.err.println("gone beyond the level " + this.depth + " " + code);
        }
    }

    public RepSet getRepSet() {
        return this.repSet;
    }

    public boolean codeMatches(String thatCode) {
        if (this.levelCode.equals("UNKNOWN")) {
            return false;
        } else {
            return this.levelCode.equals(thatCode);
        }
    }

    public int indexOfCode(String code) {
        for (int i = 0; i < this.subLevels.size(); i++) {
            Level subLevel = this.subLevels.get(i);
            if (subLevel.codeMatches(code)) {
                return i;
            }
        }
        return -1;
    }

    public static Level fromFile(String filename, int cathLevelConstant, String levelName) throws IOException {
        String line;
        Level level = new Level(cathLevelConstant, levelName, levelName);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))) {
            while ((line = bufferedReader.readLine()) != null) {
                Rep rep = new Rep(levelName, line);
                level.addRep(rep);
            }
        }
        return level;
    }

    public List<Level> getSubLevels() {
        return this.subLevels;
    }

    public Iterator<Level> getSubLevelIterator(int subLevelDepth) {
        return new LevelIterator(this.depth, subLevelDepth, this.subLevels);
    }

    @Override
    public String toString() {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("Level ").append(Level.levelName(this.depth))
                .append(" ").append(this.fullCode);
        if (this.hasReps()) {
            stringBuffer.append(" (").append(this.repSet.size()).append(" reps)");
        }
        for (int i = 0; i < this.subLevels.size(); i++) {
            stringBuffer.append("\n");
            Level subLevel = this.subLevels.get(i);
            stringBuffer.append(subLevel.toString());
        }
        return stringBuffer.toString();
    }
}
