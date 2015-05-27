package org.opencv.samples.colorblobdetect;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jp.ksksue.driver.serial.FTDriver;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.view.Menu;
import android.app.Activity;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ColorBlobDetectionActivity extends Activity implements
		CvCameraViewListener2 {
	private static final String TAG = "Ball finder::Activity";

	private boolean mIsColorSelected = false;
	private Mat mRgba;
	private Scalar mBlobColorRgba;
	private List<Scalar> mBlobColorHsvList;
	private Scalar mBlobColorHsv;
	public static List<ColorBlobDetector> mDetectorList;
	public static ColorBlobDetector mDetector;
	private Mat mSpectrum;
	private Size SPECTRUM_SIZE;
	private Scalar CONTOUR_COLOR;

	private static FTDriver com;
	public static Tracker tracker;

	public static byte velocity = 20;
	public static byte neg_velocity = -20;
	public static double x_now = 0;
	public static double y_now = 0;
	public static double theta_now = 0;

	public static boolean stopped = false;
	public static Stopwatch stopwatch = new Stopwatch();
	public static double stoptime = 0;

	private CameraBridgeViewBase mOpenCvCameraView;

	public static Mat homographyMat;
	
	public static int index = 0;
	public List<Beacon> beaconsList;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public ColorBlobDetectionActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		mBlobColorHsvList = new ArrayList<Scalar>();
		mDetectorList = new ArrayList<ColorBlobDetector>();
		beaconsList = new ArrayList<Beacon>();
		
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);

		com = new FTDriver((UsbManager) getSystemService(USB_SERVICE));
		com.begin(9600);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.color_blob_detection_surface_view);

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
		mOpenCvCameraView.setCvCameraViewListener(this);

//		tracker = new Tracker();
//		Thread t = new Thread(tracker);
//		t.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.disconnect:
			System.out.println("disconnected");
			disconnect();
			return true;
		case R.id.mat:
			System.out.println("color");
			homographyMat = getHomographyMatrix(mRgba);
			System.out.println("color cols: " + homographyMat.cols() + " rows: " + homographyMat.rows() + " dump "+ homographyMat.dump());
			break;
		case R.id.seek:
