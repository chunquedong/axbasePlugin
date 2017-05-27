/**
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.appex;

import info.axbase.appprot.ComponentRegister;
import info.axbase.appprot.Protocol;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	
	Button button1;
	Button button2;
	Button button3;
	Button button4;
	
	Protocol pluginInterface = new Protocol() {
		@Override
		public Object call(Object arg) {
			return "Hello From plugin!";
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		button1 = (Button)this.findViewById(R.id.button1);
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, InternalActivity.class);
				MainActivity.this.startActivity(i);
			}
		});
		
		button2 = (Button)this.findViewById(R.id.button2);
		button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, RegistedActivity.class);
				MainActivity.this.startActivity(i);
			}
		});
		
		button3 = (Button)this.findViewById(R.id.button3);
		button3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, PluginService.class);
				MainActivity.this.startService(i);
			}
		});
		
		ComponentRegister.getInstance().setComponent("plugin", pluginInterface);
		final Protocol host = ComponentRegister.getInstance().getComponent("host");
		button4 = (Button)this.findViewById(R.id.button4);
		button4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (host != null) {
					PluginApplication.showMessage(""+host.call("from plugin"));
				}
			}
		});
	}
}
