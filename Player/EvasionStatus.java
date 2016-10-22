import java.util.ArrayList;
import java.util.InputMismatchException;

public class EvasionStatus {
    String timeLeft = "0";
    int gameNum;
    int tickNum;
    int maxWalls;
    int maxDelay;
    int N,M;
    int hunterX,hunterY;
    int preyX, preyY;
    int numWalls;
    ArrayList<Wall> walls;
    String currentWallTimer;

    public void refreshStatus(String[] status){
        timeLeft = status[0];
        gameNum = Integer.valueOf(status[1]);
        tickNum = Integer.valueOf(status[2]);
        maxWalls = Integer.valueOf(status[3]);
        maxDelay = Integer.valueOf(status[4]);
        N = Integer.valueOf(status[5]);
        M = Integer.valueOf(status[6]);
        currentWallTimer = status[7];
        hunterX = Integer.valueOf(status[8]);
        hunterY = Integer.valueOf(status[9]);
        preyX = Integer.valueOf(status[12]);
        preyY = Integer.valueOf(status[13]);
        numWalls = Integer.valueOf(status[14]);
        walls = new ArrayList<Wall>();
        for(int i = 0; i < numWalls; i++){
            int wallType = Integer.valueOf(status[14 + i * 4 + 1]);
            int position = Integer.valueOf(status[14 + i * 4 + 2]);
            int start = Integer.valueOf(status[14 + i * 4 + 3]);
            int end = Integer.valueOf(status[14 + i * 4 + 4]);
            walls.add(new Wall(wallType, position, start, end));
        }
        System.out.println("refreshed");
    }

    public String naiveReturn(){
        String answer = String.valueOf(gameNum) + " " + String.valueOf(tickNum) + " 0";
        System.out.printf("Sent: %s \n", answer);
        return answer;
    }
}