//			for (int i = 0; i < 5; i++) {
//				comWrite(new byte[] { '+', '\r', '\n' });
//				try {
//					Thread.sleep(200);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//
			beginTracking();
			robotMove move = new robotMove();
			Thread t2 = new Thread(move);
			t2.start();
			break;
		case R.id.plus:
			for (int i = 0; i < 3; i++) {
				comWrite(new byte[] { '+', '\r', '\n' });
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		case R.id.minus:
//			for (int i = 0; i < 3; i++) {
//				comWrite(new byte[] { '-', '\r', '\n' });
//				try {
//					Thread.sleep(200);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
			index=0;
			mBlobColorHsvList.add(index, new Scalar (255,205,141,0));	//red
			System.out.println("Color: red");
			index++;
			mBlobColorHsvList.add(index, new Scalar (106,255,74,0));	//green
			System.out.println("Color: red");
			index++;
			mBlobColorHsvList.add(index, new Scalar (39, 255, 207, 0));	//yellow
			System.out.println("Color: red");
			index++;
			mBlobColorHsvList.add(index, new Scalar (151,255,125,0));	//blue
			System.out.println("Color: red");
			index++;
			break;
		case R.id.red:
			mBlobColorHsvList.add(index, new Scalar (255,205,141,0));
			System.out.println("Color: red");
			index++;
			break;
		case R.id.green:
			mBlobColorHsvList.add(index, new Scalar (106,255,74,0));
			System.out.println("Color: green");
			index++;
			break;
		case R.id.yellow:
			mBlobColorHsvList.add(index, new Scalar(39, 255, 207, 0));
			System.out.println("Color: yellow");
			index++;
			break;

		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		for(int i=0;i<4;i++){
			mDetectorList.add(i,new ColorBlobDetector());
		}
		
		Beacon b1 = new Beacon(125,125,"red","yellow");  	//rechts oben 	rot gelb
		beaconsList.add(0,b1);
		Beacon b2 = new Beacon(125,0,"green", "red");  		//rechts 		grün rot
		beaconsList.add(1,b2);
		Beacon b3 = new Beacon(125,-125,"yellow", "red");  	//rechts unten 	gelb rot
		beaconsList.add(2,b3);
		Beacon b4 = new Beacon(0,-125,"red", "blue"); 		//unten			rot blau
		beaconsList.add(3,b4);
		Beacon b5 = new Beacon(-125,-125,"yellow", "blue"); //links unten 	gelb blau
		beaconsList.add(4,b5);
		Beacon b6 = new Beacon(-125,0,"green", "blue");  	//links			grün blau
		beaconsList.add(5,b6);
		Beacon b7 = new Beacon(-125,125,"blue", "yellow");  //links oben 	blau gelb
		beaconsList.add(6,b7);
		Beacon b8 = new Beacon(0,125,"blue", "yellow");  	//oben 			blau rot
		beaconsList.add(7,b8);
		
		mSpectrum = new Mat();
		mBlobColorRgba = new Scalar(255);
		mBlobColorHsv = new Scalar(255);
		SPECTRUM_SIZE = new Size(200, 64);
		CONTOUR_COLOR = new Scalar(255, 0, 0, 255);
	}

	public void onCameraViewStopped() {
		mRgba.release();
	}

	public boolean beginTracking() {
		for(int i=0;i<4;i++){
			mDetectorList.get(i).setHsvColor(mBlobColorHsvList.get(i));
			Imgproc.resize(mDetectorList.get(i).getSpectrum(), mSpectrum, SPECTRUM_SIZE);
			//tracker.setDetector(mDetectorList.get(i));
		}
		mIsColorSelected = true;
		return false; // don't need subsequent touch events
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();
		for(int i=0;i<4;i++){
			if (mIsColorSelected) {
				mDetectorList.get(i).process(mRgba);
				List<MatOfPoint> contours = mDetectorList.get(i).getContours();
				Log.e(TAG, "Contours count: " + contours.size());
				Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

				Mat colorLabel = mRgba.submat(4, 68, 4, 68);
				colorLabel.setTo(mBlobColorRgba);

				Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70,
						70 + mSpectrum.cols());
				mSpectrum.copyTo(spectrumLabel);
			}
		}
		return mRgba;
	}

	public static void comWrite(byte[] data) {
		// System.out.println("comwrite");
		if (com.isConnected()) {
			com.write(data);
		} else {
			System.out.println("not connected\n");
		}
	}

	public static String comRead() {
		String s = "";
		int i = 0;
		int n = 0;
		while (i < 3 || n > 0) {
			byte[] buffer = new byte[256];
			n = com.read(buffer);
			s += new String(buffer, 0, n);
			i++;
		}
		return s;
	}

	public static String comReadWrite(byte[] data) {
		// System.out.println("comreadwrite " + (char)data[0]);

		com.write(data);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) { // ignore
		}
		return comRead();
	}

	public static void robotSetVelocity(byte left, byte right) {
		comReadWrite(new byte[] { 'i', left, right, '\r', '\n' });
	}

	public void robotSetLeds(byte red, byte blue) {
		comReadWrite(new byte[] { 'u', red, blue, '\r', '\n' });
	}

	public void robotSetBar(byte value) {
		comReadWrite(new byte[] { 'o', value, '\r', '\n' });
	}

	public static void robotDrive(byte distance_cm) {
		comReadWrite(new byte[] { 'k', distance_cm, '\r', '\n' });
	}

	public static void robotTurn(byte degree) {
		comReadWrite(new byte[] { 'l', degree, '\r', '\n' });
	}

	public void disconnect() {
		com.end();
		if (!com.isConnected()) {
			System.out.println("disconnected");
		}

	}

	public static boolean isStopped() {
		return stopped;
	}

	public static void setStop(boolean bool) {
		stopped = bool;
	}

	public static double getStopime() {
		return stoptime;
	}

	public static void stopStoptime(Stopwatch stoptime1) {
		stoptime = stopwatch.elapsedTime();
	}

	// ***************************** END BASIC FUNCTIONS
	// *************************************
	// ***************************** START SPECIFIC FUNCTIONS
	// *************************************

	public static void setUp() {
		// x_now = 0;
		// y_now = 0;
		// theta_now = 0;
		// stopped = false;
		//
		// Read stopThread = new Read();
		// Thread t2 = new Thread(stopThread);
		// t2.start();
		// while (!(x_now == 130 && y_now == 250 && theta_now == 0)) {
		// if (!isStopped()) {
		// System.out.println("in drive if, Positon: X: " + x_now
		// + "  Y: " + y_now + "  Theta: " + theta_now);
		// driveFromTo(x_now, y_now, theta_now, 130, 250, 0);
		// } else {
		// do {
		// turn(-90);
		// drive(20, true);
		// turn(90);
		// } while (ReadSensorsMain());
		// setStop(false);
		// }
		// }
		// System.out.println(x_now + " " + y_now + " " + theta_now);
	}

	public static void printPosition() {
		System.out.println("Positon: X: " + x_now + "  Y: " + y_now
				+ "  Theta: " + theta_now);
	}

	public static boolean ReadSensorsMain() {
		String string1;
		try {
			do {
				string1 = comReadWrite(new byte[] { 'q', '\r', '\n' });
				string1 = string1.replaceAll("\\p{C}", "");
				string1 = string1.replaceAll("command", "");
				string1 = string1.replaceAll("execution", "");
				string1 = string1.replaceAll("ecution", "");
				string1 = string1.replaceAll("marked", "");
				string1 = string1.replaceAll("sensor", "");
				string1 = string1.replaceAll(":", "");
				string1 = string1.replaceAll(" ", "");
			} while (string1.length() == 0);

			String[] arr = string1.split("0x");
			int[] sensor = new int[arr.length];

			for (int i = 1; i < arr.length; i++) {
				sensor[i - 1] = Integer.parseInt(arr[i], 16);
			}

			for (int i : sensor) {
				System.out.print(i + " ");
			}
			int links = sensor[5];
			int rechts = sensor[6];

			if (rechts <= 50 || links <= 30) {
				System.out.println(" " + links + " " + rechts);
				return true;
			}
		} catch (Exception e) {
			System.out.println(e);
		}

		return false;
	}

	public Mat getHomographyMatrix(Mat mRgba) {
		final Size mPatternSize = new Size(6, 9); // number of inner corners in
													// the used chessboard
													// pattern
		float x = -48.0f; // coordinates of first detected inner corner on
							// chessboard
		float y = 309.0f;
		float delta = 12.0f; // size of a single square edge in chessboard
		LinkedList<Point> PointList = new LinkedList<Point>();

		// Define real-world coordinates for given chessboard pattern:
		for (int i = 0; i < mPatternSize.height; i++) {
			y = 309.0f;
			for (int j = 0; j < mPatternSize.width; j++) {
				PointList.addLast(new Point(x, y));
				y += delta;
			}
			x += delta;
		}
		MatOfPoint2f RealWorldC = new MatOfPoint2f();
		RealWorldC.fromList(PointList);

		// Detect inner corners of chessboard pattern from image:
		Mat gray = new Mat();
		Imgproc.cvtColor(mRgba, gray, Imgproc.COLOR_RGBA2GRAY); // convert image
																// to grayscale
		MatOfPoint2f mCorners = new MatOfPoint2f();
		boolean mPatternWasFound = Calib3d.findChessboardCorners(gray,
				mPatternSize, mCorners);

		// Calculate homography:
		if (mPatternWasFound)
			// Calib3d.drawChessboardCorners(mRgba, mPatternSize, mCorners,
			// mPatternWasFound); //for visualization
			return Calib3d.findHomography(mCorners, RealWorldC);
		else
			return new Mat();
	}
	
//	public boolean onTouch(View v, MotionEvent event) {
//        int cols = mRgba.cols();
//        int rows = mRgba.rows();
//
//        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
//        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;
//
//        int x = (int)event.getX() - xOffset;
//        int y = (int)event.getY() - yOffset;
//
//        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");
//
//        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;
//
//        Rect touchedRect = new Rect();
//
//        touchedRect.x = (x>4) ? x-4 : 0;
//        touchedRect.y = (y>4) ? y-4 : 0;
//
//        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
//        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;
//
//        Mat touchedRegionRgba = mRgba.submat(touchedRect);
//
//        Mat touchedRegionHsv = new Mat();
//        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);
//
//        // Calculate average color of touched region
//        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
//        int pointCount = touchedRect.width*touchedRect.height;
//        for (int i = 0; i < mBlobColorHsv.val.length; i++)
//            mBlobColorHsv.val[i] /= pointCount;
//
//        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
//
//        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
//                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");
//
//        mDetector.setHsvColor(mBlobColorHsv);
//
//        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);
//
//        mIsColorSelected = true;
//
//        touchedRegionRgba.release();
//        touchedRegionHsv.release();
//
//        return false; // don't need subsequent touch events
//    }
	
    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

}
