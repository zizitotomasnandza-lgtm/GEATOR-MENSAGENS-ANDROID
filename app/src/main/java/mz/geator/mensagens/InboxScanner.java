package mz.geator.mensagens;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;

public final class InboxScanner {
    private InboxScanner() {}

    public static int scanRecent(Context context) {
        int added = 0;
        Uri uri = Telephony.Sms.Inbox.CONTENT_URI;
        String[] projection = {
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
        };

        try (Cursor cursor = context.getContentResolver().query(
                uri,
                projection,
                null,
                null,
                Telephony.Sms.DEFAULT_SORT_ORDER + " LIMIT 100"
        )) {
            if (cursor == null) return 0;

            int addressCol = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS);
            int bodyCol = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY);
            int dateCol = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE);

            while (cursor.moveToNext()) {
                String sender = cursor.getString(addressCol);
                String body = cursor.getString(bodyCol);
                long date = cursor.getLong(dateCol);

                if (!SmsReceiver.looksLikePayment(body)) continue;

                String id = HashUtil.sha256(sender + "|" + body + "|" + date);
                if (SmsRepository.exists(context, id)) continue;

                SmsItem item = new SmsItem();
                item.id = id;
                item.sender = sender == null ? "DESCONHECIDO" : sender;
                item.body = body;
                item.timestamp = date;
                item.status = "pending";
                item.lastError = "";
                item.attempts = 0;
                SmsRepository.upsert(context, item);
                added++;
            }
        }

        if (added > 0) WorkScheduler.enqueue(context);
        return added;
    }
}
