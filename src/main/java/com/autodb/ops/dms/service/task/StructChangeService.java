package com.autodb.ops.dms.service.task;

import com.autodb.ops.dms.common.Pair;
import com.autodb.ops.dms.common.exception.AppException;
import com.autodb.ops.dms.dto.caas.ChangeStash;
import com.autodb.ops.dms.dto.task.ProcessData;
import com.autodb.ops.dms.dto.task.StructChangeApply;
import com.autodb.ops.dms.dto.task.StructChangeOnline;
import com.autodb.ops.dms.dto.task.StructStashOnline;
import com.autodb.ops.dms.dto.task.TaskData;
import com.autodb.ops.dms.entity.task.StructChange;
import com.autodb.ops.dms.entity.task.StructChangeStash;
import com.autodb.ops.dms.entity.task.TaskBiz;
import com.autodb.ops.dms.entity.user.User;
import org.activiti.engine.delegate.DelegateExecution;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * StructChange Service
 *
 * @author dongjs
 * @since 16/5/27
 */
public interface StructChangeService {
    /**
     * apply
     *
     * @param user apply user
     * @param structChangeApply apply data
     * @return
     * <ul>
     *     <li>0. success</li>
     *     <li>1. unknown ds</li>
     *     <li>2. bad sql</li>
     * </ul>
     */
    Pair<Integer, String> apply(User user, StructChangeApply structChangeApply) throws AppException;

    /**
     * <ul>
     *     <li>0. success</li>
     *     <li>1. unknown ds</li>
     *     <li>2. bad sql</li>
     * </ul>
     */
    Pair<Integer, String> adjust(User user, String id, boolean reApply, String reason, String sql) throws AppException;

    Optional<String> check(String sql, String env, String sid) throws AppException;

    Pair<Integer, String> stash(User user, ChangeStash stash) throws AppException;

    List<StructChangeStash> onlineStash(int dataSourceId) throws AppException;

    TaskData task(String definitionKey, String id) throws AppException;

    Pair<Integer, String> change_exec_time(String definitionKey, String id,Date execTime) throws AppException;

    ProcessData process(String id) throws AppException;

    ProcessData processByTaskId(String definitionKey, String id) throws AppException;

    Pair<Integer, String> approve(String id, User assessor, int agree, String reason,Date execTime) throws AppException;

    Pair<Integer, String> execute(String id, User executor, byte agree, String reason) throws AppException;

    Pair<Integer, String> progress(String id) throws AppException;

    Pair<Integer, String> cancelProgress(String id) throws AppException;

    int result(User user, String id) throws AppException;

    void end(String processInstanceId) throws AppException;

    /** top **/
    List<StructChange> onlineChanges(int dataSourceId, int endChangeId) throws AppException;

    List<TaskBiz> inProcessOnline(int dataSourceId) throws AppException;

    /**
     * stash online
     * @see #apply(User, StructChangeApply)
     */
    Pair<Integer, String> online(User user, StructStashOnline online) throws AppException;

    /**
     * online
     * @see #apply(User, StructChangeApply)
     */
    Pair<Integer, String> online(User user, StructChangeOnline online) throws AppException;

    public void change(DelegateExecution execution) throws AppException;
}
