#include <iostream>
#include <fstream>
#include <cstring>
#include <vector>
using namespace std;
typedef vector<char> stack;
int const N=100;
class transition
{
    int src,dest;
    char sim,stk;
    char *put;
public:
    friend ostream &operator<<(ostream &out,const transition &t);
    transition(const int src,const int dest,const char sim,const char stk,const char* const put)
    {
        this->src=src;
        this->dest=dest;
        this->sim=sim;
        this->stk=stk;
        this->put=new char[strlen(put)+1];
        strcpy(this->put,put);
    }
    transition(const transition& other)
    {
        this->src=other.src;
        this->dest=other.dest;
        this->sim=other.sim;
        this->stk=other.stk;
        this->put=new char[strlen(other.put)+1];
        strcpy(this->put,other.put);
    }
    ~transition()
    {delete[] put;}

    bool isThis(int state,char c,char st)
    {return((src==state)&&((sim==c)||(sim=='#'))&&(stk==st));}

    const char* get_put()
    {return put;}

    int get_dest()
    {return dest;}
};
class PDA
{
    int NrStates,Start,NrFinals,NrTranz;
    vector<int> Finals;
    vector<transition> Tranz;
public:
    friend istream &operator>>(istream &in,PDA &a);
    friend ofstream &operator<<(ofstream &out,const PDA &a);
    int getStart()  {return Start;}
    bool IsFinal(int state)
    {
        for(size_t i=0;i<Finals.size();i++)
            if(state==Finals[i])
                return true;
        return false;
    }
    bool accept(const char* str,int state,stack stk)
    {
        if((state<0)||(state>=NrStates))
        {
            cout<<"corrupted state\n";
            return false;
        }
        if((strlen(str)==0)&&(IsFinal(state)))
                return true;
        if(stk.size()<1)
        {
            cout<<"corrupted stack\n";
            return false;
        }
        //cout<<state<<" ";
        const char* next_str=str;
        if(strlen(str)>0)
            next_str++;
        char st=stk.back();
        stk.pop_back();
        auto InitialEnd=stk.end();
        for(int i=0;i<NrTranz;i++)
            if(Tranz[i].isThis(state,str[0],st))
            {
                for(int j=strlen(Tranz[i].get_put())-1;j>=0;j--)
                    if(Tranz[i].get_put()[j]!='#')
                        stk.push_back(Tranz[i].get_put()[j]);
                if(accept(next_str,Tranz[i].get_dest(),stk))
                   return true;
                stk.erase(InitialEnd,stk.end());
            }
            else if(Tranz[i].isThis(state,'#',st))
                if(accept(next_str,Tranz[i].get_dest(),stk))
                   return true;
        //if(accept(str,Tranz[i].get_dest(),stk))
            //return true;
        return false;
    }
};
ostream &operator<<(ostream &out,const transition &t)
{
    out<<t.src<<" ";
    out<<t.dest<<" ";
    out<<t.sim<<" ";
    out<<t.stk<<" ";
    out<<t.put;
    return out;
}
istream &operator>>(istream &in,PDA &a)
{
    int src,dest;
    char chr,stv,put[N];
    in>>a.NrStates>>a.Start>>a.NrFinals;
    for(int i=0;i<a.NrFinals;i++)
    {in>>src; a.Finals.push_back(src);}
    in>>a.NrTranz;
    for(int i=0;i<a.NrTranz;i++)
    {
        in>>src>>dest>>chr>>stv>>put;
        transition nou(src,dest,chr,stv,put);
        a.Tranz.push_back(nou);
    }
    return in;
}
ofstream &operator<<(ofstream &out,const PDA &a)
{
    out<<a.NrStates<<" "<<a.Start<<" "<<a.NrFinals<<"\n";
    for(int i=0;i<a.NrFinals;i++)
        out<<a.Finals[i]<<" ";
    out<<"\n"<<a.NrTranz<<"\n";
    for(int i=0;i<a.NrTranz;i++)
        out<<a.Tranz[i]<<"\n";
    return out;
}
int main()
{
    ifstream in ("date.in");
    ofstream out("date.out");
    PDA autom;
    char str[N];
    in>>autom;
    //out<<autom;
    cin>>str;
    stack stk(2,'z');
    if(autom.accept(str,autom.getStart(),stk))
        cout<<"Cuvant acceptat\n";
    else
        cout<<"Cuvant respins\n";
    return 0;
}
