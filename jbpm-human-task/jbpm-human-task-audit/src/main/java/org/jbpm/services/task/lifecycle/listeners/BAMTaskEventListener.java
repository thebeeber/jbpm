/*
 * Copyright 2012 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.services.task.lifecycle.listeners;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.jbpm.services.task.audit.impl.model.BAMTaskSummaryImpl;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.TaskContext;
import org.kie.internal.task.api.TaskPersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p></p>This listener implementation populates a table named BAMTASKCUMMARY in order to allow BAM module to query all tasks.</p>
 *
 * <p>The available status for a task instance are:</p>
 * @see org.kie.api.task.model.Status
 * <ul>
 *     <li>Created</li>
 *     <li>Ready</li>
 *     <li>Reserved</li>
 *     <li>InProgress</li>
 *     <li>Suspended</li>
 *     <li>Completed</li>
 *     <li>Failed</li>
 *     <li>Error</li>
 *     <li>Exited</li>
 *     <li>Obsolete</li>
 * </ul>
 *
 * <p>The BAM module does not use all task predefined stauts, the following list shows the status for a jBPM task and the relationship with the BAM task status:</p>
 * <ul>
 *     <li>Kie Task status - BAM task status</li>
 *     <li>Created - Created</li>
 *     <li>Ready - Ready</li>
 *     <li>Reserved - Reserved</li>
 *     <li>InProgress - InProgress</li>
 *     <li>Suspended - Suspended</li>
 *     <li>Completed - Completed</li>
 *     <li>Exited - Exited</li>
 *     <li>Failed - Error</li>
 *     <li>Error - Error</li>
 *     <li>Obsolete - Error</li>
 * </ul>
 */
public class BAMTaskEventListener implements TaskLifeCycleEventListener {

    /** Class logger. */
    private static final Logger logger = LoggerFactory.getLogger(BAMTaskEventListener.class);

    public BAMTaskEventListener(boolean flag) {
    }

    public void afterTaskStartedEvent(TaskEvent event) {
    	updateTask(event, new BAMTaskWorker() {
            @Override
            public BAMTaskSummaryImpl createTask(BAMTaskSummaryImpl bamTask, Task task) {
                bamTask.setStartDate(new Date());
                return bamTask;
            }

            @Override
            public BAMTaskSummaryImpl updateTask(BAMTaskSummaryImpl bamTask, Task task) {
                bamTask.setStartDate(new Date());
                return bamTask;
            }
        });
    }

    public void afterTaskActivatedEvent(TaskEvent event) {
    	updateTask(event);
    }

    public void afterTaskClaimedEvent(TaskEvent event) {
    	updateTask(event);
    }

    public void afterTaskCompletedEvent(TaskEvent event) {

    	updateTask(event, new BAMTaskWorker() {
            @Override
            public BAMTaskSummaryImpl createTask(BAMTaskSummaryImpl bamTask, Task task) {
                return bamTask;
            }

            @Override
            public BAMTaskSummaryImpl updateTask(BAMTaskSummaryImpl bamTask, Task task) {
                Date completedDate = new Date();
                bamTask.setEndDate(completedDate);
                bamTask.setDuration(completedDate.getTime() - bamTask.getStartDate().getTime());
                return bamTask;
            }
        });
    }

    public void afterTaskAddedEvent(TaskEvent event) {
        createTask(event, null, null);
    }

    /**
     * When a task is skipped, the status for dashbuilder integration task must be Exited.
     *
     * @param ti The task.
     */
    public void afterTaskSkippedEvent(TaskEvent event) {
        createOrUpdateTask(event, Status.Exited);
    }

    /**
     * When a task is stopped, the status for dashbuilder integration task must be Exited.
     *
     * @param ti The task.
     */
    public void afterTaskStoppedEvent(TaskEvent event) {
        createOrUpdateTask(event, Status.Exited);
    }

