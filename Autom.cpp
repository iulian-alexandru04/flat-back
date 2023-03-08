//Nu merge pentru automate cu mai mult de 32/64 stari
#include <unordered_set>
#include <cstring>
#include <vector>
using namespace std;
struct leg
{
    int dest;
    char sim;
    leg(int d=0, char s='l')
    {dest=d;sim=s;}
};
unordered_set<char> simbolset;
int const N=100;
struct autom
{
    //starile sunt in [0,NrStari)
    int NrStari,Start,NrFinale,NrLeg,StariFinale;
    char lam_symbol;
    char type[N];
    vector<leg> d[N];
    int cod[N];

    autom(char chr='l')
    {
        NrStari=2;
        Start=0;
        NrFinale=1;
        StariFinale=2;
        NrLeg=1;
        lam_symbol='l';
        d[0].push_back(leg(1,chr));
    }

    autom static star(autom fsm)
    {
        leg noua;
        noua.dest=fsm.Start;
        noua.sim=fsm.lam_symbol;
        fsm.d[fsm.NrStari++].push_back(noua);
        fsm.Start=fsm.NrStari-1;
        noua.dest=fsm.Start;
        for(int i=0;i<fsm.NrStari;i++)
            if(fsm.StariFinale&(1<<i))
                {fsm.d[i].push_back(noua);fsm.NrLeg++;}
        fsm.StariFinale|=1<<fsm.Start;
        fsm.NrFinale++;
        return fsm;
    }
    autom static concat(autom fsm1, autom fsm2)
    {
        fsm1.NrFinale=fsm2.NrFinale;
        fsm1.NrLeg+=fsm2.NrLeg;
        for(int i=0;i<fsm2.NrStari;i++)
            for(size_t j=0;j<fsm2.d[i].size();j++)
                fsm1.d[i+fsm1.NrStari].push_back(leg(fsm2.d[i][j].dest+fsm1.NrStari,fsm2.d[i][j].sim));
                //out<<i<<"\t"<<d[i][j].dest<<"\t"<<d[i][j].sim<<"\n";
        for(int i=0;i<fsm1.NrStari;i++)
            if(fsm1.StariFinale&(1<<i))
                {fsm1.d[i].push_back(leg(fsm2.Start+fsm1.NrStari,'l'));fsm1.NrLeg++;}
        fsm1.StariFinale=fsm2.StariFinale<<fsm1.NrStari;
        fsm1.NrStari+=fsm2.NrStari;
        return fsm1;
    }
    autom static paralel(autom fsm1, autom fsm2)
    {
        fsm1.NrFinale+=fsm2.NrFinale;
        fsm1.NrLeg+=fsm2.NrLeg;
        fsm1.StariFinale|=fsm2.StariFinale<<fsm1.NrStari;
        for(int i=0;i<fsm2.NrStari;i++)
            for(size_t j=0;j<fsm2.d[i].size();j++)
                fsm1.d[i+fsm1.NrStari].push_back(leg(fsm2.d[i][j].dest+fsm1.NrStari,fsm2.d[i][j].sim));
        fsm1.d[fsm1.NrStari+fsm2.NrStari].push_back(leg(fsm1.Start,'l'));
        fsm1.d[fsm1.NrStari+fsm2.NrStari].push_back(leg(fsm2.Start+fsm1.NrStari,'l'));
        fsm1.NrLeg+=2;
        fsm1.NrStari+=fsm2.NrStari;
        fsm1.Start=fsm1.NrStari++;
        return fsm1;
    }
    friend ostream &operator<<(ostream &out,const autom &fsm)
    {
        out<<fsm.NrStari<<" "<<fsm.Start<<" "<<fsm.NrFinale<<"\n";
        for(int i=0;i<fsm.NrStari;i++)
            if(fsm.StariFinale&(1<<i))
                out<<i<<" ";
        out<<"\n"<<fsm.NrLeg<<"\n";
        for(int i=0;i<fsm.NrStari;i++)
            for(size_t j=0;j<fsm.d[i].size();j++)
                out<<i<<"\t"<<fsm.d[i][j].dest<<"\t"<<fsm.d[i][j].sim<<"\n";
        return out;
    }
    friend istream &operator>>(istream &in,autom &fsm)
    {
        int a,b;
        char c;
        leg noua;
        in>>fsm.NrStari>>fsm.Start>>fsm.NrFinale;
        for(int i=1;i<=fsm.NrFinale;i++)
        {in>>b; fsm.StariFinale|=1<<b;}
        in>>fsm.NrLeg;
        for(int i=1;i<=fsm.NrLeg;i++)
        {
            in>>a>>b>>c;
            noua.dest=b;
            noua.sim=c;
            fsm.d[a].push_back(noua);
            if(simbolset.find(c)==simbolset.end())
                simbolset.insert(c);
        }
    }
    /*AFISEZ CU RENUMEROTARE
    void afis_dfa()
    {
        out<<NrStari<<" "<<Start<<" "<<NrFinale<<"\n";
        for(int i=0;i<NrStari;i++)
            if(StariFinale&(1<<i))
                out<<cod[i]<<" ";
        out<<"\n"<<NrLeg<<"\n";
        for(int i=0;i<NrStari;i++)
            for(size_t j=0;j<d[i].size();j++)
                out<<cod[i]<<"\t"<<cod[d[i][j].dest]<<"\t"<<d[i][j].sim<<"\n";
                //out<<i<<"\t"<<d[i][j].dest<<"\t"<<d[i][j].sim<<"\n";
    }*/
    int merg(int crt,char c)
    {
        int dest=0;
        for(int i=0;i<NrStari;i++)
            {
                if((crt&(1<<i))==0)  continue;
                for(size_t j=0;j<d[i].size();j++)
                    if(d[i][j].sim==c)
                        dest|=1<<d[i][j].dest;
            }
        return dest;
    }
    int lamd(int crt)
    {
        int init;
        do
        {
            init=crt;
            crt|=merg(crt,'l');

        }while(crt!=init);
        return crt;
    }
    void verifica_cuv()
    {
        char *cuv=new char[100];
        cin>>cuv;
        int crt=lamd(1<<Start);
        for(int i=0;i<strlen(cuv);i++)
            crt=lamd(merg(crt,cuv[i]));
        if(crt&StariFinale)
            cout<<"Cuvant acceptat\n";
        else
            cout<<"Cuvant respins\n";
    }

}dfa,nfa;

int cod(int &ini,int cod)
{
    for(int i=0;i<ini;i++)
        if(dfa.cod[i]==cod)
            return i;
    if(cod&nfa.StariFinale)
    {
        dfa.StariFinale|=1<<ini;
        dfa.NrFinale++;
    }
    dfa.cod[ini++]=cod;
    return ini-1;
}
void transforma()
{
    int ini=0,termi=0;
    int crt=nfa.lamd(1<<nfa.Start);
    dfa.Start=crt;
    cod(ini,dfa.Start);
    while(ini>termi)
    {
        crt=dfa.cod[termi];
        for(auto sim=simbolset.begin();sim!=simbolset.end();++sim)
        {
            if(*sim=='l')   continue;
            int dest=crt;
            dest=nfa.lamd(dest);
            dest=nfa.merg(dest,*sim);
            dest=nfa.lamd(dest);
            if(dest==0) continue;
            leg noua;
            noua.sim=*sim;
            noua.dest=cod(ini,dest);
            dfa.d[termi].push_back(noua);
            dfa.NrLeg++;
        }
        termi++;
    }
    dfa.NrStari=ini;
}
