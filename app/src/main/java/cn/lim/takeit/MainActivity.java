package cn.lim.takeit;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private Camera mCamera;
    private CameraPreview mPreview;
    private int mWidth = 1280;
    private int mHeight = 720;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);


        // calc view height
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        System.out.println("display size:" + width + "/" + height);

        Camera.Size size = mCamera.getParameters().getPreviewSize();
        int newHeight = (int)((size.height * width) / size.width);

        System.out.println("newHeight:" + newHeight);
        System.out.println("preview size:" + size.width + "/" + size.height);

        // mPreview.setMinimumHeight(newHeight);

        //mPreview.setLayoutParams(new FrameLayout.LayoutParams(width, newHeight));

        preview.addView(mPreview, new ViewGroup.LayoutParams(width, newHeight));
    }

    /** A safe way to get an instance of the Camera object. */
    public Camera getCameraInstance(){
        Camera c = null;
        try {
            Camera.CameraInfo info = new Camera.CameraInfo();

            // Try to find a front-facing camera (e.g. for videoconferencing).
            int numCameras = Camera.getNumberOfCameras();
            for (int i = 0; i < numCameras; i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    c = Camera.open(i);

                    Camera.Parameters parms = c.getParameters();

                    choosePreviewSize(parms, mWidth, mHeight);
                    // leave the frame rate set to default
                    c.setParameters(parms);

                    Camera.Size size = parms.getPreviewSize();
                    Log.d(TAG, "Camera preview size is " + size.width + "x" + size.height);

                    int rotation = this.getWindowManager().getDefaultDisplay()
                            .getRotation();
                    int degrees = 0;
                    switch (rotation) {
                        case Surface.ROTATION_0: degrees = 0; break;
                        case Surface.ROTATION_90: degrees = 90; break;
                        case Surface.ROTATION_180: degrees = 180; break;
                        case Surface.ROTATION_270: degrees = 270; break;
                    }
                    System.out.println("rotation:" + rotation);
                    int result = (info.orientation - degrees + 360) % 360;
                    System.out.println("orientation:" + result);
                    c.setDisplayOrientation(result);

                    break;
                }
            }
            if (c == null) {
                Log.d("Takeit", "No front-facing camera found; opening default");
                c = Camera.open();    // opens first back-facing camera
            }
            if (c == null) {
                throw new RuntimeException("Unable to open camera");
            }

        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * Attempts to find a preview size that matches the provided width and height (which
     * specify the dimensions of the encoded video).  If it fails to find a match it just
     * uses the default preview size.
     * <p>
     * TODO: should do a best-fit match.
     */
    private static void choosePreviewSize(Camera.Parameters parms, int width, int height) {
        // We should make sure that the requested MPEG size is less than the preferred
        // size, and has the same aspect ratio.
        Camera.Size ppsfv = parms.getPreferredPreviewSizeForVideo();
        if (ppsfv != null) {
            Log.d("Takeit", "Camera preferred preview size for video is " +
                    ppsfv.width + "x" + ppsfv.height);
        }

        for (Camera.Size size : parms.getSupportedPreviewSizes()) {
            Log.e("Takeit", "getSupportedPreviewSizes:" + size.width + "x" + size.height);
        }

        for (Camera.Size size : parms.getSupportedPreviewSizes()) {
            if (size.width == width && size.height == height) {
                parms.setPreviewSize(width, height);
                //parms.setPictureSize(width, height);
                return;
            }
        }

        Log.w("Takeit", "Unable to set preview size to " + width + "x" + height);
        if (ppsfv != null) {
            parms.setPreviewSize(ppsfv.width, ppsfv.height);
            //parms.setPictureSize(ppsfv.width, ppsfv.height);
        }
    }

}
