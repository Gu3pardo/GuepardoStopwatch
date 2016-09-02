package guepardoapps.guepardostopwatch;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import guepardoapps.common.Constants;

public class ActivityImpressum extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.side_impressum);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Constants.ACTION_BAR_COLOR));
	}
}