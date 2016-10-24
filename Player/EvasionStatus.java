import java.awt.geom.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class EvasionStatus {
    int timeLeft;
    int gameNum;
    int tickNum;
    int maxWalls;
    int maxDelay;
    int N,M;
    Point2D hunter = new Point2D.Double(0, 0);
    Point2D prey = new Point2D.Double(0, 0);
    Point2D direction = new Point2D.Double(1,1);
    int numWalls;
    ArrayList<Wall> walls;
    int currentWallTimer;

    private List<Wall> wallToDestroy(){
        ArrayList<Wall> answer = new ArrayList<>();
        for(Wall wall : walls){
            if(Helper.wallIntersect(hunter, prey, wall)){
                answer.add(wall);
            }
        }
        return answer;
    }


    public void refreshStatus(String[] status){
        //Refresh basic information
        timeLeft = Integer.valueOf(status[0]);
        gameNum = Integer.valueOf(status[1]);
        tickNum = Integer.valueOf(status[2]);
        maxWalls = Integer.valueOf(status[3]);
        maxDelay = Integer.valueOf(status[4]);
        N = Integer.valueOf(status[5]);
        M = Integer.valueOf(status[6]);
        currentWallTimer = Integer.valueOf(status[7]);

        //Refresh the data of hunter and prey
        int hunterX = Integer.valueOf(status[8]);
        int hunterY = Integer.valueOf(status[9]);
        int hunterVelX = Integer.valueOf(status[10]);
        int hunterVelY = Integer.valueOf(status[10]);
        int preyX = Integer.valueOf(status[12]);
        int preyY = Integer.valueOf(status[13]);
        direction.setLocation(hunterVelX, hunterVelY);
        hunter.setLocation(hunterX,hunterY);
        prey.setLocation(preyX,preyY);

        //Refresh the walls information
        numWalls = Integer.valueOf(status[14]);
        walls = new ArrayList<>();
        for(int i = 0; i < numWalls; i++){
            int wallType = Integer.valueOf(status[14 + i * 4 + 1]);
            int position = Integer.valueOf(status[14 + i * 4 + 2]);
            int start = Integer.valueOf(status[14 + i * 4 + 3]);
            int end = Integer.valueOf(status[14 + i * 4 + 4]);
            walls.add(new Wall(wallType, position, start, end));
        }
        System.out.println("[Status]Refreshed");
    }

    public String naiveReturn(){
        String answer = String.valueOf(gameNum) + " " + String.valueOf(tickNum) + " 1 0";
        System.out.printf("Sent: %s \n", answer);
        return answer;
    }

}