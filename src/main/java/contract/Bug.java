package contract;

import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.uber.cadence.DomainAlreadyExistsError;
import com.uber.cadence.RegisterDomainRequest;
import com.uber.cadence.client.WorkflowClient;
import com.uber.cadence.client.WorkflowOptions;
import com.uber.cadence.converter.JsonDataConverter;
import com.uber.cadence.serviceclient.IWorkflowService;
import com.uber.cadence.serviceclient.WorkflowServiceTChannel;
import com.uber.cadence.worker.Worker;
import com.uber.cadence.worker.Worker.Factory;
import com.uber.cadence.worker.WorkerOptions;
import contract.workflow.TestActivitiesImpl;
import contract.workflow.TestData;
import contract.workflow.TestWorkflow;
import contract.workflow.TestWorkflowImpl;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class Bug implements CommandLineRunner {

  private static final String DOMAIN = "test_domain";
  private static final String HOST = "cadence";
  private static final int PORT = 7933;
  private static final String TASK_LIST = "task_list";
  private static final int WORKFLOW_TIMEOUT = 100;

  public static void main(String[] args) {
    SpringApplication.run(Bug.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    registerDomain();
    startFactory();

    WorkflowOptions options = new WorkflowOptions.Builder()
        .setExecutionStartToCloseTimeout(Duration.ofSeconds(WORKFLOW_TIMEOUT))
        .setTaskList(TASK_LIST)
        .build();
    TestWorkflow workflow = WorkflowClient
        .newInstance(HOST, PORT, DOMAIN)
        .newWorkflowStub(TestWorkflow.class, options);
    workflow.process(TestData.builder().id(1).value("Some").build());
  }

  private void startFactory() {
    Factory factory = new Factory(HOST, PORT, DOMAIN);

    WorkerOptions options = new WorkerOptions.Builder()
        .setDataConverter(new JsonDataConverter(builder -> {
          builder.registerTypeAdapter(TestData.class,
              (JsonSerializer<TestData>) (src, typeOfSrc, context) -> new JsonPrimitive("fail"));
          return builder;
        })).build();
    Worker worker = factory.newWorker(TASK_LIST, options);
    worker.registerWorkflowImplementationTypes(TestWorkflowImpl.class);

    TestActivitiesImpl testActivities = new TestActivitiesImpl();
    worker.registerActivitiesImplementations(testActivities);
    factory.start();
  }


  private void registerDomain() throws TException {
    IWorkflowService service = new WorkflowServiceTChannel(HOST, PORT);
    RegisterDomainRequest request = new RegisterDomainRequest();
    request.setDescription("sample domain");
    request.setEmitMetric(false);
    request.setName(DOMAIN);
    int retentionPeriodInDays = 5;
    request.setWorkflowExecutionRetentionPeriodInDays(retentionPeriodInDays);
    try {
      service.RegisterDomain(request);
      log.debug("Successfully registered domain {} with retentionDaysl{}", DOMAIN,
          retentionPeriodInDays);
    } catch (DomainAlreadyExistsError e) {
      log.error("domain  already exists {}", DOMAIN);
    }
  }
}
