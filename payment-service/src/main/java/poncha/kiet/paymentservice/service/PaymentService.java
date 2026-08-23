package poncha.kiet.paymentservice.service;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import poncha.kiet.paymentservice.dto.response.InitPaymentResponse;
import poncha.kiet.paymentservice.dto.response.IpnResponse;
import poncha.kiet.paymentservice.entity.Payment;
import poncha.kiet.paymentservice.entity.PaymentStatus;
import poncha.kiet.paymentservice.grpc.PaymentServiceGrpc;
import poncha.kiet.paymentservice.grpc.PaymentUrlRequest;
import poncha.kiet.paymentservice.grpc.PaymentUrlResponse;
import poncha.kiet.paymentservice.repository.PaymentRepository;
import poncha.kiet.paymentservice.utils.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@RequiredArgsConstructor
@GrpcService
public class PaymentService extends PaymentServiceGrpc.PaymentServiceImplBase {
    private final PaymentRepository paymentRepository;

    public static final String VERSION = "2.1.0";
    public static final String COMMAND = "pay";
    public static final String ORDER_TYPE = "190000";
    public static final long DEFAULT_MULTIPLIER = 100L;

    @Value("${payment.vnpay.tmn-code}")
    private String tmnCode;

    @Value("${payment.vnpay.timeout}")
    private Integer paymentTimeout; //minutes

    @Override
    public void getPaymentUrl(PaymentUrlRequest request, StreamObserver<PaymentUrlResponse> responseObserver) {        var amount = request.getAmount() * DEFAULT_MULTIPLIER;  // 1. amount * 100
        var txnRef = request.getTxnRef();                       // 2. registerId
        var returnUrl = VNPayUtils.buildReturnUrl(txnRef);                 // 3. FE redirect by returnUrl

        var vnCalendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        var createdDate = DateUtils.formatVnTime(vnCalendar);
        vnCalendar.add(Calendar.MINUTE, paymentTimeout);
        var expiredDate = DateUtils.formatVnTime(vnCalendar);    // 4. expiredDate for secure

        var ipAddress = request.getIpAddress();
        var orderInfo = VNPayUtils.buildPaymentDetail(txnRef);

        Map<String, String> params = new HashMap<>();

        params.put(VNPayParams.VERSION, VERSION);
        params.put(VNPayParams.COMMAND, COMMAND);

        params.put(VNPayParams.TMN_CODE, tmnCode);
        params.put(VNPayParams.AMOUNT, String.valueOf(amount));
        params.put(VNPayParams.CURRENCY, CurrencyUtils.VND.getValue());

        params.put(VNPayParams.TXN_REF, txnRef);
        params.put(VNPayParams.RETURN_URL, returnUrl);

        params.put(VNPayParams.CREATED_DATE, createdDate);
        params.put(VNPayParams.EXPIRE_DATE, expiredDate);

        params.put(VNPayParams.IP_ADDRESS, ipAddress);

        params.put(VNPayParams.LOCALE, LocaleUtils.VIETNAM.getCode());

        params.put(VNPayParams.ORDER_INFO, txnRef);
        params.put(VNPayParams.ORDER_TYPE, ORDER_TYPE);

        var initPaymentUrl = VNPayUtils.buildInitPaymentUrl(params);

        PaymentUrlResponse response = PaymentUrlResponse.newBuilder()
                .setVnpUrl(initPaymentUrl)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Transactional
    public IpnResponse process(Map<String, String> params) {
        if (!VNPayUtils.verifyIpn(params)) {
            return VnIpnResponseUtils.SIGNATURE_FAILED;
        }

        String vnpResponseCode = params.get("vnp_ResponseCode");
        String registerId = params.get("vnp_TxnRef");
        Double amount = Double.parseDouble(params.get("vnp_Amount")) / 100;
        //        Optional<Register> optionalRegister = registerRepository.findById(registerId);
        //        if (optionalRegister.isEmpty())
        //            return VnIpnResponseUtils.ORDER_NOT_FOUND;

        if (vnpResponseCode.equals("00")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDateTime dateTime = LocalDateTime.parse(params.get("vnp_PayDate"), formatter);

            //            Register register = optionalRegister.get();
            Payment payment = Payment.builder()
                    .amount(amount)
                    .status(PaymentStatus.SUCCESS)
                    .createdAt(dateTime)
                    //                    .register(register)
                    .build();
            paymentRepository.save(payment);
            return VnIpnResponseUtils.SUCCESS;
        } else {

        }

        return VnIpnResponseUtils.UNKNOWN_ERROR;
    }
}