    /**
     * When a task is failed, the status for dashbuilder integration task must be Exited.
     *
     * @param ti The task.
     */
    public void afterTaskFailedEvent(TaskEvent event) {
        createOrUpdateTask(event, Status.Error);
    }

    public void afterTaskExitedEvent(TaskEvent event) {
        createOrUpdateTask(event, Status.Exited);
    }

    public void afterTaskReleasedEvent(TaskEvent event) {
    	updateTask(event);
    }

    public void afterTaskDelegatedEvent(TaskEvent event) {
    	updateTask(event);
    }

    public void afterTaskForwaredEvent(TaskEvent event) {
    	updateTask(event);
    }

    public void afterTaskNomiatedEvent(TaskEvent event) {
    	updateTask(event);
    }

    public void afterTaskResumedEvent(TaskEvent event) {
    	updateTask(event);
    }

    public void afterTaskSuspendedEvent(TaskEvent event) {
    	updateTask(event);
    }
    
    @Override
    public void afterTaskForwardedEvent(TaskEvent event) {
    	updateTask(event);
    }
    
    @Override
    public void afterTaskNominatedEvent(TaskEvent event) {
    	updateTask(event);
    }

    /**
     * Creates or updates a bam task summary instance.
     *
     * @param ti The source task
     * @param worker Perform additional operations to the bam task summary instance.
     * @return The created or updated bam task summary instance.
     */
    protected BAMTaskSummaryImpl updateTask(TaskEvent event, BAMTaskWorker worker) {
        return updateTask(event, null, worker);
    }


    /**
     * Creates or updates a bam task summary instance.
     *
     * @param ti The source task
     * @return The created or updated bam task summary instance.
     */
    protected BAMTaskSummaryImpl updateTask(TaskEvent event) {
        return updateTask(event, null, null);
    }

    /**
     * Creates or updates a bam task summary instance.
     *
     * @param ti The source task
     * @param newStatus The new state for the task.
     * @return The created or updated bam task summary instance.
     */
    protected BAMTaskSummaryImpl createOrUpdateTask(TaskEvent event, Status newStatus) {
        return updateTask(event, newStatus, null);
    }

    /**
     * Creates or updates a bam task summary instance.
     *
     * @param ti The source task
     * @param newStatus The new state for the task.
     * @param worker Perform additional operations to the bam task summary instance.
     * @return The created or updated bam task summary instance.
     */
    protected BAMTaskSummaryImpl createTask(TaskEvent event, Status newStatus, BAMTaskWorker worker) {
        BAMTaskSummaryImpl result = null;
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();

        if (ti == null) {
            logger.error("The task instance does not exist.");
            return result;
        }

        Status status = newStatus != null ? newStatus : ti.getTaskData().getStatus();

        String actualOwner = "";
        if (ti.getTaskData().getActualOwner() != null) {
            actualOwner = ti.getTaskData().getActualOwner().getId();
        }

        result = new BAMTaskSummaryImpl(ti.getId(), ti.getName(), status.toString(), new Date(), actualOwner, ti.getTaskData().getProcessInstanceId());
        if (worker != null) worker.createTask(result, ti);
        store(event, result);
        persistenceContext.persist(result);
    

        return result;
    }
    
    protected BAMTaskSummaryImpl updateTask(TaskEvent event, Status newStatus, BAMTaskWorker worker) {
        BAMTaskSummaryImpl result = null;
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();

        if (ti == null) {
            logger.error("The task instance does not exist.");
            return result;
        }

        Status status = newStatus != null ? newStatus : ti.getTaskData().getStatus();

        result = persistenceContext.queryStringWithParametersInTransaction("select bts from BAMTaskSummaryImpl bts where bts.taskId=:taskId", true,
        												persistenceContext.addParametersToMap("taskId", ti.getId()), 
        												BAMTaskSummaryImpl.class);
        
        if (result == null) {
        	// in case there was nothing returned by the query check current transaction items - due to COMMIT flush mode 
            result = getCurrentTxBAMTasks(event);
        } 
        
        if (result == null) {
        	logger.warn("Unable find bam task entry for task id {} '{}', skipping bam task update", ti.getId(), ti.getName());
        	return null;
        }
        	
        result.setStatus(status.toString());
        if (ti.getTaskData().getActualOwner() != null) {
            result.setUserId(ti.getTaskData().getActualOwner().getId());
        }
        if (worker != null) worker.updateTask(result, ti);
        persistenceContext.merge(result);

      
        return result;
    }
    
