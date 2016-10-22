import java.io.IOException;

public class ASPRunner {

    public static void main(String[] args) throws IOException {
        GameController gameController = null;
        int portNumber = Integer.valueOf(args[0]);
        gameController = new GameController(portNumber);
        System.out.printf("Port number received, %d \n", portNumber);
    }

}
