package guepardoapps.stopme.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import guepardoapps.stopme.R;
import guepardoapps.stopme.controller.MailController;

public class ActivityAbout extends Activity {
    private Context _context;
    private MailController _mailController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_about);

        _context = this;
        _mailController = new MailController(_context);
    }

    public void GoToGitHub(View view) {
        String gitHubLink = _context.getString(R.string.gitHubLink);
        Intent gitHubBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(gitHubLink));
        _context.startActivity(gitHubBrowserIntent);
    }

    public void PayPal(View view) {
        String gitHubLink = _context.getString(R.string.payPalLink);
        Intent gitHubBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(gitHubLink));
        _context.startActivity(gitHubBrowserIntent);
    }

    public void SendMail(View view) {
        String email = _context.getString(R.string.email);
        _mailController.SendMail(email, true);
    }
}