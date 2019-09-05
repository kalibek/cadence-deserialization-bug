package contract.workflow;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestActivitiesImpl implements TestActivities {

  @Override
  public void invoke(TestData testData) {
    log.info("got test data {}", testData);
  }
}
