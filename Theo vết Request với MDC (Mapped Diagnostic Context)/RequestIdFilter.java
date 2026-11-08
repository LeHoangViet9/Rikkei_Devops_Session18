import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        // Tạo UUID và đưa vào MDC context của Logback
        String requestId = UUID.randomUUID().toString();
        MDC.put(REQUEST_ID, requestId);
        
        try {
            // Cho phép request đi tiếp tới các Filter khác hoặc Controller
            filterChain.doFilter(request, response);
        } finally {
            // Bắt buộc phải xóa requestId khỏi MDC sau khi request hoàn thành
            MDC.remove(REQUEST_ID);
            // Hoặc dùng MDC.clear(); nếu muốn xóa toàn bộ context
        }
    }
}