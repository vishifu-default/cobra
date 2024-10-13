package org.cobra.networks.mocks;

import org.cobra.networks.auth.SecurityProtocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class EchoServer extends Thread {
    public static final int SO_TIMEOUT_MS = 30_000;

    public final int port;
    private final ServerSocket serverSocket;
    private final List<Thread> threads;
    private final List<Socket> sockets;
    private volatile boolean closing = false;
    private final AtomicBoolean renegotiated = new AtomicBoolean();

    public EchoServer(SecurityProtocol securityProtocol) throws IOException {
        if (securityProtocol == SecurityProtocol.PLAINTEXT) {
            this.serverSocket = new ServerSocket(0);
        } else {
            throw new IllegalArgumentException("Unsupported security protocol: " + securityProtocol);
        }
        this.port = serverSocket.getLocalPort();
        this.threads = new ArrayList<>();
        this.sockets = new ArrayList<>();
    }

    public void renegotiate() {
        this.renegotiated.set(true);
    }

    @Override
    public void run() {
        try {
            while (!this.closing) {
                final Socket socket = this.serverSocket.accept();
                synchronized (this.sockets) {
                    if (this.closing) {
                        socket.close();
                        break;
                    }

                    this.sockets.add(socket);
                    Thread thread = new Thread(() -> {
                        try {
                            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                            while (socket.isConnected() && !socket.isClosed()) {
                                int size = inputStream.readInt();
                                if (this.renegotiated.get()) {
                                    this.renegotiated.set(false);
                                }

                                byte[] bytes = new byte[size];
                                inputStream.readFully(bytes);
                                outputStream.writeInt(size);
                                outputStream.write(bytes);
                                outputStream.flush();
                            }
                        } catch (IOException ignored) {
                        } finally {
                            try {
                                socket.close();
                            } catch (IOException ignored) {

                            }
                        }
                    });
                    thread.start();
                    this.threads.add(thread);
                }
            }
        } catch (IOException ignored) {
        }
    }

    public void closeConnections() throws IOException {
        synchronized (this.sockets) {
            for (Socket socket : this.sockets) {
                socket.close();
            }
        }
    }

    public void close() throws InterruptedException, IOException {
        this.closing = true;
        this.serverSocket.close();
        closeConnections();
        for (Thread th : this.threads)
            th.join();
        join();
    }

    public String socketAddress() {
        return serverSocket.getLocalSocketAddress().toString();
    }
}
