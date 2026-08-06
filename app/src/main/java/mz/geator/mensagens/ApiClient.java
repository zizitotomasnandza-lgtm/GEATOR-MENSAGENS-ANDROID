package mz.geator.mensagens;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public final class ApiClient {
    private ApiClient() {}

    public static Result test(Context context) {
        String base = ConfigStore.url(context);
        String token = ConfigStore.token(context);
        if (base.isEmpty() || token.isEmpty()) return new Result(false, 0, "Configure URL e token.");

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(base + "/api/sms/status").openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setRequestProperty("x-geator-sms-token", token);
            int code = connection.getResponseCode();
            String body = read(code >= 200 && code < 400 ? connection.getInputStream() : connection.getErrorStream());
            return new Result(code >= 200 && code < 300, code, body);
        } catch (Exception e) {
            return new Result(false, 0, e.getMessage());
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    public static Result send(Context context, SmsItem item) {
        String base = ConfigStore.url(context);
        String token = ConfigStore.token(context);
        if (base.isEmpty() || token.isEmpty()) return new Result(false, 0, "Configuração ausente.");

        HttpURLConnection connection = null;
        try {
            JSONObject payload = new JSONObject();
            payload.put("id", item.id);
            payload.put("origem", item.sender);
            payload.put("texto", item.body);
            payload.put("recebidoEm", Instant.ofEpochMilli(item.timestamp).toString());

            byte[] bytes = payload.toString().getBytes(StandardCharsets.UTF_8);
            connection = (HttpURLConnection) new URL(base + "/api/sms").openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("x-geator-sms-token", token);
            connection.getOutputStream().write(bytes);

            int code = connection.getResponseCode();
            String body = read(code >= 200 && code < 400 ? connection.getInputStream() : connection.getErrorStream());

            // 200/201 = enviado. 404 "sem pendente" deve continuar pendente para reenvio.
            boolean success = code >= 200 && code < 300;
            return new Result(success, code, body);
        } catch (Exception e) {
            return new Result(false, 0, e.getMessage());
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String read(InputStream stream) {
        if (stream == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) out.append(line);
            return out.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public record Result(boolean ok, int code, String body) {}
}
