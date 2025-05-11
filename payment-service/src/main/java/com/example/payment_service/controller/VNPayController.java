package com.example.payment_service.controller;

import com.example.payment_service.DTO.OrderDTO;
import com.example.payment_service.exception.BadRequestException;
import com.example.payment_service.service.PaymentService;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/vnpay")
public class VNPayController {

    @Value("${vnpay.tmn_code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash_secret}")
    private String vnp_HashSecret;

    @Value("${vnpay.pay_url}")
    private String vnp_Url;

    @Value("${vnpay.return_url}")
    private String vnp_ReturnUrl;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create-payment")
    public ResponseEntity<String> createPayment(@RequestBody OrderDTO orderDTO) {
        return ResponseEntity.ok(paymentService.createVNPayUrl(orderDTO));
    }

    @GetMapping("/payment-return")
    public ResponseEntity<String> paymentReturn(@RequestParam Map<String, String> queryParams) {
        return ResponseEntity.ok(paymentService.returnVNPay(queryParams));
    }

//    @GetMapping("/create")
//    public ResponseEntity<?> createVNPayUrl(@RequestParam("amount") Long amount) {
//        try {
//            long amountInVND = (long) (amount * 100); // VNPay yêu cầu số tiền tính bằng VND * 100
//            String vnp_TxnRef = String.valueOf(System.currentTimeMillis()); // Mã giao dịch duy nhất
//            String vnp_IpAddr = "127.0.0.1"; // IP người dùng (có thể lấy từ request)
//
//            Map<String, String> vnp_Params = new HashMap<>();
//            vnp_Params.put("vnp_Version", "2.1.0");
//            vnp_Params.put("vnp_Command", "pay");
//            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
//            vnp_Params.put("vnp_Amount", String.valueOf(amountInVND));
//            vnp_Params.put("vnp_CurrCode", "VND");
//            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
//            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang #" + vnp_TxnRef);
//            vnp_Params.put("vnp_OrderType", "other");
//            vnp_Params.put("vnp_Locale", "vn");
//            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
//            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
//            vnp_Params.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
//
//            // **Tạo chữ ký bảo mật (checksum)**
//            String signData = createQueryString(vnp_Params);
//            String vnp_SecureHash = hmacSHA512(vnp_HashSecret, signData);
//            vnp_Params.put("vnp_SecureHash", vnp_SecureHash);
//
//            // **Tạo URL thanh toán**
//            String paymentUrl = vnp_Url + "?" + createQueryString(vnp_Params);
//            return ResponseEntity.ok(paymentUrl);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi tạo URL thanh toán");
//        }
//    }
//
//    private String createQueryString(Map<String, String> params) {
//        List<String> keys = new ArrayList<>(params.keySet());
//        Collections.sort(keys);
//        StringBuilder queryString = new StringBuilder();
//        for (String key : keys) {
//            queryString.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
//                    .append("=")
//                    .append(URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
//                    .append("&");
//        }
//        return queryString.substring(0, queryString.length() - 1); // Xóa ký tự "&" cuối cùng
//    }
//
//    private String hmacSHA512(String key, String data) throws Exception {
//        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA512");
//        Mac mac = Mac.getInstance("HmacSHA512");
//        mac.init(secretKeySpec);
//        byte[] bytes = mac.doFinal(data.getBytes("UTF-8"));
//        return Hex.encodeHexString(bytes).toUpperCase();
//    }
}
