package contract.workflow;

import com.uber.cadence.workflow.WorkflowMethod;

public interface TestWorkflow {

  @WorkflowMethod
  void process(TestData data);

}
