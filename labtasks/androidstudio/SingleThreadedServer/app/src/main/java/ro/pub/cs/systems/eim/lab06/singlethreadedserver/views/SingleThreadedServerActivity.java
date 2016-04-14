package ro.pub.cs.systems.eim.lab06.singlethreadedserver.views;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import ro.pub.cs.systems.eim.lab06.singlethreadedserver.R;
import ro.pub.cs.systems.eim.lab06.singlethreadedserver.general.Constants;
import ro.pub.cs.systems.eim.lab06.singlethreadedserver.general.Utilities;

public class SingleThreadedServerActivity extends AppCompatActivity {

    private EditText serverTextEditText;

    private ServerTextContentWatcher serverTextContentWatcher = new ServerTextContentWatcher();
    private class ServerTextContentWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            Log.v(Constants.TAG, "Text changed in edit text: " + charSequence.toString());
            if (Constants.SERVER_START.equals(charSequence.toString())) {
                serverThread = new ServerThread();
                serverThread.startServer();
                Log.v(Constants.TAG, "Starting server...");
            }
            if (Constants.SERVER_STOP.equals(charSequence.toString())) {
                serverThread.stopServer();
                Log.v(Constants.TAG, "Stopping server...");
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }

    }

    private class CommunicationThread extends Thread {
        private Socket socket;

        public CommunicationThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException interruptedException) {
                Log.e(Constants.TAG, interruptedException.getMessage());
                if (Constants.DEBUG) {
                    interruptedException.printStackTrace();
                }
            }
            try {
                Log.v(Constants.TAG, "Connection opened with " + socket.getInetAddress() + ":" + socket.getLocalPort());
                PrintWriter printWriter = Utilities.getWriter(socket);
                printWriter.println(serverTextEditText.getText().toString());
                socket.close();
                Log.v(Constants.TAG, "Connection closed");
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    private ServerThread serverThread;
    private class ServerThread extends Thread {

        private boolean isRunning;

        private ServerSocket serverSocket;

        public void startServer() {
            isRunning = true;
            start();
            Log.v(Constants.TAG, "startServer() method invoked " + serverSocket);
        }

        public void stopServer() {
            isRunning = false;
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
            Log.v(Constants.TAG, "stopServer() method invoked ");
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(Constants.SERVER_PORT);
                while (isRunning) {
                    Socket socket = serverSocket.accept();
                    CommunicationThread communicationThread = new CommunicationThread(socket);
                    communicationThread.start();
//                    try {
//                        Thread.sleep(3000);
//                    } catch (InterruptedException interruptedException) {
//                        Log.e(Constants.TAG, interruptedException.getMessage());
//                        if (Constants.DEBUG) {
//                            interruptedException.printStackTrace();
//                        }
//                    }
//                    Log.v(Constants.TAG, "Connection opened with " + socket.getInetAddress() + ":" + socket.getLocalPort());
//                    PrintWriter printWriter = Utilities.getWriter(socket);
//                    printWriter.println(serverTextEditText.getText().toString());
//                    socket.close();
//                    Log.v(Constants.TAG, "Connection closed");
                }
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_threaded_server);

        serverTextEditText = (EditText)findViewById(R.id.server_text_edit_text);
        serverTextEditText.addTextChangedListener(serverTextContentWatcher);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serverThread != null) {
            serverThread.stopServer();
        }
    }

}
