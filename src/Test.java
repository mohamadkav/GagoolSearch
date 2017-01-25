/**
 * Created by mohammad on 1/25/17.
 */
public class Test {
    public static void main(String[] args) {
        String a="\u200C";
        for(char c:a.toCharArray()){
            System.out.println(a.indexOf(c)+"  "+Integer.toHexString((int)c)+"--"+c);
        }


    }
}
