package ro.pub.cs.systems.eim.lab06.pheasantgame.views;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Random;

import ro.pub.cs.systems.eim.lab06.pheasantgame.R;
import ro.pub.cs.systems.eim.lab06.pheasantgame.general.Constants;
import ro.pub.cs.systems.eim.lab06.pheasantgame.general.Utilities;

public class ServerFragment extends Fragment {

    private TextView serverHistoryTextView;

    private class CommunicationThread extends Thread {

        private Socket socket;
        private Random random = new Random();

        private String expectedWordPrefix = new String();

        public CommunicationThread(Socket socket) {
            if (socket != null) {
                this.socket = socket;
                Log.d(Constants.TAG, "[SERVER] Created communication thread with: " + socket.getInetAddress() + ":" + socket.getLocalPort());
            }
        }

        public void run() {
            if (socket == null) {
                return;
            }
            boolean isRunning = true;

            InputStream requestStream = null;
            OutputStream responseStream = null;
            try {
                requestStream = socket.getInputStream();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
            try {
                responseStream = socket.getOutputStream();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
            BufferedReader requestReader = new BufferedReader(new InputStreamReader(requestStream));
            PrintStream responsePrintWriter = new PrintStream(responseStream);

            while (isRunning) {

                // TODO: exercise 7a
                String request = new String();
                try {
                    Log.d(Constants.TAG, "[SERVER] Waiting to receive data with prefix \"" + expectedWordPrefix + "\" on socket " + socket);
                    request = requestReader.readLine();
                    final String finalizedRequest = request;
                    serverHistoryTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            serverHistoryTextView.setText("Server received word " + finalizedRequest + " from client\n" + serverHistoryTextView.getText().toString());
                        }
                    });
                    Log.d(Constants.TAG, "[SERVER] Received " + request + " on socket " + socket);
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
                try {
                    responseStream = socket.getOutputStream();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
//                PrintStream responsePrintWriter = new PrintStream(responseStream);
                if (Constants.END_GAME.equals(request)) {
                    Log.d(Constants.TAG, "[SERVER] Sent \"" + Constants.END_GAME + "\" on socket " + socket);
                    responsePrintWriter.println(Constants.END_GAME);
                    serverHistoryTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            serverHistoryTextView.setText("Communication ended!\n" + serverHistoryTextView.getText().toString());
                        }
                    });
                    isRunning = false;
                } else {
                    if ((Utilities.wordValidation(request)) && (request.length() > 2) && (expectedWordPrefix.isEmpty() || request.startsWith(expectedWordPrefix))) {
                        String wordPrefix = request.substring(request.length()-2, request.length());
                        List<String> wordList = Utilities.getWordListStartingWith(wordPrefix);

                        if (wordList.size() == 0) {
                            Log.d(Constants.TAG, "[SERVER] Sent \"" + Constants.END_GAME + "\" on socket " + socket);
                            responsePrintWriter.println(Constants.END_GAME);
                            serverHistoryTextView.post(new Runnable() {
                                @Override
                                public void run() {
                                    serverHistoryTextView.setText("Server sent word \"" + Constants.END_GAME + "\" to client because it was locked out\n" + serverHistoryTextView.getText().toString());
                                }
                            });
                            isRunning = false;
                        } else {
                            int wordIndex = random.nextInt(wordList.size());
                            final String word = wordList.get(wordIndex);
                            expectedWordPrefix = word.substring(word.length()-2, word.length());
                            Log.d(Constants.TAG, "[SERVER] Sent \"" + word + "\" on socket " + socket);
                            responsePrintWriter.println(wordList.get(wordIndex));
                            serverHistoryTextView.post(new Runnable() {
                                @Override
                                public void run() {
                                    serverHistoryTextView.setText("Server sent word " + word + " to client\n" + serverHistoryTextView.getText().toString());
                                }
                            });
                        }
                    } else {
                        Log.d(Constants.TAG, "[SERVER] Sent \"" + request + "\" on socket " + socket);
                        responsePrintWriter.println(request);
                        final String finalizedRequest = request;
                        serverHistoryTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                serverHistoryTextView.setText("Server sent back the word " + finalizedRequest + " to client as it is not valid!\n" + serverHistoryTextView.getText().toString());
                            }
                        });
                    }
                }
