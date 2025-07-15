import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FSM {
    int MAX_STATES = 1000; // Maximum number of states
    private int stateCount, start;
    private Set<Character> alphabet = new HashSet<>();
    private Set<Integer> finalStates = new HashSet<>();
    private Set<Transition>[] transitions;
    char lambdaSymbol = 'l';

    public FSM() {
        stateCount = 2;
        start = 0;
        finalStates.add(1);
        lambdaSymbol = 'l';
        transitions = new HashSet[MAX_STATES];
        transitions[0] = new HashSet<>();
        transitions[1] = new HashSet<>();
        transitions[0].add(new Transition(1, lambdaSymbol));
    }

    public static FSM star(FSM fsm) {
        Transition toStart = new Transition(fsm.start, fsm.lambdaSymbol);
        for (int i : fsm.finalStates)
            fsm.transitions[i].add(toStart);
        fsm.finalStates.add(fsm.start);
        return fsm;
    }

    public static FSM concat(FSM fsm1, FSM fsm2) {
        for (int i = 0; i < fsm2.stateCount; i++)
            for (Transition l : fsm2.transitions[i])
                fsm1.transitions[i + fsm1.stateCount].add(new Transition(l.destination + fsm1.stateCount, l.symbol));

        Transition toFSM2Start = new Transition(fsm2.start + fsm1.stateCount, 'l');
        for (int i : fsm1.finalStates)
            fsm1.transitions[i].add(toFSM2Start);

        fsm1.finalStates = new HashSet<>(fsm2.finalStates.stream().map(s -> s + fsm1.stateCount).collect(Collectors.toSet()));

        fsm1.stateCount += fsm2.stateCount;
        return fsm1;
    }


    public static FSM paralel(FSM fsm1, FSM fsm2) {
        for (int i : fsm2.finalStates)
            fsm1.finalStates.add(i + fsm1.stateCount);

        for (int i = 0; i < fsm2.stateCount; i++)
            for (Transition l : fsm2.transitions[i])
                fsm1.transitions[i + fsm1.stateCount].add(new Transition(l.destination + fsm1.stateCount, l.symbol));

        fsm1.transitions[fsm1.stateCount + fsm2.stateCount].add(new Transition(fsm1.start, 'l'));
        fsm1.transitions[fsm1.stateCount + fsm2.stateCount].add(new Transition(fsm2.start + fsm1.stateCount, 'l'));
        fsm1.stateCount += fsm2.stateCount;
        fsm1.start = fsm1.stateCount++;
        return fsm1;
    }


    Set<Integer> getDestinations(Set<Integer> states, char c) {
        return states.stream().flatMap(s -> getDestinations(s, c)).collect(Collectors.toSet());
    }

    Stream<Integer> getDestinations(int state, char c) {
        return transitions[state].stream()
                .filter(t -> t.symbol == c)
                .map(t -> t.destination);
    }

    Set<Integer> lamd(Set<Integer> states) {
        Set<Integer> prevStates;
        do {
            prevStates = new HashSet<>(states);
            states.addAll(getDestinations(states, lambdaSymbol));
        } while (!states.equals(prevStates));
        return states;
    }

    boolean checkWord(String word) {
        Set<Integer> crt = lamd(Set.of(start));
        for (char c : word.toCharArray()) {
            crt = lamd(getDestinations(crt, c));
        }
        return !Collections.disjoint(crt, finalStates);
    }


    int getOrAssignCode(ArrayList<Set<Integer>> codArr, Set<Integer> codValue, FSM dfa, FSM nfa) {
        int i = codArr.indexOf(codValue);
        if (i != -1)
            return i;

        int idx = codArr.size();
        codArr.add(codValue);
        if (codValue.stream().anyMatch(nfa.finalStates::contains))
            dfa.finalStates.add(idx);
        return idx;
    }

    void transforma(FSM dfa, FSM nfa) {
        var ini = new ArrayList<Set<Integer>>();
        Set<Integer> crt = nfa.lamd(Set.of(nfa.start));
        getOrAssignCode(ini, crt, dfa, nfa);
        int idx = 0;
        while (idx <= ini.size()) {
            crt = ini.get(idx);
            for (char sim : nfa.alphabet) {
                if (sim == 'l') continue;
                Set<Integer> dest = crt;
                dest = nfa.lamd(dest);
                dest = nfa.getDestinations(dest, sim);
                dest = nfa.lamd(dest);
                if (dest.isEmpty()) continue;
                Transition noua = new Transition(getOrAssignCode(ini, dest, dfa, nfa), sim);
                dfa.transitions[idx].add(noua);
            }
            idx++;
        }
        dfa.stateCount = ini.size();
    }

    void removeState(int x) {
        // Remove all transitions to state x and update destination indices
        for (int i = 0; i < stateCount; i++)
            transitions[i] = transitions[i].stream()
                    .filter(t -> t.destination() != x)
                    .map(t -> t.destination() < x ? t : new Transition(t.destination() - 1, t.symbol()))
                    .collect(Collectors.toSet());

        transitions[x].clear();
        // Shift states after x
        for (int i = x + 1; i < stateCount; i++)
            transitions[i - 1] = transitions[i];

        // Update final states
        finalStates = finalStates.stream()
                .filter(s -> s != x)
                .map(s -> s > x ? s - 1 : s)
                .collect(Collectors.toSet());
        stateCount--;
    }

    void mergeState(int x, int y) { // x < y
        // redirect incoming into y towards x
        for (int i = 0; i < stateCount; i++)
            transitions[i] = transitions[i].stream()
                    .map(t -> t.destination() == y ? new Transition(x, t.symbol()) : t)
                    .collect(Collectors.toSet());

        // outgoings of y will start from x
        transitions[x] = Stream.concat(transitions[x].stream(), transitions[y].stream()).collect(Collectors.toSet());
        removeState(y);
    }

    Optional<Integer> getDestination(int state, char symbol) {
        return transitions[state].stream()
                .filter(t -> t.symbol() == symbol)
                .map(Transition::destination)
                .findFirst();
    }

    Stream<Integer> getAllDestinations(int state, char symbol) {
        return transitions[state].stream()
                .filter(t -> t.symbol() == symbol)
                .map(Transition::destination);
    }

    public void read(Scanner in) throws IOException {
        stateCount = in.nextInt();
        start = in.nextInt();
        int finalStatesCount = in.nextInt();
        for (int i = 1; i <= finalStatesCount; i++)
            finalStates.add(in.nextInt());

        transitions = new HashSet[stateCount + 1];
        for (int i = 0; i <= stateCount; i++) {
            transitions[i] = new HashSet<>();
        }

        int transitionsCount = in.nextInt();
        for (int i = 1; i <= transitionsCount; i++) {
            int from = in.nextInt();
            int to = in.nextInt();
            char symbol = in.next().charAt(0);
            transitions[from].add(new Transition(to, symbol));
            alphabet.add(symbol);
        }
    }

    void removeUnreachableStates() {
        boolean[] reach = new boolean[stateCount + 1];
        Queue<Integer> q = new LinkedList<>();
        q.add(start);
        reach[start] = true;
        while (!q.isEmpty()) {
            int x = q.poll();
            for (Transition t : transitions[x]) {
                if (!reach[t.destination]) {
                    reach[t.destination] = true;
                    q.add(t.destination);
                }
            }
        }
        int oldCount = stateCount;
        for (int i = 0; i < oldCount; i++)
            if (!reach[i])
                removeState(i + stateCount - oldCount);
    }

    void minimize() {
        boolean[][] distinguishable = new boolean[stateCount + 1][stateCount + 1]; // include the black hole state
        for (int i = 0; i <= stateCount; i++)
            for (int j = 0; j <= stateCount; j++)
                distinguishable[i][j] = finalStates.contains(i) != finalStates.contains(j);

        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i <= stateCount; i++) {
                for (int j = i + 1; j <= stateCount; j++) {
                    if (!distinguishable[i][j]) {
                        for (char c : alphabet) {
                            var iDest = getDestination(i, c).orElse(stateCount);
                            var jDest = getDestination(j, c).orElse(stateCount);
                            if (distinguishable[iDest][jDest] || distinguishable[jDest][iDest]) {
                                distinguishable[i][j] = true;
                                changed = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        int oldCount = stateCount;
        for (int i = 0; i < stateCount; i++)
            for (int j = i + 1; j < stateCount; j++)
                if (!distinguishable[i][j])
                    mergeState(i - oldCount + stateCount, j - oldCount + stateCount);
    }

    boolean check(String src, PrintWriter out) {
        Set<Integer> crtStates = Set.of(start);
        for (char crtChar : src.toCharArray()) {
            crtStates = crtStates.stream()
                    .flatMap(state -> getAllDestinations(state, crtChar))
                    .collect(Collectors.toSet());

            out.printf("%c: %s%n", crtChar, crtStates);
            out.flush();
        }
        return !Collections.disjoint(crtStates, finalStates);
    }

    void print(Writer out) throws IOException {
        out.write(stateCount + " " + start + " " + finalStates.size() + "\n");
        for (Integer finalState : finalStates)
            out.write(finalState + " ");
        int transitionsCount = Arrays.stream(transitions).mapToInt(Set::size).sum();
        out.write("\n" + transitionsCount + "\n");
        for (int i = 0; i < stateCount; i++)
            for (Transition t : transitions[i])
                out.write(i + "\t" + t.destination + "\t" + t.symbol + "\n");
        out.flush();
    }

    public void graphviz(PrintWriter out) {
        out.println("digraph finite_state_machine {");
        out.println("    fontname=\"Helvetica,Arial,sans-serif\"");
        out.println("    node [fontname=\"Helvetica,Arial,sans-serif\"]");
        out.println("    edge [fontname=\"Helvetica,Arial,sans-serif\"]");
        out.println("    rankdir=LR;");
        out.println("    node [shape = circle style=filled fillcolor=\"lightcyan\"]; " + start + ";");
        out.print("    node [shape = doublecircle style=filled fillcolor=\"navy\" fontcolor=\"white\"];");
        for (Integer finalState : finalStates)
            out.print(" " + finalState);
        out.println(";");
        out.println("    node [shape = circle style=\"\" color=\"black\" fontcolor=\"black\"];");

        for (int i = 0; i < stateCount; i++)
            for (Transition t : transitions[i])
                out.println("    " + i + " -> " + t.destination + " [label = \"" + t.symbol + "\"];");
        out.println("}");
    }

    private record Transition(int destination, char symbol) { }

    public static void main(String[] args) {
        FSM initial = new FSM();
        try {
            Scanner in = new Scanner(new File("legacy/test-data/NFA2DFAdate.fsm"));
            initial.read(in);
        } catch (Exception e) {
            System.out.println("Eroare");
        }
        try (PrintWriter out = new PrintWriter("graphviz.out")) {
//            initial.graphviz(out);
            Scanner keyboardScanner = new Scanner(System.in);
            var accepted = initial.check(keyboardScanner.next(), new PrintWriter(System.out, true));
            if (accepted) out.println("Accepted!");
            else out.println("Rejected!");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}