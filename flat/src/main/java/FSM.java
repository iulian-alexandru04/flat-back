import java.io.*;
import java.util.*;

public class FSM {
    int stateCount, Start, finalStatesCount, transitionsCount;
    Set<Character> simbolset = new HashSet<>();
    List<Integer> finalStates = new ArrayList<>();
    List<Transition>[] d;

    boolean find(int a, int b, char c) {
        System.out.println("find with a = " + a + ", b = " + b + ", c = " + c);
        Transition primita = new Transition(b, c);
        for (int i = 0; i < d[a].size(); i++)
            if (d[a].get(i).equals(primita)) {
                System.out.println("find ok");
                return true;
            }
        System.out.println("find ok");
        return false;

    }

    void removeState(int x) {
        // Remove all transitions to state x and update destination indices
        for (int i = 0; i < stateCount; i++) {
            for (int j = 0; j < d[i].size(); j++) {
                if (d[i].get(j).destination == x) {
                    d[i].remove(j);
                    transitionsCount--;
                    j--;
                } else if (d[i].get(j).destination > x) {
                    var temp = new Transition(d[i].get(j).destination - 1, d[i].get(j).symbol);
                    d[i].set(j, temp);
                }
            }
        }
        transitionsCount -= d[x].size();
        d[x].clear();
        // Shift states after x
        for (int i = x + 1; i < stateCount; i++) {
            List<Transition> temp = d[i];
            d[i] = d[i - 1];
            d[i - 1] = temp;
        }
        // Update final states
        for (int i = 0; i < finalStatesCount; i++) {
            if (finalStates.get(i) > x) {
                finalStates.set(i, finalStates.get(i) - 1);
            } else if (finalStates.get(i) == x) {
                finalStates.remove(i);
                i--;
                finalStatesCount--;
            }
        }
        stateCount--;
    }

    void mergeState(int x, int y) { // x < y
        // Update transitions: redirect y to x, decrement destinations > y
        for (int i = 0; i < stateCount; i++) {
            for (int j = 0; j < d[i].size(); j++) {
                if (d[i].get(j).destination == y) {
                    Transition updated = new Transition(x, d[i].get(j).symbol);
                    d[i].set(j, updated);
                } else if (d[i].get(j).destination > y) {
                    Transition updated = new Transition(d[i].get(j).destination - 1, d[i].get(j).symbol);
                    d[i].set(j, updated);
                }
            }
        }
        // Move transitions from y to x, avoid duplicates
        while (!d[y].isEmpty()) {
            Transition temp = d[y].get(d[y].size() - 1);
            d[y].remove(d[y].size() - 1);
            if (d[x].contains(temp)) {
                transitionsCount--;
            } else {
                d[x].add(temp);
            }
        }
        d[y].clear();
        // Shift states after y
        for (int i = y + 1; i < stateCount; i++) {
            List<Transition> tmp = d[i];
            d[i] = d[i - 1];
            d[i - 1] = tmp;
        }
        // Update final states
        for (int i = 0; i < finalStatesCount; i++) {
            if (finalStates.get(i) > y) {
                finalStates.set(i, finalStates.get(i) - 1);
            } else if (finalStates.get(i) == y) {
                finalStates.remove(i);
                i--;
                finalStatesCount--;
            }
        }
        stateCount--;
    }

    int getDestination(int x, char c) {
        for (int i = 0; i < d[x].size(); i++) {
            if (d[x].get(i).symbol == c)
                return d[x].get(i).destination;
        }
        return -1;
    }

    boolean isFinal(int x) {
        for (int i = 0; i < finalStates.size(); i++) {
            if (finalStates.get(i) == x)
                return true;
        }
        return false;
    }

    public void read(Scanner in) throws IOException {
        int a, b;
        char c;

        System.out.println("before states count");
        stateCount = in.nextInt();
        System.out.println("after states count");
        Start = in.nextInt();
        finalStatesCount = in.nextInt();
        for (int i = 1; i <= finalStatesCount; i++) {
            b = in.nextInt();
            finalStates.add(b);
        }

        System.out.println("before read transitions");
        transitionsCount = in.nextInt();
        d = new ArrayList[stateCount + 1];
        for(int i = 0; i <= stateCount; i++) {
            d[i] = new ArrayList<>();
        }
        for (int i = 1; i <= transitionsCount; i++) {
            a = in.nextInt();
            b = in.nextInt();
            c = in.next().charAt(0);
            if (find(a, b, c))
                throw new RuntimeException("Duplicate transition");
            var initialNode = d[a];
            initialNode.add(new Transition(b, c));
            simbolset.add(c);
        }
        System.out.println("after read transitions");
        if (transitionsCount != stateCount * simbolset.size())
            throw new RuntimeException("Invalid DFA: not complete");
    }

