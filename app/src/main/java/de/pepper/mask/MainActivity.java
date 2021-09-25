package de.pepper.mask;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;


import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.TakePictureBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.camera.TakePicture;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.aldebaran.qi.sdk.object.image.EncodedImage;
import com.aldebaran.qi.sdk.object.image.EncodedImageHandle;
import com.aldebaran.qi.sdk.object.image.TimestampedImageHandle;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import de.entrance.objectclassification.tflite.Classifire;


public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {


    Classifire classifire =  null;

    ImageView imageView;
    TextView textView;

    HumanAwareness humanAwareness = null ;
   // Bitmap bitmap = null;


    private QiContext qiContext;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        QiSDK.register(this, this);

        imageView = (ImageView)findViewById(R.id.ivMain);
        textView = (TextView)findViewById(R.id.tvObject);

        try {
            classifire = new Classifire(this, -1 ,"model_mask25.tflite","labels_mask25.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {

        // Store the provided QiContext.
        this.qiContext = qiContext;


        new Thread(new Runnable() {
            public void run() {

                while (true) {
                    Bitmap bitmap ;

                    bitmap = takePicture(qiContext);

                    boolean isPerson = findPersonsAround(qiContext);



                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (bitmap != null && isPerson) {

                                    classifire.processImage(bitmap, 0 );
                                    Log.i("az", classifire.getId() +" -  "+ classifire.getConfidenc() );
                                    // Log.i("az", "" + classifire.getConfidenc());
                                    imageView.setImageBitmap(bitmap);
                                    textView.setText(classifire.getId() +" -  "+ classifire.getConfidenc());

                                    if(classifire.getId().equals("WithoutMask")){
                                        textView.setTextColor(Color.RED);
                                        textView.setBackgroundColor(Color.YELLOW);
                                    }else{
                                        textView.setTextColor(Color.BLUE);
                                        textView.setBackgroundColor(Color.WHITE);
                                    }

                            }else {
                                textView.setText(" No Person " );
                            }

                        }
                    });
                    try {Thread.sleep(100);} catch (Exception e) { e.printStackTrace();}
                }

            }
        }).start();



    }

    @Override
    public void onRobotFocusLost() {
        // Remove the QiContext.
        this.qiContext = null;


    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }




    public Bitmap takePicture(QiContext myContext) {

        TakePicture takePicture = TakePictureBuilder.with(myContext).build();
        TimestampedImageHandle result = takePicture.run();
        EncodedImageHandle encodedImageHandle = result.getImage();
        EncodedImage encodedImage = encodedImageHandle.getValue();
        ByteBuffer buffer = encodedImage.getData();
        buffer.rewind();
        final int pictureBufferSize = buffer.remaining();
        byte[] facePictureArray = new byte[pictureBufferSize];
        buffer.get(facePictureArray);

       // try {Thread.sleep(200);} catch (Exception e) { e.printStackTrace();}

         return  BitmapFactory.decodeByteArray(facePictureArray, 0, pictureBufferSize);

    }



    /// Metho to finde the Humans
    private boolean findPersonsAround(QiContext myContext) {

        if (myContext != null){
            //try {  Thread.sleep(50);  } catch (Exception e) {e.printStackTrace(); }
            if (myContext.getHumanAwareness().getHumansAround().size() > 0) {
                return true;
            }else {
                return false;
            }

        }else {
            return false;
        }


    }
/*
    // Method to get The Human Futurs
    private void retrieveCharacteristics(final List<Human> humans) {

        for (int i = 0; i < humans.size(); i++) {
            // Get the human.
            Human human = humans.get(i);
            // Get face picture.
            ByteBuffer facePictureBuffer = human.getFacePicture().getImage().getData();
            tempFacePictureBuffer = facePictureBuffer;
            facePictureBuffer.rewind();
            int pictureBufferSize = facePictureBuffer.remaining();
            facePictureArray = new byte[pictureBufferSize];
            facePictureBuffer.get(facePictureArray);

            // Test if the robot has an empty picture
            if (pictureBufferSize != 0) {

                Log.i("img", "Picture available");
                try {Thread.sleep(1000); } catch (Exception e) {e.printStackTrace(); }

                //  facePicture = null;
                //  facePicture = BitmapFactory.decodeByteArray(facePictureArray, 0, pictureBufferSize);
            }
        }
    }

 */






}