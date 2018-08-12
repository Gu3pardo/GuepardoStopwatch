package guepardoapps.stopme.controller;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

public class MailController {
    private Context _context;

    public MailController(@NonNull Context context) {
        _context = context;
    }

    public void SendMail(
            @NonNull String address,
            @NonNull String subject,
            @NonNull String text,
            boolean startNewActivity) {
        Intent mailIntent = new Intent(Intent.ACTION_SEND);

        if (startNewActivity) {
            mailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        mailIntent.setType("plain/text");
        mailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
        mailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        mailIntent.putExtra(Intent.EXTRA_TEXT, text);

        _context.startActivity(Intent.createChooser(mailIntent, "Send mail..."));
    }

    public void SendMail(
            @NonNull String subject,
            @NonNull String text,
            boolean startNewActivity) {
        Intent mailIntent = new Intent(Intent.ACTION_SEND);

        if (startNewActivity) {
            mailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        mailIntent.setType("plain/text");
        mailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        mailIntent.putExtra(Intent.EXTRA_TEXT, text);

        _context.startActivity(Intent.createChooser(mailIntent, "Send mail..."));
    }

    public void SendMail(
            @NonNull String address,
            boolean startNewActivity) {
        Intent mailIntent = new Intent(Intent.ACTION_SEND);

        if (startNewActivity) {
            mailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        mailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
        mailIntent.setType("message/rfc822");

        _context.startActivity(Intent.createChooser(mailIntent, "Choose an Email client :"));
    }
}
