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

        for(int i=0;i<m.games.size()*4/5;i++)
            m.learn(i);

        double right=0,total=0;
        outcome o;
        for(int i=m.games.size()*4/5;i<m.games.size();i++){
            o=m.predict(i);
            if(o==m.games.get(i).getOutcome())
                right++;
            total++;
        }

        int p=Math.round(Math.round(right/total*100));
        System.out.println("Precentage correct: "+p);

        m.logData(p);
    }

    public MachineLearning(){
        weights=new double[42];
        for(int c=0;c<weights.length;c++)
            weights[c]=1;
    }

    /*
     * Predicts the outcome then adjusts weights based on the actual outcome
     *
     * @param   i   The index of the game in the games array
     * @return      Returns true of the predicted outcome was correct
     */
    private boolean learn(int i){
        double score=0;
        status[] stats=this.games.get(i).getArr();
        outcome o,actual=this.games.get(i).getOutcome();

        o=this.predict(i);

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

    /*
     * Predicts the outcome of the specified game.
     *
     * @param   i   The index of the game in the games array
     * @return      The predicted outcome of the game
     */
    private outcome predict(int i){
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

        if(this.games.get(i).getOutcome()==outcome.DRAW)
            System.out.println(score);
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