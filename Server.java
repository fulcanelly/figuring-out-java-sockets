import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import java.net.*;
import java.io.*;

public class Server {

    ExecutorService service = Executors.newCachedThreadPool();
    ServerSocket socket;

    Socket setUpSocket(Socket socket) {
        try {
            socket.setSoTimeout(1000);
            return socket;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    Socket acceptor() {
        try {

            var conn = socket.accept();
            System.out.println("got an connection");
            return setUpSocket(conn);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    void start() {
        try {
            socket = new ServerSocket(31);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        Stream.generate(this::acceptor)
            .map(RequestHadler::new)
            .peek(rh -> rh.setExecutorService(service))
            .forEach(service::execute);
    }

    public static void main(String[] args) {
        new Server().start();
    }   
} 

class CloseConnectionSignal extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
}

class RequestHadler implements Runnable {
    
    final Socket socket;
    ExecutorService service;

    PrintWriter out;
    BufferedReader in;
    boolean isRunning = true;

    void setExecutorService(ExecutorService service) {
        this.service = service;
    }

    public RequestHadler(Socket socket) {
        System.out.println("connection established");

        this.socket = socket;
    }  

    void schedule(Runnable runable) {
        if (isRunning) service.execute(runable);
    }

    void setUpIO() {
        try {
            out = new PrintWriter(
                socket.getOutputStream(), true
            );
            
            in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()
            ));
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    Long readIncomeNumber() throws Exception {
        var input = in.readLine();

        if (input == null) {
            isRunning = false;
            System.out.println("closing connection");
            throw new CloseConnectionSignal();
        }
        return Long.valueOf(
            input
        );

    }

    void sendAnswer(String answer) {
        try {
            out.println(answer);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    Long handleNumber(Long num) {
        return num + 1; 
    }

    
    void execute() {
        System.out.println("scheduling stuff");
        
        schedule(() -> {

            System.out.println("hi from another thread");
            
            
            try {
                var number = handleNumber(
                    readIncomeNumber()
                );
                System.out.println("read number " + number);
            
                schedule(() -> {
                    sendAnswer(
                        String.valueOf(number)
                    );
                    schedule(this::execute);
                });

            } catch(CloseConnectionSignal e) {
                return;
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
            
        });
    }

    public void run() {
        System.out.println("setuping connection hadlers");

        setUpIO();
        execute();

    }
}