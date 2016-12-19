package moe.martini.midictrl;

import android.content.Context;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;

import static moe.martini.midictrl.R.id.info;

public class ControlActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    int midiDevice = 0;
    MidiManager m;
    MidiDeviceInfo[] infos;
    MidiInputPort inputPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        // Initialize MIDI
        m = (MidiManager)getApplicationContext().getSystemService(Context.MIDI_SERVICE);
        // Get connected Devices
        infos = m.getDevices();
        // Build String Array
        String[] devices = new String[infos.length];
        for (int i = 0; i < infos.length; i++) {
            devices[i] = infos[i].getProperties().getString(MidiDeviceInfo.PROPERTY_NAME);
        }
        // Populate the Spinner
        Spinner spinnerDevices = (Spinner) findViewById(R.id.spinnerDevices);
        ArrayAdapter<String> adapterDevices = new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_item, devices);
        spinnerDevices.setAdapter(adapterDevices);
        spinnerDevices.setOnItemSelectedListener(this);

        //Connect Buttons
        ImageButton buttonPlay = (ImageButton) findViewById(R.id.buttonPlay);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                byte[] buffer = new byte[32];
                int numBytes = 0;
                buffer[numBytes++] = (byte) (0xF0); // MMC
                buffer[numBytes++] = (byte) (0x7F); // MMC
                buffer[numBytes++] = (byte) (0x7F); // all devices
                buffer[numBytes++] = (byte) (0x06); // command
                buffer[numBytes++] = (byte) (0x02); // play
                buffer[numBytes++] = (byte) (0xF7); // end
                sendMidi(buffer, numBytes);
            }
        });
        ImageButton buttonRecord = (ImageButton) findViewById(R.id.buttonRecord);
        buttonRecord.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                byte[] buffer = new byte[32];
                int numBytes = 0;
                buffer[numBytes++] = (byte) (0xF0); // MMC
                buffer[numBytes++] = (byte) (0x7F); // MMC
                buffer[numBytes++] = (byte) (0x7F); // all devices
                buffer[numBytes++] = (byte) (0x06); // command
                buffer[numBytes++] = (byte) (0x06); // record
                buffer[numBytes++] = (byte) (0xF7); // end
                sendMidi(buffer, numBytes);
            }
        });
        ImageButton buttonStop = (ImageButton) findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                byte[] buffer = new byte[32];
                int numBytes = 0;
                buffer[numBytes++] = (byte) (0xF0); // MMC
                buffer[numBytes++] = (byte) (0x7F); // MMC
                buffer[numBytes++] = (byte) (0x7F); // all devices
                buffer[numBytes++] = (byte) (0x06); // command
                buffer[numBytes++] = (byte) (0x01); // stop
                buffer[numBytes++] = (byte) (0xF7); // end
                sendMidi(buffer, numBytes);
            }
        });
        ImageButton buttonRewind = (ImageButton) findViewById(R.id.buttonRewind);
        buttonRewind.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                byte[] buffer = new byte[32];
                int numBytes = 0;
                buffer[numBytes++] = (byte)(0xF0); // MMC
                buffer[numBytes++] = (byte)(0x7F); // MMC
                buffer[numBytes++] = (byte)(0x7F); // all devices
                buffer[numBytes++] = (byte)(0x06); // command
                buffer[numBytes++] = (byte)(0x44);
                buffer[numBytes++] = (byte)(0x06);
                buffer[numBytes++] = (byte)(0x01);
                buffer[numBytes++] = (byte)(0x00);
                buffer[numBytes++] = (byte)(0x00);
                buffer[numBytes++] = (byte)(0x00);
                buffer[numBytes++] = (byte)(0x00);
                buffer[numBytes++] = (byte)(0x00);
                buffer[numBytes++] = (byte)(0xF7); // end
                sendMidi(buffer, numBytes);
            }
        });
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                byte[] buffer = new byte[32];
                int numBytes = 0;
                buffer[numBytes++] = (byte)(0xB0); // CC on Channel 0
                buffer[numBytes++] = (byte)(0x00); // Controller #0
                buffer[numBytes++] = (byte)(progress); // value
                sendMidi(buffer, numBytes);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });
    }

    public void sendMidi(byte[] buffer, int numBytes) {
        if (inputPort != null) {
            /*byte[] buffer = new byte[32];
            int numBytes = 0;
            buffer[numBytes++] = (byte) (0xF0); // MMC
            buffer[numBytes++] = (byte) (0x7F); // MMC
            buffer[numBytes++] = (byte) (0x7F); // all devices
            buffer[numBytes++] = (byte) (0x06); // command
            buffer[numBytes++] = cmd;          // stop
            buffer[numBytes++] = (byte) (0xF7); // end*/
            int offset = 0;
            // post is non-blocking
            try {
                inputPort.send(buffer, offset, numBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Input Port connected!",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        System.out.println(i);
        m.openDevice(infos[midiDevice], new MidiManager.OnDeviceOpenedListener() {
            @Override
            public void onDeviceOpened(MidiDevice device) {
                if (device == null) {
                    Toast.makeText(getApplicationContext(), "Could not open device!",
                            Toast.LENGTH_LONG).show();
                } else {
                    inputPort = device.openInputPort(0);
                }
            }
        }, new Handler(Looper.getMainLooper()));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
