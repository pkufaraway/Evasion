import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    int raceForVertWall = 0;
    int raceForHorizWall = 0;
    
    int leftWall = 0;
    int rightWall = 499;
    int topWall = 499;
    int bottomWall = 0;


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
    
    private boolean moveIsSafe(int x, int y) {
      return hunter.distance(prey.getX() + x, prey.getY() + y) > 4 && 
          Point2D.distance(hunter.getX() + direction.getX(),
              hunter.getY() + direction.getY(), prey.getX() + x, prey.getY() + y) > 4;
    }
    
    private boolean canMakePastPossibleWall(int x, int y) {
      int wallTimer = currentWallTimer;
      int steps = 0;
      while (wallTimer > 0) {
        steps++;
        //Conditions for safe step
        if (!(hunter.distance(prey.getX() + steps * x, prey.getY() + steps * y) > 4.0) ||
            !(Point2D.distance(hunter.getX() + (steps * direction.getX()),
              hunter.getY() + (steps * direction.getY()),
              prey.getX() + steps * x, prey.getY() + steps * y) > 4.0)) {
         return false; 
        }
        wallTimer--;
      }
      return true;
    }
    
    private void findWalls() {
      for (Wall wall : walls) {
        if (wall.wallType == 1) {
          leftWall = (wall.position > leftWall) ? wall.position : leftWall;
          rightWall = (wall.position < rightWall) ? wall.position : rightWall;
        } else {
          topWall = (wall.position < topWall) ? wall.position : topWall;
          bottomWall = (wall.position > bottomWall) ? wall.position : bottomWall;
        }
      }
    }
    
    private void searchForNewRun() {
      //Hunter is above prey, moving down, and we can get by
      if ((topWall - bottomWall / 2) > prey.getY() && 
          (topWall - bottomWall / 2) < hunter.getY() &&
          direction.getY() < 0 &&
          canMakePastPossibleWall(0, 1)) {
        raceForVertWall = currentWallTimer;
      //Hunter is below, moving up and we can get by
      } else if ((topWall - bottomWall / 2) < prey.getY() && 
          (topWall - bottomWall / 2) > hunter.getY() &&
          direction.getY() > 0 &&
          canMakePastPossibleWall(0, -1)) {
        raceForVertWall = -currentWallTimer;
      //Hunter is to the left and we can get by
      } else if ((rightWall - leftWall / 2) < prey.getX() && 
          (rightWall - leftWall / 2) > hunter.getX() &&
          direction.getX() > 0 &&
          canMakePastPossibleWall(-1, 0)) {
        raceForHorizWall = -currentWallTimer;
      //Hunter is to the right and we can get by
      } else if ((rightWall - leftWall / 2) < prey.getX() && 
          (rightWall - leftWall / 2) > hunter.getX() &&
          direction.getX() > 0 &&
          canMakePastPossibleWall(1, 0)) {
        raceForHorizWall = currentWallTimer;
      }
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
    
    /***
     * 0 = don't move
     * 1 = move up
     * 2 = move down
     * 3 = move left
     * 4 = move right
     * 
     * Just return the safest move which will maximize distance from the hunter
     * @return
     */
    private int findNormalMove() {
      ArrayList<Double> distances = new ArrayList<Double>();
      double distanceUp = hunter.distance(prey.getX(), prey.getY() + 1.0);
      double distanceDown = hunter.distance(prey.getX(), prey.getY() - 1.0);
      double distanceLeft = hunter.distance(prey.getX() - 1.0, prey.getY());
      double distanceRight = hunter.distance(prey.getX() + 1.0, prey.getY());
      
      distanceUp = moveIsSafe(0, 1) ? distanceUp : -Double.MAX_VALUE;
      distanceDown = moveIsSafe(0, -1) ? distanceDown : -Double.MAX_VALUE;
      distanceLeft = moveIsSafe(-1, 0) ? distanceLeft : -Double.MAX_VALUE;
      distanceRight = moveIsSafe(1, 0) ? distanceRight : -Double.MAX_VALUE;

      distances.add(-1.0);
      distances.add(distanceUp);
      distances.add(distanceDown);
      distances.add(distanceLeft);
      distances.add(distanceRight);
      
      Double max = Collections.max(distances);
      
      return distances.indexOf(max);
    }

    public String preyReturn(){
        int xMove = 0;
        int yMove = 0;
        findWalls();
        
        StringBuilder buffer = new StringBuilder();
        buffer.append(gameNum);
        buffer.append(" ");
        buffer.append(tickNum);
        buffer.append(" ");
        if (tickNum % 2 == 0) {
            if (raceForVertWall == 0 && raceForHorizWall == 0) {
                searchForNewRun();
            } else if (raceForVertWall != 0) {
                if (raceForVertWall < 0) {
                    yMove = -1;
                    System.out.println("Escaping up from wall");
                    raceForVertWall++;
                } else {
                    yMove = 1;
                    System.out.println("Escaping down from wall");
                    raceForVertWall--;
                }
            } else if (raceForHorizWall != 0) {
                if (raceForHorizWall < 0) {
                    xMove = -1;
                    System.out.println("Escaping left from wall");
                    raceForHorizWall++;
                } else {
                    xMove = 1;
                    System.out.println("Escaping right from wall");
                    raceForHorizWall--;
                }
            } else {
                //just move away from the hunter, but in the direction of more open space
              int normalMove = findNormalMove();
              switch(normalMove) {
                case 1: 
                  System.out.println("Normal move up");
                  yMove = 1;
                  break;
                case 2:
                  System.out.println("Normal move down");
                  yMove = -1;
                  break;
                case 3: 
                  System.out.println("Normal move left");

                  xMove = -1;
                  break;
                case 4: 
                  System.out.println("Normal move right");
                  xMove = 1;
                  break;
              }
            }        
        }
        
        buffer.append(xMove + " " + yMove);
        buffer.append("\n");
        System.out.println(buffer.toString());
        return buffer.toString();
    }

}