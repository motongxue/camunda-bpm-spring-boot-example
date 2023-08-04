package com.example.leave.listener;


import static com.example.leave.FlowConstants.NODE_GM;
import static com.example.leave.FlowConstants.NODE_HR;

import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

import com.example.leave.LeaveDTO;
import com.example.leave.db.UserDB;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 *
 */
@Component
@RequiredArgsConstructor
public class CreateTaskListener implements TaskListener {

    private final UserDB userDB;
    private final ObjectMapper objectMapper;

    @Override
    public void notify(DelegateTask delegateTask) {
        String taskDefinitionKey = delegateTask.getTaskDefinitionKey();
        // 获取LeaveDTO对象
        Map<String, Object> variables = delegateTask.getExecution().getVariables();
        String assignee;
        // 判断当前任务的key是归属于哪个角色
        if (Objects.equals(taskDefinitionKey, NODE_HR)) {
            // 人事主管
            assignee = userDB.getHr();
        } else if (Objects.equals(taskDefinitionKey, NODE_GM)) {
            // 总经理
            assignee = userDB.getGm();
        } else {
            // 部门经理
            assignee = userDB.getDm(Objects.toString(variables.get("department")));
        }
        // 设置任务的执行人
        delegateTask.setAssignee(assignee);
        LeaveDTO leaveDTO = objectMapper.convertValue(variables, LeaveDTO.class);
        // 保存任务的执行人
        userDB.saveRelLeaveHandler(assignee, leaveDTO);
    }

//    private String assignment() {
//
//    }
}
