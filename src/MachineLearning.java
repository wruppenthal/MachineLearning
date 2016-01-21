/**
 * Created by WilliamRuppenthal on 1/11/16.
 */
import java.nio.Buffer;
import java.util.*;
import java.io.*;
import java.sql.Timestamp;

enum outcome{
    WIN,DRAW,LOSS
}

enum status{
    X,B,O
}

/*
 * Stats
 *
 * Draw %: 9.5
 * WIN %: 66
 * LOSS %: 25
 */

public class MachineLearning{
    public List<Game> games=new ArrayList<Game>();
    private double[] weights;


    public static void main(String[] args){
        MachineLearning m = new MachineLearning();

        m.readData("connect-4.data");

        int c=0,d=0;
        for(Game g:m.games){
            if(g.getOutcome() == outcome.LOSS)
                c++;
            d++;
        }

        boolean[] results=new boolean[200];
        for(int i=0;i<1000;i++)
            if(i<100)
                results[i]=m.learn(i);
            else if(i>899)
                results[i-800]=m.learn(i);
            else
                m.learn(i);

        int earlyp=0,latep=0;    //early and late percentage correct, respectively
        int right=0;    //Number of games right
        for(int i=0;i<100;i++)
            if(results[i])
                right++;
        earlyp=right;

        right=0;
        for(int i=100;i<200;i++)
            if(results[i])
                right++;
        latep=right;

        System.out.println("Percentage right in first 100: "+earlyp);
        System.out.println("Percentage right in last 100: "+latep);

        m.logData(earlyp,latep);
    }

    public MachineLearning(){
        weights=new double[42];
        for(int c=0;c<weights.length;c++)
            weights[c]=1;
    }

    private boolean learn(int i){
        double score=0;
        status[] stats=this.games.get(i).getArr();
        outcome o,actual=this.games.get(i).getOutcome();

        for(int c=0;c<weights.length;c++)
            if(stats[c]==status.X)
                score+=this.weights[c];
            else if(stats[c]==status.O)
                score-=this.weights[c];

        if(score>.3)
            o=outcome.WIN;
        else if(score>-.3)
            o=outcome.DRAW;
        else
            o=outcome.LOSS;

        if(o!=actual){
            for(int c=0;c<this.weights.length;c++){
                if(actual==outcome.WIN)
                    if(stats[c]==status.X)
                        weights[c]+=1/Math.sqrt(i);
                    else if(stats[c]==status.O)
                        weights[c]-=1/Math.sqrt(i);
                else if(actual==outcome.LOSS)
                    if(stats[c]==status.X)
                        weights[c]-=1/Math.sqrt(i);
                    else if(stats[c]==status.O)
                        weights[c]+=1/Math.sqrt(i);
            }

            return false;
        }
        return true;
    }

    private void readData(String filename){
        try{
            final Scanner in=new Scanner(new FileReader(filename));

            while(in.hasNext()){
                final String[] columns=in.next().split(",");
                status[] a=new status[columns.length-1];
                for (int c=0;c<a.length;c++)
                    if(columns[c].equals("b"))
                        a[c]=status.B;
                    else if(columns[c].equals("x"))
                        a[c]=status.X;
                    else
                        a[c]=status.O;

                outcome o;
                if(columns[columns.length-1].equals("win"))
                    o=outcome.WIN;
                else if(columns[columns.length-1].equals("draw"))
                    o=outcome.DRAW;
                else
                    o=outcome.LOSS;

                games.add(new Game(a,o));
            }
        }catch(FileNotFoundException e){}
    }

    private void logData(int earlyp,int latep){
        File f=new File("log.txt");
        String s=(new Timestamp(System.currentTimeMillis())).toString()
                +"\n"+earlyp+"\n"+latep;
        try{
            if(!f.exists())
                f.createNewFile();

            FileWriter fw=new FileWriter(f,true);
            BufferedWriter bw=new BufferedWriter(fw);
            PrintWriter out=new PrintWriter(bw);

            out.println(s);

            out.close();
        }catch(Exception e){
            System.out.println("Failed to log");
        }
    }
}