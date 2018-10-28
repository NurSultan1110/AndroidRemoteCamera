package com.project.coderneo.feature;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.icu.text.UnicodeSetSpanner;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main extends AppCompatActivity {

    private String currentModel;
    private String currentPort;

    private String[] isoValues;
    private String[] whiteBalanceValues;

    private List<Camera> listItems = new ArrayList<>();
    private ListAdapter customAdapter;
    private ArrayAdapter<String> spinnerAdapterISO;
    private ArrayAdapter<String> spinnerAdapterWhiteBalance;

    private static final int SOCKET_TIMEOUT = 10000;
    private static final String DEFAULT_SERVER_ADDRESS = "192.168.43.210";
    private static final String DEFAULT_SERVER_PORT = "8889";
    private static final String LOG_TAG = "LOG_TAG";

    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    private ListView lvCameras;

    private TextView txtModel;
    private TextView txtPort;
    private Spinner spinnerISO;
    private TextView txtRecordingStatus;
    private EditText inputShutterSpeed;
    private EditText inputFocal;
    private Spinner spinnerWhiteBalance;
    private EditText inputWhiteBalanceCalvin;
    private TextView txtMemoryStatus;

    private Button startAllCameras;
    private Button stopAllCameras;
    private Button captureAllCameras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        lvCameras = findViewById(R.id.lvCameras);

        startAllCameras = findViewById(R.id.startAllCameras);
        stopAllCameras = findViewById(R.id.stopAllCameras);
        captureAllCameras = findViewById(R.id.captureAllCameras);

        customAdapter = new ListAdapter(this, R.layout.itemlistrow, listItems);
        lvCameras.setAdapter(customAdapter);

        connection().show();
//        ChatOperator chatOperator = new ChatOperator();
//        chatOperator.execute(DEFAULT_SERVER_ADDRESS, DEFAULT_SERVER_PORT, "exit");

        lvCameras.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                isoValues = null;
                whiteBalanceValues = null;
                Toast.makeText(getApplicationContext(), listItems.get(position).getModel()
                        + listItems.get(position).getPort()
                        + listItems.get(position).getRecordingStatus(), Toast.LENGTH_SHORT).show();
                currentModel = listItems.get(position).getModel();
                currentPort = listItems.get(position).getPort();
                getIsoValues(listItems.get(position).getModel(), listItems.get(position).getPort());
//                getWhiteBalanceValues(listItems.get(position).getModel(), listItems.get(position).getPort());
//                getSettings(listItems.get(position).getModel(), listItems.get(position).getPort());
            }
        });

        startAllCameras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Camera> cameraList = new ArrayList<>();
                for(Camera camera: listItems){
                    if(camera.getRecordingStatus() != null){
                        if(camera.getRecordingStatus().equalsIgnoreCase("0")
                                && camera.getModel() != null
                                && camera.getPort() != null){
                            cameraList.add(camera);
                        }
                    }
                }
                if(!cameraList.isEmpty()){
                    startRecording(cameraList);
                }
            }
        });

        stopAllCameras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Camera> cameraList = new ArrayList<>();
                for(Camera camera: listItems){
                    if(camera.getRecordingStatus() != null){
                        if(camera.getRecordingStatus().equalsIgnoreCase("1")
                                && camera.getModel() != null
                                && camera.getPort() != null){
                            cameraList.add(camera);
                        }
                    }
                }
                if(!cameraList.isEmpty()){
                    stopRecording(cameraList);
                }
            }
        });

        captureAllCameras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Camera> cameraList = new ArrayList<>();
                for(Camera camera: listItems){
                    if(camera.getRecordingStatus() != null){
                        if(camera.getRecordingStatus().equalsIgnoreCase("0")
                                && camera.getModel() != null
                                && camera.getPort() != null){
                            cameraList.add(camera);
                        }
                    }
                }
                if(!cameraList.isEmpty()){
                    capture(cameraList);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.refresh) {
            refresh();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void refresh(){
        try{
            getAllCameras();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class Receiver extends AsyncTask<Void, Void, Void> {
        private String messageFromServer;
        @Override
        protected Void doInBackground(Void... params) {
            while (true) {
                try {
                    if (bufferedReader.ready()) {
                        messageFromServer = bufferedReader.readLine();
                        publishProgress(null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.d(LOG_TAG, messageFromServer);
            if (messageFromServer != null) {
                if (messageFromServer.length() > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String[] msg = messageFromServer.split("_");
                                if (!msg[1].equalsIgnoreCase("ok")) {
                                    showMessage(msg[2]).show();
                                } else {
                                    switch (msg[0]) {
                                        case "1": //Получить список всех камер и их статус по записи
                                            cameraList(msg[2]);
                                            break;
                                        case "2": //Получить список значении по ISO
                                            isoAndWhiteList(msg[2]);
                                            getWhiteBalanceValues(currentModel, currentPort);
                                            break;
                                        case "8": //Получить список значении по whiteBalance
                                            whiteBalanceList(msg[2]);
                                            getSettings(currentModel, currentPort);
                                            break;
                                        case "3": //Получить все настройки по одной камере
                                            settings(msg[2]);
                                            break;
                                        case "4":
                                        case "5":
                                        case "6":
                                        case "7":
                                            showMessage("Успешно!").show();
                                            break;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
    }

    private void settings(String message) {
        try {
            String[] settings = message.split(";");
            Camera camera = new Camera();
            for (String setting : settings) {
                String[] str = setting.split("&");
                switch (str[0]) {
                    case "iso":
                        try {
                            camera.setIso(str[1]);
                        } catch (Exception ignore) {
                        }
                        continue;
                    case "shutterspeed":
                        try {
                            camera.setShutterSpeed(str[1]);
                        } catch (Exception ignore) {
                        }
                        continue;
                    case "focal":
                        try {
                            camera.setFocal(str[1]);
                        } catch (Exception ignore) {
                        }
                        continue;
                    case "whitebalance":
                        try {
                            camera.setWhiteBalance(str[1]);
                        } catch (Exception ignore) {
                        }
                        continue;
                    case "whitebalancecelvin":
                        try {
                            camera.setWhiteBalanceCelvin(str[1]);
                        } catch (Exception ignore) {
                        }
                        continue;
                    case "recordingstatus":
                        try {
                            camera.setRecordingStatus(str[1]);
                        } catch (Exception ignore) {
                        }
                        continue;
                    case "memorystatus":
                        try {
                            camera.setMemoryStatus(str[1]);
                        } catch (Exception ignore) {
                        }
                        continue;
                    case "model":
                        try {
                            camera.setModel(str[1]);
                        } catch (Exception ignore) {
                        }
                        continue;
                    case "port":
                        try {
                            camera.setPort(str[1]);
                        } catch (Exception ignore) {
                        }
                        continue;
                    default:
                        try {
                            Log.e(LOG_TAG, str[0] + " | " + str[1]);
                        } catch (Exception ignore) {
                        }
                }
            }
            Log.d(LOG_TAG, camera.toString());
            if (camera.getModel() != null && camera.getPort() != null) {
                showCamera(camera).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Dialog showCamera(Camera camera) {
        try {
            LayoutInflater inflater = this.getLayoutInflater();
            View view = (View) LayoutInflater.from(getApplicationContext()).inflate(R.layout.connection, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(view);

            Log.d(LOG_TAG, Arrays.toString(isoValues));
            Log.d(LOG_TAG, Arrays.toString(whiteBalanceValues));

            txtModel = view.findViewById(R.id.txtModel);
            txtPort = view.findViewById(R.id.txtPort);
            spinnerISO = view.findViewById(R.id.spinnerISO);
            txtRecordingStatus = view.findViewById(R.id.txtRecordingStatus);
            inputShutterSpeed = view.findViewById(R.id.inputShutterSpeed);
            inputFocal = view.findViewById(R.id.inputFocal);
            spinnerWhiteBalance = view.findViewById(R.id.spinnerWhiteBalance);
            inputWhiteBalanceCalvin = view.findViewById(R.id.inputWhiteBalanceCalvin);
            txtMemoryStatus = view.findViewById(R.id.txtMemoryStatus);

            if (isoValues == null){
                isoValues = new String[]{"Test1", "Test2", "Test3"};
            }
//            isoValues = Arrays.asList("Automatic", "Daylight", "Fluorescent: Warm White", "Fluorescent: Daylight");
            spinnerAdapterISO = new ArrayAdapter<>(getApplicationContext(), R.layout.row, R.id.value, isoValues);
            spinnerISO.setAdapter(spinnerAdapterISO);

            if(whiteBalanceValues == null){
                whiteBalanceValues = new String[]{"Test4", "Test5", "Test6"};
            }
//            whiteBalanceValues = Arrays.asList("Automatic1", "Daylight1", "Fluorescent: Warm White1", "Fluorescent: Daylight1");
            spinnerAdapterWhiteBalance = new ArrayAdapter<>(getApplicationContext(), R.layout.row, R.id.value, whiteBalanceValues);
            spinnerWhiteBalance.setAdapter(spinnerAdapterWhiteBalance);

            try {
                spinnerISO.setSelection(((ArrayAdapter<String>) spinnerISO.getAdapter()).getPosition(camera.getIso()));
                spinnerWhiteBalance.setSelection(((ArrayAdapter<String>) spinnerWhiteBalance.getAdapter()).getPosition(camera.getWhiteBalance()));
            }catch (Exception e){
                e.printStackTrace();
            }
//            for (int i = 0; i < isoValues.length; i++) {
//                if (camera.getIso().equalsIgnoreCase(isoValues[i])) {
//                    spinnerISO.setSelection(i);
//                }
//            }
//            for (int i = 0; i < whiteBalanceValues.length; i++) {
//                if (camera.getWhiteBalance().equalsIgnoreCase(whiteBalanceValues[i])) {
//                    spinnerISO.setSelection(i);
//                }
//            }

            txtModel.setText(camera.getModel());
            txtPort.setText(camera.getPort());
            txtRecordingStatus.setText(camera.getRecordingStatus());
            inputShutterSpeed.setText(camera.getShutterSpeed());
            inputFocal.setText(camera.getFocal());
            inputWhiteBalanceCalvin.setText(camera.getWhiteBalanceCelvin());
            txtMemoryStatus.setText(camera.getMemoryStatus());

            builder.setMessage("Настройки камеры")
                    .setCancelable(true);
//                    .setNegativeButton("Закрыть",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    dialog.dismiss();
//                                }
//                            });
            if(camera.getRecordingStatus() != null) {
                if (camera.getRecordingStatus().equalsIgnoreCase("0")) {
                    builder.setNeutralButton("Запись",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    startRecording(new Camera(txtModel.getText().toString(), txtPort.getText().toString(), txtRecordingStatus.getText().toString()));
                                }
                            })
                            .setNegativeButton("Снимок",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            capture(new Camera(txtModel.getText().toString(), txtPort.getText().toString(), txtRecordingStatus.getText().toString()));
                                        }
                                    });
                } else {
                    builder.setNeutralButton("Остановить",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    stopRecording(new Camera(txtModel.getText().toString(), txtPort.getText().toString(), txtRecordingStatus.getText().toString()));
                                }
                            });
                }
            }else {
                builder.setNeutralButton("Запись",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startRecording(new Camera(txtModel.getText().toString(), txtPort.getText().toString(), txtRecordingStatus.getText().toString()));
                            }
                        })
                        .setNegativeButton("Снимок",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        capture(new Camera(txtModel.getText().toString(), txtPort.getText().toString(), txtRecordingStatus.getText().toString()));
                                    }
                                });
            }
            builder.setPositiveButton("Сохранить",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Camera cam = new Camera();
                            cam.setModel(txtModel.getText().toString());
                            cam.setPort(txtPort.getText().toString());
                            cam.setIso(spinnerISO.getSelectedItem().toString());
                            cam.setRecordingStatus(txtRecordingStatus.getText().toString());
                            cam.setShutterSpeed(inputShutterSpeed.getText().toString());
                            cam.setFocal(inputFocal.getText().toString());
                            cam.setWhiteBalance(spinnerWhiteBalance.getSelectedItem().toString());
                            cam.setWhiteBalanceCelvin(inputWhiteBalanceCalvin.getText().toString());
                            cam.setMemoryStatus(txtMemoryStatus.getText().toString());
                            setSettings(cam);
                            dialog.dismiss();
                        }
                    });
            return builder.create();
        } catch (Exception e) {
            e.printStackTrace();
            return showMessage(e.getMessage());
        }
    }

    private void whiteBalanceList(String message) {
        Log.d(LOG_TAG + "QQQ2", message);
        try {
            whiteBalanceValues = message.split("&");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void isoAndWhiteList(String message) {
        Log.d(LOG_TAG + "QQQ1", message);
        try {
            isoValues = message.split("&");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cameraList(String message) {
        try {
            listItems.clear();
            String[] modelsAndPorts = message.split(";");
            for (String str : modelsAndPorts) {
                String[] modelAndPort = str.split("&");
                listItems.add(new Camera(modelAndPort[0], modelAndPort[1], modelAndPort[2]));
            }
            customAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Получить список всех камер и их статус
    private void getAllCameras() {
        new Sender("get_all_cameras" + "\r").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //Получить список значении по ISO
    private void getIsoValues(String model, String port) {
        new Sender("getisovalues_" + model + "&" + port + "\r").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //Получить список значении по whiteBalance
    private void getWhiteBalanceValues(String model, String port) {
        new Sender("getwhitebalancevalues_" + model + "&" + port + "\r").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //Получить все настройки по одной камере
    private void getSettings(String model, String port) {
        new Sender("getsettings_" + model + "&" + port + "\r").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //Изменить настройки по одной камере
    private void setSettings(Camera camera) {
        new Sender("set_" +
                camera.getModel() + "&" + camera.getPort() + "_" +
                "iso&" + camera.getIso() + "_" +
                "shutterspeed&" + camera.getShutterSpeed() + "_" +
                "focal&" + camera.getFocal() + "_" +
                "whitebalance&" + camera.getWhiteBalance() + "_" +
                "whitebalancecelvin&" + camera.getWhiteBalanceCelvin() + "\r").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //Начать запись для камер, у которых не идет запись
    private void startRecording(List<Camera> cameraList) {
        String msg = "";
        for (Camera camera : cameraList) {
            if (camera.getRecordingStatus() == null) continue;
            if (camera.getRecordingStatus().equals("0")) {
                msg += "_" + camera.getModel() + "&" + camera.getPort();
            }
        }
        Log.d(LOG_TAG + "start", msg);
        if (!msg.isEmpty()) {
            new Sender("startrecording" + msg + "\r").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    //Начать запись для камер, у которых не идет запись
    private void startRecording(Camera camera) {
        new Sender("startrecording" + "_" + camera.getModel() + "&" + camera.getPort() + "\r").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //Остановить запись для камер, у которых идет запись
    private void stopRecording(List<Camera> cameraList) {
        String msg = "";
        for (Camera camera : cameraList) {
            if (camera.getRecordingStatus() == null) continue;
            if (camera.getRecordingStatus().equals("1")) {
                msg += "_" + camera.getModel() + "&" + camera.getPort();
            }
        }
        Log.d(LOG_TAG + "stop:", msg);
        if (!msg.isEmpty()) {
            new Sender("stoprecording" + msg + "\r").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    //Остановить запись для камер, у которых идет запись
    private void stopRecording(Camera camera) {
        new Sender("stoprecording" + "_" + camera.getModel() + "&" + camera.getPort() + "\r").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //Сделать снимок для камер, у которых не идет запись
    private void capture(List<Camera> cameraList) {
        String msg = "";
        for (Camera camera : cameraList) {
            if (camera.getRecordingStatus() == null) continue;
            if (camera.getRecordingStatus().equals("0")) {
                msg += "_" + camera.getModel() + "&" + camera.getPort();
            }
        }
        if (!msg.isEmpty()) {
            new Sender("capture" + msg + "\r").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    //Сделать снимок для камер, у которых не идет запись
    private void capture(Camera camera) {
        new Sender("capture" + "_" + camera.getModel() + "&" + camera.getPort() + "\r").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private Dialog connection() {
        try {
            final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(16, 16, 16, 16);

            TextInputLayout txtInputAddress = new TextInputLayout(this);
            txtInputAddress.setLayoutParams(params);
            final EditText inputAddress = new EditText(this);
            inputAddress.setText(DEFAULT_SERVER_ADDRESS);
            inputAddress.setLayoutParams(params);
            inputAddress.setHint(R.string.address);
            txtInputAddress.addView(inputAddress);
            layout.addView(txtInputAddress);

            TextInputLayout txtInputPort = new TextInputLayout(this);
            txtInputPort.setLayoutParams(params);
            final EditText inputPort = new EditText(this);
            inputPort.setText(DEFAULT_SERVER_PORT);
            inputPort.setLayoutParams(params);
            inputPort.setHint(R.string.port);
            txtInputPort.addView(inputPort);
            layout.addView(txtInputPort);

//            LayoutInflater inflater = this.getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setView(inflater.inflate(R.layout.connection, null));
            builder.setView(layout);
            builder.setMessage("Подключение к серверу")
                    .setCancelable(false)
                    .setNegativeButton("Выход",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    System.exit(0);
                                }
                            })
                    .setPositiveButton("Подключиться",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (!inputAddress.getText().toString().trim().isEmpty()
                                            && !inputPort.getText().toString().trim().isEmpty()) {
                                        new ChatOperator()
                                                .execute(inputAddress.getText().toString().trim(),
                                                        inputPort.getText().toString().trim(), "exit");
                                    }
                                }
                            });
            return builder.create();
        } catch (Exception e) {
            e.printStackTrace();
            return showMessage(e.getMessage());
        }
    }

    private Dialog showMessage(String message) {
        try {
            if (message != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Важное сообщение!")
                        .setMessage(message)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setCancelable(false)
                        .setNegativeButton("ОК",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                return builder.create();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("StaticFieldLeak")
    private class ChatOperator extends AsyncTask<String, StringBuilder, String> {

        private boolean isCorrect = true;
        private String errorMessage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            StringBuilder sb = new StringBuilder();
            try {
                if (socket == null) {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(params[0], Integer.parseInt(params[1])), SOCKET_TIMEOUT);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Server connected", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (socket.isConnected()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Server just connected", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(params[0], Integer.parseInt(params[1])), SOCKET_TIMEOUT);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Server connected", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                printWriter = new PrintWriter(socket.getOutputStream(), true);
                InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                bufferedReader = new BufferedReader(inputStreamReader);
            } catch (IOException e) {
                errorMessage = e.getMessage();
                return "error";
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            if (errorMessage != null) {
                Objects.requireNonNull(showMessage(errorMessage)).show();
            } else if (isCorrect) {
//                Toast.makeText(getApplicationContext(), "Server connected", Toast.LENGTH_SHORT).show();
                new Receiver().execute();
                getAllCameras();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class Sender extends AsyncTask<Void, Void, Void> {

        private String message;

        Sender(String message) {
            this.message = message;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (message != null) {
                printWriter.write(message.trim() + "\n");
                printWriter.flush();
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        System.exit(0);
    }

}
