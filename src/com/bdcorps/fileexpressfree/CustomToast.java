package com.bdcorps.fileexpressfree;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast extends Activity{
	public static void ToastMe(String text,Context context){
		LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		View layout = inflater.inflate(R.layout.toast_custom_layout,null);

		Toast toast = new Toast(context);
		TextView t = (TextView) layout.findViewById(R.id.textView1);
		t.setText(text);
		toast.setGravity(Gravity.BOTTOM, 0, 150);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();	
	}
}
