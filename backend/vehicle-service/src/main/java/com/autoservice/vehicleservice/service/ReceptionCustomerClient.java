package com.autoservice.vehicleservice.service;
import com.autoservice.vehicleservice.exception.BusinessException;
import com.autoservice.vehicleservice.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
@Component
public class ReceptionCustomerClient {
 private final RestClient client;
 public ReceptionCustomerClient(@Value("${services.identity.base-url:http://localhost:8081}") String url) {
  var factory=new SimpleClientHttpRequestFactory();factory.setConnectTimeout(3000);factory.setReadTimeout(5000);
  client=RestClient.builder().baseUrl(url).requestFactory(factory).build();
 }
 public void requireCustomer(Long id,String authorization) {
  if(id==null||id<=0)throw new BusinessException(ErrorCode.VALIDATION_ERROR,"Mã khách hàng không hợp lệ.");
  try {
   var response=client.get().uri("/api/v1/admin/users/{id}",id).header("Authorization",authorization).retrieve().body(Response.class);
   if(response==null||response.data()==null||!id.equals(response.data().id())||!"CUSTOMER".equals(response.data().role()))
    throw new BusinessException(ErrorCode.VALIDATION_ERROR,"Không tìm thấy hồ sơ khách hàng hợp lệ.");
  } catch(RestClientException e){throw new BusinessException(ErrorCode.CUSTOMER_LOOKUP_FAILED,"Không xác minh được khách hàng; chưa tạo xe. Hãy thử lại khi Identity hoạt động.",e);}
 }
 private record Response(Customer data){}
 private record Customer(Long id,String role){}
}
