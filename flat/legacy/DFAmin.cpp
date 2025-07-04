#include <unordered_set>
#include <algorithm>
#include <iostream>
#include <fstream>
#include <vector>
#include <queue>
using namespace std;
ifstream in ("date.in");
ofstream out("date.out");
struct leg
{
    int dest;char sim;
    bool operator==(const leg &x)
    {
        return((dest==x.dest)&&(sim==x.sim));
    }
};
int const N=100;
class DFA
{
    int NrStari,Start,NrFinale,NrLeg;
    unordered_set<char> simbolset;
    vector<int> StariFinale;
    vector<leg> d[N];
    bool gasit(int a,int b,int c)
    {
        leg primita;
        primita.sim=c;
        primita.dest=b;
        for(size_t i=0;i<d[a].size();i++)
            if(d[a][i]==primita)
                return true;
        return false;
    }
    void del_stare(int x)
    {
        for(int i=0;i<NrStari;i++)
            for(size_t j=0;j<d[i].size();j++)
            {
                if(d[i][j].dest==x)
                {
                    d[i].erase(d[i].begin()+j);
                    NrLeg--;
                    j--;
                }
                else if(d[i][j].dest>x)
                    d[i][j].dest--;
            }
        NrLeg-=d[x].size();
        d[x].erase(d[x].begin(),d[x].end());
        for(int i=x+1;i<NrStari;i++)
            d[i].swap(d[i-1]);
        for(size_t i=0;i<NrFinale;i++)
            if(StariFinale[i]>x)
                StariFinale[i]--;
            else if(StariFinale[i]==x)
                {StariFinale.erase(StariFinale.begin()+i);i--;NrFinale--;}
        NrStari--;
    }
    void mergeState(int x, int y) // x<y
    {
        for(int i=0;i<NrStari;i++)
            for(size_t j=0;j<d[i].size();j++)
                if(d[i][j].dest==y)
                    d[i][j].dest=x;
                else if(d[i][j].dest>y)
                    d[i][j].dest--;
        leg temp;
        while(!d[y].empty())
        {
            temp=d[y].back();
            d[y].pop_back();
            if(find(d[x].begin(),d[x].end(),temp)!=d[x].end())//temp is in d[x] ?
                NrLeg--;
            else
                d[x].push_back(temp);
        }
        d[y].erase(d[y].begin(),d[y].end());
        for(int i=y+1;i<NrStari;i++)
            d[i].swap(d[i-1]);
        for(size_t i=0;i<NrFinale;i++)
            if(StariFinale[i]>y)
                StariFinale[i]--;
            else if(StariFinale[i]==y)
                {StariFinale.erase(StariFinale.begin()+i);i--;NrFinale--;}
        NrStari--;
    }
    int getDest(int x,char c)
    {
        for(size_t i=0;i<d[x].size();i++)
            if(d[x][i].sim==c)
                return d[x][i].dest;
        return -1;
    }
    bool isFinal(int x)
    {
        for(size_t i=0;i<StariFinale.size();i++)
            if(StariFinale[i]==x)
                return true;
        return false;
    }
public:
    void citire()
    {
        int a,b;
        char c;
        leg noua;
        in>>NrStari>>Start>>NrFinale;
        for(int i=1;i<=NrFinale;i++)
        {in>>b; StariFinale.push_back(b);}
        in>>NrLeg;
        for(int i=1;i<=NrLeg;i++)
        {
            in>>a>>b>>c;
            if(gasit(a,b,c))
                throw 5;
            noua.sim=c;
            noua.dest=b;
            d[a].push_back(noua);
            if(simbolset.find(c)==simbolset.end())
                simbolset.insert(c);
        }
        if(NrLeg!=NrStari*simbolset.size())
            throw 5;
    }
    void del_unreach()
    {
        bool reach[N];
        for(int i=0;i<NrStari;i++)
            reach[i]=false;
        queue<int> q;
        q.push(Start);
        reach[Start]=true;
        while(!q.empty())
        {
            int x=q.front();
            q.pop();
            for(size_t i=0;i<d[x].size();i++)
                if(!reach[d[x][i].dest])
                {
                    reach[d[x][i].dest]=true;
                    q.push(d[x][i].dest);
                }
        }
        int vechi=NrStari;
        for(int i=0;i<vechi;i++)
            if(!reach[i])
            del_stare(i-vechi+NrStari);
    }
    void separabile()
    {
        bool separ[N][N];
        for(int i=0;i<NrStari;i++)
            for(int j=0;j<NrStari;j++)
                if(isFinal(i)!=isFinal(j))
                    separ[i][j]=true;
                else
                    separ[i][j]=false;
        bool changed=true;
        while(changed)
        {
            changed=false;
            for(int i=0;i<NrStari;i++)
                for(int j=i+1;j<NrStari;j++)
                    if(!separ[i][j])
                        for(auto c=simbolset.begin();c!=simbolset.end();c++)
                        {
                            int iDest=getDest(i,*c);
                            int jDest=getDest(j,*c);
                            bool iTranz=(iDest>=0);
                            bool jTranz=(jDest>=0);
                            if((iTranz==false)&&(jTranz==false))
                                continue;
                            if(iTranz!=jTranz)
                            {
                                separ[i][j]=true;
                                changed=true;
                                continue;
                            }
                            if(separ[iDest][jDest] || separ[jDest][iDest])
                            {separ[i][j]=true;changed=true;}
                        }
        }
        int vechi=NrStari;

    //
    //Afis
    //
        for(int i=0;i<NrStari;i++)
            {
                for(int j=i+1;j<NrStari;j++)
                    cout<<separ[i][j]<<" ";
                cout<<"\n";
            }
    //
    //End afis
    //

        for(int i=0;i<NrStari;i++)
            for(int j=i+1;j<NrStari;j++)
                if(!separ[i][j])
                    mergeState(i-vechi+NrStari,j-vechi+NrStari);
    }
    void afis()
    {
        out<<NrStari<<" "<<Start<<" "<<NrFinale<<"\n";
        for(int i=0;i<NrFinale;i++)
                out<<StariFinale[i]<<" ";
        out<<"\n"<<NrLeg<<"\n";
        for(int i=0;i<NrStari;i++)
            for(size_t j=0;j<d[i].size();j++)
                out<<i<<"\t"<<d[i][j].dest<<"\t"<<d[i][j].sim<<"\n";
    }

};
int main()
{
    DFA initial;
    try
    {
        initial.citire();
    }
    catch(int){cout<<"Eroare\n";}
    initial.del_unreach();
    initial.separabile();
    initial.afis();
    return 0;
}
