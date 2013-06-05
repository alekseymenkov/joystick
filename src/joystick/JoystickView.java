package joystick;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {

	private Paint circlePaint;
	private Paint handlePaint;
	private double touchX;
	private double touchY;
	private int handleRadius;
	private int handleInnerBoundaries;
	private JoystickMovedListener listener;

	static final int OFFSET = 100;


	public JoystickView(Context context) {
		super(context);
		initJoystickView();
	}


	public JoystickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initJoystickView();
	}


	public JoystickView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initJoystickView();
	}


	private void initJoystickView() {
		setFocusable(true);

		circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaint.setColor(Color.GRAY);
		circlePaint.setStrokeWidth(5);
		circlePaint.setStyle(Paint.Style.STROKE);

		handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		handlePaint.setColor(Color.DKGRAY);
		handlePaint.setStrokeWidth(1);
		handlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
	}


	public void setOnJostickMovedListener(JoystickMovedListener listener) {
		this.listener = listener;
	}


	public void setPositionX(int x) {

		touchX = (x - OFFSET) * (getMeasuredWidth() - handleInnerBoundaries * 2) / 200.0;
		invalidate();
		return;
	}


	public void setPositionY(int y) {

		touchY = (-y + OFFSET) * (getMeasuredHeight() - handleInnerBoundaries * 2) / 200.0;
		invalidate();
		return;
	}


	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		handleRadius = 20;
		handleInnerBoundaries = 5;
	}


	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas) {

		int px = getMeasuredWidth() / 2;
		int py = getMeasuredHeight() / 2;

		final RectF figure = new RectF(handleInnerBoundaries, handleInnerBoundaries,
				px * 2 - handleInnerBoundaries, py * 2 - handleInnerBoundaries);

		canvas.drawRoundRect(figure, px / 5, py / 5, circlePaint);

		canvas.drawCircle((int) touchX + px, (int) touchY + py, handleRadius, handlePaint);

		canvas.save();
	}


	public boolean onTouchEvent(MotionEvent event) {

		int actionType = event.getAction();
		if (actionType == MotionEvent.ACTION_MOVE) {
			int px = getMeasuredWidth() / 2;
			int py = getMeasuredHeight() / 2;

			touchX = (event.getX() - px);
			touchX = Math.max(Math.min(touchX, (px - handleInnerBoundaries)), (-px + handleInnerBoundaries));

			touchY = (event.getY() - py);
			touchY = Math.max(Math.min(touchY, (py - handleInnerBoundaries)), (-py + handleInnerBoundaries));

			if (listener != null) {
				listener.OnMoved((int) (touchX / (px - handleInnerBoundaries) * 100.0) + OFFSET, (int) (-touchY / (py - handleInnerBoundaries) * 100.0) + OFFSET);
			}

			invalidate();
		} else if (actionType == MotionEvent.ACTION_UP) {
			returnHandleToCenter();
		}
		return true;
	}


	public void returnHandleToCenter() {

		Handler handler = new Handler();
		final int numberOfFrames = 4;
		final double intervalsX = (0 - touchX) / numberOfFrames;
		final double intervalsY = (0 - touchY) / numberOfFrames;
		final int px = getMeasuredWidth() / 2;
		final int py = getMeasuredHeight() / 2;

		for (int i = 0; i < numberOfFrames + 1; i++) {
			final int runNumber = i;
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (runNumber == numberOfFrames) {
						touchX = 0;
						touchY = 0;
					} else {
						touchX += intervalsX;
						touchY += intervalsY;
					}
					// Pressure
					if (listener != null) {
						listener.OnMoved((int) (touchX / (px - handleInnerBoundaries) * 100.0) + OFFSET, (int) (-touchY / (py - handleInnerBoundaries) * 100.0) + OFFSET);
					}
					invalidate();
				}
			}, i * 40);
		}
	}
}