# Formal-Languages-and-Automata

DFA,NFA,NFA-l format(.fsm):
type
start (lam_symbol)
F=no_final_states
final1 final2 ... finalF
T=no_transitions
source destination symbol
.
.
.
source destination symbol


types:
1=DFA
2=NFA
3=NFA-l (only now lam_symbol is present)

all states (start, final1,...,finalF, source, destination)
are the numbers asociated with the sates

any printable ASCII character can be a symbol (including lam_symbol)

TO DO: https://en.wikipedia.org/wiki/Nondeterministic_finite_automaton#References