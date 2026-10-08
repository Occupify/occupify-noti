package com.occupify.notification.controller;

import com.occupify.notification.dto.base.CreatedResponse;
import com.occupify.notification.dto.base.PageResponse;
import com.occupify.notification.dto.base.SingleResponse;
import com.occupify.notification.dto.base.SuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class AbstractBaseController {

    @Autowired
    protected ResponseFactory responseFactory;

    protected <T> ResponseEntity<SingleResponse<T>> successSingle(T data, String message) {
        return responseFactory.successSingle(data, message);
    }

    protected ResponseEntity<SuccessResponse> success(String message) {
        return responseFactory.success(message);
    }

    protected <T> ResponseEntity<CreatedResponse<T>> created(T data, String message) {
        return responseFactory.created(data, message);
    }

    protected ResponseEntity<CreatedResponse<Void>> created(String message) {
        return responseFactory.created(message);
    }

    protected <T> ResponseEntity<PageResponse<T>> paging(Page<T> page, String message) {
        PageResponse<T> response = responseFactory.createPageResponse(HttpStatus.OK, message, page.getContent(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
        return ResponseEntity.ok(response);
    }
}
