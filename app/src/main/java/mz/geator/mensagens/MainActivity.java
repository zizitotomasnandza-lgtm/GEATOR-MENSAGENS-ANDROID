package mz.geator.mensagens;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

public final class MainActivity extends AppCompatActivity {
    private EditText edtUrl;
    private EditText edtToken;
    private TextView txtStatus;
    private TextView txtHistory;

    private final ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> refreshStatus()
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtUrl = findViewById(R.id.edtUrl);
        edtToken = findViewById(R.id.edtToken);
        txtStatus = findViewById(R.id.txtStatus);
        txtHistory = findViewById(R.id.txtHistory);

        edtUrl.setText(ConfigStore.url(this));
        edtToken.setText(ConfigStore.token(this));

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            ConfigStore.save(this,
                    edtUrl.getText().toString(),
                    edtToken.getText().toString());
            Toast.makeText(this, "Configuração guardada.", Toast.LENGTH_SHORT).show();
            refreshStatus();
        });

        findViewById(R.id.btnTest).setOnClickListener(v -> testConnection());
        findViewById(R.id.btnPermissions).setOnClickListener(v -> requestSmsPermissions());
        findViewById(R.id.btnSyncInbox).setOnClickListener(v -> scanInbox());
        findViewById(R.id.btnRetry).setOnClickListener(v -> {
            WorkScheduler.enqueue(this);
            Toast.makeText(this, "Reenvio iniciado.", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.btnBattery).setOnClickListener(v -> openBatterySettings());

        refreshStatus();
        refreshHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
        refreshHistory();
    }

    private void requestSmsPermissions() {
        permissionsLauncher.launch(new String[]{
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.POST_NOTIFICATIONS
        });
    }

    private void testConnection() {
        ConfigStore.save(this,
                edtUrl.getText().toString(),
                edtToken.getText().toString());

        txtStatus.setText("Estado: testando ligação...");
        Executors.newSingleThreadExecutor().execute(() -> {
            ApiClient.Result result = ApiClient.test(this);
            runOnUiThread(() -> txtStatus.setText(
                    result.ok()
                            ? "Estado: bot ligado ✅"
                            : "Estado: erro " + result.code() + " — " + result.body()
            ));
        });
    }

    private void scanInbox() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            requestSmsPermissions();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            int added = InboxScanner.scanRecent(this);
            runOnUiThread(() -> {
                Toast.makeText(this,
                        added + " mensagem(ns) adicionada(s).",
                        Toast.LENGTH_SHORT).show();
                refreshHistory();
            });
        });
    }

    private void openBatterySettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
        }
    }

    private void refreshStatus() {
        boolean receive = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                == PackageManager.PERMISSION_GRANTED;
        boolean read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                == PackageManager.PERMISSION_GRANTED;
        int pending = SmsRepository.pending(this).size();

        txtStatus.setText(
                "Estado: SMS receber=" + (receive ? "sim" : "não")
                        + " | SMS ler=" + (read ? "sim" : "não")
                        + " | pendentes=" + pending
        );
    }

    private void refreshHistory() {
        List<SmsItem> items = SmsRepository.all(this);
        if (items.isEmpty()) {
            txtHistory.setText("Nenhuma mensagem processada.");
            return;
        }

        StringBuilder out = new StringBuilder();
        DateFormat format = DateFormat.getDateTimeInstance();

        int limit = Math.min(items.size(), 30);
        for (int i = 0; i < limit; i++) {
            SmsItem item = items.get(i);
            out.append("[").append(item.status.toUpperCase()).append("] ")
                    .append(format.format(new Date(item.timestamp))).append("\n")
                    .append("De: ").append(item.sender).append("\n")
                    .append(item.body).append("\n");

            if (item.lastError != null && !item.lastError.isEmpty()) {
                out.append("Erro: ").append(item.lastError).append("\n");
            }

            out.append("Tentativas: ").append(item.attempts).append("\n\n");
        }

        txtHistory.setText(out.toString());
    }
}
