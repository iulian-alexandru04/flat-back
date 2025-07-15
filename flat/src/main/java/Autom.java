import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Autom {
    private record Transition(int destination, char symbol) {
    }

    int stateCount, start;
    Set<Character> alphabet = new HashSet<>();
    Set<Integer> finalStates = new HashSet<>();
    Set<Transition>[] transitions = new Set[100];
    char lambdaSymbol = 'l';

    public Autom(char chr) {
        stateCount = 2;
        start = 0;
        finalStates.add(1);
        transitions[0] = new HashSet<>();
        transitions[1] = new HashSet<>();
        transitions[0].add(new Transition(1, chr));
        lambdaSymbol = 'l';
    }

    public static Autom star(Autom fsm) {
        Transition toStart = new Transition(fsm.start, fsm.lambdaSymbol);
        for (int i : fsm.finalStates)
            fsm.transitions[i].add(toStart);
        fsm.finalStates.add(fsm.start);
        return fsm;
    }

    public static Autom concat(Autom fsm1, Autom fsm2) {
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

    public static Autom paralel(Autom fsm1, Autom fsm2) {
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

    int getOrAssignCode(ArrayList<Set<Integer>> codArr, Set<Integer> codValue, Autom dfa, Autom nfa) {
        int i = codArr.indexOf(codValue);
        if (i != -1)
            return i;

        int idx = codArr.size();
        codArr.add(codValue);
        if (codValue.stream().anyMatch(nfa.finalStates::contains))
            dfa.finalStates.add(idx);
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
            }
            idx++;
        }
        dfa.stateCount = ini.size();
    }

    public static void main(String[] args) throws IOException {

    }
}