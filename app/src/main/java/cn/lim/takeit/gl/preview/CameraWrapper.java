package cn.lim.takeit.gl.preview;

import java.io.IOException;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;

import cn.lim.takeit.gl.util.CamParaUtil;
import cn.lim.takeit.gl.util.FileUtil;
import cn.lim.takeit.gl.util.ImageUtil;

public class CameraWrapper {
	private static final String TAG = "Takeit";
	private Camera mCamera;
	private Camera.Parameters mParams;
	private boolean isPreviewing = false;
	private float mPreviwRate = -1f;
	private static CameraWrapper mCameraInterface;

	public interface CamOpenOverCallback{
		public void cameraHasOpened();
	}

	private CameraWrapper(){

	}
	public static synchronized CameraWrapper getInstance(){
		if(mCameraInterface == null){
			mCameraInterface = new CameraWrapper();
		}
		return mCameraInterface;
	}
	/**��Camera
	 * @param callback
	 */
	public void doOpenCamera(CamOpenOverCallback callback){
		Log.i(TAG, "Camera open....");
		if(mCamera == null){
			mCamera = Camera.open();
			Log.i(TAG, "Camera open over....");
			if(callback != null){
				callback.cameraHasOpened();
			}
		}else{
			Log.i(TAG, "Camera open �쳣!!!");
			doStopCamera();
		}
			
	
	}
	/**ʹ��Surfaceview����Ԥ��
	 * @param holder
	 * @param previewRate
	 */
	public void doStartPreview(SurfaceHolder holder, float previewRate){
		Log.i(TAG, "doStartPreview...");
		if(isPreviewing){
			mCamera.stopPreview();
			return;
		}
		if(mCamera != null){
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			initCamera(previewRate);
		}


	}
	/**ʹ��TextureViewԤ��Camera
	 * @param surface
	 * @param previewRate
	 */
	public void doStartPreview(SurfaceTexture surface, float previewRate){
		Log.i(TAG, "doStartPreview...");
		if(isPreviewing){
			mCamera.stopPreview();
			return;
		}
		if(mCamera != null){
			try {
				mCamera.setPreviewTexture(surface);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			initCamera(previewRate);
		}
		
	}

	/**
	 * ֹͣԤ�����ͷ�Camera
	 */
	public void doStopCamera(){
		if(null != mCamera)
		{
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview(); 
			isPreviewing = false; 
			mPreviwRate = -1f;
			mCamera.release();
			mCamera = null;     
		}
	}
	/**
	 * ����
	 */
	public void doTakePicture(){
		if(isPreviewing && (mCamera != null)){
			mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
		}
	}
	public boolean isPreviewing(){
		return isPreviewing;
	}



	private void initCamera(float previewRate){
		if(mCamera != null){

			mParams = mCamera.getParameters();
			mParams.setPictureFormat(PixelFormat.JPEG);//�������պ�洢��ͼƬ��ʽ
//			CamParaUtil.getInstance().printSupportPictureSize(mParams);
//			CamParaUtil.getInstance().printSupportPreviewSize(mParams);
			//����PreviewSize��PictureSize
			Size pictureSize = CamParaUtil.getInstance().getPropPictureSize(
					mParams.getSupportedPictureSizes(),previewRate, 800);
			mParams.setPictureSize(pictureSize.width, pictureSize.height);
			Size previewSize = CamParaUtil.getInstance().getPropPreviewSize(
					mParams.getSupportedPreviewSizes(), previewRate, 800);
			mParams.setPreviewSize(previewSize.width, previewSize.height);

			mCamera.setDisplayOrientation(90);

//			CamParaUtil.getInstance().printSupportFocusMode(mParams);
			List<String> focusModes = mParams.getSupportedFocusModes();
			if(focusModes.contains("continuous-video")){
				mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			}
			mCamera.setParameters(mParams);	
			mCamera.startPreview();//����Ԥ��



			isPreviewing = true;
			mPreviwRate = previewRate;

			mParams = mCamera.getParameters(); //����getһ��
			Log.i(TAG, "��������:PreviewSize--With = " + mParams.getPreviewSize().width
					+ "Height = " + mParams.getPreviewSize().height);
			Log.i(TAG, "��������:PictureSize--With = " + mParams.getPictureSize().width
					+ "Height = " + mParams.getPictureSize().height);
		}
	}



	/*Ϊ��ʵ�����յĿ������������ձ�����Ƭ��Ҫ���������ص�����*/
	ShutterCallback mShutterCallback = new ShutterCallback() 
	//���Ű��µĻص������������ǿ����������Ʋ��š����ꡱ��֮��Ĳ�����Ĭ�ϵľ������ꡣ
	{
		public void onShutter() {
			// TODO Auto-generated method stub
			Log.i(TAG, "myShutterCallback:onShutter...");
		}
	};
	PictureCallback mRawCallback = new PictureCallback() 
	// �����δѹ��ԭ���ݵĻص�,����Ϊnull
	{

		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Log.i(TAG, "myRawCallback:onPictureTaken...");

		}
	};
	PictureCallback mJpegPictureCallback = new PictureCallback() 
	//��jpegͼ�����ݵĻص�,����Ҫ��һ���ص�
	{
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Log.i(TAG, "myJpegCallback:onPictureTaken...");
			Bitmap b = null;
			if(null != data){
				b = BitmapFactory.decodeByteArray(data, 0, data.length);//data���ֽ����ݣ����������λͼ
				mCamera.stopPreview();
				isPreviewing = false;
			}
			//����ͼƬ��sdcard
			if(null != b)
			{
				//����FOCUS_MODE_CONTINUOUS_VIDEO)֮��myParam.set("rotation", 90)ʧЧ��
				//ͼƬ��Ȼ������ת�ˣ�������Ҫ��ת��
				Bitmap rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);
				FileUtil.saveBitmap(rotaBitmap);
			}
			//�ٴν���Ԥ��
			mCamera.startPreview();
			isPreviewing = true;
		}
	};


}
