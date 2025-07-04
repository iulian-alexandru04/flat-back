#include <iostream>
#include <fstream>
#include <vector>
#include <queue>
#include <cstring>
using namespace std;
ifstream in ("date.in");
int const N=100;
int nrstari,nrstarifin,nrrel;
struct rel{int dest; char simb;};
vector<rel> leg[N];
bool fin[N];
void citire()
{
    int a,b; char c;
    rel temp;
    in>>nrstari>>nrstarifin>>nrrel;
    for(int i=1;i<=nrstarifin;i++)
    {in>>a; fin[a]=true;}
    for(int i=1;i<=nrrel;i++)
    {
        in>>a>>b>>c;
        temp.dest=b;
        temp.simb=c;
        leg[a].push_back(temp);
    }
}
void verifica(char *src)
{
    queue<int> coada[2];
    bool IsInC[N];
    coada[1].push(1);
    IsInC[1]=true;
    int crt,comp;
    int dim=strlen(src);
    int i;
    for(i=0;i<dim;i++)
    {
        comp=(i+1)%2;
        cout<<src[i]<<": ";
        while(!coada[comp].empty())
        {
            crt=coada[comp].front();
            coada[comp].pop();
            for(size_t d=0;d<leg[crt].size();d++)
            {
                if((leg[crt][d].simb==src[i])&&(!IsInC[leg[crt][d].dest]))
                {
                    cout<<leg[crt][d].dest<<" ";
                    coada[i%2].push(leg[crt][d].dest);
                    IsInC[leg[crt][d].dest]=true;
                }
            }
        }
        cout<<"\n";
        for(int i=1;i<=nrstari;i++)
            IsInC[i]=false;
    }
    comp=(i+1)%2;
    while(!coada[comp].empty())
    {
        crt=coada[comp].front();
            coada[comp].pop();
        if(fin[crt]){cout<<"Cuvant acceptat!\n";return;}
    }
    cout<<"Cuvant respins!\n";
}
int main()
{
    citire();
    char cuv[N];
    cin>>cuv;
    verifica(cuv);
    return 0;
}
