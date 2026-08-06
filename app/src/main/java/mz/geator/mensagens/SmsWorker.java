package mz.geator.mensagens;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

public final class SmsWorker extends Worker {
    public SmsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        List<SmsItem> items = SmsRepository.pending(context);
        boolean retryNeeded = false;

        for (SmsItem item : items) {
            item.attempts += 1;
            item.status = "sending";
            SmsRepository.upsert(context, item);

            ApiClient.Result result = ApiClient.send(context, item);
            if (result.ok()) {
                item.status = "sent";
                item.lastError = "";
            } else {
                item.status = "pending";
                item.lastError = "HTTP " + result.code() + ": " + result.body();
                retryNeeded = true;
            }
            SmsRepository.upsert(context, item);
        }

        return retryNeeded ? Result.retry() : Result.success();
    }
}
