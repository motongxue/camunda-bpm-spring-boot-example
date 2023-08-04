package com.example.leave.listener;

import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import com.example.leave.LeaveDTO;
import com.example.leave.db.LeaveDB;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * camunda中填写请假中使用代理表达式，并绑定该类名，
 * 实现JavaDelegate接口，成为一个监听器
 */
@Component
@RequiredArgsConstructor
public class LeaveApplyStartExecution implements JavaDelegate {

    private final LeaveDB db;
    private final ObjectMapper objectMapper;

    @Override
    @SuppressWarnings("unchecked")
    public void execute(DelegateExecution execution) throws Exception {
        String seqNo = execution.getBusinessKey();
        LeaveDTO dto = db.query(seqNo);
        Objects.requireNonNull(dto);
        Map<String, Object> variables = objectMapper.convertValue(dto, Map.class);
        //设置流程变量
        execution.setVariables(variables);
    }

}