    void delUnreach() {
        boolean[] reach = new boolean[stateCount + 1];
        for (int i = 0; i < stateCount; i++)
            reach[i] = false;
        Queue<Integer> q = new LinkedList<>();
        q.add(Start);
        reach[Start] = true;
        while (!q.isEmpty()) {
            int x = q.poll();
            for (int i = 0; i < d[x].size(); i++) {
                if (!reach[d[x].get(i).destination]) {
                    reach[d[x].get(i).destination] = true;
                    q.add(d[x].get(i).destination);
                }
            }
        }
        int vechi = stateCount;
        for (int i = 0; i < vechi; i++)
            if (!reach[i])
                removeState(i - vechi + stateCount);
    }

    void separabile() {
        boolean[][] separ = new boolean[stateCount + 1][stateCount + 1];
        for (int i = 0; i < stateCount; i++) {
            for (int j = 0; j < stateCount; j++) {
                if (isFinal(i) != isFinal(j))
                    separ[i][j] = true;
                else
                    separ[i][j] = false;
            }
        }
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < stateCount; i++) {
                for (int j = i + 1; j < stateCount; j++) {
                    if (!separ[i][j]) {
                        for (char c : simbolset) {
                            int iDest = getDestination(i, c);
                            int jDest = getDestination(j, c);
                            boolean iTranz = (iDest >= 0);
                            boolean jTranz = (jDest >= 0);
                            if (!iTranz && !jTranz)
                                continue;
                            if (iTranz != jTranz) {
                                separ[i][j] = true;
                                changed = true;
                                continue;
                            }
                            if (separ[iDest][jDest] || separ[jDest][iDest]) {
                                separ[i][j] = true;
                                changed = true;
                            }
                        }
                    }
                }
            }
        }
        int vechi = stateCount;

        //
        // Print
        //
        for (int i = 0; i < stateCount; i++) {
            for (int j = i + 1; j < stateCount; j++)
                System.out.print((separ[i][j] ? 1 : 0) + " ");
            System.out.println();
        }
        //
        // End print
        //

        for (int i = 0; i < stateCount; i++) {
            for (int j = i + 1; j < stateCount; j++) {
                if (!separ[i][j])
                    mergeState(i - vechi + stateCount, j - vechi + stateCount);
            }
        }
    }

    void print(Writer out) throws IOException {
        out.write(stateCount + " " + Start + " " + finalStatesCount + "\n");
        for (int i = 0; i < finalStatesCount; i++)
            out.write(finalStates.get(i) + " ");
        out.write("\n" + transitionsCount + "\n");
        for (int i = 0; i < stateCount; i++)
            for (int j = 0; j < d[i].size(); j++)
                out.write(i + "\t" + d[i].get(j).destination + "\t" + d[i].get(j).symbol + "\n");
        out.flush();
    }

    public void graphviz(PrintWriter out) {
        out.println("digraph finite_state_machine {");
        out.println("    fontname=\"Helvetica,Arial,sans-serif\"");
        out.println("    node [fontname=\"Helvetica,Arial,sans-serif\"]");
        out.println("    edge [fontname=\"Helvetica,Arial,sans-serif\"]");
        out.println("    rankdir=LR;");
        out.println("    node [shape = circle style=filled fillcolor=\"lightcyan\"]; " + Start + ";");
        out.print("    node [shape = doublecircle style=filled fillcolor=\"navy\" fontcolor=\"white\"];");
        for (int i = 0; i < finalStatesCount; i++)
            out.print(" " + finalStates.get(i));
        out.println(";");
        out.println("    node [shape = circle style=\"\" color=\"black\" fontcolor=\"black\"];");

        for (int i = 0; i < stateCount; i++)
            for (int j = 0; j < d[i].size(); j++)
                out.println("    " + i + " -> " + d[i].get(j).destination + " [label = \"" + d[i].get(j).symbol + "\"];");
        out.println("}");
    }

    private record Transition(int destination, char symbol) {
    }


    public static void main(String[] args) {
        FSM initial = new FSM();
        System.out.println("init ok");
        try {
            System.out.println(System.getProperty("user.dir"));
            System.out.println("before scanner");
            Scanner in = new Scanner(new File("legacy/date.in"));
            System.out.println("before read");
            initial.read(in);
        } catch (Exception e) {
            System.out.println("Eroare");
        }
        try (PrintWriter out = new PrintWriter("graphviz.out")) {
            initial.graphviz(out);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}

