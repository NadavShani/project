
public class testnumber {

    private int [][] mat;

    public boolean function(int [][] mat) {
        int col=0,current=0,value=0;

        for(int row=0;row<4;row++){
            col=0;
            for(current=row,value=mat[current][col];current<4;current++){
                if(mat[current][col++] != value)
                    return false;
            }
        }

        return true;
    }

    public static void main(String [] args){
        int [][] mat = new int[4][3];
        mat[0][0] = 1;
        mat[0][1] = 0;
        mat[0][2] = 0;
        mat[1][0] = 2;
        mat[1][1] = 1;
        mat[1][2] = 0;
        mat[2][0] = 3;
        mat[2][1] = 2;
        mat[2][2] = 1;
        mat[3][0] = 0;
        mat[3][1] = 3;
        mat[3][2] = 2;



    }
}