	@SuppressWarnings("unchecked")
	private void store(TaskEvent event, BAMTaskSummaryImpl bamTask) {
		TaskContext context = (TaskContext) event.getTaskContext();
		Set<BAMTaskSummaryImpl> bamTasks = (Set<BAMTaskSummaryImpl>) context.get("local:current-bam-tasks");
		if (bamTasks == null) {
			bamTasks = new HashSet<BAMTaskSummaryImpl>();
			context.set("local:current-bam-tasks", bamTasks);
		}
		bamTasks.add(bamTask);
	}
	
	@SuppressWarnings("unchecked")
	private BAMTaskSummaryImpl getCurrentTxBAMTasks(TaskEvent event) {
		TaskContext context = (TaskContext) event.getTaskContext();
		Set<BAMTaskSummaryImpl> currentTxBAMTasks = (Set<BAMTaskSummaryImpl>) context.get("local:current-bam-tasks");
		if (currentTxBAMTasks != null) {
			
			if (currentTxBAMTasks != null) {
				for (BAMTaskSummaryImpl bamTask : currentTxBAMTasks) {
					if (bamTask.getTaskId() == event.getTask().getId()) {
						return bamTask;
					}
				}
				
			}
		}
		
		return null;
	}

    /**
     * Interface for performing additional operations to a <code>org.jbpm.services.task.impl.model.BAMTaskSummaryImpl</code> instance.
     */
    protected interface BAMTaskWorker {
        BAMTaskSummaryImpl createTask(BAMTaskSummaryImpl bamTask, Task task);
        BAMTaskSummaryImpl updateTask(BAMTaskSummaryImpl bamTask, Task task);
    }

	@Override
	public void beforeTaskActivatedEvent(TaskEvent event) {

		
	}

	@Override
	public void beforeTaskClaimedEvent(TaskEvent event) {
		
		
	}

	@Override
	public void beforeTaskSkippedEvent(TaskEvent event) {	
	}

	@Override
	public void beforeTaskStartedEvent(TaskEvent event) {
	}

	@Override
	public void beforeTaskStoppedEvent(TaskEvent event) {
	}

	@Override
	public void beforeTaskCompletedEvent(TaskEvent event) {
	}

	@Override
	public void beforeTaskFailedEvent(TaskEvent event) {
	}

	@Override
	public void beforeTaskAddedEvent(TaskEvent event) {
	}

	@Override
	public void beforeTaskExitedEvent(TaskEvent event) {
	}

	@Override
	public void beforeTaskReleasedEvent(TaskEvent event) {
	}

	@Override
	public void beforeTaskResumedEvent(TaskEvent event) {

	}

	@Override
	public void beforeTaskSuspendedEvent(TaskEvent event) {

	}

	@Override
	public void beforeTaskForwardedEvent(TaskEvent event) {
	}

	@Override
	public void beforeTaskDelegatedEvent(TaskEvent event) {
	}
	
	@Override
	public void beforeTaskNominatedEvent(TaskEvent event) {
	}
	
	private class BAMSummaryComparator implements Comparator<BAMTaskSummaryImpl> {

		@Override
		public int compare(BAMTaskSummaryImpl o1, BAMTaskSummaryImpl o2) {	
			return (o1.getTaskId()<o2.getTaskId() ? -1 : (o1.getTaskId()==o2.getTaskId() ? 0 : 1));
		}
	}
}
