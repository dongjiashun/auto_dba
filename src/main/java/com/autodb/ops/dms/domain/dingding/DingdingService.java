package com.autodb.ops.dms.domain.dingding;

import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface DingdingService {
    @RequestLine("POST /robot/send?access_token={token}")
    @Headers({"Content-Type: application/json"})
    @Body("{content}")
    Object check(@Param(value = "token") String token ,
                 @Param("content") String content);
}
