/**
 * Created by WilliamRuppenthal on 1/11/16.
 */
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
    public List<Game> practice=new ArrayList<Game>();
    public List<Game> test=new ArrayList<Game>();

    private double[] weights;
    private double winCutOff;
    private double drawCutOff;


    public static void main(String[] args){
        MachineLearning m = new MachineLearning();

        m.readData("connect-4.data");

        int c=0,d=0;
        for(Game g:m.games){
            if(g.getOutcome() == outcome.LOSS)
                c++;
            d++;
        }

        /* Set up game arrays */
        m.practice=m.games.subList(0,m.games.size()*4/5);
        m.test=m.games.subList(m.games.size()*4/5,m.games.size());

        for(int i=0;i<100000;i++)
            m.learn(i,m.practice);

        double wr=0,wt=0,dr=0,dt=0,lr=0,lt=0;   //Wins right, wins total, etc.
        outcome o,actual;
        for(int i=0;i<m.test.size();i++){
            o=m.predict(i,m.test);
            actual=m.test.get(i).getOutcome();
            if(actual==outcome.WIN){
                if(o == actual)
                    wr++;
                wt++;
            }
            else if(actual==outcome.LOSS){
                if(o == actual)
                    lr++;
                lt++;
            }
            else{
                if(o==actual)
                    dr++;
                dt++;
            }
        }

        int p=Math.round(Math.round((wr+lr+dr)/(wt+lt+dt)*100));
        System.out.println("Percentage correct: "+p);
        System.out.println("Wins correct: "+wr/wt*100);
        System.out.println("Draws correct: "+dr/dt*100);
        System.out.println("Losses correct: "+lr/lt*100);

        System.out.println("Win cutoff: "+m.winCutOff);
        System.out.println("Draw cutoff: "+m.drawCutOff+"\n");

        System.out.println(Arrays.toString(m.weights));

        m.logData(p);
    }

    /*
     * Sets up arrays and such
     */
    public MachineLearning(){
        weights=new double[42];
        for(int c=0;c<weights.length;c++)
            weights[c]=0;

        winCutOff=0;
        drawCutOff=0;
    }

    /*
     * Predicts the outcome then adjusts weights based on the actual outcome
     *
     * @param   i   The index of the game in the games array
     * @return      Returns true of the predicted outcome was correct
     */
    private boolean learn(int i, List<Game> l){

        if(i>=l.size())
            i%=l.size();
        int actualI=i;

        status[] stats=l.get(i).getArr();
        outcome o,actual=l.get(i).getOutcome();

        o=this.predict(i,l);

        if(o!=actual){
            for(int c=0;c<this.weights.length;c++){
                if(actual==outcome.WIN)
                    if(stats[c]==status.X)
                        weights[c]+=(1/Math.pow(actualI+1,0.95));
                    else if(stats[c]==status.O)
                        weights[c]-=(1/Math.pow(actualI+1,0.95));
                else if(actual==outcome.LOSS)
                    if(stats[c]==status.X)
                        weights[c]-=(1/Math.pow(actualI+1,0.95));
                    else if(stats[c]==status.O)
                        weights[c]+=(1/Math.pow(actualI+1,0.95));
            }

            if(actual==outcome.WIN)
                this.winCutOff-=(1/Math.pow(actualI+1,0.95));
            else if(actual==outcome.LOSS)
                this.drawCutOff+=(1/Math.pow(actualI+1,0.95));
            else{
                this.winCutOff+=(1/Math.pow(actualI+1,0.95));
                this.drawCutOff-=(1/Math.pow(actualI+1,0.95));
            }
            if(this.winCutOff<this.drawCutOff)
                this.drawCutOff=this.winCutOff;

            return false;
        }
        return true;
    }

    /*
     * Predicts the outcome of the specified game.
     *
     * @param   i   The index of the game in the games array
     * @return      The predicted outcome of the game
     */
    private outcome predict(int i,List<Game> l){
        if(i>=l.size())
            i%=l.size();
        double score=0;
        status[] stats=l.get(i).getArr();
        outcome o;

        for(int c=0;c<weights.length;c++)
            if(stats[c]==status.X)
                score+=this.weights[c];
            else if(stats[c]==status.O)
                score-=this.weights[c];

        if(score>this.winCutOff)
            o=outcome.WIN;
        else if(score>this.drawCutOff)
            o=outcome.DRAW;
        else
            o=outcome.LOSS;

//        if(this.games.get(i).getOutcome()==outcome.DRAW)
//            System.out.println(score);
        return o;
    }

    /*
     * Reads in the data to the classes games array.
     *
     * @param   filename    The name of the data file
     */
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

    /*
     * Records a timestamp and the percentage correct from the test data.
     *
     * @param   p   The percentage correct
     */
    private void logData(int p){
        File f=new File("log.txt");
        String s=(new Timestamp(System.currentTimeMillis())).toString()
                +"\n"+p;
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