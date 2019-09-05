package contract.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TestData {

  private long id;
  private String value;
}
