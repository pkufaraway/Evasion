import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/*** Class that connects to a socket (5000 if the socket number is not defined
 * by the user), and reads the entire graph from the socket. You can get the
 * elements of the game from this class via getter methods. You can write your 
 * move to the server and the server will call the AI with the newest data
 * from the server.
 *
 * @author William Brantley
 *
 */
class GameController {
    private final Integer portNumber;
    private Socket gameSocket;
    private PrintWriter outputStream;
    private BufferedReader inputStream;
    private EvasionStatus gameStatus;

    GameController(Integer portNumber) throws IOException {
        this.portNumber = portNumber;
        gameStatus = new EvasionStatus();
        connectToSocket();
        writeToSocket("WA");
        listenForMoves();
    }

    private void connectToSocket() {
        try {
            gameSocket = new Socket(InetAddress.getLocalHost(), this.portNumber);
            outputStream = new PrintWriter(gameSocket.getOutputStream(), true);
            inputStream = new BufferedReader(
                    new InputStreamReader(gameSocket.getInputStream()));
        } catch (Exception notHandled) {
            notHandled.printStackTrace();
        }
    }

    private void listenForMoves() throws IOException {
        String incomingString;
        while (true) {
            incomingString = inputStream.readLine();
            System.out.println(incomingString);
            String[] splitString = incomingString.trim().split(" ");
            if(splitString.length > 5) {
                gameStatus.refreshStatus(splitString);
            }
            writeToSocket(gameStatus.naiveReturn());

        }
        //endGame();
    }

    private void writeToSocket(String moveToMake) {
        outputStream.write(moveToMake + "\n");
        outputStream.flush();
    }

    private void endGame() throws IOException {
        outputStream.close();
        inputStream.close();
        gameSocket.close();
    }

}
