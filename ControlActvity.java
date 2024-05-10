package com.example.borewell;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.UUID;

public class ControlActvity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private TextToSpeech textToSpeech;
    private boolean isSpeaking = false;
    TextView mac;
    private boolean isReadingData = false;
    private TextView humidityValue;
    private TextView temperatureValue;
    private TextView distanceValue;

    private static final String DEVICE_ADDRESS = "00:22:12:01:8A:61"; // Replace with your device's MAC address
    private static final UUID UUID_SERIAL_PORT_SERVICE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_actvity);

        // Initialize views
        CardView button1 = findViewById(R.id.button1);
        CardView button2 = findViewById(R.id.button2);
        CardView button3 = findViewById(R.id.button3);
        CardView button4 = findViewById(R.id.button4);
        CardView button5 = findViewById(R.id.button5);
        mac=findViewById(R.id.mac);

        humidityValue = findViewById(R.id.humidityValue);
        temperatureValue = findViewById(R.id.temperatureValue);
        distanceValue = findViewById(R.id.distanceValue);

        mac.setText(DEVICE_ADDRESS);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        connectBluetooth();

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, this);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                    speak("Moving up");
                    sendData("u");
                    startReadingData();
                    Toast.makeText(ControlActvity.this, "Moving up", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ControlActvity.this, "Bluetooth connection lost", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                    speak("Moving down");
                    sendData("d");
                    startReadingData();
                    Toast.makeText(ControlActvity.this, "Moving down", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ControlActvity.this, "Bluetooth connection lost", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                    speak("Holding the object");
                    sendData("c");
                    startReadingData();
                    Toast.makeText(ControlActvity.this, "Holding the object", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ControlActvity.this, "Bluetooth connection lost", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                    speak("Releasing the object");
                    sendData("o");
                    startReadingData();
                    Toast.makeText(ControlActvity.this, "Releasing the object", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ControlActvity.this, "Bluetooth connection lost", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                    speak("Stop");
                    sendData("s");
                    stopSpeaking();
                    stopReadingData();
                    Toast.makeText(ControlActvity.this, "Stop", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ControlActvity.this, "Bluetooth connection lost", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void connectBluetooth() {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID_SERIAL_PORT_SERVICE);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            Toast.makeText(this, "Connected to Bluetooth device", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Bluetooth", "Failed to connect to Bluetooth device: " + e.getMessage());
            Toast.makeText(this, "Failed to connect to Bluetooth device", Toast.LENGTH_SHORT).show();
            // You might want to return here instead of finishing the activity
            // finish();
        }
    }


    private void sendData(String data) {
        if (outputStream != null) {
            try {
                outputStream.write(data.getBytes());
            ///    Toast.makeText(this, "Data sent successfully", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to send data", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Output stream is not initialized", Toast.LENGTH_SHORT).show();
        }
    }


    private void startReadingData() {
        isReadingData = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024]; // Adjust buffer size as needed
                int bytes;
                while (isReadingData) {
                    try {
                        bytes = inputStream.read(buffer);
                        String receivedData = new String(buffer, 0, bytes);
                        Log.d("Bluetooth", "Received data: " + receivedData);

                        // Update TextViews with received data
                        updateTextViews(receivedData);
                    } catch (IOException e) {
                        Log.e("Bluetooth", "Error reading data from input stream: " + e.getMessage());
                        e.printStackTrace();
                        break; // Break the loop if there's an error reading data
                    }
                }
            }
        }).start();
    }


    private void updateTextViews(final String receivedData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (receivedData.startsWith("*t") && receivedData.contains("*h") && receivedData.contains("*d")) {
                    String temperature = receivedData.substring(receivedData.indexOf("*t") + 2, receivedData.indexOf("*h"));
                    String humidity = receivedData.substring(receivedData.indexOf("*h") + 2, receivedData.indexOf("*d"));
                    String distance = receivedData.substring(receivedData.indexOf("*d") + 2);

                    // Update TextViews with parsed values
                    humidityValue.setText(humidity );
                    temperatureValue.setText(temperature);
                    distanceValue.setText(distance);
                }
            }
        });
    }

    private void stopReadingData() {
        isReadingData = false;
    }
    // TextToSpeech initialization callback
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Set language for speech synthesis
            int result = textToSpeech.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported");
            }
        } else {
            Log.e("TTS", "Initialization failed");
        }
    }

    // Method to speak the given text
    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "repeated_speech");
        }
    }

    // Method to stop speaking
    private void stopSpeaking() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release TextToSpeech resources
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
