import java.io.IOException;

public class GameRunner {

    public static void main(String[] args) throws IOException {
        GameController gameController = null;
        int portNumber = Integer.valueOf(args[0]);
        System.out.printf("Port number received, %d \n", portNumber);
        gameController = new GameController(portNumber);
    }

}
