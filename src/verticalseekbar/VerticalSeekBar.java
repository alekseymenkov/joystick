package verticalseekbar;


import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;


public class VerticalSeekBar extends SeekBar {
	private int lastProgress = 0;

	public VerticalSeekBar(Context context) {
		super(context);
	}


	public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}


	public VerticalSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(h, w, oldh, oldw);
	}


	private OnSeekBarChangeListener mOnChangeListener;
	@Override
	public void setOnSeekBarChangeListener(OnSeekBarChangeListener onChangeListener){
		this.mOnChangeListener = onChangeListener;
		//		super.setOnSeekBarChangeListener(onChangeListener);
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

	public void setProgress(int progress) {
		super.setProgress(progress);
		onSizeChanged(getWidth(), getHeight(), 0, 0);

		if(progress != lastProgress) {
			// Only enact listener if the progress has actually changed
			lastProgress = progress;
			if (mOnChangeListener != null)
				mOnChangeListener.onProgressChanged(this, progress, true);
		}

		return;
	}


	protected void onDraw(Canvas c) {
		c.rotate(-90);
		c.translate(-getHeight(), 0);

		super.onDraw(c);
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (!isEnabled()) {
			return false;
		}

		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mOnChangeListener.onStartTrackingTouch(this);
			setPressed(true);
			setSelected(true);
			break;
		case MotionEvent.ACTION_MOVE:
			super.onTouchEvent(event);
			int progress = getMax() - (int) (getMax() * event.getY() / getHeight());

			// Ensure progress stays within boundaries
			if(progress < 0) {progress = 0;}
			if(progress > getMax()) {progress = getMax();}
			setProgress(progress);  // Draw progress

			if(progress != lastProgress) {
				// Only enact listener if the progress has actually changed
				lastProgress = progress;
				mOnChangeListener.onProgressChanged(this, progress, true);
			}

			onSizeChanged(getWidth(), getHeight() , 0, 0);
			setPressed(true);
			setSelected(true);
			break;
		case MotionEvent.ACTION_UP:
			mOnChangeListener.onStopTrackingTouch(this);
			setPressed(false);
			setSelected(false);
			break;
		case MotionEvent.ACTION_CANCEL:
			super.onTouchEvent(event);
			setPressed(false);
			setSelected(false);
			break;

		}
		return true;
	}
}