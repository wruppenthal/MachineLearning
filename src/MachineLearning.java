/**
 * Created by WilliamRuppenthal on 1/11/16.
 */
import java.util.*;
import java.io.*;

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


    public static void main(String[] args){
        MachineLearning m = new MachineLearning();

        m.readData("connect-4.data");

        int c=0,d=0;
        for(Game g:m.games){
            if(g.getOutcome() == outcome.LOSS)
                c++;
            d++;
        }
        System.out.println((double)c/(double)d);
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
}