//                try {
//                    Log.d(Constants.TAG, "[SERVER] Waiting to receive data with prefix \"" + expectedWordPrefix + "\" on socket " + socket);
//                    String receivedWord = requestReader.readLine();
//                    int wordLen = receivedWord.length();
//                    final String finalizedRequest = receivedWord;
//                    serverHistoryTextView.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            serverHistoryTextView.setText("Server received word " + finalizedRequest + " from client\n" + serverHistoryTextView.getText().toString());
//                        }
//                    });
//                    Log.d(Constants.TAG, "[SERVER] Received " + receivedWord + " on socket " + socket);
//
//                    if (receivedWord.equals(Constants.END_GAME)) {
//                        Log.d(Constants.TAG, "[SERVER] Sent \"" + Constants.END_GAME + "\" on socket " + socket);
//                        responsePrintWriter.println(Constants.END_GAME);
//                        serverHistoryTextView.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                serverHistoryTextView.setText("Communication ended!\n" + serverHistoryTextView.getText().toString());
//                            }
//                        });
//                        isRunning = false;
//                    }
//                    else {
//                        boolean isValid = Utilities.wordValidation(receivedWord);
//
//                        if (isValid) {
//
//                            String wordSuffix = receivedWord.substring(wordLen - 2, wordLen);
//
//                            if (!expectedWordPrefix.equals("") && wordSuffix.equals(expectedWordPrefix)) {
//
//                                List<String> wordList = Utilities.getWordListStartingWith(wordSuffix);
//                                if (wordList.size() >= 1) {
//                                    int randIDx = random.nextInt(wordList.size());
//                                    String toSendWord = wordList.get(randIDx);
//                                    expectedWordPrefix = toSendWord.substring(0, 2);
//                                    responsePrintWriter.print(toSendWord);
//                                }
//                                else {
//                                    responsePrintWriter.print(Constants.END_GAME);
//                                }
//                            }
//                            else {
//                                Log.d(Constants.TAG, "[SERVER] Sent \"" + receivedWord + "\" on socket " + socket);
//                                responsePrintWriter.println(receivedWord);
//                                final String finalizedRequest = receivedWord;
//                                serverHistoryTextView.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        serverHistoryTextView.setText("Server sent back the word " + finalizedRequest + " to client as it is not valid!\n" + serverHistoryTextView.getText().toString());
//                                    }
//                                });
//                            }
//                        } else {
//                            Log.d(Constants.TAG, "[SERVER] Sent \"" + receivedWord + "\" on socket " + socket);
//                            responsePrintWriter.println(receivedWord);
//                            final String finalizedRequest = receivedWord;
//                            serverHistoryTextView.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    serverHistoryTextView.setText("Server sent back the word " + finalizedRequest + " to client as it is not valid!\n" + serverHistoryTextView.getText().toString());
//                                }
//                            });
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

            }
            try {
                socket.close();
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

        private ServerSocket serverSocket;

        private boolean isRunning;

        public ServerThread() {
            try {
                Log.d(Constants.TAG, "[SERVER] Created server thread, listening on port " + Constants.SERVER_PORT);
                serverSocket = new ServerSocket(Constants.SERVER_PORT);
                isRunning = true;
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "An exception has occurred:" + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
        }

        @Override
        public void run() {

            while(isRunning) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                    Log.d(Constants.TAG, "[SERVER] Incoming communication " + socket.getInetAddress() + ":" + socket.getLocalPort());
                } catch (SocketException socketException) {
                    Log.e(Constants.TAG, "An exception has occurred: "+ socketException.getMessage());
                    if (Constants.DEBUG) {
                        socketException.printStackTrace();
                    }
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "An exception has occurred:" + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }

                if (socket != null) {
                    CommunicationThread communicationThread = new CommunicationThread(socket);
                    communicationThread.start();
                }
            }

        }

        public void stopServer() {
            try {
                isRunning = false;
                if (serverSocket != null) {
                    serverSocket.close();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle state) {
        return inflater.inflate(R.layout.fragment_server, parent, false);
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);

        serverHistoryTextView = (TextView)getActivity().findViewById(R.id.server_history_text_view);

        serverThread = new ServerThread();
        serverThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serverThread != null) {
            serverThread.stopServer();
        }
    }

}

