/**
 * Created by WilliamRuppenthal on 1/12/16.
 */
public class Game{
        private outcome o;
        private status[] a;
        public Game(status[] arr,outcome out){
            o=out;
            a=arr;
        }

        public status[] getArr(){
            return a;
        }

        public outcome getOutcome(){
            return o;
        }

        public String toString(){
            String s="";
            for(status c:a)
                s+=c+", ";
            s+=o;
            return s;
        }
    }
