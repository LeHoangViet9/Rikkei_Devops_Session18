import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class DiscordAppender extends AppenderBase<ILoggingEvent> {

    // Biến này sẽ được Logback tự động map từ file cấu hình XML
    private String webhookUrl;

    @Override
    protected void append(ILoggingEvent eventObject) {
        // Chỉ xử lý log ở mức ERROR
        if (eventObject.getLevel().levelInt == Level.ERROR_INT) {
            sendWebhook(eventObject);
        }
    }

    private void sendWebhook(ILoggingEvent eventObject) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        try {
            // Lấy nội dung log và escape ký tự " để tránh lỗi format JSON
            String message = eventObject.getFormattedMessage().replace("\"", "\\\"");
            
            // Payload chuẩn của Discord (với Slack thì key thường là "text" thay vì "content")
            String jsonPayload = "{\"content\": \"🚨 **[SYSTEM ERROR]** \\n```\\n" + message + "\\n```\"}";

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // SỬ DỤNG GỬI BẤT ĐỒNG BỘ (sendAsync)
            // Cực kỳ quan trọng để không làm chậm response của API hiện tại khi call Webhook bị chậm/time-out
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                  .exceptionally(ex -> {
                      System.err.println("Gửi cảnh báo Discord thất bại: " + ex.getMessage());
                      return null;
                  });

        } catch (Exception e) {
            // Không dùng log.error() ở đây để tránh tạo vòng lặp vô hạn (Infinite Loop Logging)
            System.err.println("Lỗi cấu hình webhook Discord: " + e.getMessage());
        }
    }

    // Bắt buộc phải có Getter/Setter để cấu hình từ logback-spring.xml
    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
}