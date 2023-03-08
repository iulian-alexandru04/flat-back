#include<cstring>
#include"Autom.cpp"
using namespace std;
class RegEx
{
    public:
    class Node
    {
    public:
        char op;
        char symb;
        bool group;
        Node* left;
        Node* right;
        Node() {group=false; left=right=NULL;}
        Node(char symb) {group=false; this->symb=symb; left=right=NULL;}
        void afis()
        {
            if(!left)
            {
                cout<<symb;
                return;
            }
            if(op=='*') cout<<"(";
            left->afis();
            if(op=='*') cout<<")";
            if(op!='.') cout<<op;
            if(right)   right->afis();

        }
    };
    static int const N=1000;
    char expr[N];
    Node* arb;

    Node* getToken(int& i)
    {
        if(isalpha(expr[i]))
            return new Node(expr[i++]);
        if(expr[i]=='(')
        {
            int aux=strchr(expr+i,')')-expr;
            Node* temp=make_arb(++i,aux);
            temp->group=true;
            i=aux+1;
            return temp;
        }
        throw "expresie incorecta";
    }
    char getOp(int& i)
    {
        if(expr[i]=='|' || expr[i]=='*')
            return expr[i++];
        if(isalpha(expr[i])||expr[i]=='(')
            return '.';
        throw "expresie incorecta";
    }
    Node* make_arb(int start, int end)
    {
        Node* arb=new Node();
        while(start<end)
        {
            if(arb->left)
            {
                Node* left=arb;
                arb=new Node();
                arb->left=left;
            }
            else
                arb->left=getToken(start);
            arb->op=getOp(start);
            if(arb->op!='*')
                arb->right=getToken(start);
            if(arb->op=='*' && !arb->left->group)
            {
                arb->op=arb->left->op;
                arb->right=new Node();
                arb->right->op='*';
                arb->right->left=arb->left->right;
                arb->left->right=NULL;
                Node* temp=arb->left;
                arb->left=temp->left;
                delete temp;
            }
        }
        return arb;
    }
    autom getAutom()
    {
        return getAutom(arb);
    }
    autom getAutom(Node* nod)
    {
        if(!nod->left)   return autom(nod->symb);
        if(nod->op=='*') return autom::star(getAutom(nod->left));
        if(nod->op=='|') return autom::paralel(getAutom(nod->left),getAutom(nod->right));
        if(nod->op=='.') return autom::concat(getAutom(nod->left),getAutom(nod->right));
    }
public:
    friend istream &operator>>(istream &in,RegEx &expr)
    {
        in>>expr.expr;
        expr.arb=expr.make_arb(0,strlen(expr.expr));
        return in;
    }
    friend ostream &operator<<(ostream &out,const RegEx &expr)
    {
        expr.arb->afis();
        cout<<"\n";
        return out;
    }
};
