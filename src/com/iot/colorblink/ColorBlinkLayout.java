package com.iot.colorblink;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

public class ColorBlinkLayout extends FrameLayout {
	private static final int MESSAGE_BLINK = 0x42;
	private static final int BLINK_DELAY = 500;

	private boolean mBlink;
	private boolean mBlinkState;
	private final Handler mHandler;
	int mBlinkInterval;
	int mBlinkColors[];
	BlinkMode mMode;
	private static final BlinkMode[] sBlinkModeArray = { BlinkMode.OVERLAY,
			BlinkMode.BACKGROUND };
	private static final String LOG_TAG = "ColorBlinkLayout";
	int mIndex = 0;

	public enum BlinkMode {
		OVERLAY(0), BACKGROUND(1);
		final int nativeInt;

		BlinkMode(int ni) {
			nativeInt = ni;
		}
	}

	BlinkMode mOverlay = BlinkMode.OVERLAY;

	public ColorBlinkLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray ta = context.obtainStyledAttributes(attrs,
				R.styleable.ColorBlinkLayout);
		mBlinkInterval = ta.getInt(R.styleable.ColorBlinkLayout_interval,
				BLINK_DELAY);
		int resId = ta.getResourceId(R.styleable.ColorBlinkLayout_color, 0);
		if (resId > 0) {
			String colors[] = context.getResources().getStringArray(resId);
			mBlinkColors = new int[colors.length];
			for (int i = 0; i < colors.length; i++) {
				String color = colors[i];
				mBlinkColors[i] = Integer.decode(color) | 0xFF000000;

			}
		}
		int modeIndex = ta.getInt(R.styleable.ColorBlinkLayout_mode, 0);
		mMode = sBlinkModeArray[modeIndex];
		ta.recycle();
		mHandler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				if (msg.what == MESSAGE_BLINK) {
					if (mBlink) {
						mBlinkState = !mBlinkState;
						makeBlink();
					}
					invalidate();
					return true;
				}
				return false;
			}
		});
	}

	private void makeBlink() {
		Message message = mHandler.obtainMessage(MESSAGE_BLINK);
		mHandler.sendMessageDelayed(message, mBlinkInterval);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		mBlink = true;
		mBlinkState = true;

		makeBlink();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		mBlink = false;
		mBlinkState = true;
		mHandler.removeMessages(MESSAGE_BLINK);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (mBlinkState) {
			super.dispatchDraw(canvas);
		} else if (mBlinkColors != null) {
			if (mIndex >= mBlinkColors.length) {
				mIndex = 0;
			}
			int color = mBlinkColors[mIndex++];
			int canvasCount = canvas.saveLayer(getScrollX(), getScrollY(),
					getScrollX() + getWidth(), getScrollY() + getHeight(),
					null, Canvas.ALL_SAVE_FLAG);
			super.dispatchDraw(canvas);
			switch (mMode) {
			case OVERLAY:
				canvas.drawColor(color, PorterDuff.Mode.SRC_ATOP);
				break;
			case BACKGROUND:
				canvas.drawColor(color, PorterDuff.Mode.MULTIPLY);
				break;

			}
			canvas.restoreToCount(canvasCount);
		}

	}
}
