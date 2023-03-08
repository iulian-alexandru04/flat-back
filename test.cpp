#include<iostream>
#include"RegEx.cpp"
using namespace std;
int main()
{
    int n;
    RegEx my_expresion;
    cin>>my_expresion;
    nfa=my_expresion.getAutom();
    transforma();
    cout<<my_expresion<<"\n"<<dfa;
    cout<<"\n\nNr cuvinte de testat:";
    cin>>n;
    for(int i=1;i<=n;i++)
        dfa.verifica_cuv();
    return 0;
}
