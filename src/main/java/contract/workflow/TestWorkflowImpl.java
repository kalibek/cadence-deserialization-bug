package contract.workflow;

import com.uber.cadence.activity.ActivityOptions;
import com.uber.cadence.workflow.Workflow;
import java.time.Duration;
import org.slf4j.Logger;

public class TestWorkflowImpl implements TestWorkflow {

  private final ActivityOptions options =
      new ActivityOptions.Builder().setScheduleToCloseTimeout(Duration.ofHours(1)).build();
  private final TestActivities activities =
      Workflow.newActivityStub(TestActivities.class, options);
  private static Logger log = Workflow.getLogger(TestWorkflowImpl.class);

  @Override
  public void process(TestData data) {
    log.info("workflow got data {}", data);
    activities.invoke(data);
    log.info("workflow finished");
  }
}
