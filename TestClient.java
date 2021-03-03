
import java.io.*;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.*;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;



class Speaker implements Runnable {
    


    Supplier<Socket> connector;

    PrintWriter out;
    BufferedReader in;
    Socket socket;


    Speaker(Supplier<Socket> connector) {
        this.connector = connector;
    }

    public void run() {
     //   Stream.generate(() -> null)
        //    .forEach(none -> tryDoSomeNetworking(this::doStuff));
        tryDoSomeNetworking(this::doStuff);
    }

    Random random = new Random();

    void doStuff() {
        try {
            var num = random.nextLong();
            System.out.println("sneding: " + num);
            out.println(String.valueOf(num));
           // out.flush();
            var input = in.readLine();
            
            System.out.println("got in answer: " + input.toString());
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    void updateConnection() {
        try {
            System.out.println("connecting\n");
            socket = connector.get();
            
            out = new PrintWriter(
                socket.getOutputStream(), true
            );
            
            in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()
            ));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch(Exception e) {

        }
    }
    void tryDoSomeNetworking(Runnable runnable) {
        while (true) {
            try {
                runnable.run();
                return;
            } catch(Exception e) {
                sleep(1000);
                e.printStackTrace();
                updateConnection();
            }
        }

    }
    void start() {
        updateConnection();
        new Thread(this).start();
    }
}

public class TestClient {
    
    Socket getConnection() {
        try {
            return new Socket("localhost", 31);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    void start() {
        IntStream.range(0, 200).forEach(one -> new Speaker(this::getConnection).start());
    }

    public static void main(String[] args) {
        new TestClient().start();
    }   
}