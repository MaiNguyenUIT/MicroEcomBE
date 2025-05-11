package com.example.payment_service.service;

import com.example.payment_service.DTO.OrderDTO;
import com.example.payment_service.ENUM.PAYMENT_STATUS;
import com.example.payment_service.event.PaymentEvent;
import com.example.payment_service.exception.BadRequestException;
import com.example.payment_service.model.Payment;
import com.example.payment_service.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PaymentServiceImpl implements PaymentService{
    @Value("${vnpay.tmn_code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash_secret}")
    private String vnp_HashSecret;

    @Value("${vnpay.pay_url}")
    private String vnp_Url;

    @Value("${vnpay.return_url}")
    private String vnp_ReturnUrl;

    private final StreamBridge streamBridge;

    public PaymentServiceImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public String createVNPayUrl(OrderDTO orderDTO) {
        try {
            long amountInVND = (long) (orderDTO.getOrderAmount() * 100L); // VNPay yêu cầu số tiền tính bằng VND * 100
            String vnp_TxnRef = orderDTO.getOrderId().toString(); // Mã giao dịch duy nhất
            String vnp_IpAddr = "127.0.0.1"; // IP người dùng (có thể lấy từ request)

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amountInVND));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang #" + vnp_TxnRef);
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
            vnp_Params.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

            // **Tạo chữ ký bảo mật (checksum)**
            String signData = createQueryString(vnp_Params);
            String vnp_SecureHash = hmacSHA512(vnp_HashSecret, signData);
            vnp_Params.put("vnp_SecureHash", vnp_SecureHash);

            // **Tạo URL thanh toán**
            return vnp_Url + "?" + createQueryString(vnp_Params);

        } catch (Exception e) {
            throw new BadRequestException("Loi khi thanh toan");
        }
    }

    @Override
    public String returnVNPay(Map<String, String> queryParams) {
        String vnp_SecureHash = queryParams.get("vnp_SecureHash");
        System.out.println(vnp_SecureHash);
        queryParams.remove("vnp_SecureHash");

        queryParams.replaceAll((k, v) -> URLDecoder.decode(v, StandardCharsets.UTF_8));

        // **Kiểm tra chữ ký bảo mật**
        String signData = createQueryString(queryParams);
        String checkSum = hmacSHA512(vnp_HashSecret, signData);
        System.out.println(checkSum);
        assert checkSum != null;
        if (!checkSum.equals(vnp_SecureHash)) {
            throw new BadRequestException("Giao dịch không hợp lệ (Sai chữ ký)");
        }

        // **Kiểm tra trạng thái giao dịch**
        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
        if ("00".equals(vnp_ResponseCode)) {
            PaymentEvent paymentEvent = new PaymentEvent();
            paymentEvent.setPaymentStatus(PAYMENT_STATUS.SUCCESS);
            paymentEvent.setOrderId(Long.valueOf(queryParams.get("vnp_TxnRef")));

            Payment payment = new Payment();
            payment.setPaymentStatus(PAYMENT_STATUS.SUCCESS);
            payment.setOrderId(queryParams.get("vnp_TxnRef"));
            payment.setAmount(queryParams.get("vnp_Amount"));
            payment.setMethod("VNPay");
            paymentRepository.save(payment);

            streamBridge.send("paymentSuccess-out-0", paymentEvent);
            return "Thanh toán thành công!";
        } else {
            streamBridge.send("paymentFail-out-0", PAYMENT_STATUS.FAIL);
            return "Thanh toán thất bại!";
        }
    }

    private String createQueryString(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        StringBuilder queryString = new StringBuilder();
        for (String key : keys) {
            queryString.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
                    .append("&");
        }
        return queryString.substring(0, queryString.length() - 1); // Xóa ký tự "&" cuối cùng
    }
    private String hmacSHA512(String secret, String data) {
        try {
            if (secret == null || data == null) {
                throw new IllegalArgumentException("Key hoặc data không được null");
            }

            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);

            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);

            // Chuyển đổi bytes thành hex (chữ hoa)
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xFF)); // %02X để lấy chữ HOA
            }
            return sb.toString();

        } catch (Exception ex) {
            ex.printStackTrace(); // In lỗi ra console
            return null; // Trả về null thay vì chuỗi rỗng để dễ debug hơn
        }
    }
}
