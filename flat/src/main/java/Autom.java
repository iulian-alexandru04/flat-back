import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Autom {
    private record Transition(int destination, char symbol) {
    }

    final static int N = 100;
    int stateCount, start, finalStatesCount, transitionsCount;
    List<Transition>[] transitions = new ArrayList[N];
    boolean[] finalStates = new boolean[N];
    char lambdaSymbol = 'l';
    int[] cod = new int[N];
    Set<Character> alphabet = new HashSet<>();

    public Autom(char chr) {
        stateCount = 2;
        start = 0;
        finalStatesCount = 1;
        finalStates[0] = false;
        finalStates[1] = true;
        transitionsCount = 1;
        transitions[0] = new ArrayList<>();
        transitions[1] = new ArrayList<>();
        transitions[0].add(new Transition(1, chr));
        lambdaSymbol = 'l';
    }

    public static Autom star(Autom fsm) {
        for (int i = 0; i < fsm.stateCount; i++) {
            if (fsm.finalStates[i]) {
                Transition noua = new Transition(fsm.start, fsm.lambdaSymbol);
                fsm.transitions[i].add(noua);
                fsm.transitionsCount++;
            }
        }
        fsm.finalStates[fsm.start] = true;
        fsm.finalStatesCount++;
        return fsm;
    }

    public static Autom concat(Autom fsm1, Autom fsm2) {
        fsm1.finalStatesCount = fsm2.finalStatesCount;
        fsm1.transitionsCount += fsm2.transitionsCount;
        for (int i = 0; i < fsm2.stateCount; i++) {
            for (int j = 0; j < fsm2.transitions[i].size(); j++) {
                Transition l = fsm2.transitions[i].get(j);
                fsm1.transitions[i + fsm1.stateCount].add(new Transition(l.destination + fsm1.stateCount, l.symbol));
            }
        }
        for (int i = 0; i < fsm1.stateCount; i++) {
            if (fsm1.finalStates[i]) {
                fsm1.transitions[i].add(new Transition(fsm2.start + fsm1.stateCount, 'l'));
                fsm1.transitionsCount++;
            }
        }
        fsm1.finalStates = new boolean[N];
        for (int i = 0; i < fsm2.stateCount; i++) {
            if (fsm2.finalStates[i])
                fsm1.finalStates[i + fsm1.stateCount] = true;
        }
        fsm1.stateCount += fsm2.stateCount;
        return fsm1;
    }

    public static Autom paralel(Autom fsm1, Autom fsm2) {
        fsm1.finalStatesCount += fsm2.finalStatesCount;
        fsm1.transitionsCount += fsm2.transitionsCount;
        for (int i = 0; i < fsm2.stateCount; i++) {
            if (fsm2.finalStates[i])
                fsm1.finalStates[i + fsm1.stateCount] = true;
        }
        for (int i = 0; i < fsm2.stateCount; i++) {
            for (int j = 0; j < fsm2.transitions[i].size(); j++) {
                Transition l = fsm2.transitions[i].get(j);
                fsm1.transitions[i + fsm1.stateCount].add(new Transition(l.destination + fsm1.stateCount, l.symbol));
            }
        }
        fsm1.transitions[fsm1.stateCount + fsm2.stateCount].add(new Transition(fsm1.start, 'l'));
        fsm1.transitions[fsm1.stateCount + fsm2.stateCount].add(new Transition(fsm2.start + fsm1.stateCount, 'l'));
        fsm1.transitionsCount += 2;
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

    Set<Integer> lamd(Set<Integer> states)
    {
        Set<Integer> prevStates;
        do {
            prevStates = new HashSet<>(states);
            states.addAll(getDestinations(states, lambdaSymbol));
        } while (!states.equals(prevStates));
        return states;
    }

    boolean checkWord(String word) {
        Set<Integer> crt = lamd(Set.of(start));
        for(char c : word.toCharArray()) {
            crt = lamd(getDestinations(crt, c));
        }
        return crt.stream().anyMatch(state -> finalStates[state]);
    }

    int getOrAssignCode(ArrayList<Set<Integer>> codArr, Set<Integer> codValue, Autom dfa, Autom nfa) {
        for (int i = 0; i < codArr.size(); i++) {
            if (codValue.equals(codArr.get(i)))
                return i;
        }
        int idx = codArr.size();
        codArr.add(codValue);
        boolean isFinal = codValue.stream().anyMatch(x -> nfa.finalStates[x]);
        if (isFinal) {
            dfa.finalStates[idx] = true;
            dfa.finalStatesCount++;
        }
        return idx;
    }

    void transforma(Autom dfa, Autom nfa) {
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
                dfa.transitionsCount++;
            }
            idx++;
        }
        dfa.stateCount = ini.size();
    }

    public static void main(String[] args) throws IOException {

    }
}