package com.example.TouchApp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity implements AlertDialogCallback<String>{

    private static final String TAG = "MainActivity";

    //Variables
    Button btnHug;
    ImageButton btnConnect;
    Socket mSocket;
    boolean userPress=false;
    boolean otherPress=false;
    boolean isConnected=false;
    Vibrator vib;
    TextView txtStatus;
    AlphaAnimation fadeIn, fadeOut;
    AnimationDrawable animationDrawable;


    Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Unable to connect to NodeJS server", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    public Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Socket Connected!");
        }
    };

    public Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Socket Disconnected!");
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Hooks
        btnConnect=(ImageButton)findViewById(R.id.btnConnect);
        btnHug=(Button)findViewById(R.id.btnHug);
        vib=(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        txtStatus=(TextView) findViewById(R.id.txtStatus);
        txtStatus.setText("");

        //Animation things
        ConstraintLayout constraintLayout = findViewById(R.id.main);
        animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(300);
        animationDrawable.setExitFadeDuration(600);
        fadeIn = new AlphaAnimation(0.0f , 1.0f ) ;
        fadeOut = new AlphaAnimation( 1.0f , 0.0f ) ;
        fadeIn.setDuration(1200);
        fadeOut.setDuration(1200);
        fadeOut.setStartOffset(1200);

        fadeOut.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationEnd(Animation arg0) {
                // start fadeIn when fadeOut ends (repeat)
                txtStatus.setText("");
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });

        fadeIn.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationEnd(Animation arg0) {
                // start fadeIn when fadeOut ends (repeat)
                txtStatus.startAnimation(fadeOut);

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });


        //Button setup
        btnConnect.setOnClickListener(v->openDialog());
        btnHug.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {


                if(isConnected){
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    userPress=true;
                    mSocket.emit("pressDetection","user",true);


                    tryHug();
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    userPress=false;
                    mSocket.emit("pressDetection","user",false);
                    return true;
                }
                
                }
                return false;
            }
        });

        //Socket
        ConnectApp app=new ConnectApp();
        mSocket=app.getSocket();

        //Other client
        mSocket.on("ready", v ->{
                txtStatus.setText("Other user is here");

                txtStatus.startAnimation(fadeIn);
        });
        mSocket.on("otherPress", (state) ->{
                otherPress= (boolean) state[0];
                tryHug();

        });




    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void tryHug() {
        if(userPress&&otherPress){
            userPress=false;
            otherPress=false;
            vib.vibrate(VibrationEffect.createOneShot(500,100));
            animationDrawable.start();
        }
        
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public void openDialog(){
        CodeDialog codeDialog = new CodeDialog(this,this);
        codeDialog.show(getSupportFragmentManager(), "Enter code:");

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void alertDialogCallback(String room) {
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.connect();



        mSocket.on("connect", v->{
            //vib.vibrate(VibrationEffect.createOneShot(500,100));
            mSocket.emit("joinRoom", room);
        });
        mSocket.on("joined", v->{
            vib.vibrate(VibrationEffect.createOneShot(500,100));
            isConnected=true;

            txtStatus.setText("Connected");
            txtStatus.startAnimation(fadeIn);
        });


    }
}