package com.currypoint.scheduler.writer;

import com.currypoint.common.dto.*;
import com.currypoint.common.dto.payment.razorpay.*;
import com.currypoint.common.dto.wallet.dto.pocket.MDNSummaryDetailsDTO;
import com.currypoint.common.dto.wallet.dto.pocket.PocketSummaryDetailsDTO;
import com.currypoint.common.dto.wallet.dto.pocket.PocketTransactionDetailsDTO;
import com.currypoint.common.enumeration.*;
import com.currypoint.scheduler.modal.Settlement;
import com.currypoint.scheduler.repository.SettlementRepository;
import com.currypoint.scheduler.service.OrderApiService;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class SettlementProcessWriter implements ItemWriter<Settlement> {

    @Autowired
    private OrderApiService orderApiService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private SettlementRepository settlementRepository;

    @Override
    public void write(List<? extends Settlement> list) throws Exception {
         for(Settlement settlement: list) {
             if (settlement!=null) {
                 OrderDTO orderDTO = orderApiService.getOrderByOrderId(settlement.getOrderId());
                 RestaurantDTO restaurantDTO = orderApiService.getRestaurantByID(settlement.getRestaurantId());
                 if (orderDTO != null) {
                     RazorpayPayOutDTO payOutDTO = new RazorpayPayOutDTO();
                     String amountInPaise = convertRupeeToPaise(settlement.getAmount().getAmount());
                     payOutDTO.setAmount(Integer.parseInt(amountInPaise));
                     payOutDTO.setAccount_number("");
                     payOutDTO.setCurrency("INR");
                     payOutDTO.setMode("NEFT");
                     payOutDTO.setNarration("Weekly Settlement Payout");
                     RazorpayFundAccountDTO razorpayFundAccountDTO = new RazorpayFundAccountDTO();
                     razorpayFundAccountDTO.setAccount_type("bank_account");
                     RazorpayBankAccountDTO razorpayBankAccountDTO = new RazorpayBankAccountDTO();
                     RazorpayContactDTO razorpayContactDTO = new RazorpayContactDTO();
                     if (restaurantDTO != null) {
                         razorpayContactDTO.setContact(restaurantDTO.getContact().getMobileNo());
                         razorpayContactDTO.setName(restaurantDTO.getName());
                         razorpayContactDTO.setType("Restaurant");
                         BankAccountDTO bankAccountDTO = restaurantDTO.getBankAccount();
                         if (bankAccountDTO != null) {
                             razorpayBankAccountDTO.setAccount_number(bankAccountDTO.getAccountNumber());
                             razorpayBankAccountDTO.setIfsc(bankAccountDTO.getIfsc());
                             razorpayBankAccountDTO.setName(restaurantDTO.getName());
                         }
                         razorpayBankAccountDTO.setContact(razorpayContactDTO);
                         razorpayFundAccountDTO.setBank_account(razorpayBankAccountDTO);
                         payOutDTO.setFund_account(razorpayFundAccountDTO);
                         RazorpayPayOutResponse razorpayPayOutResponse = orderApiService.payOut(payOutDTO);
                         if (razorpayPayOutResponse != null) {
                             settlement.setStatus(SettlementStatus.SETTLED);
                             settlement.setRazorpayPayOutId(razorpayPayOutResponse.getId());
                             settlement.setRazorpayUtrId(razorpayPayOutResponse.getUtr());
                             PocketSummaryDetailsDTO pocketSummaryDetailsDTO=fetchPocketDtlsMdn(restaurantDTO.getContact().getMobileNo(),settlement.getUserType(),PocketValueType.PAY_OUT);
                            if(pocketSummaryDetailsDTO!=null){
                                payOutToWallet(restaurantDTO,pocketSummaryDetailsDTO,settlement);
                            }
                             settlementRepository.save(settlement);
                         }
                     }
                 }
             }
         }
    }
    private String convertRupeeToPaise(BigDecimal paise) {
        BigDecimal value = paise.multiply(new BigDecimal("100"));
        return value.setScale(0, RoundingMode.UP).toString();
    }

    private PocketSummaryDetailsDTO fetchPocketDtlsMdn(String mdnId,UserType userType,PocketValueType pocketValueType){
        //fecth Pocket Dtls
            PocketInquiryDTO pocketInquiryDTO=new PocketInquiryDTO();
            pocketInquiryDTO.setMdnId(mdnId);
            pocketInquiryDTO.setActorRole(userType.getUserType());
            MDNSummaryDetailsDTO mdnSummaryDetailsDTO=orderApiService.fetchAllPocketSummaryDetails(pocketInquiryDTO);
            PocketSummaryDetailsDTO pocketSummaryDetailsDTO=null;
            if(mdnSummaryDetailsDTO!=null){
                Predicate<PocketValueType> pocketValueTypePredicate = x -> x.equals(PocketValueType.PAY_OUT);
                List<PocketSummaryDetailsDTO> pocketSummaryDetailsDTOS=mdnSummaryDetailsDTO.getPocketSummaryDetailsArrayDTO().parallelStream().filter(x->x.getPocketType().equals(pocketValueTypePredicate)).collect(Collectors.toList());
                if(pocketSummaryDetailsDTOS!=null && pocketSummaryDetailsDTOS.size()>0){
                  pocketSummaryDetailsDTO=pocketSummaryDetailsDTOS.get(0);
                }
            }
            return pocketSummaryDetailsDTO;
    }

    private PocketTransactionDetailsDTO payOutToWallet( RestaurantDTO restaurantDTO,PocketSummaryDetailsDTO pocketSummaryDetailsDTO,Settlement settlement){
        FundsTransferRequestDTO fundsTransferRequestDTO=new FundsTransferRequestDTO();
        SubscriberTransactionDetails subscriberTransactionDetails=new SubscriberTransactionDetails();
        subscriberTransactionDetails.setActorRoleId(restaurantDTO.getId());
        subscriberTransactionDetails.setActorRoleType(restaurantDTO.getUserType());
        subscriberTransactionDetails.setMdnId(restaurantDTO.getContact().getMobileNo());
        subscriberTransactionDetails.setPocketId(pocketSummaryDetailsDTO.getPocketId());
        fundsTransferRequestDTO.setBeneDetails(subscriberTransactionDetails);

        SubscriberTransactionDetails subscriberSourceDtls=new SubscriberTransactionDetails();
        subscriberSourceDtls.setActorRoleId(restaurantDTO.getId());
        subscriberSourceDtls.setActorRoleType(UserType.RESTAURANT);
        subscriberSourceDtls.setMdnId(restaurantDTO.getContact().getMobileNo());
        fundsTransferRequestDTO.setSourceDetails(subscriberSourceDtls);

        fundsTransferRequestDTO.setExternalReferenceId(settlement.getRazorpayPaymentId());
        fundsTransferRequestDTO.setTransactionAmount(settlement.getAmount().getAmount());
        fundsTransferRequestDTO.setCommodityType(CommodityType.WALLET);
        fundsTransferRequestDTO.setTransactionCurrency("INR");
        fundsTransferRequestDTO.setTransactionType(TransactionType.ORDER_PAYMENT);

        PocketTransactionDetailsDTO pocketTransactionDetailsDTO=orderApiService.restaurantPayment(fundsTransferRequestDTO);
        if (pocketTransactionDetailsDTO == null)
            throw new RuntimeException("Unable to prcess payout to restaurant pocket.");
        return pocketTransactionDetailsDTO;
    }
}
