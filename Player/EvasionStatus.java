import java.awt.*;
import java.awt.geom.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EvasionStatus {
    int timeLeft;
    int gameNum;
    int tickNum;
    int maxWalls;
    int maxDelay;
    int lastWallTick;
    int N,M;
    Point2D hunter = new Point2D.Double(0, 0);
    Point2D prey = new Point2D.Double(0, 0);
    Point2D direction = new Point2D.Double(1,1);
    int numWalls;
    ArrayList<Wall> walls;
    int currentWallTimer;
    Rectangle2D huntingMap = new Rectangle2D.Double();


    public void refreshStatus(String[] status){
        //Refresh basic information
        timeLeft = Integer.valueOf(status[0]);
        gameNum = Integer.valueOf(status[1]);
        tickNum = Integer.valueOf(status[2]);
        maxWalls = Integer.valueOf(status[3]);
        maxDelay = Integer.valueOf(status[4]);
        N = Integer.valueOf(status[5]);
        M = Integer.valueOf(status[6]);
        if(huntingMap.isEmpty()){
            huntingMap = new Rectangle2D.Double(0,0,N,M);
        }
        currentWallTimer = Integer.valueOf(status[7]);

        //Refresh the data of hunter and prey
        int hunterX = Integer.valueOf(status[8]);
        int hunterY = Integer.valueOf(status[9]);
        int hunterVelX = Integer.valueOf(status[10]);
        int hunterVelY = Integer.valueOf(status[11]);
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
        //System.out.println("[Status]Refreshed");
    }

    private Rectangle2D addNewWall(Point2D hunter, Wall wall){
        double x = huntingMap.getX();
        double y = huntingMap.getY();
        double h = huntingMap.getHeight();
        double w = huntingMap.getWidth();
        Rectangle2D result;
        if (wall.wallType == 1){
            if (direction.getX() > 0){
                System.out.println("Wall Left");
                result = new Rectangle2D.Double(hunter.getX(), y, x + w - hunter.getX(), h);
            }
            else {
                System.out.println("Wall Right");
                result = new Rectangle2D.Double(x, y, hunter.getX() - x, h);
            }
        } else {
            if (direction.getY() < 0){
                System.out.println("Wall Below");
                result = new Rectangle2D.Double(x, y, w, y + hunter.getY());
            }
            else {
                System.out.println("Wall Above");
                result = new Rectangle2D.Double(x, -hunter.getY(), w, - hunter.getY() - (y - h));
            }
        }
        return result;
    }

    private int wallToAdd(){
        int result = -1;
        Rectangle2D huntingMapResult = huntingMap;
        if (numWalls < maxWalls
                && tickNum - lastWallTick >= maxDelay
                && (Math.abs(hunter.getX()-prey.getX()) < 2 * maxDelay + 2 || Math.abs(hunter.getY()-prey.getY()) < 2 * maxDelay + 2)){
            double maxSize = N * M;
            Rectangle2D newHuntingMapH;
            Rectangle2D newHuntingMapV;

            Wall newWall = new Wall(0, (int)hunter.getY(), 0 , 300);
            Point2D newHunter = new Point2D.Double(hunter.getX() + direction.getX(), hunter.getY() + direction.getY());
            if (!Helper.wallIntersect(newHunter, prey, newWall)){
                newHuntingMapH = addNewWall(hunter, newWall);
                if(newHuntingMapH.getWidth() * newHuntingMapH.getHeight() < maxSize){
                    maxSize = newHuntingMapH.getWidth() * newHuntingMapH.getHeight();
                    result = 0;
                    huntingMapResult = newHuntingMapH;
                }
            }

            newWall = new Wall(1, (int)hunter.getX(), 0 , 300);
            newHunter = new Point2D.Double(hunter.getX() + direction.getX(), hunter.getY() + direction.getY());
            if (!Helper.wallIntersect(newHunter, prey, newWall)){
                newHuntingMapV = addNewWall(hunter, newWall);
                if(newHuntingMapV.getWidth() * newHuntingMapV.getHeight() < maxSize){
                    huntingMapResult = newHuntingMapV;
                    result = 1;
                }
            }
        }
        if(result != -1){
            huntingMap = huntingMapResult;
            lastWallTick = tickNum;
        }
        return result;
    }

    private List<Integer> wallToDestroy(){
        ArrayList<Integer> answer = new ArrayList<>();
        for(int i = 0; i < walls.size(); i++){
            Wall wall = walls.get(i);
            if (wall.wallType == 0) {
                if ( -wall.position > huntingMap.getY() || -wall.position < huntingMap.getY() - huntingMap.getHeight()) {
                    numWalls--;
                    answer.add(i);
                }
            } else {
                if (wall.position < huntingMap.getX() || wall.position > huntingMap.getX() + huntingMap.getWidth()) {
                    numWalls--;
                    answer.add(i);
                }
            }
        }
        return answer;
    }

    public String hunterReturn(){
        List<Integer> wallToDestroy = wallToDestroy();
        int shouldAddWall = wallToAdd();

        StringBuilder buffer = new StringBuilder();
        buffer.append(gameNum);
        buffer.append(" ");
        buffer.append(tickNum);
        buffer.append(" ");
        buffer.append(shouldAddWall + 1);
        for(int index : wallToDestroy){
            buffer.append(" ");
            buffer.append(index);
        }
        buffer.append("\n");
        String answer = buffer.toString();
        //System.out.printf("Sent: %s \n", answer);
        return answer;
    }

    public String preyReturn(){
        StringBuilder buffer = new StringBuilder();
        Random random = new Random();
        int x = random.nextInt(3) - 1;
        int y = random.nextInt(3) - 1;
        buffer.append(gameNum);
        buffer.append(" ");
        buffer.append(tickNum);
        buffer.append(" ");
        buffer.append(x);
        buffer.append(" ");
        buffer.append(y);
        buffer.append("\n");
        return buffer.toString();
    }

}