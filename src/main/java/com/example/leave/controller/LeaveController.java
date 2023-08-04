package com.example.leave.controller;


import com.example.leave.ApprovalDTO;
import com.example.leave.LeaveDTO;
import com.example.leave.db.ApprovalDB;
import com.example.leave.db.LeaveDB;
import com.example.leave.db.UserDB;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static com.example.leave.FlowConstants.PROCESS_KEY;

@RestController
@RequestMapping("leave")
@RequiredArgsConstructor
public class LeaveController {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final LeaveDB leaveDB;
    private final UserDB userDB;
    private final ApprovalDB approvalDB;

    /**
     * 请假申请
     * <p>
     * 示例数据如下
     * {
     * "no":"no3",
     * "name":"IT部员工3",
     * "department":"IT",
     * "type":2,
     * "day":5,
     * "reason":"play"
     * }
     *
     * @param dto 请假申请所需数据
     */
    @PostMapping("apply")
    public void apply(@RequestBody LeaveDTO dto) {
        // 保存请假申请信息到数据库中(实际上利用Collection保存在内存中)
        LeaveDTO leaveDTO = leaveDB.insert(dto);
        Objects.requireNonNull(leaveDTO);
        // 启动流程实例，触发CreateTaskListener监听器
        runtimeService.startProcessInstanceByKey(PROCESS_KEY, leaveDTO.getSeqNo());
    }

    /**
     * 审批人查看自己的审批任务列表
     *
     * @param user 审批人
     * @return 请假审批列表
     */
    @GetMapping("task/{user}")
    public List<LeaveDTO> task(@PathVariable String user) {
        Objects.requireNonNull(user);
        return userDB.getUserTasks(user);
    }

    /**
     * 审批请假流程
     * 示例数据如下
     * {
     * "seqNo":"4f076f91-ce0d-42ce-949c-cf393335aaf1",
     * "approver":"hr",
     * "status":1,
     * "remark":"pass"
     * }
     *
     * @param dto 审批信息
     */
    @PutMapping("approval")
    public void approval(@RequestBody ApprovalDTO dto) {
        approvalDB.insert(dto);
        userDB.delRelLeaveHandler(dto.getApprover(), dto.getSeqNo());
        String taskId = taskService
                .createTaskQuery()
                .processDefinitionKey(PROCESS_KEY)
                .processInstanceBusinessKey(dto.getSeqNo())
                .singleResult()
                .getId();
        // 任务完成时，会触发CompleteTaskListener监听器
        taskService.complete(taskId);
    }
}
