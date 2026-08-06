package mz.geator.mensagens;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

public final class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) return;

        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        if (messages == null || messages.length == 0) return;

        String sender = messages[0].getDisplayOriginatingAddress();
        StringBuilder body = new StringBuilder();
        long timestamp = messages[0].getTimestampMillis();

        for (SmsMessage message : messages) {
            body.append(message.getMessageBody());
        }

        String text = body.toString().trim();
        if (!looksLikePayment(text)) return;

        SmsItem item = new SmsItem();
        item.sender = sender == null ? "DESCONHECIDO" : sender;
        item.body = text;
        item.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
        item.id = HashUtil.sha256(item.sender + "|" + item.body + "|" + item.timestamp);
        item.status = "pending";
        item.lastError = "";
        item.attempts = 0;

        SmsRepository.upsert(context, item);
        WorkScheduler.enqueue(context);
    }

    static boolean looksLikePayment(String text) {
        String t = text == null ? "" : text.toLowerCase();
        return t.contains("confirmado")
                || t.contains("id trans")
                || t.contains("id da transacao")
                || t.contains("id da transação")
                || t.contains("recebeste")
                || t.contains("recebeu")
                || t.contains("transferiste");
    }
}
