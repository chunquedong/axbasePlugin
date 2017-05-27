/*
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.plugin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

public class ActivityStub extends Activity {

	static class MyView extends View {

		Rect r = new Rect(0, 0, 600, 100);
		Paint paint = new Paint();
		
		public MyView(Context context) {
			super(context);
		}
		
		@SuppressLint("NewApi")
		@Override
		protected void onDraw(Canvas canvas) {
			paint.setColor(Color.RED);
			paint.setShadowLayer(12.0f, 0.0f, 3.0f, Color.BLACK);
			canvas.drawText("axbase插件", 10, 10, paint);
			setLayerType(LAYER_TYPE_SOFTWARE, null);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyView myView = new MyView(this);
		setContentView(myView);
	}
}
