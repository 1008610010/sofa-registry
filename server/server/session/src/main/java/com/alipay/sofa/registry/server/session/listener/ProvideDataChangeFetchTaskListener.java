/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.server.session.listener;

import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.service.MetaNodeService;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.session.scheduler.task.ProvideDataChangeFetchTask;
import com.alipay.sofa.registry.server.session.scheduler.task.SessionTask;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.alipay.sofa.registry.task.batcher.TaskDispatcher;
import com.alipay.sofa.registry.task.batcher.TaskDispatchers;
import com.alipay.sofa.registry.task.batcher.TaskProcessor;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListener;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author shangyu.wh
 * @version $Id: SubscriberRegisterFetchTaskListener.java, v 0.1 2017-12-07 19:53 shangyu.wh Exp $
 */
public class ProvideDataChangeFetchTaskListener implements TaskListener {

    @Autowired
    private SessionServerConfig                 sessionServerConfig;

    /**
     * trigger push client process
     */
    @Autowired
    private TaskListenerManager                 taskListenerManager;

    /**
     * MetaNode service
     */
    @Autowired
    private MetaNodeService                     metaNodeService;

    @Autowired
    private Exchange                            boltExchange;

    @Autowired
    private Interests                           sessionInterests;

    @Autowired
    private Watchers                            sessionWatchers;

    @Autowired
    private Registry                            sessionRegistry;

    private TaskDispatcher<String, SessionTask> singleTaskDispatcher;

    private TaskProcessor                       dataNodeSingleTaskProcessor;

    public ProvideDataChangeFetchTaskListener(TaskProcessor dataNodeSingleTaskProcessor) {
        this.dataNodeSingleTaskProcessor = dataNodeSingleTaskProcessor;
    }

    public TaskDispatcher<String, SessionTask> getSingleTaskDispatcher() {
        if (singleTaskDispatcher == null) {
            singleTaskDispatcher = TaskDispatchers.createDefaultSingleTaskDispatcher(
                TaskType.PROVIDE_DATA_CHANGE_FETCH_TASK.getName(), dataNodeSingleTaskProcessor);
        }
        return singleTaskDispatcher;
    }

    @Override
    public boolean support(TaskEvent event) {
        return TaskType.PROVIDE_DATA_CHANGE_FETCH_TASK.equals(event.getTaskType());
    }

    @Override
    public void handleEvent(TaskEvent event) {

        SessionTask provideDataChangeFetchTask = new ProvideDataChangeFetchTask(
            sessionServerConfig, taskListenerManager, metaNodeService, sessionWatchers,
            boltExchange, sessionInterests, sessionRegistry);

        provideDataChangeFetchTask.setTaskEvent(event);

        getSingleTaskDispatcher().dispatch(provideDataChangeFetchTask.getTaskId(),
            provideDataChangeFetchTask, provideDataChangeFetchTask.getExpiryTime());
    }

}