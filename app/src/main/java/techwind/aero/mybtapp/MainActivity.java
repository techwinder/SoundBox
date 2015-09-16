package techwind.aero.mybtapp;

import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Set;

public class MainActivity extends Activity
{
    TextView m_Output;
    private final String eol = System.getProperty("line.separator");
    private final int REQUEST_ENABLE_BT = 123;
    private int m_Beep1ID, m_Beep3ID, m_Beep5ID;
    boolean m_SoundLoaded = false;
    int m_nBips = 1;

    BluetoothAdapter m_BtAdapter;
    BluetoothA2dp m_A2dpService;

    AudioManager m_AudioManager;
    MediaPlayer m_Player;
    SoundPool m_SoundPool;
    float m_Volume = 0.5f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setupLayout();
        setupAudio();
        setupBluetooth();

    }

    private void setupBluetooth()
    {
        //Setup Bluetooth
        BluetoothManager btm = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        m_BtAdapter = btm.getAdapter();
        m_BtAdapter.getProfileProxy(this, m_A2dpListener, BluetoothProfile.A2DP);

        if (!m_BtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = m_BtAdapter.getBondedDevices();
// If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                outputMsg("Paired device: "+device.getName() + " // " + device.getAddress());
            }
        }

        if (m_AudioManager.isBluetoothA2dpOn()) {
            // Adjust output for Bluetooth.
        } else if (m_AudioManager.isSpeakerphoneOn()) {
            // Adjust output for Speakerphone.
        } else if (m_AudioManager.isWiredHeadsetOn()) {
            // Adjust output for headsets
        } else {
            // If audio plays and noone can hear it, is it still playing?
        }
    }

    private void setupAudio()
    {
        m_nBips = 1;

        //SetupAudio
        m_AudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        m_AudioManager.setMode(AudioManager.STREAM_MUSIC);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        float actVolume = (float) m_AudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) m_AudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        m_Volume = actVolume / maxVolume;
        outputMsg("actVolume=" + actVolume + "  maxVolume=" + maxVolume + "  current=" + m_Volume+eol+eol);

        // Load the sounds
        SoundPool.Builder spbuilder = new SoundPool.Builder();
        AudioAttributes.Builder attrb = new AudioAttributes.Builder();
        attrb.setUsage(AudioAttributes.USAGE_MEDIA);
        AudioAttributes attr = attrb.build();
        spbuilder.setAudioAttributes(attr);
        spbuilder.setMaxStreams(1);
        m_SoundPool = spbuilder.build();
        m_SoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                m_SoundLoaded = true;
            }
        });
        m_Beep1ID = m_SoundPool.load(this, R.raw.bip_x1, 1);
        m_Beep3ID = m_SoundPool.load(this, R.raw.bip_x3, 1);
        m_Beep5ID = m_SoundPool.load(this, R.raw.bip_x5, 1);

    }

    private void setupLayout()
    {
        m_Output = (TextView)findViewById(R.id.ZeOutput);
        m_Output.setMovementMethod(new ScrollingMovementMethod());

        Button toneButton = (Button)findViewById(R.id.tone);
        toneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onTone();
            }
        });

        Button mplayerButton = (Button)findViewById(R.id.mplayer);
        mplayerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onMPlayer();
            }
        });

        Button soundPoolButton = (Button)findViewById(R.id.soundpool);
        soundPoolButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onSoundPool();
            }
        });

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.rb1) {
                    m_nBips = 1;
                } else if (checkedId == R.id.rb2) {
                    m_nBips = 3;
                } else {
                    m_nBips = 5;
                }
            }
        });
    }



    private void onTone()
    {
        try {
            for(int i=0; i<m_nBips; i++) {
                ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                //        toneG.startTone(ToneGenerator.TONE_CDMA_REORDER, 200);
                toneG.startTone(ToneGenerator.TONE_CDMA_HIGH_PBX_SLS, 200);
            }
        }
        catch(Exception e)
        {
            outputMsg(e.toString());
        }
    }


    private void onMPlayer()
    {
        if(m_Player!=null) m_Player.release();
        if(m_nBips==1)       m_Player = MediaPlayer.create(this, R.raw.bip_x1);
        else if(m_nBips==3)  m_Player = MediaPlayer.create(this, R.raw.bip_x3);
        else if(m_nBips==5)  m_Player = MediaPlayer.create(this, R.raw.bip_x5);
        if (m_Player != null) {
//            m_Player.setAudioStreamType(AudioManager.STREAM_MUSIC); /**@todo test*/
//        m_Player.setVolume(1.0f, 1.0f);
            m_Player.start();
        }
    }


    private void onSoundPool()
    {
        float volume= m_AudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume = 1.0f;
        switch(m_nBips)
        {
            case 1:
                m_SoundPool.play(m_Beep1ID, volume, volume, 1, 0, 1.0f);
                break;
            case 3:
                m_SoundPool.play(m_Beep3ID, volume, volume, 1, 0, 1.0f);
                break;
            case 5:
                m_SoundPool.play(m_Beep5ID, volume, volume, 1, 0, 1.0f);
                break;
        }
    }


    private void outputMsg(String msg)
    {
        m_Output.append(msg + eol);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    BroadcastReceiver m_Receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context ctx, Intent intent)
        {
            String action = intent.getAction();
            outputMsg("Received intent action: " + action);

            int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED);
            outputMsg(stateToString(state));

            if (action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED))
            {
                if (state == BluetoothA2dp.STATE_CONNECTED)
                {

                }
                else if (state == BluetoothA2dp.STATE_DISCONNECTED)
                {
                }
            }
            else if (action.equals(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED))
            {
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                outputMsg("Paired device: "+device.getName() + " // " + device.getAddress());
            }

        }

    };

    public static String stateToString(int state)
    {
        switch (state) {
            case BluetoothA2dp.STATE_DISCONNECTED:
                return "BluetoothA2dp.STATE_DISCONNECTED";
            case BluetoothA2dp.STATE_CONNECTING:
                return "BluetoothA2dp.STATE_CONNECTING";
            case BluetoothA2dp.STATE_CONNECTED:
                return "BluetoothA2dp.STATE_CONNECTED";
            case BluetoothA2dp.STATE_DISCONNECTING:
                return "BluetoothA2dp.STATE_DISCONNECTING";
            case BluetoothA2dp.STATE_PLAYING:
                return "BluetoothA2dp.STATE_PLAYING";
            case BluetoothA2dp.STATE_NOT_PLAYING:
                return "BluetoothA2dp.STATE_NOT_PLAYING";
            default:
                return "<unknown state " + state + ">";
        }
    }


    private BluetoothProfile.ServiceListener m_A2dpListener = new BluetoothProfile.ServiceListener() {

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy)
        {
            switch(profile)
            {
                case BluetoothProfile.HEALTH:
                    outputMsg("Service connected, profile = HEALTH"+eol);
                    break;
                case BluetoothProfile.HEADSET:
                    outputMsg("Service connected, profile = HEADSET"+eol);
                    break;
                case BluetoothProfile.A2DP:
                    outputMsg("Service connected. profile = A2DP"+eol);
                    m_A2dpService = (BluetoothA2dp) proxy;
                    if (m_AudioManager.isBluetoothA2dpOn())
                    {
                        outputMsg("AudioManager.BluetoothA2dp is on"+eol);
                    } else {
                        outputMsg("AudioManager.BluetoothA2dp is not on"+eol);
                    }
                    break;
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            outputMsg("Service A2DP is disconnected");

        }
    };

    /**
     * Callback function from the menu
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case RESULT_OK:
            {
                outputMsg("Bluetooth on OK, starting discovery");
                m_BtAdapter.startDiscovery();

                break;
            }
            case RESULT_CANCELED:
            {
                outputMsg("Bluetooth on failed");
                break;
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        m_BtAdapter.closeProfileProxy(BluetoothProfile.A2DP, m_A2dpService);
        super.onDestroy();
    }

    @Override
    protected void onResume()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(m_Receiver, filter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (m_Player != null)
        {
            m_Player.release();
            m_Player = null;
        }
        unregisterReceiver(m_Receiver);
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int action = event.getAction();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    outputMsg("Volume up");
//                    audioMgr.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                }
                return super.onKeyUp(keyCode, event);
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    outputMsg("Volume down");
//                    audioMgr.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                }
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_BACK:
            {
                System.exit(0);
                break;
            }

            default:
                return false;
        }
        return false;

    }

}
