package guepardoapps.stopme.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import guepardoapps.stopme.R;
import guepardoapps.stopme.common.*;
import guepardoapps.stopme.controller.MailController;
import guepardoapps.stopme.tools.Logger;

public class ActivityImpressum extends Activity {
    private static final String TAG = ActivityImpressum.class.getSimpleName();
    private Logger _logger;

    private Context _context;
    private MailController _mailController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_about);

        _logger = new Logger(TAG, Enables.LOGGING);
        _logger.Debug("onCreate");

        _context = this;
        _mailController = new MailController(_context);
    }

    public void GoToGitHub(View view) {
        _logger.Debug("GoToGitHub");
        String gitHubLink = _context.getString(R.string.gitHubLink);
        Intent gitHubBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(gitHubLink));
        _context.startActivity(gitHubBrowserIntent);
    }

    public void PayPal(View view) {
        _logger.Debug("PayPal");
        String gitHubLink = _context.getString(R.string.payPalLink);
        Intent gitHubBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(gitHubLink));
        _context.startActivity(gitHubBrowserIntent);
    }

    public void SendMail(View view) {
        _logger.Debug("SendMail");
        String email = _context.getString(R.string.email);
        _mailController.SendMail(email, true);
    }
}