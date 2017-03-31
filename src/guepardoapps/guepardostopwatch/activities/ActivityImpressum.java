package guepardoapps.guepardostopwatch.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import guepardoapps.guepardostopwatch.R;
import guepardoapps.guepardostopwatch.common.*;

import guepardoapps.library.toolset.common.Logger;
import guepardoapps.library.toolset.controller.MailController;

public class ActivityImpressum extends Activity {

	private static final String TAG = ActivityImpressum.class.getSimpleName();
	private Logger _logger;

	private Context _context;
	private MailController _mailController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.side_impressum);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Colors.ACTION_BAR_COLOR));

		_logger = new Logger(TAG, Enables.DEBUGGING);
		_logger.Debug("onCreate");

		_context = this;
		_mailController = new MailController(_context);
	}

	public void SendMail(View view) {
		_logger.Debug("SendMail");
		_mailController.SendMail("guepardoapps@gmail.com", true);
	}

	public void GoToGithub(View view) {
		_logger.Debug("GoToGithub");
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Gu3pardo/GuepardoStopwatch/")));
	}
}