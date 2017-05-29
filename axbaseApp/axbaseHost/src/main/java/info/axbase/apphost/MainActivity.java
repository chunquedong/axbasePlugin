/**
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.apphost;

import info.axbase.app.PluginClient;
import info.axbase.appprot.ComponentRegister;
import info.axbase.appprot.Protocol;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	TextView textView;
	Button button;

	Protocol hostInterface = new Protocol() {
		@Override
		public Object call(Object arg) {
			return "Hello From Host!";
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		textView = (TextView) this.findViewById(R.id.textView1);
		button = (Button) this.findViewById(R.id.button);

		button.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				PluginClient.getInstance()
						.launch("0729c758-3216-3c80-3113-0242ac110150",
								MainActivity.this, false);
			}
		});


		ComponentRegister.getInstance().setComponent("host", hostInterface);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
