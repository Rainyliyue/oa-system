package com.oa.application.controller;

import com.oa.application.service.ApplicationApplyService;
import com.oa.common.dto.ApprovalRequest;
import com.oa.common.dto.PageQuery;
import com.oa.common.entity.TripApply;
import com.oa.common.result.AjaxResult;
import com.oa.common.result.PageResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trip")
public class TripController {
    private final ApplicationApplyService service;

    public TripController(ApplicationApplyService service) {
        this.service = service;
    }

    @PostMapping("/user/page")
    public PageResult<TripApply> userPage(@RequestBody PageQuery query) {
        return service.tripPage(query, true);
    }

    @PostMapping("/admin/page")
    public PageResult<TripApply> adminPage(@RequestBody PageQuery query) {
        return service.tripPage(query, false);
    }

    @PostMapping
    public AjaxResult<Void> add(@RequestBody TripApply apply) {
        return service.addTrip(apply);
    }

    @PutMapping("/user/{id}")
    public AjaxResult<Void> userUpdate(@PathVariable Long id,
                                       @RequestParam Long userId,
                                       @RequestBody TripApply form) {
        return service.updateTripByUser(id, userId, form);
    }

    @DeleteMapping("/user/{id}")
    public AjaxResult<Void> userDelete(@PathVariable Long id, @RequestParam Long userId) {
        return service.deleteTripByUser(id, userId);
    }

    @PutMapping("/admin/{id}")
    public AjaxResult<Void> adminUpdate(@PathVariable Long id, @RequestBody TripApply form) {
        return service.updateTripByAdmin(id, form);
    }

    @DeleteMapping("/admin/{id}")
    public AjaxResult<Void> adminDelete(@PathVariable Long id) {
        return service.deleteTripByAdmin(id);
    }

    @PostMapping("/admin/{id}/approve")
    public AjaxResult<Void> approve(@PathVariable Long id, @RequestBody ApprovalRequest request) {
        return service.approveTrip(id, request);
    }
}
