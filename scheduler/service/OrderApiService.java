package com.currypoint.scheduler.service;

import com.currypoint.common.config.MapperResolver;
import com.currypoint.common.dto.FundsTransferRequestDTO;
import com.currypoint.common.dto.OrderDTO;
import com.currypoint.common.dto.PocketInquiryDTO;
import com.currypoint.common.dto.RestaurantDTO;
import com.currypoint.common.dto.payment.razorpay.RazorpayPayOutDTO;
import com.currypoint.common.dto.payment.razorpay.RazorpayPayOutResponse;
import com.currypoint.common.dto.wallet.dto.pocket.MDNSummaryDetailsDTO;
import com.currypoint.common.dto.wallet.dto.pocket.PocketTransactionDetailsDTO;
import com.currypoint.common.exception.SuccessfulReponse;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.devtools.remote.client.HttpHeaderInterceptor;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderApiService {

    private String paymentServiceUrl;

    private final RestTemplate restTemplate;

    public OrderApiService(@Value("${com.currypoint.apigateway.url}")String paymentServiceUrl, RestTemplate restTemplate) {
        this.paymentServiceUrl = paymentServiceUrl;
        this.restTemplate = restTemplate;
    }

    public OrderDTO getOrderByOrderId(String orderId){
        String url = paymentServiceUrl + "/api/orders/" +orderId;
        SuccessfulReponse successfulReponse = restTemplate.getForObject(url, SuccessfulReponse.class);
        OrderDTO restaurant= MapperResolver.getInstance().convertValue(successfulReponse.getResponse(), new TypeReference<OrderDTO>() { });
        if (restaurant == null)
            throw new RuntimeException("The restaurant with the provided storeId does not exist.");

        return  restaurant;
    }

    public RazorpayPayOutResponse payOut(RazorpayPayOutDTO razorpayPayOutDTO){
        String url = paymentServiceUrl + "/payment/payout/create";
        SuccessfulReponse successfulReponse = restTemplate.getForObject(url, SuccessfulReponse.class);
        RazorpayPayOutResponse payOutResponse= MapperResolver.getInstance().convertValue(successfulReponse.getResponse(), new TypeReference<RazorpayPayOutResponse>() { });
        if (payOutResponse == null)
            throw new RuntimeException("The restaurant with the provided storeId does not exist.");

        return  payOutResponse;
    }
    public RestaurantDTO getRestaurantByID(String restaurantId){
        String url = paymentServiceUrl + "/api/restaurants/" +restaurantId;
        SuccessfulReponse successfulReponse = restTemplate.getForObject(url, SuccessfulReponse.class);
        RestaurantDTO restaurant= MapperResolver.getInstance().convertValue(successfulReponse.getResponse(), new TypeReference<RestaurantDTO>() { });
        if (restaurant == null)
            throw new RuntimeException("The restaurant with the provided storeId does not exist.");

        return  restaurant;
    }
    public MDNSummaryDetailsDTO fetchAllPocketSummaryDetails(PocketInquiryDTO pocketInquiryDTO){
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new HttpHeaderInterceptor("ContentType", MediaType.APPLICATION_JSON_VALUE));
        String url = paymentServiceUrl + "/api/wallet/fetchAllPocketSummaryDetails";
        restTemplate.setInterceptors(interceptors);
        SuccessfulReponse repsonse = restTemplate.postForObject(url,pocketInquiryDTO, SuccessfulReponse.class);

        if (repsonse == null)
            throw new RuntimeException("Unable to fetchAllPocketSummaryDetails  wallet service.");
        MDNSummaryDetailsDTO pojos = MapperResolver.getInstance().convertValue(repsonse.getResponse(), new TypeReference<MDNSummaryDetailsDTO>() { });
        return  pojos;
    }

    public PocketTransactionDetailsDTO restaurantPayment(FundsTransferRequestDTO fundsTransferRequestDTO){
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new HttpHeaderInterceptor("ContentType", MediaType.APPLICATION_JSON_VALUE));
        String url = paymentServiceUrl + "/api/wallet/restaurant-payment";
        restTemplate.setInterceptors(interceptors);
        SuccessfulReponse repsonse = restTemplate.postForObject(url,fundsTransferRequestDTO, SuccessfulReponse.class);

        if (repsonse == null)
            throw new RuntimeException("Unable to create order in payment service.");
        PocketTransactionDetailsDTO pojos = MapperResolver.getInstance().convertValue(repsonse.getResponse(), new TypeReference<PocketTransactionDetailsDTO>() { });
        return  pojos;
    }
}
