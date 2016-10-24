package edu.nyu.cs.hps.evasion.game;

import java.awt.*;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class GameHost {

  public static void hostGame(int portP1, int portP2, int maxWalls, int wallPlacementDelay, String displayHost, int displayPort) throws Exception {

    System.out.println("Player 1: connect to port " + portP1);
    System.out.println("Player 2: connect to port " + portP2);
    if (displayHost != null) {
      System.out.println("Accept display socket connection on " + displayHost + ":" + displayPort);
    }

    IO io = new IO();
    List<Integer> ports = new ArrayList<>();
    ports.add(portP1);
    ports.add(portP2);
    io.start(ports);

    Socket displaySocket = null;
    PrintWriter displayWriter = null;
    if (displayHost != null) {
      while (displayWriter == null) {
        try {
          displaySocket = new Socket(displayHost, displayPort);
          displayWriter = new PrintWriter(displaySocket.getOutputStream(), true);
        } catch (Exception e) {
          System.out.println("Display output error: " + e.getMessage());
          Thread.sleep(1000);
      }
    }
  }

    System.out.println("Starting game.");
    if(displayWriter != null) {
      displayWriter.println("begin");
    }

    int hunterIndex = 0;
    int preyIndex = 1;
    int gameNum = 0;

    int p1AsPreyScore = 0;
    int p2AsPreyScore = 0;

    int p1Timeouts = 0;
    int p2Timeouts = 0;

    while(gameNum < 2) {
      Duration hunterTime = Duration.ofSeconds(120);
      Duration preyTime = Duration.ofSeconds(120);

      Game game = new Game(maxWalls, wallPlacementDelay);

      io.sendLine(hunterIndex, "hunter");
      io.sendLine(preyIndex, "prey");

      if(displayWriter != null){
        displayWriter.println("hunter: " + io.getName(hunterIndex));
        displayWriter.println("prey: " + io.getName(preyIndex));
      }

      Thread.sleep(1000 / 60);

      boolean hunterTimeout = false;
      boolean preyTimeout = false;
      boolean done = false;
      while (!done) {
        String gameString = gameNum + " " + game.getState().toString();

        if(displayWriter != null) {
          displayWriter.println(hunterTime.toMillis() + " " + preyTime.toMillis() + " " + gameString);
        }

        IO.Response hunterResponse = null;
        IO.Response preyResponse = null;

        try {
          Future<IO.Response> hunterResponseFuture = io.getResponse(hunterIndex, hunterTime.toMillis() + " " + gameString);
          hunterResponse = hunterResponseFuture.get(hunterTime.toNanos(), TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
          hunterTimeout = true;
        }

        try {
          Future<IO.Response> preyResponseFuture = io.getResponse(preyIndex, preyTime.toMillis() + " " + gameString);
          preyResponse = preyResponseFuture.get(preyTime.toNanos(), TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
          preyTimeout = true;
        }

        if(hunterTimeout || preyTimeout) {
          break;
        }

        hunterTime = hunterTime.minus(hunterResponse.elapsed);
        preyTime = preyTime.minus(preyResponse.elapsed);

        hunterTimeout = hunterTime.isNegative();
        preyTimeout = preyTime.isNegative();

        if(hunterTimeout || preyTimeout){
          break;
        }

        Game.WallCreationType hunterWallAction = Game.WallCreationType.NONE;
        List<Integer> hunterWallsToDelete = new ArrayList<>();
        Point preyMovement = new Point(0, 0);

        List<Integer> hunterData = Arrays.stream(hunterResponse.message.split("\\s+"))
          .map(Integer::parseInt)
          .collect(Collectors.toList());

        if(hunterData.get(1) == game.getState().ticknum) {
          if (hunterData.size() >= 3 && hunterData.get(0) == gameNum) {
            if (hunterData.get(2) == 1) {
              hunterWallAction = Game.WallCreationType.HORIZONTAL;
            } else if (hunterData.get(2) == 2) {
              hunterWallAction = Game.WallCreationType.VERTICAL;
            }
            hunterWallsToDelete = hunterData.subList(3, hunterData.size());
          }
        } else {
          System.out.println(io.getName(hunterIndex) + " is lagging; missed tick " + game.getState().ticknum);
        }

        List<Integer> preyData = Arrays.stream(preyResponse.message.split("\\s+"))
          .map(Integer::parseInt)
          .collect(Collectors.toList());
        if(preyData.get(1) == game.getState().ticknum) {
          if (preyData.size() >= 4 && preyData.get(0) == gameNum && preyData.get(1) == game.getState().ticknum) {
            preyMovement.x = preyData.get(2);
            preyMovement.y = preyData.get(3);
          }
        } else {
          System.out.println(io.getName(preyIndex) + " is lagging; missed tick " + game.getState().ticknum);
        }

        done = game.tick(hunterWallAction, hunterWallsToDelete, preyMovement);
      }

      if(preyIndex == 0){
        if(preyTimeout){
          p1Timeouts++;
        } else {
          p1AsPreyScore += game.getState().ticknum;
        }
        if(hunterTimeout){
          p2Timeouts++;
        }
      } else {
        if(preyTimeout){
          p2Timeouts++;
        } else {
          p2AsPreyScore += game.getState().ticknum;
        }
        if(hunterTimeout){
          p1Timeouts++;
        }
      }

      String result;
      if(hunterTimeout && preyTimeout){
        result = "Both timed out!";
      } else if(hunterTimeout){
        result = io.getName(hunterIndex) + " timed out!";
      } else if (preyTimeout){
        result = io.getName(preyIndex) + " timed out!";
      } else {
        result = "Score (hunter=" + io.getName(hunterIndex) + ", prey=" + io.getName(preyIndex) + "): " + game.getState().ticknum;
      }
      System.out.println(result);
      if(displayWriter != null) {
        displayWriter.println("result: " + result);
      }

      hunterIndex = 1-hunterIndex;
      preyIndex = 1-preyIndex;
      gameNum++;

      Thread.sleep(1000 / 60);
    }

    String finalResult = "Undetermined final result due to timeouts.";
    if(p1Timeouts == 0 && p2Timeouts == 0) {
      if (p1AsPreyScore == p2AsPreyScore) {
        finalResult = "Tied! Both = " + p1AsPreyScore;
      } else {
        String winner = (p1AsPreyScore > p2AsPreyScore) ? io.getName(0) : io.getName(1);
        finalResult = winner + " wins (" + io.getName(0) + " = " + p1AsPreyScore + ", " + io.getName(1) + " = " + p2AsPreyScore + ")";
      }
    }
    System.out.println(finalResult);
    if(displayWriter != null) {
      displayWriter.println("finalresult: " + finalResult);
    }

    io.sendLine(hunterIndex, "done");
    io.sendLine(preyIndex, "done");

    io.destroy();
    if(displayWriter != null) {
      displayWriter.println("done");
      displayWriter.close();
      displaySocket.close();
    }
  }
}
